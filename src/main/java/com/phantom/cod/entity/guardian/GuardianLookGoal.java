package com.phantom.cod.entity.guardian;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GuardianLookGoal extends Goal {

    // ==================================================
    // Configuration
    // ==================================================

    private final Mob mob;
    private final float range;

    private Player target;


    // ==================================================
    // Constructor
    // ==================================================

    public GuardianLookGoal(
            Mob mob,
            float range) {

        this.mob = mob;
        this.range = range;

        /*
         * This goal only controls looking.
         *
         * It does NOT control:
         * - movement
         * - attacking
         * - wandering
         */
        this.setFlags(
                EnumSet.of(Goal.Flag.LOOK)
        );
    }


    // ==================================================
    // Find Player Looking At Guardian
    // ==================================================

    @Override
    public boolean canUse() {

        this.target = findLookingPlayer();

        return this.target != null;
    }


    // ==================================================
    // Continue Looking
    // ==================================================

    @Override
    public boolean canContinueToUse() {

        if (this.target == null) {
            return false;
        }

        if (!this.target.isAlive()) {
            return false;
        }

        if (this.mob.distanceToSqr(this.target)
                > this.range * this.range) {

            return false;
        }

        if (!this.target.hasLineOfSight(this.mob)) {
            return false;
        }

        /*
         * Stop tracking once the player looks away.
         */
        return isPlayerLookingAtGuardian(this.target);
    }


    // ==================================================
    // Tick
    // ==================================================

    @Override
    public void tick() {

        if (this.target == null) {
            return;
        }

        /*
         * Only rotate the HEAD.
         *
         * We intentionally do NOT use:
         *
         * getLookControl().setLookAt(...)
         *
         * because that can rotate the body.
         */
        double dx =
                this.target.getX()
                        - this.mob.getX();

        double dz =
                this.target.getZ()
                        - this.mob.getZ();

        float headYaw =
                (float)
                        (
                                Math.atan2(
                                        dz,
                                        dx
                                )
                                        * (180.0D / Math.PI)
                        )
                        - 90.0F;

        this.mob.setYHeadRot(headYaw);
    }


    // ==================================================
    // Stop
    // ==================================================

    @Override
    public void stop() {

        this.target = null;
    }


    // ==================================================
    // Player Detection
    // ==================================================

    private Player findLookingPlayer() {

        Player closestPlayer = null;

        double closestDistance =
                Double.MAX_VALUE;


        for (Player player :
                this.mob.level().players()) {

            if (player.isSpectator()) {
                continue;
            }

            if (!player.isAlive()) {
                continue;
            }


            double distance =
                    this.mob.distanceToSqr(player);

            if (distance > this.range * this.range) {
                continue;
            }


            /*
             * The player must have direct line of sight.
             */
            if (!player.hasLineOfSight(this.mob)) {
                continue;
            }


            /*
             * The player must actually be looking
             * toward the guardian.
             */
            if (!isPlayerLookingAtGuardian(player)) {
                continue;
            }


            /*
             * Keep the closest valid player.
             */
            if (distance < closestDistance) {

                closestDistance = distance;

                closestPlayer = player;
            }
        }


        return closestPlayer;
    }


    // ==================================================
    // Check Player View Direction
    // ==================================================

    private boolean isPlayerLookingAtGuardian(
            Player player) {

        Vec3 playerView =
                player.getViewVector(1.0F)
                        .normalize();


        Vec3 directionToGuardian =
                this.mob.getEyePosition()
                        .subtract(
                                player.getEyePosition()
                        )
                        .normalize();


        double dot =
                playerView.dot(directionToGuardian);


        /*
         * 0.95 means the player has to be looking
         * very closely toward the guardian.
         *
         * 1.0 = directly at the guardian.
         */
        return dot > 0.95D;
    }
}