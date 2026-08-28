package com.phantom.cod.network;

import com.mojang.datafixers.util.Pair;
import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.levelgen.structure.Structure;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.List;
import java.util.Optional;

public final class ModNetwork {

    private ModNetwork() {
    }

    // ==================================================
    // SCREEN SHAKE PAYLOAD
    // ==================================================

    public record ScreenShakePayload(
            int duration,
            float intensity,
            int x,
            int y,
            int z,
            int effectType
    ) implements CustomPacketPayload {

        public static final Type<ScreenShakePayload> TYPE =
                new Type<>(
                        Identifier.fromNamespaceAndPath(
                                CrownsOfDust.MOD_ID,
                                "screen_shake"
                        )
                );

        public static final StreamCodec<
                RegistryFriendlyByteBuf,
                ScreenShakePayload
                > STREAM_CODEC =
                StreamCodec.composite(

                        ByteBufCodecs.INT,
                        ScreenShakePayload::duration,

                        ByteBufCodecs.FLOAT,
                        ScreenShakePayload::intensity,

                        ByteBufCodecs.INT,
                        ScreenShakePayload::x,

                        ByteBufCodecs.INT,
                        ScreenShakePayload::y,

                        ByteBufCodecs.INT,
                        ScreenShakePayload::z,

                        ByteBufCodecs.INT,
                        ScreenShakePayload::effectType,

                        ScreenShakePayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }


    // ==================================================
    // DESTINY COMPASS PAYLOAD
    // ==================================================

    public record DestinyCompassPayload(
            Identifier structureId
    ) implements CustomPacketPayload {

        public static final Type<DestinyCompassPayload> TYPE =
                new Type<>(
                        Identifier.fromNamespaceAndPath(
                                CrownsOfDust.MOD_ID,
                                "destiny_compass"
                        )
                );

        public static final StreamCodec<
                RegistryFriendlyByteBuf,
                DestinyCompassPayload
                > STREAM_CODEC =
                StreamCodec.composite(

                        Identifier.STREAM_CODEC,
                        DestinyCompassPayload::structureId,

                        DestinyCompassPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }


    // ==================================================
    // REGISTER PAYLOADS
    // ==================================================

    @SubscribeEvent
    public static void registerPayloads(
            RegisterPayloadHandlersEvent event) {

        event.registrar("1")

                // --------------------------------------------------
                // SERVER → CLIENT
                // --------------------------------------------------

                .playToClient(
                        ScreenShakePayload.TYPE,
                        ScreenShakePayload.STREAM_CODEC
                )

                // --------------------------------------------------
                // CLIENT → SERVER
                // --------------------------------------------------

                .playToServer(
                        DestinyCompassPayload.TYPE,
                        DestinyCompassPayload.STREAM_CODEC,
                        (payload, context) -> {

                            context.enqueueWork(() -> {

                                if (context.player()
                                        instanceof ServerPlayer player) {

                                    handleDestinyCompass(
                                            player,
                                            payload.structureId()
                                    );
                                }
                            });
                        }
                );
    }


    // ==================================================
    // DESTINY COMPASS
    // ==================================================

    private static void handleDestinyCompass(
            ServerPlayer player,
            Identifier structureId) {

        // --------------------------------------------------
        // Security
        // --------------------------------------------------

        if (!CrownsOfDust.MOD_ID.equals(
                structureId.getNamespace())) {

            return;
        }

        // --------------------------------------------------
        // Server level
        // --------------------------------------------------

        ServerLevel level =
                player.level();

        // --------------------------------------------------
        // Structure key
        // --------------------------------------------------

        ResourceKey<Structure> structureKey =
                ResourceKey.create(
                        Registries.STRUCTURE,
                        structureId
                );

        // --------------------------------------------------
        // Get structure registry
        // --------------------------------------------------

        var structureRegistry =
                level.registryAccess()
                        .lookupOrThrow(
                                Registries.STRUCTURE
                        );

        // --------------------------------------------------
        // Resolve structure holder
        // --------------------------------------------------

        Optional<Holder.Reference<Structure>> structure =
                structureRegistry.get(
                        structureKey
                );

        if (structure.isEmpty()) {

            CrownsOfDust.LOGGER.warn(
                    "Destiny Compass: structure {} does not exist",
                    structureId
            );

            return;
        }

        // --------------------------------------------------
        // Create HolderSet containing ONLY this structure
        // --------------------------------------------------

        HolderSet<Structure> wantedStructures =
                HolderSet.direct(
                        List.of(structure.get())
                );

        // --------------------------------------------------
        // Search
        //
        // maxSearchRadius is measured in chunks.
        //
        // 16384 chunks = 262144 blocks.
        // --------------------------------------------------

        int maxSearchRadius = 16_384;

        Pair<BlockPos, Holder<Structure>> result =
                level.getChunkSource()
                        .getGenerator()
                        .findNearestMapStructure(
                                level,
                                wantedStructures,
                                player.blockPosition(),
                                maxSearchRadius,
                                false
                        );

        // --------------------------------------------------
        // Nothing found
        // --------------------------------------------------

        if (result == null) {

            CrownsOfDust.LOGGER.warn(
                    "Destiny Compass: could not find {} near {}",
                    structureId,
                    player.blockPosition()
            );

            return;
        }

        // --------------------------------------------------
        // Found structure
        // --------------------------------------------------

        BlockPos structurePos =
                result.getFirst();

        // --------------------------------------------------
        // Convert to GlobalPos
        // --------------------------------------------------

        GlobalPos target =
                GlobalPos.of(
                        level.dimension(),
                        structurePos
                );

        // --------------------------------------------------
        // Find Destiny Compass
        // --------------------------------------------------

        ItemStack compass =
                findDestinyCompass(player);

        if (compass.isEmpty()) {

            CrownsOfDust.LOGGER.warn(
                    "Destiny Compass: player {} does not have the compass",
                    player.getName().getString()
            );

            return;
        }

        // --------------------------------------------------
        // Create lodestone target
        // --------------------------------------------------

        LodestoneTracker tracker =
                new LodestoneTracker(
                        Optional.of(target),
                        false
                );

        // --------------------------------------------------
        // Apply target
        // --------------------------------------------------

        compass.set(
                DataComponents.LODESTONE_TRACKER,
                tracker
        );
    }


    // ==================================================
    // FIND DESTINY COMPASS
    // ==================================================

    private static ItemStack findDestinyCompass(
            ServerPlayer player) {

        // --------------------------------------------------
        // Main inventory
        // --------------------------------------------------

        for (ItemStack stack :
                player.getInventory().getNonEquipmentItems()) {

            if (stack.is(
                    ModItems.DESTINY_COMPASS.get()
            )) {

                return stack;
            }
        }

        // --------------------------------------------------
        // Offhand
        // --------------------------------------------------

        ItemStack offhand =
                player.getOffhandItem();

        if (offhand.is(
                ModItems.DESTINY_COMPASS.get()
        )) {

            return offhand;
        }

        return ItemStack.EMPTY;
    }


    // ==================================================
    // REGISTER
    // ==================================================

    public static void register(IEventBus eventBus) {

        eventBus.addListener(
                ModNetwork::registerPayloads
        );
    }
}