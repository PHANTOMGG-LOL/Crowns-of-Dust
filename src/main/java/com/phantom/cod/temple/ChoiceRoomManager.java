package com.phantom.cod.temple;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.block.PedestalBlockEntity;
import com.phantom.cod.registry.ModBlocks;
import com.phantom.cod.registry.ModEntities;

import com.phantom.cod.registry.ModItems;
import com.phantom.cod.registry.ModSounds;
import com.phantom.cod.event.CustomWeaponInventoryEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(
        modid = CrownsOfDust.MOD_ID
)
public final class ChoiceRoomManager {

    private ChoiceRoomManager() {
    }


    // ==================================================
    // CHOICE ROOM DIMENSION
    // ==================================================

    private static final ResourceKey<Level> CHOICE_ROOM =
            ResourceKey.create(
                    Registries.DIMENSION,
                    Identifier.fromNamespaceAndPath(
                            CrownsOfDust.MOD_ID,
                            "choice_room"
                    )
            );


    // ==================================================
    // SEARCH AREA
    // ==================================================

    private static final int MIN_X = -2;
    private static final int MAX_X = 93;

    private static final int MIN_Y = -2;
    private static final int MAX_Y = 5;

    private static final int MIN_Z = -2;
    private static final int MAX_Z = 41;


    // ==================================================
    // LAST CHOSEN WEAPON
    // ==================================================

    private static final String TAG_LAST_WEAPON =
            "cod_last_choice_weapon";

    private static final String WEAPON_DIAMOND =
            "diamond_sword";

    private static final String WEAPON_NETHERITE =
            "netherite_sword";


    // ==================================================
    // RETURN LOCATION
    // ==================================================

    private static final String TAG_RETURN_DIMENSION =
            "cod_choice_return_dimension";

    private static final String TAG_RETURN_X =
            "cod_choice_return_x";

    private static final String TAG_RETURN_Y =
            "cod_choice_return_y";

    private static final String TAG_RETURN_Z =
            "cod_choice_return_z";

    private static final String TAG_RETURN_YAW =
            "cod_choice_return_yaw";

    private static final String TAG_RETURN_PITCH =
            "cod_choice_return_pitch";


    // ==================================================
    // ENCOUNTER
    // ==================================================

    /*
     * Herobrine / Null stays for 10 seconds.
     */
    private static final int ENCOUNTER_DURATION =
            10 * 20;


    /*
     * Lightning happens 3 seconds after
     * the entity appears.
     */
    private static final int LIGHTNING_DELAY =
            3 * 20;


    private static final List<ChoiceEncounter>
            ACTIVE_ENCOUNTERS =
            new ArrayList<>();


    // ==================================================
    // CHOICE ROOM WHISPER
    // ==================================================

    /*
     * How often the whisper repeats.
     * The first whisper is played as soon as the player
     * is detected inside the Choice Room.
     */
    private static final int WHISPER_INTERVAL =
            16 * 20;


    private static final List<WhisperPlayer>
            ACTIVE_WHISPERS =
            new ArrayList<>();


    // ==================================================
    // INITIALIZE CHOICE ROOM
    // ==================================================

    public static void initialize(
            ServerLevel level) {

        if (!level.dimension()
                .equals(CHOICE_ROOM)) {

            return;
        }

        CrownsOfDust.LOGGER.info(
                "Initializing Choice Room..."
        );

        int pedestalCount = 0;


        // ==================================================
        // SEARCH FOR REINFORCED DEEPSLATE
        // ==================================================

        for (int x = MIN_X; x <= MAX_X; x++) {

            for (int y = MIN_Y; y <= MAX_Y; y++) {

                for (int z = MIN_Z; z <= MAX_Z; z++) {

                    BlockPos basePos =
                            new BlockPos(
                                    x,
                                    y,
                                    z
                            );


                    if (!level.getBlockState(basePos)
                            .is(Blocks.REINFORCED_DEEPSLATE)) {

                        continue;
                    }


                    // ==================================================
                    // PEDESTAL POSITION
                    // ==================================================

                    BlockPos pedestalPos =
                            basePos.above();


                    // ==================================================
                    // GET / CREATE PEDESTAL
                    // ==================================================

                    BlockEntity blockEntity =
                            level.getBlockEntity(
                                    pedestalPos
                            );


                    if (!(blockEntity
                            instanceof PedestalBlockEntity)) {

                        if (!level.getBlockState(
                                pedestalPos
                        ).isAir()) {

                            continue;
                        }


                        level.setBlock(
                                pedestalPos,
                                ModBlocks.PEDESTAL
                                        .get()
                                        .defaultBlockState(),
                                3
                        );


                        blockEntity =
                                level.getBlockEntity(
                                        pedestalPos
                                );
                    }


                    // ==================================================
                    // INITIALIZE PEDESTAL
                    // ==================================================

                    if (blockEntity
                            instanceof PedestalBlockEntity pedestal) {

                        pedestal.setTemplePedestal(false);


                        // ==================================================
                        // FIRST WEAPON
                        // ==================================================

                        if (pedestalCount == 0) {

                            pedestal.setChoiceWeapon(
                                    PedestalBlockEntity
                                            .ChoiceWeapon
                                            .DIAMOND_SWORD
                            );
                        }


                        // ==================================================
                        // SECOND WEAPON
                        // ==================================================

                        else if (pedestalCount == 1) {

                            pedestal.setChoiceWeapon(
                                    PedestalBlockEntity
                                            .ChoiceWeapon
                                            .NETHERITE_SWORD
                            );
                        }
                    }


                    pedestalCount++;


                    CrownsOfDust.LOGGER.info(
                            "Choice Room pedestal {} created at {} {} {}",
                            pedestalCount,
                            pedestalPos.getX(),
                            pedestalPos.getY(),
                            pedestalPos.getZ()
                    );


                    // ==================================================
                    // ONLY NEED TWO
                    // ==================================================

                    if (pedestalCount >= 2) {

                        CrownsOfDust.LOGGER.info(
                                "Choice Room initialized successfully."
                        );

                        return;
                    }
                }
            }
        }


        // ==================================================
        // RESULT
        // ==================================================

        if (pedestalCount == 0) {

            CrownsOfDust.LOGGER.warn(
                    "Choice Room found no reinforced deepslate."
            );

        } else if (pedestalCount == 1) {

            CrownsOfDust.LOGGER.warn(
                    "Choice Room found only one weapon pedestal."
            );
        }
    }


    // ==================================================
// PREPARE PLAYER FOR CHOICE ROOM
// ==================================================

    public static void prepareForChoiceRoom(
            ServerPlayer player) {

        CompoundTag data =
                player.getPersistentData();

        // ==================================================
        // CLEAR THE CURRENT CHOSEN WEAPON
        // ==================================================
        //
        // This is the intentional removal path. The inventory
        // protection system will not restore the weapon after
        // this call because ownership is cleared first.
        // ==================================================

        CustomWeaponInventoryEvents.clearChosenWeapon(player);

        // ==================================================
        // CLEAR PREVIOUS CHOICE
        // ==================================================

        data.remove(
                TAG_LAST_WEAPON
        );

        CrownsOfDust.LOGGER.info(
                "Cleared previous Choice Room weapons from {}.",
                player.getGameProfile().name()
        );
    }


    // ==================================================
// REMOVE ALL COPIES OF ONE ITEM
// ==================================================

    private static void removeAllItems(
            ServerPlayer player,
            net.minecraft.world.item.Item item) {

        Inventory inventory =
                player.getInventory();

        // ==================================================
        // SEARCH ENTIRE PLAYER INVENTORY
        // ==================================================

        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            ItemStack stack =
                    inventory.getItem(slot);

            // ------------------------------------------------
            // Not our weapon.
            // ------------------------------------------------

            if (!stack.is(item)) {
                continue;
            }

            // ------------------------------------------------
            // Remove the entire stack.
            // ------------------------------------------------

            inventory.setItem(
                    slot,
                    ItemStack.EMPTY
            );
        }

        inventory.setChanged();
    }


    // ==================================================
    // REMEMBER TEMPLE LOCATION
    // ==================================================

    public static void rememberReturnLocation(
            ServerPlayer player) {

        /*
         * Remove the player's previous weapon
         * before entering the Choice Room.
         */

        prepareForChoiceRoom(
                player
        );


        CompoundTag data =
                player.getPersistentData();


        ServerLevel level =
                player.level();


        // ==================================================
        // DIMENSION
        // ==================================================

        data.putString(
                TAG_RETURN_DIMENSION,
                level.dimension()
                        .identifier()
                        .toString()
        );


        // ==================================================
        // POSITION
        // ==================================================

        data.putDouble(
                TAG_RETURN_X,
                player.getX()
        );

        data.putDouble(
                TAG_RETURN_Y,
                player.getY()
        );

        data.putDouble(
                TAG_RETURN_Z,
                player.getZ()
        );


        // ==================================================
        // ROTATION
        // ==================================================

        data.putFloat(
                TAG_RETURN_YAW,
                player.getYRot()
        );

        data.putFloat(
                TAG_RETURN_PITCH,
                player.getXRot()
        );


        CrownsOfDust.LOGGER.info(
                "Saved Choice Room return location for {}.",
                player.getGameProfile().name()
        );
    }


    // ==================================================
// HANDLE WEAPON CHOICE
// ==================================================

    public static void handleWeaponChoice(
            ServerLevel level,
            PedestalBlockEntity pedestal,
            ServerPlayer player) {

        // ==================================================
        // MUST BE CHOICE ROOM
        // ==================================================

        if (!level.dimension()
                .equals(CHOICE_ROOM)) {

            return;
        }


        // ==================================================
        // GET WEAPON
        // ==================================================

        PedestalBlockEntity.ChoiceWeapon weapon =
                pedestal.getChoiceWeapon();


        if (weapon ==
                PedestalBlockEntity.ChoiceWeapon.NONE) {

            return;
        }


        // ==================================================
        // GET PLAYER DATA
        // ==================================================

        CompoundTag data =
                player.getPersistentData();


        // ==================================================
        // PREVENT DOUBLE CHOICE
        // ==================================================

        if (data.contains(TAG_LAST_WEAPON)) {

            player.sendSystemMessage(
                    Component.literal(
                            "You have already chosen a weapon."
                    )
            );

            return;
        }


        // ==================================================
        // CREATE WEAPON
        // ==================================================

        ItemStack weaponStack;


        switch (weapon) {

            // ==================================================
            // DIAMOND / HEROBRINE
            // ==================================================

            case DIAMOND_SWORD -> {

                weaponStack =
                        new ItemStack(
                                ModItems.HEROBRINES_LEGACY.get()
                        );
            }


            // ==================================================
            // NETHERITE / NULL
            // ==================================================

            case NETHERITE_SWORD -> {

                weaponStack =
                        new ItemStack(
                                ModItems.NULLS_SILENCE.get()
                        );
            }


            // ==================================================
            // NONE
            // ==================================================

            case NONE -> {
                return;
            }


            default -> {
                return;
            }
        }


        // ==================================================
        // CHECK INVENTORY SPACE
        // ==================================================

        /*
         * Keep an exact copy BEFORE Inventory.add() can mutate
         * the temporary ItemStack.
         */
        ItemStack chosenWeaponSnapshot =
                weaponStack.copy();


        /*
         * DO NOT consume the choice if the player
         * cannot actually receive the weapon.
         */
        if (!player.getInventory()
                .add(weaponStack)) {

            player.sendSystemMessage(
                    Component.literal(
                            "You need space in your inventory "
                                    + "to choose a weapon."
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.RED)
                            .withBold(true))
            );

            return;
        }


        // ==================================================
        // MARK WEAPON AS CHOSEN
        // ==================================================

        /*
         * Use the snapshot instead of weaponStack.
         *
         * Inventory.add() may consume/mutate weaponStack after
         * successfully inserting it.
         *
         * The snapshot still contains the correct ItemStack.
         */
        CustomWeaponInventoryEvents.markWeaponChosen(
                player,
                chosenWeaponSnapshot
        );


        // ==================================================
        // RECORD CHOICE
        // ==================================================

        switch (weapon) {

            case DIAMOND_SWORD -> {

                data.putString(
                        TAG_LAST_WEAPON,
                        WEAPON_DIAMOND
                );
            }


            case NETHERITE_SWORD -> {

                data.putString(
                        TAG_LAST_WEAPON,
                        WEAPON_NETHERITE
                );
            }


            case NONE -> {
                return;
            }


            default -> {
                return;
            }
        }


        // ==================================================
        // START ENCOUNTER
        // ==================================================

        switch (weapon) {

            // ==================================================
            // HEROBRINE
            // ==================================================

            case DIAMOND_SWORD -> {

                startHerobrineEncounter(
                        level,
                        pedestal,
                        player
                );
            }


            // ==================================================
            // NULL
            // ==================================================

            case NETHERITE_SWORD -> {

                startNullEncounter(
                        level,
                        pedestal,
                        player
                );


                // ==================================================
                // DARKNESS
                // ==================================================

                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.DARKNESS,
                                20 * 5,
                                0,
                                false,
                                false
                        )
                );
            }


            case NONE -> {
                return;
            }


            default -> {
                return;
            }
        }


        // ==================================================
        // MESSAGE
        // ==================================================

        player.sendSystemMessage(
                Component.literal(
                        "The choice has been made."
                ).withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withBold(true))
        );
    }


    // ==================================================
    // START HEROBRINE ENCOUNTER
    // ==================================================

    private static void startHerobrineEncounter(
            ServerLevel level,
            PedestalBlockEntity pedestal,
            ServerPlayer player) {

        // ==================================================
        // PREVENT DUPLICATE ENCOUNTER
        // ==================================================

        for (ChoiceEncounter encounter :
                ACTIVE_ENCOUNTERS) {

            if (encounter.player
                    .getUUID()
                    .equals(player.getUUID())) {

                return;
            }
        }


        // ==================================================
        // SPAWN HEROBRINE
        // ==================================================

        Entity herobrine =
                spawnHerobrine(
                        level,
                        pedestal,
                        player
                );


        if (herobrine == null) {

            CrownsOfDust.LOGGER.error(
                    "Failed to spawn Herobrine encounter."
            );

            return;
        }


        // ==================================================
        // START TIMER
        // ==================================================

        ACTIVE_ENCOUNTERS.add(
                new ChoiceEncounter(
                        player,
                        herobrine
                )
        );


        CrownsOfDust.LOGGER.info(
                "Herobrine encounter started for {}.",
                player.getGameProfile().name()
        );
    }


    // ==================================================
    // START NULL ENCOUNTER
    // ==================================================

    private static void startNullEncounter(
            ServerLevel level,
            PedestalBlockEntity pedestal,
            ServerPlayer player) {

        // ==================================================
        // PREVENT DUPLICATE ENCOUNTER
        // ==================================================

        for (ChoiceEncounter encounter :
                ACTIVE_ENCOUNTERS) {

            if (encounter.player
                    .getUUID()
                    .equals(player.getUUID())) {

                return;
            }
        }


        // ==================================================
        // SPAWN NULL
        // ==================================================

        Entity nullEntity =
                spawnNull(
                        level,
                        pedestal,
                        player
                );


        if (nullEntity == null) {

            CrownsOfDust.LOGGER.error(
                    "Failed to spawn Null encounter."
            );

            return;
        }


        // ==================================================
        // START TIMER
        // ==================================================

        ACTIVE_ENCOUNTERS.add(
                new ChoiceEncounter(
                        player,
                        nullEntity
                )
        );


        CrownsOfDust.LOGGER.info(
                "Null encounter started for {}.",
                player.getGameProfile().name()
        );
    }


    // ==================================================
    // SPAWN HEROBRINE
    // ==================================================

    private static Entity spawnHerobrine(
            ServerLevel level,
            PedestalBlockEntity pedestal,
            ServerPlayer player) {

        BlockPos pedestalPos =
                pedestal.getBlockPos();


        // ==================================================
        // DIRECTION FROM PLAYER TO PEDESTAL
        // ==================================================

        double dx =
                pedestalPos.getX()
                        + 0.5D
                        - player.getX();

        double dz =
                pedestalPos.getZ()
                        + 0.5D
                        - player.getZ();


        double length =
                Math.sqrt(
                        dx * dx +
                                dz * dz
                );


        if (length < 0.001D) {

            dx = 0.0D;
            dz = 1.0D;

            length = 1.0D;
        }


        dx /= length;
        dz /= length;


        // ==================================================
        // BEHIND PEDESTAL
        // ==================================================

        double spawnDistance =
                2.5D;


        double x =
                pedestalPos.getX()
                        + 0.5D
                        + dx * spawnDistance;

        double y =
                pedestalPos.getY();

        double z =
                pedestalPos.getZ()
                        + 0.5D
                        + dz * spawnDistance;


        // ==================================================
        // CREATE ENTITY
        // ==================================================

        Entity entity =
                ModEntities.HEROBRINE
                        .get()
                        .create(
                                level,
                                EntitySpawnReason.TRIGGERED
                        );


        if (entity == null) {
            return null;
        }


        entity.setPos(
                x,
                y,
                z
        );


        // ==================================================
        // FACE PLAYER
        // ==================================================

        double lookX =
                player.getX()
                        - entity.getX();

        double lookZ =
                player.getZ()
                        - entity.getZ();


        float yaw =
                (float)
                        (Math.toDegrees(
                                Math.atan2(
                                        lookZ,
                                        lookX
                                )
                        ) - 90.0D);


        entity.setYRot(yaw);
        entity.setYHeadRot(yaw);


        // ==================================================
        // ADD TO WORLD
        // ==================================================

        level.addFreshEntity(
                entity
        );


        return entity;
    }


    // ==================================================
    // SPAWN DELAYED LIGHTNING
    // ==================================================

    private static void spawnEncounterLightning(
            ServerLevel level,
            Entity entity) {

        Identifier lightningId =
                Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "lightning_bolt"
                );


        EntityType<?> lightningType =
                BuiltInRegistries.ENTITY_TYPE
                        .getValue(
                                lightningId
                        );


        // ==================================================
        // FIRST LIGHTNING
        // ==================================================

        spawnOneLightning(
                level,
                lightningType,
                entity.getX(),
                entity.getY(),
                entity.getZ()
        );


        // ==================================================
        // SECOND LIGHTNING
        // ==================================================

        spawnOneLightning(
                level,
                lightningType,
                entity.getX() + 2.0D,
                entity.getY(),
                entity.getZ() + 1.0D
        );


        // ==================================================
        // THIRD LIGHTNING
        // ==================================================

        spawnOneLightning(
                level,
                lightningType,
                entity.getX() - 2.0D,
                entity.getY(),
                entity.getZ() - 1.0D
        );
    }


    // ==================================================
    // SPAWN ONE LIGHTNING
    // ==================================================

    private static void spawnOneLightning(
            ServerLevel level,
            EntityType<?> lightningType,
            double x,
            double y,
            double z) {

        Entity spawned =
                lightningType.spawn(
                        level,
                        BlockPos.containing(
                                x,
                                y,
                                z
                        ),
                        EntitySpawnReason.TRIGGERED
                );


        if (!(spawned
                instanceof LightningBolt lightning)) {

            return;
        }


        // ==================================================
        // EXACT POSITION
        // ==================================================

        lightning.setPos(
                x,
                y,
                z
        );


        // ==================================================
        // VISUAL ONLY
        // ==================================================

        /*
         * Prevents the lightning from behaving
         * like a normal damaging/weather strike.
         */

        lightning.setVisualOnly(true);
    }


    // ==================================================
    // SPAWN NULL
    // ==================================================

    private static Entity spawnNull(
            ServerLevel level,
            PedestalBlockEntity pedestal,
            ServerPlayer player) {

        BlockPos pedestalPos =
                pedestal.getBlockPos();


        // ==================================================
        // DIRECTION FROM PLAYER TO PEDESTAL
        // ==================================================

        double dx =
                pedestalPos.getX()
                        + 0.5D
                        - player.getX();

        double dz =
                pedestalPos.getZ()
                        + 0.5D
                        - player.getZ();


        double length =
                Math.sqrt(
                        dx * dx +
                                dz * dz
                );


        if (length < 0.001D) {

            dx = 0.0D;
            dz = 1.0D;

            length = 1.0D;
        }


        dx /= length;
        dz /= length;


        // ==================================================
        // BEHIND PEDESTAL
        // ==================================================

        double spawnDistance =
                2.5D;


        double x =
                pedestalPos.getX()
                        + 0.5D
                        + dx * spawnDistance;

        double y =
                pedestalPos.getY();

        double z =
                pedestalPos.getZ()
                        + 0.5D
                        + dz * spawnDistance;


        // ==================================================
        // CREATE NULL
        // ==================================================

        Entity entity =
                ModEntities.NULL
                        .get()
                        .create(
                                level,
                                EntitySpawnReason.TRIGGERED
                        );


        if (entity == null) {
            return null;
        }


        entity.setPos(
                x,
                y,
                z
        );


        // ==================================================
        // FACE PLAYER
        // ==================================================

        double lookX =
                player.getX()
                        - entity.getX();

        double lookZ =
                player.getZ()
                        - entity.getZ();


        float yaw =
                (float)
                        (Math.toDegrees(
                                Math.atan2(
                                        lookZ,
                                        lookX
                                )
                        ) - 90.0D);


        entity.setYRot(yaw);
        entity.setYHeadRot(yaw);


        // ==================================================
        // ADD TO WORLD
        // ==================================================

        level.addFreshEntity(
                entity
        );


        return entity;
    }


    // ==================================================
    // SERVER TICK
    // ==================================================

    @SubscribeEvent
    public static void onServerTick(
            ServerTickEvent.Post event) {

        // ==================================================
        // CHOICE ROOM WHISPERS
        // ==================================================

        /*
         * We detect players directly from the Choice Room
         * dimension instead of relying on the teleport code.
         * This means the whisper also starts correctly if the
         * player is sent there by another system.
         */
        ServerLevel choiceRoom =
                event.getServer().getLevel(CHOICE_ROOM);

        if (choiceRoom != null) {

            // --------------------------------------------------
            // Add players who have entered the room.
            // --------------------------------------------------

            for (ServerPlayer player :
                    choiceRoom.players()) {

                boolean alreadyActive = false;

                for (WhisperPlayer whisper :
                        ACTIVE_WHISPERS) {

                    if (whisper.player.getUUID()
                            .equals(player.getUUID())) {

                        alreadyActive = true;
                        break;
                    }
                }

                if (!alreadyActive) {

                    ACTIVE_WHISPERS.add(
                            new WhisperPlayer(player)
                    );

                    // Play immediately when entering.
                    playChoiceRoomWhisper(player);
                }
            }
        }


        // --------------------------------------------------
        // Update active whispers.
        // --------------------------------------------------

        Iterator<WhisperPlayer> whisperIterator =
                ACTIVE_WHISPERS.iterator();

        while (whisperIterator.hasNext()) {

            WhisperPlayer whisper =
                    whisperIterator.next();

            ServerPlayer player =
                    whisper.player;

            // Stop automatically when the player leaves.
            if (player.isRemoved()
                    || !player.isAlive()
                    || !player.level().dimension()
                    .equals(CHOICE_ROOM)) {

                whisperIterator.remove();
                continue;
            }

            whisper.ticksUntilNext--;

            if (whisper.ticksUntilNext <= 0) {

                playChoiceRoomWhisper(player);

                whisper.ticksUntilNext =
                        WHISPER_INTERVAL;
            }
        }


        // ==================================================
        // ENCOUNTERS
        // ==================================================

        if (ACTIVE_ENCOUNTERS.isEmpty()) {
            return;
        }


        Iterator<ChoiceEncounter> iterator =
                ACTIVE_ENCOUNTERS.iterator();


        while (iterator.hasNext()) {

            ChoiceEncounter encounter =
                    iterator.next();


            encounter.ticksRemaining--;


            // ==================================================
            // LIGHTNING AFTER 3 SECONDS
            // ==================================================

            if (!encounter.lightningTriggered
                    && encounter.ticksRemaining
                    <= ENCOUNTER_DURATION
                    - LIGHTNING_DELAY) {

                if (encounter.entity != null
                        && !encounter.entity.isRemoved()) {

                    if (encounter.player.level()
                            instanceof ServerLevel encounterLevel) {

                        spawnEncounterLightning(
                                encounterLevel,
                                encounter.entity
                        );
                    }
                }


                encounter.lightningTriggered =
                        true;
            }


            // ==================================================
            // PLAYER INVALID
            // ==================================================

            if (encounter.player.isRemoved()
                    || !encounter.player.isAlive()) {

                if (encounter.entity != null
                        && !encounter.entity.isRemoved()) {

                    encounter.entity.discard();
                }


                iterator.remove();

                continue;
            }


            // ==================================================
            // ENTITY DISAPPEARED
            // ==================================================

            if (encounter.entity == null
                    || encounter.entity.isRemoved()) {

                iterator.remove();


                returnPlayerToTemple(
                        encounter.player
                );


                continue;
            }


            // ==================================================
            // TEN SECONDS COMPLETE
            // ==================================================

            if (encounter.ticksRemaining <= 0) {

                // ------------------------------------------
                // REMOVE ENTITY
                // ------------------------------------------

                if (encounter.entity != null
                        && !encounter.entity.isRemoved()) {

                    encounter.entity.discard();
                }


                // ------------------------------------------
                // REMOVE ENCOUNTER
                // ------------------------------------------

                iterator.remove();


                // ------------------------------------------
                // RETURN PLAYER
                // ------------------------------------------

                returnPlayerToTemple(
                        encounter.player
                );


                CrownsOfDust.LOGGER.info(
                        "Choice Room encounter ended for {}.",
                        encounter.player
                                .getGameProfile()
                                .name()
                );
            }
        }
    }


    // ==================================================
    // PLAY CHOICE ROOM WHISPER
    // ==================================================

    private static void playChoiceRoomWhisper(
            ServerPlayer player) {

        if (!(player.level()
                instanceof ServerLevel level)) {

            return;
        }

        level.playSound(
                null,
                player.blockPosition(),
                ModSounds.WHISPER.get(),
                net.minecraft.sounds.SoundSource.AMBIENT,
                0.65F,
                0.85F + level.getRandom().nextFloat() * 0.25F
        );
    }


    // ==================================================
    // RETURN PLAYER TO TEMPLE
    // ==================================================

    private static void returnPlayerToTemple(
            ServerPlayer player) {

        CompoundTag data =
                player.getPersistentData();


        // ==================================================
        // CHECK RETURN LOCATION
        // ==================================================

        if (!data.contains(
                TAG_RETURN_DIMENSION
        )) {

            CrownsOfDust.LOGGER.error(
                    "No stored Temple return location for {}.",
                    player.getGameProfile().name()
            );


            player.sendSystemMessage(
                    Component.literal(
                            "The Temple could not find your return path."
                    )
            );


            return;
        }


        // ==================================================
        // GET DIMENSION
        // ==================================================

        Identifier identifier =
                Identifier.parse(
                        data.getStringOr(
                                TAG_RETURN_DIMENSION,
                                "minecraft:overworld"
                        )
                );


        ResourceKey<Level> dimensionKey =
                ResourceKey.create(
                        Registries.DIMENSION,
                        identifier
                );


        MinecraftServer server =
                player.level()
                        .getServer();


        ServerLevel returnLevel =
                server.getLevel(
                        dimensionKey
                );


        if (returnLevel == null) {

            CrownsOfDust.LOGGER.error(
                    "Could not find return dimension: {}",
                    identifier
            );


            return;
        }


        // ==================================================
        // POSITION
        // ==================================================

        Vec3 returnPos =
                new Vec3(
                        data.getDoubleOr(
                                TAG_RETURN_X,
                                0.0D
                        ),

                        data.getDoubleOr(
                                TAG_RETURN_Y,
                                0.0D
                        ),

                        data.getDoubleOr(
                                TAG_RETURN_Z,
                                0.0D
                        )
                );


        // ==================================================
        // ROTATION
        // ==================================================

        float yaw =
                data.getFloatOr(
                        TAG_RETURN_YAW,
                        0.0F
                );


        float pitch =
                data.getFloatOr(
                        TAG_RETURN_PITCH,
                        0.0F
                );


        // ==================================================
        // TELEPORT
        // ==================================================

        TeleportTransition transition =
                new TeleportTransition(
                        returnLevel,
                        returnPos,
                        Vec3.ZERO,
                        yaw,
                        pitch,
                        TeleportTransition.DO_NOTHING
                );


        player.teleport(
                transition
        );


        // ==================================================
        // CLEAR RETURN DATA
        // ==================================================

        data.remove(
                TAG_RETURN_DIMENSION
        );

        data.remove(
                TAG_RETURN_X
        );

        data.remove(
                TAG_RETURN_Y
        );

        data.remove(
                TAG_RETURN_Z
        );

        data.remove(
                TAG_RETURN_YAW
        );

        data.remove(
                TAG_RETURN_PITCH
        );


        CrownsOfDust.LOGGER.info(
                "Returned {} to the Temple.",
                player.getGameProfile().name()
        );
    }


    // ==================================================
    // OLD RESET METHOD
    // ==================================================

    @Deprecated
    public static void resetPlayer(
            ServerPlayer player) {

        /*
         * Previous weapons are intentionally removed
         * when the player enters the Choice Room.
         */
    }


    // ==================================================
    // WHISPER PLAYER
    // ==================================================

    private static final class WhisperPlayer {

        final ServerPlayer player;

        int ticksUntilNext;


        WhisperPlayer(
                ServerPlayer player) {

            this.player =
                    player;

            this.ticksUntilNext =
                    WHISPER_INTERVAL;
        }
    }


    // ==================================================
    // CHOICE ENCOUNTER
    // ==================================================

    private static final class ChoiceEncounter {

        final ServerPlayer player;

        final Entity entity;

        int ticksRemaining;

        boolean lightningTriggered;


        ChoiceEncounter(
                ServerPlayer player,
                Entity entity) {

            this.player =
                    player;

            this.entity =
                    entity;

            this.ticksRemaining =
                    ENCOUNTER_DURATION;

            this.lightningTriggered =
                    false;
        }
    }
}
