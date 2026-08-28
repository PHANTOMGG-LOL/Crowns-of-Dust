package com.phantom.cod.temple;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.block.PedestalBlockEntity;
import com.phantom.cod.network.ModNetwork;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(
        modid = CrownsOfDust.MOD_ID
)
public final class TempleManager {

    private TempleManager() {
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
    // SEARCH RADIUS
    // ==================================================

    private static final int SEARCH_RADIUS = 32;


    // ==================================================
    // RITUAL OWNER
    // ==================================================

    private static UUID ritualOwner = null;


    // ==================================================
    // TEMPLE ORIGIN
    // ==================================================

    private static BlockPos ritualOrigin = null;


    // ==================================================
    // RITUAL TIMEOUT
    // ==================================================

    private static long ritualTimeout = 0L;


    // ==================================================
    // TEMPLE COOLDOWN
    // ==================================================

    private static long templeCooldownEnd = 0L;


    // ==================================================
    // TIMER VALUES
    // ==================================================

    /*
     * Player has 10 minutes to complete
     * the four-stone ritual.
     */
    private static final long RITUAL_TIMEOUT =
            10L * 60L * 20L;


    /*
     * Temple remains unavailable for
     * 2 minutes after completion.
     */
    private static final long TEMPLE_COOLDOWN =
            2L * 60L * 20L;


    // ==================================================
    // TELEPORT QUEUE
    // ==================================================

    private static final List<PendingTeleport>
            PENDING_TELEPORTS =
            new ArrayList<>();


    private static final class PendingTeleport {

        final ServerPlayer player;

        final ServerLevel choiceLevel;

        // --------------------------------------------------
        // ORIGINAL TEMPLE
        // --------------------------------------------------

        final ServerLevel templeLevel;

        final BlockPos templeOrigin;

        // --------------------------------------------------
        // CHOICE ROOM DESTINATION
        // --------------------------------------------------

        final double x;
        final double y;
        final double z;

        final float yaw;
        final float pitch;

        int ticksRemaining;


        PendingTeleport(
                ServerPlayer player,
                ServerLevel choiceLevel,
                ServerLevel templeLevel,
                BlockPos templeOrigin,
                double x,
                double y,
                double z,
                float yaw,
                float pitch,
                int ticksRemaining) {

            this.player = player;

            this.choiceLevel = choiceLevel;

            this.templeLevel = templeLevel;

            this.templeOrigin =
                    templeOrigin.immutable();

            this.x = x;
            this.y = y;
            this.z = z;

            this.yaw = yaw;
            this.pitch = pitch;

            this.ticksRemaining =
                    ticksRemaining;
        }
    }


    // ==================================================
    // CHECK STONE ALREADY PLACED
    // ==================================================

    public static boolean isStoneAlreadyPlaced(
            ServerLevel level,
            BlockPos origin,
            TempleStone stone) {

        if (!stone.isValid()) {
            return false;
        }


        BlockPos min =
                origin.offset(
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS
                );


        BlockPos max =
                origin.offset(
                        SEARCH_RADIUS,
                        SEARCH_RADIUS,
                        SEARCH_RADIUS
                );


        for (BlockPos pos :
                BlockPos.betweenClosed(
                        min,
                        max
                )) {

            var blockEntity =
                    level.getBlockEntity(pos);


            if (!(blockEntity
                    instanceof PedestalBlockEntity pedestal)) {

                continue;
            }


            if (!pedestal.isTemplePedestal()) {
                continue;
            }


            if (pedestal.getTempleStone() == stone) {
                return true;
            }
        }


        return false;
    }


    // ==================================================
    // CHECK TEMPLE COOLDOWN
    // ==================================================

    public static boolean isOnCooldown(
            ServerLevel level) {

        return templeCooldownEnd >
                level.getGameTime();
    }


    // ==================================================
    // GET REMAINING COOLDOWN
    // ==================================================

    public static long getRemainingCooldown(
            ServerLevel level) {

        return Math.max(
                0L,
                templeCooldownEnd
                        - level.getGameTime()
        );
    }


    // ==================================================
    // CHECK CLAIM
    // ==================================================

    public static boolean isClaimed() {

        return ritualOwner != null;
    }


    // ==================================================
    // CHECK OWNER
    // ==================================================

    public static boolean isOwner(
            Player player) {

        return ritualOwner != null
                && ritualOwner.equals(
                player.getUUID()
        );
    }


    // ==================================================
    // CLAIM TEMPLE
    // ==================================================

    public static void claimTemple(
            ServerLevel level,
            BlockPos origin,
            Player player) {

        if (ritualOwner != null) {
            return;
        }


        ritualOwner =
                player.getUUID();


        ritualOrigin =
                origin.immutable();


        ritualTimeout =
                level.getGameTime()
                        + RITUAL_TIMEOUT;


        CrownsOfDust.LOGGER.info(
                "Temple ritual claimed by {} at {}",
                player.getName().getString(),
                ritualOrigin
        );
    }


    // ==================================================
    // CLEAR RITUAL
    // ==================================================

    public static void clearRitual() {

        ritualOwner = null;

        ritualOrigin = null;

        ritualTimeout = 0L;
    }


    // ==================================================
    // CHECK ALL FOUR STONES
    // ==================================================

    public static boolean hasAllFourStones(
            ServerLevel level,
            BlockPos origin) {

        boolean valor = false;

        boolean wisdom = false;

        boolean unity = false;

        boolean resolve = false;


        BlockPos min =
                origin.offset(
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS
                );


        BlockPos max =
                origin.offset(
                        SEARCH_RADIUS,
                        SEARCH_RADIUS,
                        SEARCH_RADIUS
                );


        for (BlockPos pos :
                BlockPos.betweenClosed(
                        min,
                        max
                )) {

            var blockEntity =
                    level.getBlockEntity(pos);


            if (!(blockEntity
                    instanceof PedestalBlockEntity pedestal)) {

                continue;
            }


            if (!pedestal.isTemplePedestal()) {
                continue;
            }


            switch (pedestal.getTempleStone()) {

                case VALOR ->
                        valor = true;

                case WISDOM ->
                        wisdom = true;

                case UNITY ->
                        unity = true;

                case RESOLVE ->
                        resolve = true;

                case NONE -> {
                }
            }
        }


        return valor
                && wisdom
                && unity
                && resolve;
    }


    // ==================================================
    // CLEAR ALL TEMPLE STONES
    // ==================================================

    public static void clearAllTempleStones(
            ServerLevel level,
            BlockPos origin) {

        BlockPos min =
                origin.offset(
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS
                );


        BlockPos max =
                origin.offset(
                        SEARCH_RADIUS,
                        SEARCH_RADIUS,
                        SEARCH_RADIUS
                );


        for (BlockPos pos :
                BlockPos.betweenClosed(
                        min,
                        max
                )) {

            var blockEntity =
                    level.getBlockEntity(pos);


            if (!(blockEntity
                    instanceof PedestalBlockEntity pedestal)) {

                continue;
            }


            if (!pedestal.isTemplePedestal()) {
                continue;
            }


            if (pedestal.hasTempleStone()) {

                pedestal.setTempleStone(
                        TempleStone.NONE
                );
            }
        }
    }


    // ==================================================
    // TEMPLE COMPLETION
    // ==================================================

    public static void completeTemple(
            ServerLevel level,
            BlockPos origin,
            Player player) {

        // ==================================================
        // SERVER PLAYER
        // ==================================================

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }


        // ==================================================
        // VERIFY OWNER
        // ==================================================

        if (!isOwner(player)) {

            CrownsOfDust.LOGGER.warn(
                    "Temple completion attempted by "
                            + "non-owner {}",
                    player.getName().getString()
            );

            return;
        }


        // ==================================================
        // FIND CHOICE ROOM
        // ==================================================

        ServerLevel choiceLevel =
                level.getServer()
                        .getLevel(CHOICE_ROOM);


        if (choiceLevel == null) {

            CrownsOfDust.LOGGER.error(
                    "Choice room dimension could not "
                            + "be found: {}",
                    CHOICE_ROOM.identifier()
            );


            serverPlayer.sendSystemMessage(
                   Component.literal(
                            "The Temple could not open "
                                    + "its destination."
                    ).withStyle(style -> style
                           .withColor(ChatFormatting.RED)
                           .withBold(true))
            );


            return;
        }


        // ==================================================
        // PLACE CHOICE ROOM
        // ==================================================

        if (!ChoiceRoomStructure.place(
                choiceLevel
        )) {

            CrownsOfDust.LOGGER.error(
                    "The Choice Room structure could "
                            + "not be placed."
            );


            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "The Choice Room could not "
                                    + "be prepared."
                    )
            );


            return;
        }


        // ==================================================
        // INITIALIZE CHOICE ROOM PEDESTALS
        // ==================================================

        ChoiceRoomManager.initialize(
                choiceLevel
        );


        // ==================================================
        // START 2-MINUTE COOLDOWN
        // ==================================================

        templeCooldownEnd =
                level.getGameTime()
                        + TEMPLE_COOLDOWN;


        // ==================================================
        // CLEAR RITUAL OWNER
        // ==================================================

        clearRitual();


        // ==================================================
        // BIG TEMPLE SHAKE
        // ==================================================

        ModNetwork.ScreenShakePayload payload =
                new ModNetwork.ScreenShakePayload(
                        100,
                        1.75F,
                        origin.getX(),
                        origin.getY(),
                        origin.getZ(),
                        3
                );


        PacketDistributor.sendToPlayersNear(
                level,
                null,
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                32.0D,
                payload
        );


        // ==================================================
        // SAVE TEMPLE RETURN LOCATION
        // ==================================================

        ChoiceRoomManager.rememberReturnLocation(
                serverPlayer
        );


        // ==================================================
        // CHOICE ROOM DESTINATION
        // ==================================================

        double x =
                ChoiceRoomStructure.getPlayerX();


        double y =
                ChoiceRoomStructure.getPlayerY();


        double z =
                ChoiceRoomStructure.getPlayerZ();


        // ==================================================
        // QUEUE TELEPORT
        //
        // 100 TICKS = 5 SECONDS
        //
        // THE STONES REMAIN VISIBLE DURING THIS TIME.
        // ==================================================

        PENDING_TELEPORTS.add(
                new PendingTeleport(
                        serverPlayer,
                        choiceLevel,

                        // ----------------------------------
                        // ORIGINAL TEMPLE
                        // ----------------------------------

                        level,
                        origin,

                        // ----------------------------------
                        // CHOICE ROOM
                        // ----------------------------------

                        x,
                        y,
                        z,

                        0.0F,
                        0.0F,

                        100
                )
        );
    }


    // ==================================================
    // SERVER TICK
    // ==================================================

    @SubscribeEvent
    public static void onServerTick(
            ServerTickEvent.Post event) {

        // ==================================================
        // RITUAL TIMEOUT
        // ==================================================

        if (ritualOwner != null
                && ritualOrigin != null
                && ritualTimeout > 0L) {

            ServerPlayer owner =
                    event.getServer()
                            .getPlayerList()
                            .getPlayer(
                                    ritualOwner
                            );


            if (owner != null) {

                if (owner.level()
                        instanceof ServerLevel templeLevel) {

                    if (templeLevel.getGameTime()
                            >= ritualTimeout) {

                        // ==========================================
                        // REMOVE PARTIAL STONES
                        // ==========================================

                        clearAllTempleStones(
                                templeLevel,
                                ritualOrigin
                        );


                        // ==========================================
                        // RESET RITUAL
                        // ==========================================

                        clearRitual();


                        owner.sendSystemMessage(
                                Component.literal(
                                        "The ancient ritual has faded. "
                                                + "The Temple awaits again."
                                ).withStyle(style -> style
                                        .withColor(ChatFormatting.YELLOW)
                                        .withBold(true))
                        );


                        CrownsOfDust.LOGGER.info(
                                "Temple ritual expired."
                        );
                    }
                }
            }
        }


        // ==================================================
        // TELEPORT QUEUE
        // ==================================================

        if (PENDING_TELEPORTS.isEmpty()) {
            return;
        }


        Iterator<PendingTeleport> iterator =
                PENDING_TELEPORTS.iterator();


        while (iterator.hasNext()) {

            PendingTeleport pending =
                    iterator.next();


            // ==============================================
            // PLAYER VALIDATION
            // ==============================================

            if (pending.player == null
                    || !pending.player.isAlive()) {

                iterator.remove();

                continue;
            }


            // ==============================================
            // COUNTDOWN
            // ==============================================

            pending.ticksRemaining--;


            if (pending.ticksRemaining > 0) {
                continue;
            }


            // ==============================================
            // REMOVE TEMPLE STONES
            //
            // IMPORTANT:
            // This happens AFTER the 5-second cinematic,
            // immediately before teleporting.
            // ==============================================

            clearAllTempleStones(
                    pending.templeLevel,
                    pending.templeOrigin
            );


            // ==============================================
            // TELEPORT
            // ==============================================

            TeleportTransition transition =
                    new TeleportTransition(
                            pending.choiceLevel,

                            new net.minecraft.world.phys.Vec3(
                                    pending.x,
                                    pending.y,
                                    pending.z
                            ),

                            net.minecraft.world.phys.Vec3.ZERO,

                            pending.yaw,
                            pending.pitch,

                            TeleportTransition.DO_NOTHING
                    );


            pending.player.teleport(
                    transition
            );


            // ==============================================
            // REMOVE QUEUED TELEPORT
            // ==============================================

            iterator.remove();
        }
    }
}