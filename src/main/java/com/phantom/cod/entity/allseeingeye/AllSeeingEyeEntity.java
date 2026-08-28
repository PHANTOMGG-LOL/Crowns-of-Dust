package com.phantom.cod.entity.allseeingeye;

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

public class AllSeeingEyeEntity extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private boolean chasingPlayer = false;

    private final ServerBossEvent bossBar =
            new ServerBossEvent(
                    this.getUUID(),
                    Component.literal("All Seeing Eye")
                            .withStyle(style ->
                                    style
                                            .withColor(0x00FFFF)
                                            .withBold(true)
                            ),
                    BossEvent.BossBarColor.BLUE,
                    BossEvent.BossBarOverlay.PROGRESS
            );


    public AllSeeingEyeEntity(
            EntityType<? extends Monster> entityType,
            Level level) {

        super(entityType, level);
    }


    public boolean isChasingPlayer() {
        return this.chasingPlayer;
    }


    public void setChasingPlayer(boolean chasingPlayer) {
        this.chasingPlayer = chasingPlayer;
    }


    @Override
    public void tick() {

        super.tick();

        if (!this.level().isClientSide()) {

            this.setChasingPlayer(
                    this.getTarget() instanceof Player
            );

            if (this.getMaxHealth() > 0.0F) {

                float progress =
                        this.getHealth() / this.getMaxHealth();

                progress = Math.max(
                        0.0F,
                        Math.min(1.0F, progress)
                );

                this.bossBar.setProgress(progress);
            }
        }
    }


    @Override
    protected void registerGoals() {

        // Float
        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        // Chase player
        this.goalSelector.addGoal(
                2,
                new MeleeAttackGoal(
                        this,
                        1.5D,
                        true
                )
        );

        // Normal wandering
        this.goalSelector.addGoal(
                5,
                new RandomStrollGoal(
                        this,
                        0.5D
                )
        );

        // Look at players
        this.goalSelector.addGoal(
                6,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        8.0F
                )
        );

        // Random looking
        this.goalSelector.addGoal(
                7,
                new RandomLookAroundGoal(this)
        );

        // Target nearest player
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
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers) {

        AllSeeingEyeBehavior.registerAnimationController(
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