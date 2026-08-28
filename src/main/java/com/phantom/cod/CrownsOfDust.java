package com.phantom.cod;

import com.mojang.logging.LogUtils;
import com.phantom.cod.event.DestinyCompassEvents;
import com.phantom.cod.event.JournalEvents;
import com.phantom.cod.event.PedestalBossEvents;
import com.phantom.cod.network.ModNetwork;
import com.phantom.cod.registry.*;
import com.phantom.cod.worldgen.StructurePedestalHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

import org.slf4j.Logger;

@Mod(CrownsOfDust.MOD_ID)
public class CrownsOfDust {

    public static final String MOD_ID = "cod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CrownsOfDust(IEventBus eventBus, ModContainer modContainer) {

        // =====================================================
        // MOD REGISTRIES
        // =====================================================

        ModEntities.register(eventBus);

        ModSounds.register(eventBus);

        ModItems.register(eventBus);

        ModBlocks.register(eventBus);

        ModBlockEntities.register(eventBus);

        ModNetwork.register(eventBus);


        // =====================================================
        // GAME EVENT BUS
        // =====================================================

        NeoForge.EVENT_BUS.register(StructurePedestalHandler.class);

        NeoForge.EVENT_BUS.register(PedestalBossEvents.class);

        NeoForge.EVENT_BUS.register(DestinyCompassEvents.class);

        NeoForge.EVENT_BUS.register(JournalEvents.class);
    }
}