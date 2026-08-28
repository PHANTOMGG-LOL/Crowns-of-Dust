package com.phantom.cod.entity.kingswill;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.keyframehandler.AutoPlayingSoundKeyframeHandler;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animatable.manager.AnimatableManager;

public final class KingsWillBehavior {

    private static final RawAnimation IDLE =
            RawAnimation.begin()
                    .thenLoop("misc.idle");

    private static final RawAnimation WALK =
            RawAnimation.begin()
                    .thenLoop("move.walk");

    private KingsWillBehavior() {
    }

    public static void registerAnimationController(
            KingsWillEntity entity,
            AnimatableManager.ControllerRegistrar controllers) {

        controllers.add(
                new AnimationController<>(
                        "movement",
                        test -> movementAnimation(entity, test)
                )
                        .setSoundKeyframeHandler(
                                new AutoPlayingSoundKeyframeHandler<>()
                        )
        );
    }

    private static PlayState movementAnimation(
            KingsWillEntity entity,
            AnimationTest<?> test) {

        if (entity.walkAnimation.isMoving()) {
            return test.setAndContinue(WALK);
        }

        return test.setAndContinue(IDLE);
    }
}