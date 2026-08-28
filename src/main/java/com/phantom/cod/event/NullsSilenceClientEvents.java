package com.phantom.cod.event;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;


/**
 * ============================================================
 * NULL'S SILENCE - CLIENT RENDERING
 * ============================================================
 *
 * This file handles ONLY the visual disappearance.
 *
 * Normal Invisibility potion:
 *
 *     Body hidden normally
 *     Armor is NOT forcibly removed by our event.
 *
 * Null's Silence:
 *
 *     Body      -> hidden
 *     Helmet    -> hidden
 *     Chestplate-> hidden
 *     Leggings  -> hidden
 *     Boots     -> hidden
 *     Held item -> hidden
 *
 * The complete player render is cancelled.
 */
@EventBusSubscriber(
        modid = "cod",
        value = Dist.CLIENT
)
public class NullsSilenceClientEvents {

    // ============================================================
    // RENDER STATE KEY
    // ============================================================

    /**
     * Custom render-state value.
     *
     * This tells RenderPlayerEvent.Pre:
     *
     * "This invisibility is specifically Null's Silence."
     */
    public static final ContextKey<Boolean> NULL_SILENCE =
            new ContextKey<>(
                    Identifier.fromNamespaceAndPath(
                            "cod",
                            "null_silence"
                    )
            );


    // ============================================================
    // REGISTER RENDER STATE MODIFIER
    // ============================================================
    //
    // This is a MOD BUS event in NeoForge 26.2.
    //
    // @EventBusSubscriber automatically sends it to the
    // appropriate mod event bus.
    // ============================================================

    @SubscribeEvent
    public static void registerRenderStateModifier(
            RegisterRenderStateModifiersEvent event
    ) {

        event.registerAvatarEntityModifier(
                new AvatarRenderStateModifier() {

                    @Override
                    public <T extends Avatar & ClientAvatarEntity>
                    void accept(
                            T avatar,
                            AvatarRenderState state
                    ) {

                        // ------------------------------------------------
                        // Check the client's synchronized Invisibility.
                        // ------------------------------------------------

                        MobEffectInstance invisibility =
                                avatar.getEffect(
                                        MobEffects.INVISIBILITY
                                );


                        boolean nullSilence =
                                invisibility != null
                                        &&
                                        invisibility.getAmplifier()
                                                ==
                                                1
                                        &&
                                        invisibility.getDuration()
                                                <=
                                                20;


                        // ------------------------------------------------
                        // Store our custom render-state marker.
                        // ------------------------------------------------

                        state.setRenderData(
                                NULL_SILENCE,
                                nullSilence
                        );
                    }
                }
        );
    }


    // ============================================================
    // CANCEL PLAYER RENDER
    // ============================================================

    @SubscribeEvent
    public static void onPlayerRender(
            RenderPlayerEvent.Pre event
    ) {

        boolean nullSilence =
                event.getRenderState()
                        .getRenderDataOrDefault(
                                NULL_SILENCE,
                                false
                        );


        // --------------------------------------------------------
        // ONLY Null's Silence cancels the complete render.
        // --------------------------------------------------------

        if (nullSilence) {

            event.setCanceled(true);
        }
    }
}