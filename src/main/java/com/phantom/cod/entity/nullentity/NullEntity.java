package com.phantom.cod.entity.nullentity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import com.phantom.cod.entity.guardian.GuardianLookGoal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;


/**
 * Null
 *
 * A stationary guardian entity.
 *
 * Behavior:
 *
 * - Does not wander.
 * - Does not attack normally.
 * - Does not naturally despawn.
 * - Cannot be damaged by players.
 * - Instantly kills a player who attacks him.
 * - Looks at players only when they are looking at him.
 * - Only the head rotates.
 * - Has no animations.
 */
public class NullEntity
        extends Monster
        implements GeoEntity {


    // ==================================================
    // GeckoLib
    // ==================================================

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);


    // ==================================================
    // Constructor
    // ==================================================

    public NullEntity(
            EntityType<? extends Monster> entityType,
            Level level) {

        super(entityType, level);
    }


    // ==================================================
    // AI Goals
    // ==================================================

    @Override
    protected void registerGoals() {

        /*
         * Null has exactly one AI goal:
         *
         * Watch players who are looking at him.
         *
         * No wandering.
         * No chasing.
         * No melee attack.
         */
        this.goalSelector.addGoal(
                0,
                new GuardianLookGoal(
                        this,
                        32.0F
                )
        );
    }


    // ==================================================
    // Damage
    // ==================================================

    @Override
    public boolean hurtServer(
            ServerLevel level,
            DamageSource source,
            float amount) {

        /*
         * If a PLAYER attacks Null:
         *
         * Null takes no damage.
         * The player is instantly killed.
         */
        if (source.getEntity() instanceof Player player) {

            player.hurt(
                    this.damageSources().mobAttack(this),
                    Float.MAX_VALUE
            );

            return false;
        }


        /*
         * Other damage sources are allowed.
         *
         * This keeps /kill and command-based removal
         * functional.
         */
        return super.hurtServer(
                level,
                source,
                amount
        );
    }


    // ==================================================
    // GeckoLib Animation Controllers
    // ==================================================

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers) {

        /*
         * Null intentionally has no animations.
         */
    }


    @Override
    public AnimatableInstanceCache
    getAnimatableInstanceCache() {

        return this.cache;
    }


    // ==================================================
    // Persistence
    // ==================================================

    @Override
    public boolean removeWhenFarAway(
            double distance) {

        /*
         * Null never naturally despawns.
         */
        return false;
    }
}