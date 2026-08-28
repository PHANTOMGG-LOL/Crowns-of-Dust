package com.phantom.cod.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


/**
 * ============================================================
 * HEROBRINE'S LEGACY
 * ============================================================
 *
 * PASSIVE:
 * Strength I while anywhere in inventory.
 *
 * ON HIT:
 * 50% chance to summon lightning on the target.
 *
 * RIGHT CLICK:
 * Teleport up to 40 blocks.
 *
 * Teleport Cooldown:
 * 10 seconds.
 *
 * SHIFT + RIGHT CLICK:
 * The Judgement.
 *
 * The Judgement:
 * - Lasts 10 seconds.
 * - Strikes nearby living entities with lightning.
 * - Radius: 20 blocks.
 * - Gives the player Strength II.
 * - Cooldown: 3 minutes.
 *
 * IMPORTANT:
 * Judgement state is stored dynamically against the PLAYER.
 *
 * Therefore:
 * - Switching items does NOT cancel Judgement.
 * - Dropping the sword does NOT cancel Judgement.
 * - Removing the sword from inventory does NOT cancel Judgement.
 */
@EventBusSubscriber(
        modid = "cod"
)
public class HerobrinesLegacyItem extends Item {

    // ============================================================
    // SETTINGS
    // ============================================================

    /**
     * Passive Strength I refresh duration.
     */
    private static final int PASSIVE_DURATION = 80;


    /**
     * Normal teleport distance.
     */
    private static final int TELEPORT_RANGE = 40;


    /**
     * Normal teleport cooldown.
     */
    private static final long TELEPORT_COOLDOWN =
            20L * 10L;


    // ============================================================
    // THE JUDGEMENT SETTINGS
    // ============================================================

    /**
     * Judgement duration:
     *
     * 10 seconds.
     */
    private static final long JUDGEMENT_DURATION =
            20L * 10L;


    /**
     * Judgement cooldown:
     *
     * 3 minutes.
     */
    private static final long JUDGEMENT_COOLDOWN =
            20L * 60L * 3;


    /**
     * All living entities within this radius are struck.
     */
    private static final double JUDGEMENT_RADIUS =
            20.0D;


    /**
     * Lightning strike interval.
     *
     * 20 ticks = 1 second.
     */
    private static final int JUDGEMENT_STRIKE_INTERVAL =
            20;


    // ============================================================
    // ON-HIT SETTINGS
    // ============================================================

    /**
     * 50% chance to summon lightning.
     */
    private static final float LIGHTNING_CHANCE =
            0.50F;


    // ============================================================
    // PLAYER DYNAMIC DATA
    // ============================================================

    /**
     * Normal teleport cooldown.
     */
    private static final String TAG_TELEPORT_CD =
            "herobrines_legacy_teleport_cd";


    /**
     * When Judgement ends.
     */
    private static final String TAG_JUDGEMENT_END =
            "herobrines_legacy_judgement_end";


    /**
     * When Judgement can be used again.
     */
    private static final String TAG_JUDGEMENT_CD =
            "herobrines_legacy_judgement_cd";


    /**
     * Dynamic player data.
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


    private static long getPlayerLong(
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

    public HerobrinesLegacyItem(
            Properties properties
    ) {

        super(properties);
    }


    // ============================================================
    // ON HIT - HEROBRINE'S LIGHTNING
    // ============================================================
    //
    // 50% chance to summon a REAL lightning bolt.
    //
    // This uses the same lightning spawning method that is
    // already working in your current code.
    // ============================================================

    @Override
    public void hurtEnemy(
            ItemStack itemStack,
            LivingEntity mob,
            LivingEntity attacker
    ) {

        // --------------------------------------------------------
        // Only players can trigger the weapon ability.
        // --------------------------------------------------------

        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }


        // --------------------------------------------------------
        // 50% chance.
        // --------------------------------------------------------

        if (
                player.getRandom().nextFloat()
                        >=
                        LIGHTNING_CHANCE
        ) {

            return;
        }


        // --------------------------------------------------------
        // Server level.
        // --------------------------------------------------------

        ServerLevel level =
                player.level();


        // ========================================================
        // GET VANILLA LIGHTNING ENTITY TYPE
        // ========================================================

        Identifier lightningId =
                Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "lightning_bolt"
                );


        EntityType<?> lightningType =
                BuiltInRegistries.ENTITY_TYPE.getValue(
                        lightningId
                );


        // ========================================================
        // SPAWN LIGHTNING
        // ========================================================

        Entity spawned =
                lightningType.spawn(
                        level,
                        BlockPos.containing(
                                mob.getX(),
                                mob.getY(),
                                mob.getZ()
                        ),
                        EntitySpawnReason.TRIGGERED
                );


        // --------------------------------------------------------
        // Make sure it is actually a LightningBolt.
        // --------------------------------------------------------

        if (!(spawned instanceof LightningBolt lightning)) {
            return;
        }


        // --------------------------------------------------------
        // Put the lightning exactly on the target.
        // --------------------------------------------------------

        lightning.setPos(
                mob.getX(),
                mob.getY(),
                mob.getZ()
        );
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
        // The Judgement.
        // ========================================================

        if (player.isCrouching()) {

            return activateJudgement(
                    level,
                    player
            );
        }


        // ========================================================
        // NORMAL RIGHT CLICK
        // ========================================================
        //
        // Teleport.
        // ========================================================

        ItemStack stack =
                player.getItemInHand(hand);


        long time =
                level.getGameTime();


        return handleTeleport(
                level,
                player,
                stack,
                time
        );
    }


    // ============================================================
    // THE JUDGEMENT - ACTIVATION
    // ============================================================
    //
    // Starts the ability and stores everything dynamically
    // against the PLAYER.
    // ============================================================

    private static InteractionResult activateJudgement(
            Level level,
            Player player
    ) {

        long currentTime =
                level.getGameTime();


        // ========================================================
        // CHECK COOLDOWN
        // ========================================================

        long cooldownEnd =
                getPlayerLong(
                        player,
                        TAG_JUDGEMENT_CD
                );


        if (currentTime < cooldownEnd) {

            return InteractionResult.CONSUME;
        }


        // ========================================================
        // CLIENT
        // ========================================================
        //
        // Actual ability logic happens on the server.
        // ========================================================

        if (level.isClientSide()) {

            return InteractionResult.SUCCESS;
        }


        // ========================================================
        // START JUDGEMENT
        // ========================================================

        setPlayerLong(
                player,
                TAG_JUDGEMENT_END,
                currentTime + JUDGEMENT_DURATION
        );


        // ========================================================
        // START COOLDOWN
        // ========================================================

        setPlayerLong(
                player,
                TAG_JUDGEMENT_CD,
                currentTime + JUDGEMENT_COOLDOWN
        );


        // ========================================================
        // STRENGTH II
        // ========================================================
        //
        // Amplifier 1 = Strength II.
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.STRENGTH,
                20,
                1
        );


        // ========================================================
        // IMMEDIATE LIGHTNING STRIKE
        // ========================================================
        //
        // The first wave happens immediately rather than waiting
        // one full second.
        // ========================================================

        if (player instanceof ServerPlayer serverPlayer) {

            strikeNearbyTargets(
                    serverPlayer
            );
        }


        return InteractionResult.SUCCESS;
    }


    // ============================================================
    // THE JUDGEMENT - ACTIVE TICK
    // ============================================================
    //
    // This keeps running even when the sword is no longer held.
    //
    // Every second:
    //
    // - Nearby living entities are found.
    // - Lightning is spawned on each target.
    // - Strength II is refreshed.
    // ============================================================

    @SubscribeEvent
    public static void onJudgementTick(
            PlayerTickEvent.Post event
    ) {

        // --------------------------------------------------------
        // Server side only.
        // --------------------------------------------------------

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }


        long currentTime =
                player.level().getGameTime();


        // ========================================================
        // GET JUDGEMENT END TIME
        // ========================================================

        long judgementEnd =
                getPlayerLong(
                        player,
                        TAG_JUDGEMENT_END
                );


        // ========================================================
        // JUDGEMENT NOT ACTIVE
        // ========================================================

        if (currentTime >= judgementEnd) {
            return;
        }


        // ========================================================
        // KEEP STRENGTH II ACTIVE
        // ========================================================

        applyEffectWithPriority(
                player,
                MobEffects.STRENGTH,
                10,
                1
        );


        // ========================================================
        // LIGHTNING WAVE
        // ========================================================
        //
        // Every 20 ticks = every second.
        // ========================================================

        if (
                player.tickCount
                        %
                        JUDGEMENT_STRIKE_INTERVAL
                        ==
                        0
        ) {

            strikeNearbyTargets(
                    player
            );
        }
    }


    // ============================================================
    // STRIKE NEARBY TARGETS
    // ============================================================
    //
    // Finds EVERY living entity within 20 blocks.
    //
    // The player activating Judgement is excluded.
    //
    // This includes:
    //
    // - Hostile mobs
    // - Passive mobs
    // - Villagers
    // - Other players
    // - Any other LivingEntity
    // ============================================================

    private static void strikeNearbyTargets(
            ServerPlayer player
    ) {

        ServerLevel level =
                player.level();


        // ========================================================
        // SEARCH AREA
        // ========================================================

        AABB area =
                player.getBoundingBox()
                        .inflate(
                                JUDGEMENT_RADIUS
                        );


        // ========================================================
        // GET LIVING ENTITIES
        // ========================================================

        for (
                LivingEntity target :
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        area
                )
        ) {

            // ----------------------------------------------------
            // Don't strike the user.
            // ----------------------------------------------------

            if (target == player) {
                continue;
            }


            // ----------------------------------------------------
            // Don't strike dead entities.
            // ----------------------------------------------------

            if (!target.isAlive()) {
                continue;
            }


            // ----------------------------------------------------
            // Don't strike spectator players.
            // ----------------------------------------------------

            if (
                    target instanceof ServerPlayer targetPlayer
                            &&
                            targetPlayer.isSpectator()
            ) {

                continue;
            }


            // ====================================================
            // CREATE VANILLA LIGHTNING
            // ====================================================

            Identifier lightningId =
                    Identifier.fromNamespaceAndPath(
                            "minecraft",
                            "lightning_bolt"
                    );


            EntityType<?> lightningType =
                    BuiltInRegistries.ENTITY_TYPE.getValue(
                            lightningId
                    );


            // ====================================================
            // SPAWN LIGHTNING
            // ====================================================

            Entity spawned =
                    lightningType.spawn(
                            level,
                            BlockPos.containing(
                                    target.getX(),
                                    target.getY(),
                                    target.getZ()
                            ),
                            EntitySpawnReason.TRIGGERED
                    );


            // ----------------------------------------------------
            // Make sure the entity is LightningBolt.
            // ----------------------------------------------------

            if (!(spawned instanceof LightningBolt lightning)) {
                continue;
            }


            // ----------------------------------------------------
            // Set exact target position.
            // ----------------------------------------------------

            lightning.setPos(
                    target.getX(),
                    target.getY(),
                    target.getZ()
            );
        }
    }


    // ============================================================
    // TELEPORT
    // ============================================================

    private InteractionResult handleTeleport(
            Level level,
            Player player,
            ItemStack stack,
            long time
    ) {

        // --------------------------------------------------------
        // Dynamic PLAYER cooldown.
        //
        // NOT stored inside ItemStack.
        // --------------------------------------------------------

        if (
                time <
                        getPlayerLong(
                                player,
                                TAG_TELEPORT_CD
                        )
        ) {

            return InteractionResult.CONSUME;
        }


        // --------------------------------------------------------
        // Server performs actual teleport.
        // --------------------------------------------------------

        if (!(player instanceof ServerPlayer serverPlayer)) {

            return InteractionResult.SUCCESS;
        }


        ServerLevel serverLevel =
                serverPlayer.level();


        // ========================================================
        // TELEPORT DIRECTION
        // ========================================================

        Vec3 look =
                serverPlayer
                        .getViewVector(1.0F)
                        .normalize();


        Vec3 destination =
                serverPlayer.position()
                        .add(
                                look.scale(
                                        TELEPORT_RANGE
                                )
                        );


        // ========================================================
        // DIRECT TELEPORT
        // ========================================================
        //
        // No safe-location search.
        // ========================================================

        serverPlayer.teleportTo(
                serverLevel,
                destination.x,
                destination.y,
                destination.z,
                java.util.Set.of(),
                serverPlayer.getYRot(),
                serverPlayer.getXRot(),
                false
        );


        serverPlayer.setDeltaMovement(
                Vec3.ZERO
        );

        serverPlayer.hurtMarked = true;


        // ========================================================
        // START TELEPORT COOLDOWN
        // ========================================================

        setPlayerLong(
                player,
                TAG_TELEPORT_CD,
                time + TELEPORT_COOLDOWN
        );


        return InteractionResult.SUCCESS;
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


        // --------------------------------------------------------
        // Anywhere in normal inventory.
        // --------------------------------------------------------

        if (!hasWeaponInInventory(player)) {
            return;
        }


        // Strength I.
        applyEffectWithPriority(
                player,
                MobEffects.STRENGTH,
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

    private static void applyEffectWithPriority(
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
        // Refresh our effect.
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

        // ========================================================
        // DYNAMIC TIMERS
        // ========================================================

        long teleportRemaining = 0;

        long judgementRemaining = 0;

        long judgementCooldownRemaining = 0;


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
            // Teleport cooldown.
            // ----------------------------------------------------

            teleportRemaining =
                    Math.max(
                            0,
                            getPlayerLong(
                                    player,
                                    TAG_TELEPORT_CD
                            ) - currentTime
                    );


            // ----------------------------------------------------
            // Judgement active time.
            // ----------------------------------------------------

            judgementRemaining =
                    Math.max(
                            0,
                            getPlayerLong(
                                    player,
                                    TAG_JUDGEMENT_END
                            ) - currentTime
                    );


            // ----------------------------------------------------
            // Judgement cooldown.
            // ----------------------------------------------------

            judgementCooldownRemaining =
                    Math.max(
                            0,
                            getPlayerLong(
                                    player,
                                    TAG_JUDGEMENT_CD
                            ) - currentTime
                    );
        }


        // ========================================================
        // NAME
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§5Herobrine's Legacy"
                )
        );


        // ========================================================
        // PASSIVE
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§7Passive §8- §fStrength I while in inventory"
                )
        );


        // ========================================================
        // ON HIT
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§cOn Hit §8- §f50% chance to summon lightning"
                )
        );


        // ========================================================
        // TELEPORT
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§dTeleport §8- §fTeleport up to 40 blocks"
                )
        );


        // ========================================================
        // TELEPORT COOLDOWN
        // ========================================================

        if (teleportRemaining > 0) {

            tooltip.accept(
                    Component.literal(
                            "§cCooldown: §f"
                                    + formatSeconds(
                                    teleportRemaining
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
        // THE JUDGEMENT
        // ========================================================

        tooltip.accept(
                Component.literal(
                        "§dThe Judgement §8- §fLightning strikes nearby targets"
                )
        );


        // ========================================================
        // JUDGEMENT TIMER
        // ========================================================

        if (judgementRemaining > 0) {

            tooltip.accept(
                    Component.literal(
                            "§cActive: §f"
                                    + formatSeconds(
                                    judgementRemaining
                            )
                    )
            );

        } else if (
                judgementCooldownRemaining > 0
        ) {

            tooltip.accept(
                    Component.literal(
                            "§cCooldown: §f"
                                    + formatSeconds(
                                    judgementCooldownRemaining
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
    // FORMAT TIMER
    // ============================================================
    //
    // Converts ticks into a readable countdown.
    //
    // Examples:
    //
    // 200 ticks -> 10s
    // 140 ticks -> 7s
    // 60 ticks  -> 3s
    // ============================================================

    private static String formatSeconds(
            long ticks
    ) {

        long seconds =
                (ticks + 19L) / 20L;


        return seconds + "s";
    }
}
