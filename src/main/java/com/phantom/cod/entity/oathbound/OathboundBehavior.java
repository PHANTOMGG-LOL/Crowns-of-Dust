package com.phantom.cod.entity.oathbound;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.keyframehandler.AutoPlayingSoundKeyframeHandler;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animatable.manager.AnimatableManager;

public final class OathboundBehavior {

    private static final RawAnimation IDLE =
            RawAnimation.begin()
                    .thenLoop("misc.idle");

    private static final RawAnimation WALK =
            RawAnimation.begin()
                    .thenLoop("move.walk");


    private OathboundBehavior() {
    }


    public static void registerAnimationController(
            OathboundEntity entity,
            AnimatableManager.ControllerRegistrar controllers) {

        controllers.add(
                new AnimationController<>(
                        "movement",
                        test -> movementAnimation(
                                entity,
                                test
                        )
                )
                        .setSoundKeyframeHandler(
                                new AutoPlayingSoundKeyframeHandler<>()
                        )
        );
    }


    private static PlayState movementAnimation(
            OathboundEntity entity,
            AnimationTest<?> test) {

        if (entity.getDeltaMovement()
                .horizontalDistanceSqr() > 0.0001D) {

            return test.setAndContinue(WALK);
        }

        return test.setAndContinue(IDLE);
    }
}