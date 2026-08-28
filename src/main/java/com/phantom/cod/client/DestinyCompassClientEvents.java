package com.phantom.cod.client;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.registry.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(
        modid = CrownsOfDust.MOD_ID,
        value = Dist.CLIENT
)
public class DestinyCompassClientEvents {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {

        if (!event.getItemStack().is(ModItems.DESTINY_COMPASS.get())) {
            return;
        }

        Minecraft.getInstance().gui.setScreen(
                new DestinyCompassScreen()
        );

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}