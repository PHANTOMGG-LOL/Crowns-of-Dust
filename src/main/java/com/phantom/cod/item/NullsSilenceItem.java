package com.phantom.cod.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.damagesource.DamageSource;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


/**
 * ============================================================
 * NULL'S SILENCE
 * ============================================================
 *
 * PASSIVE:
 * Speed I while Null's Silence exists anywhere in inventory.
 *
 * RIGHT CLICK:
 * Silence.
 *
 * SILENCE:
 * - Full player invisibility
 * - Armor disappears
 * - Held item disappears
 * - Speed III
 * - Strength I
 * - Soul Fire Flame trail
 *
 * ON HIT:
 * - Heal 50% of actual health damage dealt.
 *
 * ON KILL:
 * - Creates a radius-3 corrupted area.
 * - Black Concrete + Crying Obsidian.
 *
 * SHIFT + RIGHT CLICK:
 * Blackout.
 *
 * BLACKOUT:
 * - 20 block effect radius
 * - Other players get Blindness + Nausea
 * - User is completely immune
 * - Large destructive explosion
 * - Circular crater
 * - Corrupted Null ground around crater
 *
 * SILENCE:
 * Duration: 30 seconds
 * Cooldown: 90 seconds
 *
 * BLACKOUT:
 * Cooldown: 5 minutes
 *
 * IMPORTANT:
 * Ability timers belong to the PLAYER,
 * not the ItemStack.
 */
@EventBusSubscriber(modid = "cod")
public class NullsSilenceItem extends Item {

    // ============================================================
    // PASSIVE
    // ============================================================

    private static final int PASSIVE_DURATION = 80;


    // ============================================================
    // SILENCE SETTINGS
    // ============================================================

    public static final long SILENCE_DURATION =
            20L * 30L;


    public static final long SILENCE_COOLDOWN =
            20L * 90L;


    // ============================================================
    // NULL INVISIBILITY MARKER
    // ============================================================

    public static final int SILENCE_INVISIBILITY_AMPLIFIER = 1;


    // ============================================================
    // DEATH AREA SETTINGS
    // ============================================================

    /**
     * Radius of the Null death area.
     */
    private static final int DEATH_AREA_RADIUS = 3;


    /**
     * 25% Crying Obsidian.
     *
     * 75% Black Concrete.
     */
    private static final float CRYING_OBSIDIAN_CHANCE = 0.25F;


    // ============================================================
    // BLACKOUT SETTINGS
    // ============================================================

    /**
     * Players within 20 blocks are affected.
     *
     * The caster is excluded.
     */
    private static final double BLACKOUT_EFFECT_RADIUS = 20.0D;


    /**
     * Explosion power.
     *
     * 9.0F creates a very large destructive explosion.
     */
    private static final float BLACKOUT_EXPLOSION_RADIUS = 9.0F;


    /**
     * Radius of the circular crater.
     */
    private static final int BLACKOUT_CRATER_RADIUS = 10;


    /**
     * Depth of the circular crater.
     */
    private static final int BLACKOUT_CRATER_DEPTH = 7;


    /**
     * Radius of the corrupted ground surrounding
     * the crater.
     */
    private static final int BLACKOUT_GROUND_RADIUS = 14;


    /**
     * Blackout cooldown:
     *
     * 5 minutes.
     */
    private static final long BLACKOUT_COOLDOWN =
            20L * 60L * 5;


    // ============================================================
    // DYNAMIC PLAYER DATA
    // ============================================================

    /**
     * Silence active until this game time.
     */
    private static final String TAG_SILENCE_END =
            "null_silence_end";


    /**
     * Silence cooldown.
     */
    private static final String TAG_SILENCE_CD =
            "null_silence_cd";


    /**
     * Blackout cooldown.
     */
    private static final String TAG_BLACKOUT_CD =
            "null_blackout_cd";


    /**
     * Dynamic player timers.
     *
     * Nothing is stored inside the ItemStack.
     */
    private static final Map<String, Long> LONG_DATA =
            new HashMap<>();


    // ============================================================
    // DYNAMIC DATA HELPERS
    // ============================================================

    private static String makeKey(
            Player player,
            String key
    ) {

        return player.getUUID()
                + "_"
                + key;
    }


    public static long getPlayerLong(
            Player player,
            String key
    ) {

        return LONG_DATA.getOrDefault(
                makeKey(player, key),
                0L
        );
    }


    private static void setPlayerLong(
            Player player,
            String key,
            long value
    ) {

        LONG_DATA.put(
                makeKey(player, key),
                value
        );
    }


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public NullsSilenceItem(
            Properties properties
    ) {

        super(properties);
    }


    // ============================================================
    // RIGHT CLICK
    // ============================================================

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        // ========================================================
        // SHIFT + RIGHT CLICK
        // ========================================================
        //
        // Blackout has its own independent cooldown.
        // ========================================================

        if (player.isCrouching()) {

            return activateBlackout(
                    level,
                    player
            );
        }


        // ========================================================
        // NORMAL RIGHT CLICK
        // ========================================================
        //
        // Silence.
        // ========================================================

        long currentTime =
                level.getGameTime();


        // ========================================================
        // CHECK SILENCE COOLDOWN
        // ========================================================

        long cooldownEnd =
                getPlayerLong(
                        player,
                        TAG_SILENCE_CD
                );


        if (currentTime < cooldownEnd) {

            return InteractionResult.CONSUME;
        }


        // ========================================================
        // CLIENT
        // ========================================================

        if (level.isClientSide()) {

            return InteractionResult.SUCCESS;
        }


        // ========================================================
        // SET SILENCE END
        // ========================================================

        setPlayerLong(
                player,
                TAG_SILENCE_END,
                currentTime + SILENCE_DURATION
        );


        // ========================================================
        // SET SILENCE COOLDOWN
        // ========================================================

        setPlayerLong(
                player,
                TAG_SILENCE_CD,
                currentTime + SILENCE_COOLDOWN
        );


        // ========================================================
        // NULL INVISIBILITY
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.INVISIBILITY,
                20,
                SILENCE_INVISIBILITY_AMPLIFIER
        );


        // ========================================================
        // SPEED III
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.SPEED,
                20,
                2
        );


        // ========================================================
        // STRENGTH I
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.STRENGTH,
                20,
                0
        );


        return InteractionResult.SUCCESS;
    }


    // ============================================================
    // BLACKOUT - ACTIVATION
    // ============================================================

    /**
     * Shift + Right Click.
     *
     * Blackout is completely independent from Silence.
     *
     * The caster:
     * - Does NOT get Blindness.
     * - Does NOT get Nausea.
     * - Does NOT take explosion damage.
     * - Does NOT get explosion knockback.
     */
    private static InteractionResult activateBlackout(
            Level level,
            Player player
    ) {

        long currentTime =
                level.getGameTime();


        // ========================================================
        // CHECK BLACKOUT COOLDOWN
        // ========================================================

        long cooldownEnd =
                getPlayerLong(
                        player,
                        TAG_BLACKOUT_CD
                );


        if (currentTime < cooldownEnd) {

            return InteractionResult.CONSUME;
        }


        // ========================================================
        // CLIENT
        // ========================================================

        if (level.isClientSide()) {

            return InteractionResult.SUCCESS;
        }


        // ========================================================
        // SERVER PLAYER
        // ========================================================

        if (!(player instanceof ServerPlayer serverPlayer)) {

            return InteractionResult.CONSUME;
        }


        ServerLevel serverLevel =
                serverPlayer.level();


        // ========================================================
        // START BLACKOUT COOLDOWN
        // ========================================================

        setPlayerLong(
                player,
                TAG_BLACKOUT_CD,
                currentTime + BLACKOUT_COOLDOWN
        );


        // ========================================================
        // APPLY EFFECTS TO OTHER PLAYERS
        // ========================================================

        applyBlackoutEffects(
                serverPlayer
        );


        // ========================================================
        // PROTECT CASTER
        // ========================================================
        //
        // The caster must survive their own Blackout.
        //
        // Invulnerability prevents explosion damage.
        // We also restore movement afterward so the blast does
        // not throw Null across the crater.
        // ========================================================

        boolean wasInvulnerable =
                serverPlayer.isInvulnerable();


        serverPlayer.setInvulnerable(
                true
        );


        // ========================================================
        // CREATE MASSIVE EXPLOSION
        // ========================================================

        serverLevel.explode(
                serverPlayer,

                serverPlayer.getX(),
                serverPlayer.getY(),
                serverPlayer.getZ(),

                BLACKOUT_EXPLOSION_RADIUS,

                false,

                Level.ExplosionInteraction.BLOCK
        );


        // ========================================================
        // RESTORE INVULNERABILITY
        // ========================================================

        serverPlayer.setInvulnerable(
                wasInvulnerable
        );


        // ========================================================
        // STOP CASTER KNOCKBACK
        // ========================================================

        serverPlayer.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
        );


        serverPlayer.hurtMarked =
                true;


        // ========================================================
        // CREATE CIRCULAR CRATER
        // ========================================================

        createBlackoutCrater(
                serverLevel,
                serverPlayer.blockPosition()
        );


        // ========================================================
        // CREATE CORRUPTED GROUND
        // ========================================================

        createBlackoutGround(
                serverLevel,
                serverPlayer.blockPosition()
        );


        // ========================================================
        // BLACKOUT PARTICLES
        // ========================================================

        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,

                serverPlayer.getX(),
                serverPlayer.getY() + 0.5D,
                serverPlayer.getZ(),

                100,

                5.0D,
                2.0D,
                5.0D,

                0.08D
        );


        return InteractionResult.SUCCESS;
    }


    // ============================================================
    // BLACKOUT EFFECTS
    // ============================================================

    /**
     * Applies Blackout effects ONLY to other players.
     *
     * The caster is explicitly skipped.
     */
    private static void applyBlackoutEffects(
            ServerPlayer source
    ) {

        ServerLevel level =
                source.level();


        AABB area =
                source.getBoundingBox()
                        .inflate(
                                BLACKOUT_EFFECT_RADIUS
                        );


        for (
                ServerPlayer target :
                level.getEntitiesOfClass(
                        ServerPlayer.class,
                        area
                )
        ) {

            // ====================================================
            // NEVER AFFECT THE CASTER
            // ====================================================

            if (target == source) {

                continue;
            }


            // ====================================================
            // IGNORE SPECTATORS
            // ====================================================

            if (target.isSpectator()) {

                continue;
            }


            // ====================================================
            // BLINDNESS
            // ====================================================

            applyEffectWithPriority(
                    target,
                    MobEffects.BLINDNESS,
                    20 * 8,
                    0
            );


            // ====================================================
            // NAUSEA
            // ====================================================

            applyEffectWithPriority(
                    target,
                    MobEffects.NAUSEA,
                    20 * 8,
                    0
            );
        }
    }


    // ============================================================
    // CIRCULAR BLACKOUT CRATER
    // ============================================================

    /**
     * Creates a deliberate circular bowl-shaped crater.
     *
     * Surface:
     * radius 10
     *
     * Deeper layers become smaller:
     *
     * Depth 0 -> radius 10
     * Depth 1 -> radius 9
     * Depth 2 -> radius 8
     * Depth 3 -> radius 7
     * Depth 4 -> radius 6
     * Depth 5 -> radius 5
     * Depth 6 -> radius 4
     *
     * This creates a circular crater rather than relying
     * entirely on vanilla explosion randomness.
     */
    private static void createBlackoutCrater(
            ServerLevel level,
            BlockPos center
    ) {

        // ========================================================
        // BUILD THE CIRCULAR CRATER
        // ========================================================

        for (
                int x = -BLACKOUT_CRATER_RADIUS;
                x <= BLACKOUT_CRATER_RADIUS;
                x++
        ) {

            for (
                    int z = -BLACKOUT_CRATER_RADIUS;
                    z <= BLACKOUT_CRATER_RADIUS;
                    z++
            ) {

                // ------------------------------------------------
                // Keep the crater perfectly circular.
                // ------------------------------------------------

                double distance =
                        Math.sqrt(
                                x * x +
                                        z * z
                        );


                if (
                        distance >
                                BLACKOUT_CRATER_RADIUS
                ) {

                    continue;
                }


                // ------------------------------------------------
                // Calculate how deep this part of the crater is.
                //
                // Center:
                //      deepest
                //
                // Edge:
                //      shallowest
                // ------------------------------------------------

                double normalizedDistance =
                        distance /
                                BLACKOUT_CRATER_RADIUS;


                double depthFactor =
                        1.0D -
                                normalizedDistance;


                int depth =
                        (int) Math.round(
                                BLACKOUT_CRATER_DEPTH *
                                        depthFactor
                        );


                // ------------------------------------------------
                // Keep the outer edge at least one block deep.
                // ------------------------------------------------

                if (depth < 1) {

                    depth = 1;
                }


                int floorY =
                        center.getY()
                                -
                                depth;


                // =================================================
                // CARVE THE CRATER
                // =================================================

                for (
                        int y =
                        center.getY() + 3;

                        y > floorY;

                        y--
                ) {

                    BlockPos pos =
                            new BlockPos(
                                    center.getX() + x,
                                    y,
                                    center.getZ() + z
                            );


                    // ------------------------------------------------
                    // Bedrock is protected.
                    // ------------------------------------------------

                    if (
                            level.getBlockState(pos)
                                    .is(Blocks.BEDROCK)
                    ) {

                        continue;
                    }


                    // ------------------------------------------------
                    // Remove the terrain from the crater.
                    // ------------------------------------------------

                    level.setBlock(
                            pos,
                            Blocks.AIR.defaultBlockState(),
                            3
                    );
                }


                // =================================================
                // CORRUPTED CRATER FLOOR
                // =================================================

                BlockPos floorPos =
                        new BlockPos(
                                center.getX() + x,
                                floorY,
                                center.getZ() + z
                        );


                if (
                        !level.getBlockState(floorPos)
                                .is(Blocks.BEDROCK)
                ) {

                    setNullCorruptionBlock(
                            level,
                            floorPos
                    );
                }


                // =================================================
                // CORRUPTED CRATER UNDER-FLOOR
                // =================================================
                //
                // This gives the crater a darker, corrupted
                // appearance instead of leaving ordinary stone
                // immediately underneath the Null floor.
                // =================================================

                BlockPos underFloorPos =
                        floorPos.below();


                if (
                        !level.getBlockState(underFloorPos)
                                .is(Blocks.BEDROCK)
                ) {

                    setNullCorruptionBlock(
                            level,
                            underFloorPos
                    );
                }
            }
        }
    }


    // ============================================================
    // NULL CORRUPTION BLOCK
    // ============================================================

    /**
     * Places one Null corruption block.
     *
     * 25% Crying Obsidian
     * 75% Black Concrete
     */
    private static void setNullCorruptionBlock(
            ServerLevel level,
            BlockPos pos
    ) {

        // --------------------------------------------------------
        // Never replace bedrock.
        // --------------------------------------------------------

        if (
                level.getBlockState(pos)
                        .is(Blocks.BEDROCK)
        ) {

            return;
        }


        // --------------------------------------------------------
        // Crying Obsidian.
        // --------------------------------------------------------

        if (
                level.getRandom().nextFloat()
                        <
                        CRYING_OBSIDIAN_CHANCE
        ) {

            level.setBlock(
                    pos,
                    Blocks.CRYING_OBSIDIAN
                            .defaultBlockState(),
                    3
            );

        } else {

            // ----------------------------------------------------
            // Black Concrete.
            // ----------------------------------------------------

            level.setBlock(
                    pos,
                    Blocks.CONCRETE.black()
                            .defaultBlockState(),
                    3
            );
        }
    }


    // ============================================================
    // BLACKOUT GROUND
    // ============================================================

    /**
     * Creates a corrupted Null ground ring around the crater.
     *
     * Crater:
     * 0-10 blocks
     *
     * Corrupted ground:
     * 10-14 blocks
     *
     * Mix:
     * 25% Crying Obsidian
     * 75% Black Concrete
     */
    private static void createBlackoutGround(
            ServerLevel level,
            BlockPos center
    ) {

        for (
                int x = -BLACKOUT_GROUND_RADIUS;
                x <= BLACKOUT_GROUND_RADIUS;
                x++
        ) {

            for (
                    int z = -BLACKOUT_GROUND_RADIUS;
                    z <= BLACKOUT_GROUND_RADIUS;
                    z++
            ) {

                int distanceSquared =
                        x * x +
                                z * z;


                // =================================================
                // OUTSIDE BLACKOUT GROUND
                // =================================================

                if (
                        distanceSquared
                                >
                                BLACKOUT_GROUND_RADIUS *
                                        BLACKOUT_GROUND_RADIUS
                ) {

                    continue;
                }


                // =================================================
                // INSIDE CRATER
                // =================================================
                //
                // Do not fill the hole.
                // =================================================

                if (
                        distanceSquared
                                <=
                                BLACKOUT_CRATER_RADIUS *
                                        BLACKOUT_CRATER_RADIUS
                ) {

                    continue;
                }


                int worldX =
                        center.getX() + x;


                int worldZ =
                        center.getZ() + z;


                // =================================================
                // FIND THE GROUND
                // =================================================

                int startY =
                        center.getY() + 2;


                for (
                        int y = startY;
                        y >= center.getY() - 8;
                        y--
                ) {

                    BlockPos groundPos =
                            new BlockPos(
                                    worldX,
                                    y,
                                    worldZ
                            );


                    BlockPos abovePos =
                            groundPos.above();


                    // ------------------------------------------------
                    // Ground must be solid.
                    // ------------------------------------------------

                    if (
                            !level.getBlockState(
                                    groundPos
                            ).isSolidRender()
                    ) {

                        continue;
                    }


                    // ------------------------------------------------
                    // Space above must be open.
                    // ------------------------------------------------

                    if (
                            !level.getBlockState(
                                    abovePos
                            ).isAir()
                    ) {

                        continue;
                    }


                    // =================================================
                    // NULL CORRUPTION
                    // =================================================

                    setNullCorruptionBlock(
                            level,
                            groundPos
                    );


                    // ------------------------------------------------
                    // Ground found.
                    // ------------------------------------------------

                    break;
                }
            }
        }
    }


    // ============================================================
    // ACTIVE SILENCE TICK
    // ============================================================

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {

            return;
        }


        ServerLevel level =
                player.level();


        long currentTime =
                level.getGameTime();


        long silenceEnd =
                getPlayerLong(
                        player,
                        TAG_SILENCE_END
                );


        // --------------------------------------------------------
        // Silence isn't active.
        // --------------------------------------------------------

        if (currentTime >= silenceEnd) {

            return;
        }


        // ========================================================
        // NULL INVISIBILITY
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.INVISIBILITY,
                10,
                SILENCE_INVISIBILITY_AMPLIFIER
        );


        // ========================================================
        // SPEED III
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.SPEED,
                10,
                2
        );


        // ========================================================
        // STRENGTH I
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.STRENGTH,
                10,
                0
        );


        // ========================================================
        // SOUL FIRE TRAIL
        // ========================================================

        if (player.tickCount % 2 == 0) {

            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,

                    player.getX(),
                    player.getY() + 0.35D,
                    player.getZ(),

                    3,

                    0.22D,
                    0.35D,
                    0.22D,

                    0.015D
            );
        }
    }


    // ============================================================
    // LIFE STEAL
    // ============================================================

    @SubscribeEvent
    public static void onLivingDamage(
            LivingDamageEvent.Post event
    ) {

        // --------------------------------------------------------
        // Server side only.
        // --------------------------------------------------------

        if (!(event.getEntity().level()
                instanceof ServerLevel)) {

            return;
        }


        DamageSource source =
                event.getSource();


        // --------------------------------------------------------
        // Must have been caused by a player.
        // --------------------------------------------------------

        if (!(source.getEntity()
                instanceof ServerPlayer attacker)) {

            return;
        }


        // --------------------------------------------------------
        // Null's Silence must be the weapon currently used.
        // --------------------------------------------------------

        ItemStack weapon =
                attacker.getMainHandItem();


        if (!(weapon.getItem()
                instanceof NullsSilenceItem)) {

            return;
        }


        // --------------------------------------------------------
        // Actual health damage after mitigation.
        // --------------------------------------------------------

        float healthDamage =
                event.getHealthDamage();


        if (healthDamage <= 0.0F) {

            return;
        }


        // --------------------------------------------------------
        // Heal 50%.
        // --------------------------------------------------------

        attacker.heal(
                healthDamage * 0.5F
        );
    }


    // ============================================================
    // ON KILL
    // ============================================================

    @SubscribeEvent
    public static void onLivingDeath(
            LivingDeathEvent event
    ) {

        LivingEntity target =
                event.getEntity();


        // --------------------------------------------------------
        // Server side only.
        // --------------------------------------------------------

        if (!(target.level()
                instanceof ServerLevel level)) {

            return;
        }


        DamageSource source =
                event.getSource();


        // --------------------------------------------------------
        // Must be killed by a player.
        // --------------------------------------------------------

        if (!(source.getEntity()
                instanceof ServerPlayer attacker)) {

            return;
        }


        // --------------------------------------------------------
        // Null's Silence must be the weapon that killed it.
        // --------------------------------------------------------

        ItemStack weapon =
                attacker.getMainHandItem();


        if (!(weapon.getItem()
                instanceof NullsSilenceItem)) {

            return;
        }


        // --------------------------------------------------------
        // Create Null death ground.
        // --------------------------------------------------------

        createNullDeathGround(
                level,
                target.blockPosition()
        );
    }


    // ============================================================
    // NULL DEATH GROUND
    // ============================================================

    private static void createNullDeathGround(
            ServerLevel level,
            BlockPos deathPos
    ) {

        for (
                int x = -DEATH_AREA_RADIUS;
                x <= DEATH_AREA_RADIUS;
                x++
        ) {

            for (
                    int z = -DEATH_AREA_RADIUS;
                    z <= DEATH_AREA_RADIUS;
                    z++
            ) {

                // ------------------------------------------------
                // Circular radius.
                // ------------------------------------------------

                if (
                        x * x +
                                z * z
                                >
                                DEATH_AREA_RADIUS *
                                        DEATH_AREA_RADIUS
                ) {

                    continue;
                }


                int worldX =
                        deathPos.getX() + x;


                int worldZ =
                        deathPos.getZ() + z;


                int startY =
                        deathPos.getY() + 2;


                for (
                        int y = startY;
                        y >= deathPos.getY() - 8;
                        y--
                ) {

                    BlockPos groundPos =
                            new BlockPos(
                                    worldX,
                                    y,
                                    worldZ
                            );


                    BlockPos abovePos =
                            groundPos.above();


                    // ------------------------------------------------
                    // Ground must be solid.
                    // ------------------------------------------------

                    if (
                            !level.getBlockState(
                                    groundPos
                            ).isSolidRender()
                    ) {

                        continue;
                    }


                    // ------------------------------------------------
                    // Space above must be open.
                    // ------------------------------------------------

                    if (
                            !level.getBlockState(
                                    abovePos
                            ).isAir()
                    ) {

                        continue;
                    }


                    // =================================================
                    // NULL BLOCK MIX
                    // =================================================

                    if (
                            level.getRandom().nextFloat()
                                    <
                                    CRYING_OBSIDIAN_CHANCE
                    ) {

                        level.setBlock(
                                groundPos,
                                Blocks.CRYING_OBSIDIAN
                                        .defaultBlockState(),
                                3
                        );

                    } else {

                        level.setBlock(
                                groundPos,
                                Blocks.CONCRETE.black()
                                        .defaultBlockState(),
                                3
                        );
                    }


                    break;
                }
            }
        }
    }


    // ============================================================
    // INVENTORY PASSIVE
    // ============================================================

    @Override
    public void inventoryTick(
            ItemStack stack,
            ServerLevel level,
            Entity entity,
            net.minecraft.world.entity.EquipmentSlot slot
    ) {

        if (!(entity instanceof Player player)) {

            return;
        }


        if (!hasWeaponInInventory(player)) {

            return;
        }


        // Speed I.
        applyEffectWithPriority(
                player,
                MobEffects.SPEED,
                PASSIVE_DURATION,
                0
        );
    }


    // ============================================================
    // INVENTORY CHECK
    // ============================================================

    private boolean hasWeaponInInventory(
            Player player
    ) {

        for (
                ItemStack stack :
                player.getInventory()
                        .getNonEquipmentItems()
        ) {

            if (stack.is(this)) {

                return true;
            }
        }


        return false;
    }


    // ============================================================
    // PRIORITY EFFECT
    // ============================================================

    public static void applyEffectWithPriority(
            Player player,
            Holder<MobEffect> effect,
            int duration,
            int amplifier
    ) {

        MobEffectInstance current =
                player.getEffect(effect);


        // --------------------------------------------------------
        // No existing effect.
        // --------------------------------------------------------

        if (current == null) {

            player.addEffect(
                    new MobEffectInstance(
                            effect,
                            duration,
                            amplifier,
                            false,
                            false,
                            true
                    )
            );

            return;
        }


        // --------------------------------------------------------
        // Existing effect is stronger.
        // --------------------------------------------------------

        if (
                current.getAmplifier()
                        >
                        amplifier
        ) {

            return;
        }


        // --------------------------------------------------------
        // Same amplifier and enough duration remaining.
        // --------------------------------------------------------

        if (
                current.getAmplifier()
                        ==
                        amplifier
                        &&
                        current.getDuration()
                                >
                                duration / 2
        ) {

            return;
        }


        // --------------------------------------------------------
        // Refresh.
        // --------------------------------------------------------

        player.addEffect(
                new MobEffectInstance(
                        effect,
                        duration,
                        amplifier,
                        false,
                        false,
                        true
                )
        );
    }


    // ============================================================
    // TOOLTIP
    // ============================================================

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {

        long silenceRemaining = 0;

        long cooldownRemaining = 0;

        long blackoutCooldownRemaining = 0;


        Level level =
                Minecraft.getInstance().level;


        Player player =
                Minecraft.getInstance().player;


        if (
                level != null
                        &&
                        player != null
        ) {

            long currentTime =
                    level.getGameTime();


            // ----------------------------------------------------
            // Silence active time.
            // ----------------------------------------------------

            silenceRemaining =
                    Math.max(
                            0,
                            getPlayerLong(
                                    player,
                                    TAG_SILENCE_END
                            ) - currentTime
                    );


            // ----------------------------------------------------
            // Silence cooldown.
            // ----------------------------------------------------

            cooldownRemaining =
                    Math.max(
                            0,
                            getPlayerLong(
                                    player,
                                    TAG_SILENCE_CD
                            ) - currentTime
                    );


            // ----------------------------------------------------
            // Blackout cooldown.
            // ----------------------------------------------------

            blackoutCooldownRemaining =
                    Math.max(
                            0,
                            getPlayerLong(
                                    player,
                                    TAG_BLACKOUT_CD
                            ) - currentTime
                    );
        }


        // ========================================================
        // NAME
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§8Null's Silence"
                )
        );


        // ========================================================
        // PASSIVE
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§7Passive §8- §fSpeed I while in inventory"
                )
        );


        // ========================================================
        // SILENCE
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§dSilence §8- §fInvisibility, Speed III, Strength I"
                )
        );


        // ========================================================
        // SILENCE ACTIVE
        // ========================================================

        if (silenceRemaining > 0) {

            tooltip.accept(
                    Component.literal(
                            "§cActive: §f"
                                    + formatSeconds(
                                    silenceRemaining
                            )
                    )
            );
        }


        // ========================================================
        // SILENCE COOLDOWN
        // ========================================================

        if (cooldownRemaining > 0) {

            tooltip.accept(
                    Component.literal(
                            "§cCooldown: §f"
                                    + formatSeconds(
                                    cooldownRemaining
                            )
                    )
            );

        } else {

            tooltip.accept(
                    Component.literal(
                            "§aReady"
                    )
            );
        }


        // ========================================================
        // BLACKOUT
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§5Blackout §8- §fBlindness, Nausea & massive blast"
                )
        );


        // ========================================================
        // BLACKOUT COOLDOWN
        // ========================================================

        if (blackoutCooldownRemaining > 0) {

            tooltip.accept(
                    Component.literal(
                            "§cCooldown: §f"
                                    + formatSeconds(
                                    blackoutCooldownRemaining
                            )
                    )
            );

        } else {

            tooltip.accept(
                    Component.literal(
                            "§aReady"
                    )
            );
        }
    }


    // ============================================================
    // TIMER FORMAT
    // ============================================================

    private static String formatSeconds(
            long ticks
    ) {

        long seconds =
                (ticks + 19L) / 20L;


        return seconds + "s";
    }
}
