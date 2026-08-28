package com.phantom.cod.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.registry.ModBlockEntities;
import com.phantom.cod.registry.ModEntities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = CrownsOfDust.MOD_ID,
        value = Dist.CLIENT
)
public class CrownsOfDustClient {

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event) {

        // ==================================================
        // Entity Renderer Registration
        // ==================================================

        // The King's Will renderer.
        event.registerEntityRenderer(
                ModEntities.KINGS_WILL.get(),
                context -> new GeoEntityRenderer<>(
                        context,
                        ModEntities.KINGS_WILL.get()
                )
        );

        // The All Seeing Eye renderer.
        event.registerEntityRenderer(
                ModEntities.ALL_SEEING_EYE.get(),
                context -> new GeoEntityRenderer<>(
                        context,
                        ModEntities.ALL_SEEING_EYE.get()
                )
        );

        // The Oathbound renderer.
        event.registerEntityRenderer(
                ModEntities.OATHBOUND.get(),
                context -> new GeoEntityRenderer<>(
                        context,
                        ModEntities.OATHBOUND.get()
                )
        );

        // The Last Monarch renderer.
        event.registerEntityRenderer(
                ModEntities.LAST_MONARCH.get(),
                context -> new GeoEntityRenderer<>(
                        context,
                        ModEntities.LAST_MONARCH.get()
                )
        );

        // Herobrine renderer.
        event.registerEntityRenderer(
                ModEntities.HEROBRINE.get(),
                context -> new GeoEntityRenderer<>(
                        context,
                        ModEntities.HEROBRINE.get()
                )
        );

        // Null renderer.
        event.registerEntityRenderer(
                ModEntities.NULL.get(),
                context -> new GeoEntityRenderer<>(
                        context,
                        ModEntities.NULL.get()
                )
        );

        // ==================================================
        // Block Entity Renderer Registration
        // ==================================================

        // Pedestal renderer.
        event.registerBlockEntityRenderer(
                ModBlockEntities.PEDESTAL.get(),
                PedestalRenderer::new
        );
    }
}