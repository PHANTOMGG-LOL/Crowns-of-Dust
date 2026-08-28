package com.phantom.cod.event;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.registry.ModEntities;
import com.phantom.cod.registry.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * ============================================================
 * CHOSEN WEAPON HORROR EVENTS
 * ============================================================
 *
 * HEROBRINE'S LEGACY
 *      diamond_sword
 *           ↓
 *       Herobrine
 *
 * NULL'S SILENCE
 *      netherite_sword
 *           ↓
 *          Null
 *
 *
 * RANDOM EVENTS
 *
 * Whisper
 *      10 seconds
 *
 * Walking
 *      5 seconds
 *
 * Running
 *      5 seconds
 *
 * Entity Encounter
 *      Entity appears 25-45 blocks away
 *      Entity remains for 10 seconds
 *      Looking at entity starts heartbeat
 *      Heartbeat lasts 5 seconds
 *
 *
 * RANDOM WAIT
 *
 * Every event happens after a random:
 *
 *      5 - 15 minutes
 *
 *
 * The weapon does NOT need to be held.
 */
@EventBusSubscriber(
        modid = CrownsOfDust.MOD_ID
)
public final class ChosenWeaponHorrorEvents {

    private ChosenWeaponHorrorEvents() {
    }


    // ============================================================
    // PLAYER CHOICE
    // ============================================================

    /**
     * Same persistent-data key used by the Choice Room.
     */
    private static final String TAG_LAST_WEAPON =
            "cod_last_choice_weapon";


    /**
     * Herobrine's chosen weapon.
     */
    private static final String WEAPON_HEROBRINE =
            "diamond_sword";


    /**
     * Null's chosen weapon.
     */
    private static final String WEAPON_NULL =
            "netherite_sword";


    // ============================================================
    // RANDOM EVENT WAIT
    // ============================================================

    /**
     * Minimum:
     *
     * 5 minutes.
     */
    private static final int MIN_EVENT_INTERVAL =
            3 * 60 * 20;


    /**
     * Maximum:
     *
     * 15 minutes.
     */
    private static final int MAX_EVENT_INTERVAL =
            10 * 60 * 20;


    // ============================================================
    // EVENT DURATIONS
    // ============================================================

    private static final int WHISPER_DURATION =
            10 * 20;


    private static final int WALKING_DURATION =
            5 * 20;


    private static final int RUNNING_DURATION =
            5 * 20;


    private static final int ENTITY_DURATION =
            10 * 20;


    private static final int HEARTBEAT_DURATION =
            5 * 20;


    // ============================================================
    // ENTITY DISTANCE
    // ============================================================

    private static final int MIN_ENTITY_DISTANCE =
            25;


    private static final int MAX_ENTITY_DISTANCE =
            45;


    // ============================================================
    // LOOK DETECTION
    // ============================================================

    /**
     * How close the player's crosshair must be
     * to the entity.
     *
     * Smaller = harder to notice.
     */
    private static final double LOOK_DOT_THRESHOLD =
            0.985D;


    /**
     * Maximum distance for look detection.
     */
    private static final double LOOK_DISTANCE =
            60.0D;


    // ============================================================
    // EVENT TYPES
    // ============================================================

    private enum EventType {

        WHISPER,

        WALKING,

        RUNNING,

        ENTITY
    }


    // ============================================================
    // PLAYER STATES
    // ============================================================

    /**
     * Runtime state.
     *
     * This is NOT stored in the item.
     */
    private static final Map<UUID, HorrorState>
            PLAYER_STATES =
            new HashMap<>();


    // ============================================================
    // SERVER TICK
    // ============================================================

    @SubscribeEvent
    public static void onServerTick(
            ServerTickEvent.Post event
    ) {

        MinecraftServer server =
                event.getServer();


        for (
                ServerPlayer player :
                server.getPlayerList()
                        .getPlayers()
        ) {

            updatePlayer(player);
        }
    }


    // ============================================================
    // UPDATE PLAYER
    // ============================================================

    private static void updatePlayer(
            ServerPlayer player
    ) {

        /*
         * --------------------------------------------------------
         * Read chosen weapon.
         * --------------------------------------------------------
         */
        String chosenWeapon =
                player.getPersistentData()
                        .getStringOr(
                                TAG_LAST_WEAPON,
                                ""
                        );


        /*
         * --------------------------------------------------------
         * Player hasn't selected Herobrine or Null.
         * --------------------------------------------------------
         */
        if (!isSupportedWeapon(chosenWeapon)) {

            HorrorState oldState =
                    PLAYER_STATES.remove(
                            player.getUUID()
                    );


            /*
             * If something somehow remains active,
             * clean it up.
             */
            if (oldState != null) {

                cleanupState(oldState);
            }


            return;
        }


        /*
         * --------------------------------------------------------
         * Get player state.
         * --------------------------------------------------------
         */
        HorrorState state =
                PLAYER_STATES.computeIfAbsent(
                        player.getUUID(),
                        uuid ->
                                createInitialState(player)
                );


        /*
         * --------------------------------------------------------
         * Active event.
         * --------------------------------------------------------
         */
        if (state.activeEvent != null) {

            updateActiveEvent(
                    player,
                    state
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * Waiting for next event.
         * --------------------------------------------------------
         */
        if (state.ticksUntilNextEvent > 0) {

            state.ticksUntilNextEvent--;

            return;
        }


        /*
         * --------------------------------------------------------
         * Start random event.
         * --------------------------------------------------------
         */
        startRandomEvent(
                player,
                state,
                chosenWeapon
        );
    }


    // ============================================================
    // INITIAL STATE
    // ============================================================

    private static HorrorState createInitialState(
            ServerPlayer player
    ) {

        HorrorState state =
                new HorrorState();


        /*
         * First event also waits 5-15 minutes.
         */
        state.ticksUntilNextEvent =
                randomEventInterval(
                        player
                );


        return state;
    }


    // ============================================================
    // CHECK WEAPON
    // ============================================================

    private static boolean isSupportedWeapon(
            String weapon
    ) {

        return WEAPON_HEROBRINE.equals(weapon)
                || WEAPON_NULL.equals(weapon);
    }


    // ============================================================
    // RANDOM EVENT
    // ============================================================

    private static void startRandomEvent(
            ServerPlayer player,
            HorrorState state,
            String weapon
    ) {

        EventType eventType =
                chooseRandomEvent(player);


        state.activeEvent =
                eventType;


        switch (eventType) {

            case WHISPER -> {

                state.ticksRemaining =
                        WHISPER_DURATION;

                playWhisper(player);
            }


            case WALKING -> {

                state.ticksRemaining =
                        WALKING_DURATION;

                playWalking(player);
            }


            case RUNNING -> {

                state.ticksRemaining =
                        RUNNING_DURATION;

                playRunning(player);
            }


            case ENTITY -> {

                startEntityEncounter(
                        player,
                        state,
                        weapon
                );
            }
        }
    }


    // ============================================================
    // RANDOM EVENT TYPE
    // ============================================================

    private static EventType chooseRandomEvent(
            ServerPlayer player
    ) {

        int roll =
                player.level()
                        .getRandom()
                        .nextInt(100);


        /*
         * 40%
         *
         * Whisper
         */
        if (roll < 40) {

            return EventType.WHISPER;
        }


        /*
         * 25%
         *
         * Walking
         */
        if (roll < 65) {

            return EventType.WALKING;
        }


        /*
         * 20%
         *
         * Running
         */
        if (roll < 85) {

            return EventType.RUNNING;
        }


        /*
         * 15%
         *
         * Entity
         */
        return EventType.ENTITY;
    }


    // ============================================================
    // UPDATE ACTIVE EVENT
    // ============================================================

    private static void updateActiveEvent(
            ServerPlayer player,
            HorrorState state
    ) {

        /*
         * Entity encounter has its own logic.
         */
        if (
                state.activeEvent
                        == EventType.ENTITY
        ) {

            updateEntityEncounter(
                    player,
                    state
            );

            return;
        }


        /*
         * Normal sound event.
         */
        state.ticksRemaining--;


        if (state.ticksRemaining <= 0) {

            endEvent(
                    player,
                    state
            );
        }
    }


    // ============================================================
    // WHISPER
    // ============================================================

    private static void playWhisper(
            ServerPlayer player
    ) {

        playSoundToPlayer(
                player,
                ModSounds.WHISPER.get(),
                0.8F,
                0.9F
                        + player.level()
                        .getRandom()
                        .nextFloat()
                        * 0.2F
        );
    }


    // ============================================================
    // WALKING
    // ============================================================

    private static void playWalking(
            ServerPlayer player
    ) {

        playSoundToPlayer(
                player,
                ModSounds.ENCOUNTER_WALKING.get(),
                0.75F,
                0.9F
                        + player.level()
                        .getRandom()
                        .nextFloat()
                        * 0.2F
        );
    }


    // ============================================================
    // RUNNING
    // ============================================================

    private static void playRunning(
            ServerPlayer player
    ) {

        playSoundToPlayer(
                player,
                ModSounds.ENCOUNTER_RUNNING.get(),
                0.85F,
                0.9F
                        + player.level()
                        .getRandom()
                        .nextFloat()
                        * 0.2F
        );
    }


    // ============================================================
    // PLAY SOUND TO ONE PLAYER
    // ============================================================

    private static void playSoundToPlayer(
            ServerPlayer player,
            net.minecraft.sounds.SoundEvent sound,
            float volume,
            float pitch
    ) {

        /*
         * NeoForge 26.2 uses a Holder<SoundEvent>
         * in ClientboundSoundPacket.
         */
        var holder =
                BuiltInRegistries.SOUND_EVENT
                        .wrapAsHolder(sound);


        ClientboundSoundPacket packet =
                new ClientboundSoundPacket(
                        holder,
                        SoundSource.AMBIENT,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        volume,
                        pitch,
                        player.level()
                                .getRandom()
                                .nextLong()
                );


        /*
         * Send ONLY to this player.
         */
        player.connection.send(
                packet
        );
    }


    // ============================================================
    // ENTITY ENCOUNTER
    // ============================================================

    private static void startEntityEncounter(
            ServerPlayer player,
            HorrorState state,
            String weapon
    ) {

        if (
                !(player.level()
                        instanceof ServerLevel level)
        ) {

            endEvent(
                    player,
                    state
            );

            return;
        }


        /*
         * Find distant position.
         */
        BlockPos spawnPos =
                findEncounterPosition(
                        level,
                        player
                );


        if (spawnPos == null) {

            endEvent(
                    player,
                    state
            );

            return;
        }


        /*
         * Create correct entity.
         */
        Entity entity =
                createEncounterEntity(
                        level,
                        spawnPos,
                        weapon
                );


        if (entity == null) {

            endEvent(
                    player,
                    state
            );

            return;
        }


        /*
         * Face the player.
         */
        facePlayer(
                entity,
                player
        );


        /*
         * Spawn.
         */
        level.addFreshEntity(
                entity
        );


        /*
         * Save state.
         */
        state.entity =
                entity;


        state.ticksRemaining =
                ENTITY_DURATION;


        state.heartbeatStarted =
                false;


        state.heartbeatTicksRemaining =
                0;
    }


    // ============================================================
    // CREATE ENTITY
    // ============================================================

    private static Entity createEncounterEntity(
            ServerLevel level,
            BlockPos position,
            String weapon
    ) {

        Entity entity;


        /*
         * --------------------------------------------------------
         * HEROBRINE
         * --------------------------------------------------------
         */
        if (
                WEAPON_HEROBRINE.equals(
                        weapon
                )
        ) {

            entity =
                    ModEntities.HEROBRINE
                            .get()
                            .create(
                                    level,
                                    EntitySpawnReason.TRIGGERED
                            );
        }


        /*
         * --------------------------------------------------------
         * NULL
         * --------------------------------------------------------
         */
        else if (
                WEAPON_NULL.equals(
                        weapon
                )
        ) {

            entity =
                    ModEntities.NULL
                            .get()
                            .create(
                                    level,
                                    EntitySpawnReason.TRIGGERED
                            );
        }


        else {

            return null;
        }


        if (entity == null) {

            return null;
        }


        /*
         * Position.
         */
        entity.setPos(
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D
        );


        /*
         * Don't let the encounter entity
         * wander away.
         */
        entity.setNoGravity(true);


        return entity;
    }


    // ============================================================
    // FIND ENTITY POSITION
    // ============================================================

    private static BlockPos findEncounterPosition(
            ServerLevel level,
            ServerPlayer player
    ) {

        for (
                int attempt = 0;
                attempt < 12;
                attempt++
        ) {

            int distance =
                    MIN_ENTITY_DISTANCE
                            + level.getRandom().nextInt(
                            MAX_ENTITY_DISTANCE
                                    - MIN_ENTITY_DISTANCE
                                    + 1
                    );

            double angle =
                    level.getRandom().nextDouble()
                            * Math.PI
                            * 2.0D;

            int x =
                    Mth.floor(
                            player.getX()
                                    + Math.cos(angle)
                                    * distance
                    );

            int z =
                    Mth.floor(
                            player.getZ()
                                    + Math.sin(angle)
                                    * distance
                    );

            /*
             * Start around the player's current height.
             */
            int startY =
                    player.getBlockY() + 8;

            /*
             * Search downward for the first solid block.
             */
            for (
                    int y = startY;
                    y >= player.getBlockY() - 12;
                    y--
            ) {

                BlockPos groundPos =
                        new BlockPos(
                                x,
                                y,
                                z
                        );

                BlockPos entityPos =
                        groundPos.above();

                /*
                 * Ground must be solid.
                 */
                if (
                        !level.getBlockState(
                                groundPos
                        ).isSolid()
                ) {
                    continue;
                }

                /*
                 * Entity's spawn position must be empty.
                 */
                if (
                        !level.getBlockState(
                                entityPos
                        ).isAir()
                ) {
                    continue;
                }

                /*
                 * Make sure the chunk is loaded.
                 */
                if (
                        !level.hasChunk(
                                x >> 4,
                                z >> 4
                        )
                ) {
                    continue;
                }

                return entityPos;
            }
        }

        return null;
    }


    // ============================================================
    // FACE PLAYER
    // ============================================================

    private static void facePlayer(
            Entity entity,
            ServerPlayer player
    ) {

        double dx =
                player.getX()
                        - entity.getX();


        double dz =
                player.getZ()
                        - entity.getZ();


        float yaw =
                (float)
                        (
                                Math.toDegrees(
                                        Math.atan2(
                                                dz,
                                                dx
                                        )
                                )
                                        - 90.0D
                        );


        entity.setYRot(
                yaw
        );


        entity.setYHeadRot(
                yaw
        );


        entity.setYBodyRot(
                yaw
        );
    }


    // ============================================================
    // UPDATE ENTITY
    // ============================================================

    private static void updateEntityEncounter(
            ServerPlayer player,
            HorrorState state
    ) {

        Entity entity =
                state.entity;


        /*
         * Entity disappeared.
         */
        if (
                entity == null
                        || entity.isRemoved()
        ) {

            endEvent(
                    player,
                    state
            );

            return;
        }


        // ========================================================
        // LOOKING DETECTION
        // ========================================================

        if (
                !state.heartbeatStarted
                        && entity
                        instanceof LivingEntity livingEntity
        ) {

            if (
                    isPlayerLookingAtEntity(
                            player,
                            livingEntity
                    )
            ) {

                state.heartbeatStarted =
                        true;


                state.heartbeatTicksRemaining =
                        HEARTBEAT_DURATION;


                playHeartbeat(
                        player
                );
            }
        }


        // ========================================================
        // HEARTBEAT TIMER
        // ========================================================

        if (state.heartbeatStarted) {

            state.heartbeatTicksRemaining--;


            if (
                    state.heartbeatTicksRemaining
                            <= 0
            ) {

                state.heartbeatStarted =
                        false;
            }
        }


        // ========================================================
        // ENTITY TIMER
        // ========================================================

        state.ticksRemaining--;


        if (state.ticksRemaining <= 0) {

            entity.discard();


            endEvent(
                    player,
                    state
            );
        }
    }


    // ============================================================
    // LOOK AT ENTITY
    // ============================================================

    private static boolean isPlayerLookingAtEntity(
            ServerPlayer player,
            LivingEntity entity
    ) {

        /*
         * --------------------------------------------------------
         * Distance check.
         * --------------------------------------------------------
         */
        double distanceSqr =
                player.distanceToSqr(
                        entity
                );


        if (
                distanceSqr
                        >
                        LOOK_DISTANCE
                                * LOOK_DISTANCE
        ) {

            return false;
        }


        /*
         * --------------------------------------------------------
         * Player eye position.
         * --------------------------------------------------------
         */
        Vec3 eyePosition =
                player.getEyePosition(
                        1.0F
                );


        /*
         * --------------------------------------------------------
         * Entity center.
         * --------------------------------------------------------
         */
        AABB box =
                entity.getBoundingBox();


        Vec3 target =
                new Vec3(
                        (box.minX + box.maxX)
                                / 2.0D,

                        (box.minY + box.maxY)
                                / 2.0D,

                        (box.minZ + box.maxZ)
                                / 2.0D
                );


        /*
         * --------------------------------------------------------
         * Direction from player to entity.
         * --------------------------------------------------------
         */
        Vec3 toEntity =
                target.subtract(
                        eyePosition
                );


        if (
                toEntity.lengthSqr()
                        < 0.0001D
        ) {

            return true;
        }


        Vec3 direction =
                toEntity.normalize();


        /*
         * --------------------------------------------------------
         * Player's current view direction.
         * --------------------------------------------------------
         */
        Vec3 look =
                player.getViewVector(
                        1.0F
                ).normalize();


        /*
         * --------------------------------------------------------
         * Dot product.
         *
         * 1.0 = looking directly at entity
         * 0.0 = 90 degrees away
         *
         * 0.985 gives us a fairly tight
         * "you're actually looking at it" check.
         * --------------------------------------------------------
         */
        double dot =
                look.dot(
                        direction
                );


        return dot >= LOOK_DOT_THRESHOLD;
    }


    // ============================================================
    // HEARTBEAT
    // ============================================================

    private static void playHeartbeat(
            ServerPlayer player
    ) {

        playSoundToPlayer(
                player,
                ModSounds.HEART_BEAT.get(),
                1.0F,
                1.0F
        );
    }


    // ============================================================
    // END EVENT
    // ============================================================

    private static void endEvent(
            ServerPlayer player,
            HorrorState state
    ) {

        /*
         * Remove entity.
         */
        if (
                state.entity != null
                        && !state.entity.isRemoved()
        ) {

            state.entity.discard();
        }


        /*
         * Clear current event.
         */
        state.entity =
                null;


        state.activeEvent =
                null;


        state.ticksRemaining =
                0;


        state.heartbeatStarted =
                false;


        state.heartbeatTicksRemaining =
                0;


        /*
         * --------------------------------------------------------
         * NEXT EVENT:
         *
         * RANDOM 5-15 MINUTES.
         * --------------------------------------------------------
         */
        state.ticksUntilNextEvent =
                randomEventInterval(
                        player
                );
    }


    // ============================================================
    // CLEANUP
    // ============================================================

    private static void cleanupState(
            HorrorState state
    ) {

        if (
                state.entity != null
                        && !state.entity.isRemoved()
        ) {

            state.entity.discard();
        }


        state.entity =
                null;


        state.activeEvent =
                null;
    }


    // ============================================================
    // RANDOM 5-15 MINUTES
    // ============================================================

    private static int randomEventInterval(
            ServerPlayer player
    ) {

        return MIN_EVENT_INTERVAL
                + player.level()
                .getRandom()
                .nextInt(
                        MAX_EVENT_INTERVAL
                                - MIN_EVENT_INTERVAL
                                + 1
                );
    }


    // ============================================================
    // HORROR STATE
    // ============================================================

    private static final class HorrorState {

        EventType activeEvent;

        int ticksUntilNextEvent;

        int ticksRemaining;

        Entity entity;

        boolean heartbeatStarted;

        int heartbeatTicksRemaining;
    }
}