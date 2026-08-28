package com.phantom.cod.registry;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.block.PedestalBlock;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    // ==================================================
    // Block Registry
    // ==================================================

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(
                    CrownsOfDust.MOD_ID
            );


    // ==================================================
    // Pedestal
    // ==================================================

    public static final DeferredBlock<PedestalBlock> PEDESTAL =
            BLOCKS.registerBlock(
                    "pedestal",
                    PedestalBlock::new,
                    properties -> properties
                            .strength(-1.0F, 3600000.0F)
                            .noLootTable()
                            .noOcclusion()
            );


    // ==================================================
    // Register
    // ==================================================

    public static void register(IEventBus eventBus) {

        BLOCKS.register(eventBus);
    }
}