package com.phantom.cod.entity.oathbound;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class OathboundEntity extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private final ServerBossEvent bossBar =
            new ServerBossEvent(
                    this.getUUID(),
                    Component.literal("The Oathbound")
                            .withStyle(style ->
                                    style
                                            .withColor(0xFFD700)
                                            .withBold(true)
                            ),
                    BossEvent.BossBarColor.YELLOW,
                    BossEvent.BossBarOverlay.PROGRESS
            );


    public OathboundEntity(
            EntityType<? extends Monster> entityType,
            Level level) {

        super(entityType, level);
    }


    @Override
    protected void registerGoals() {

        // Basic movement
        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        // Chase and vanilla melee attack
        this.goalSelector.addGoal(
                2,
                new MeleeAttackGoal(
                        this,
                        1.0D,
                        true
                )
        );

        // Wander when there is no target
        this.goalSelector.addGoal(
                5,
                new RandomStrollGoal(
                        this,
                        0.8D
                )
        );

        // Look at nearby players
        this.goalSelector.addGoal(
                6,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        8.0F
                )
        );

        // Randomly look around
        this.goalSelector.addGoal(
                7,
                new RandomLookAroundGoal(this)
        );


        // Find nearby players
        this.targetSelector.addGoal(
                1,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
        );
    }


    @Override
    public void tick() {

        super.tick();

        if (!this.level().isClientSide()
                && this.getMaxHealth() > 0.0F) {

            float progress =
                    this.getHealth() / this.getMaxHealth();

            progress = Math.max(
                    0.0F,
                    Math.min(1.0F, progress)
            );

            this.bossBar.setProgress(progress);
        }
    }


    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers) {

        OathboundBehavior.registerAnimationController(
                this,
                controllers
        );
    }


    @Override
    public void startSeenByPlayer(ServerPlayer player) {

        super.startSeenByPlayer(player);

        this.bossBar.addPlayer(player);
    }


    @Override
    public void stopSeenByPlayer(ServerPlayer player) {

        super.stopSeenByPlayer(player);

        this.bossBar.removePlayer(player);
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {

        return this.cache;
    }


    @Override
    public boolean removeWhenFarAway(double distance) {

        return false;
    }
}