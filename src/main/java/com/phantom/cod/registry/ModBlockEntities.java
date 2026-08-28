package com.phantom.cod.registry;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.block.PedestalBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    CrownsOfDust.MOD_ID
            );

    public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL =
            BLOCK_ENTITY_TYPES.register(
                    "pedestal",
                    () -> new BlockEntityType<>(
                            PedestalBlockEntity::new,
                            false,
                            ModBlocks.PEDESTAL.get()
                    )
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}