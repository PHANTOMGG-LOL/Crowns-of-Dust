package com.phantom.cod.client;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.network.ModNetwork.ScreenShakePayload;
import com.phantom.cod.registry.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.particles.ParticleTypes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.Random;

@EventBusSubscriber(
        modid = CrownsOfDust.MOD_ID,
        value = Dist.CLIENT
)
public final class CameraShake {

    // ==================================================
    // SHAKE
    // ==================================================

    private static int remainingTicks = 0;
    private static int totalTicks = 0;

    private static float intensity = 0.0F;


    // ==================================================
    // TEMPLE FLASH
    // ==================================================

    /*
     * 100 ticks = 5 seconds.
     */
    private static int flashTicks = 0;
    private static int flashTotalTicks = 0;


    // ==================================================
    // KINGDOM CINEMATIC
    // ==================================================

    private static boolean cinematicActive = false;


    // ==================================================
    // EVENT POSITION
    // ==================================================

    private static double eventX = 0.0D;
    private static double eventY = 0.0D;
    private static double eventZ = 0.0D;


    // ==================================================
    // RANDOM
    // ==================================================

    private static final Random RANDOM =
            new Random();


    private CameraShake() {
    }


    // ==================================================
    // RECEIVE SCREEN SHAKE
    // ==================================================

    @SubscribeEvent
    public static void registerClientPayload(
            RegisterClientPayloadHandlersEvent event) {

        event.register(
                ScreenShakePayload.TYPE,
                (payload, context) -> {

                    // ==================================================
                    // SHAKE
                    // ==================================================

                    remainingTicks =
                            payload.duration();

                    totalTicks =
                            payload.duration();

                    intensity =
                            payload.intensity();


                    // ==================================================
                    // EVENT POSITION
                    // ==================================================

                    eventX =
                            payload.x() + 0.5D;

                    eventY =
                            payload.y() + 0.5D;

                    eventZ =
                            payload.z() + 0.5D;


                    // ==================================================
                    // EFFECT TYPE
                    // ==================================================

                    int effectType =
                            payload.effectType();


                    // ==================================================
                    // KINGDOM SKY
                    // ==================================================

                    if (effectType == 0) {

                        /*
                         * Kingdom boss summon.
                         */

                        cinematicActive = true;

                    } else if (effectType == 1) {

                        /*
                         * Boss death.
                         *
                         * Restore normal environment.
                         */

                        cinematicActive = false;
                    }


                    // ==================================================
                    // TEMPLE FLASH
                    // ==================================================

                    if (effectType == 3) {

                        /*
                         * Start 5-second flash.
                         */

                        flashTicks = 100;
                        flashTotalTicks = 100;
                    }


                    // ==================================================
                    // SOUND
                    // ==================================================

                    /*
                     * 0 = Kingdom summon
                     * 2 = Temple stone
                     * 3 = Temple completion
                     *
                     * 1 = boss death, no awakening sound.
                     */

                    if (effectType == 0
                            || effectType == 2
                            || effectType == 3) {

                        Minecraft minecraft =
                                Minecraft.getInstance();

                        if (minecraft.player != null) {

                            minecraft.player.playSound(
                                    ModSounds.BOSS_AWAKENING.get(),
                                    1.0F,
                                    1.0F
                            );
                        }
                    }
                }
        );
    }


    // ==================================================
    // CLIENT TICK
    // ==================================================

    public static void tick() {

        // ==================================================
        // FLASH TIMER
        // ==================================================

        if (flashTicks > 0) {
            flashTicks--;
        }


        // ==================================================
        // SHAKE
        // ==================================================

        if (remainingTicks <= 0) {
            return;
        }


        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null ||
                minecraft.level == null) {

            return;
        }


        remainingTicks--;


        // ==================================================
        // SHAKE PROGRESS
        // ==================================================

        float progress =
                (float) remainingTicks
                        / (float) totalTicks;


        float currentIntensity =
                intensity * progress;


        // ==================================================
        // CAMERA ROTATION
        // ==================================================

        float yawShake =
                (RANDOM.nextFloat() * 2.0F - 1.0F)
                        * currentIntensity
                        * 4.5F;


        float pitchShake =
                (RANDOM.nextFloat() * 2.0F - 1.0F)
                        * currentIntensity
                        * 3.5F;


        minecraft.player.setYRot(
                minecraft.player.getYRot()
                        + yawShake
        );


        minecraft.player.setXRot(
                Math.max(
                        -90.0F,
                        Math.min(
                                90.0F,
                                minecraft.player.getXRot()
                                        + pitchShake
                        )
                )
        );


        // ==================================================
        // DUST
        // ==================================================

        spawnDust(
                minecraft,
                currentIntensity
        );
    }


    // ==================================================
    // DUST EFFECT
    // ==================================================

    private static void spawnDust(
            Minecraft minecraft,
            float currentIntensity) {

        if (minecraft.level == null ||
                minecraft.player == null) {

            return;
        }


        // ==================================================
        // DISTANCE
        // ==================================================

        double distance =
                minecraft.player.distanceToSqr(
                        eventX,
                        eventY,
                        eventZ
                );


        if (distance > 40.0D * 40.0D) {
            return;
        }


        // ==================================================
        // PARTICLE COUNT
        // ==================================================

        int particleCount;


        if (currentIntensity >
                intensity * 0.75F) {

            particleCount = 35;

        } else if (currentIntensity >
                intensity * 0.40F) {

            particleCount = 22;

        } else {

            particleCount = 10;
        }


        // ==================================================
        // PARTICLES
        // ==================================================

        for (int i = 0; i < particleCount; i++) {

            double angle =
                    RANDOM.nextDouble()
                            * Math.PI * 2.0D;


            double radius =
                    2.0D
                            + RANDOM.nextDouble()
                            * 22.0D;


            double x =
                    eventX
                            + Math.cos(angle)
                            * radius;


            double z =
                    eventZ
                            + Math.sin(angle)
                            * radius;


            double y =
                    eventY
                            - 0.2D
                            + RANDOM.nextDouble()
                            * 1.8D;


            int type =
                    RANDOM.nextInt(10);


            if (type < 6) {

                minecraft.level.addParticle(
                        ParticleTypes.POOF,
                        x,
                        y,
                        z,
                        0.0D,
                        0.02D
                                + RANDOM.nextDouble()
                                * 0.05D,
                        0.0D
                );

            } else if (type < 9) {

                minecraft.level.addParticle(
                        ParticleTypes.CLOUD,
                        x,
                        y,
                        z,
                        0.0D,
                        0.01D
                                + RANDOM.nextDouble()
                                * 0.04D,
                        0.0D
                );

            } else {

                minecraft.level.addParticle(
                        ParticleTypes.ASH,
                        x,
                        y,
                        z,
                        0.0D,
                        0.015D
                                + RANDOM.nextDouble()
                                * 0.03D,
                        0.0D
                );
            }
        }
    }


    // ==================================================
    // TEMPLE WHITE FLASH
    // ==================================================

    @SubscribeEvent
    public static void renderTempleFlash(
            RenderGuiEvent.Post event) {

        if (flashTicks <= 0 ||
                flashTotalTicks <= 0) {

            return;
        }


        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }


        GuiGraphicsExtractor graphics =
                event.getGuiGraphics();


        int width =
                graphics.guiWidth();

        int height =
                graphics.guiHeight();


        // ==================================================
        // FLASH PROGRESS
        // ==================================================

        float progress =
                1.0F
                        - ((float) flashTicks
                        / (float) flashTotalTicks);


        float alpha;


        /*
         * 0.00 - 0.15
         *
         * Fade toward white.
         */

        if (progress < 0.15F) {

            alpha =
                    progress / 0.15F;


            /*
             * 0.15 - 0.85
             *
             * Completely white.
             */

        } else if (progress < 0.85F) {

            alpha = 1.0F;


            /*
             * 0.85 - 1.00
             *
             * Reveal the new world.
             */

        } else {

            alpha =
                    1.0F
                            - ((progress - 0.85F)
                            / 0.15F);
        }


        alpha =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                alpha
                        )
                );


        int alphaValue =
                (int) (alpha * 255.0F);


        int color =
                (alphaValue << 24)
                        | 0x00FFFFFF;


        // ==================================================
        // FULL SCREEN WHITE
        // ==================================================

        graphics.fill(
                0,
                0,
                width,
                height,
                color
        );
    }


    // ==================================================
    // KINGDOM FOG / SKY COLOR
    // ==================================================

    @SubscribeEvent
    public static void onFogColor(
            ViewportEvent.ComputeFogColor event) {

        if (!cinematicActive) {
            return;
        }


        Minecraft minecraft =
                Minecraft.getInstance();


        if (minecraft.player == null) {
            return;
        }


        // ==================================================
        // DISTANCE FROM BOSS EVENT
        // ==================================================

        double distance =
                minecraft.player.distanceToSqr(
                        eventX,
                        eventY,
                        eventZ
                );


        if (distance > 40.0D * 40.0D) {
            return;
        }


        // ==================================================
        // CURRENT COLOR
        // ==================================================

        float currentRed =
                event.getRed();

        float currentGreen =
                event.getGreen();

        float currentBlue =
                event.getBlue();


        // ==================================================
        // CRIMSON COLOR
        // ==================================================

        float targetRed = 0.48F;
        float targetGreen = 0.015F;
        float targetBlue = 0.02F;


        // ==================================================
        // STRENGTH
        // ==================================================

        float strength = 0.90F;


        // ==================================================
        // APPLY
        // ==================================================

        event.setRed(
                currentRed * (1.0F - strength)
                        + targetRed * strength
        );


        event.setGreen(
                currentGreen * (1.0F - strength)
                        + targetGreen * strength
        );


        event.setBlue(
                currentBlue * (1.0F - strength)
                        + targetBlue * strength
        );
    }


    // ==================================================
    // CLIENT TICK EVENT
    // ==================================================

    @SubscribeEvent
    public static void onClientTick(
            ClientTickEvent.Post event) {

        tick();
    }
}