package com.phantom.cod.registry;

import com.phantom.cod.CrownsOfDust;

import com.phantom.cod.entity.ModEntityAttributes;

import com.phantom.cod.entity.kingswill.KingsWillEntity;
import com.phantom.cod.entity.allseeingeye.AllSeeingEyeEntity;
import com.phantom.cod.entity.oathbound.OathboundEntity;
import com.phantom.cod.entity.lastmonarch.LastMonarchEntity;

import com.phantom.cod.entity.herobrine.HerobrineEntity;
import com.phantom.cod.entity.nullentity.NullEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModEntities {

    // ==================================================
    // Entity Registry
    // ==================================================

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.ENTITY_TYPE,
                    CrownsOfDust.MOD_ID
            );


    // ==================================================
    // The King's Will
    // ==================================================

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<KingsWillEntity>
            > KINGS_WILL = ENTITY_TYPES.register(
            "kings_will",
            location ->
                    EntityType.Builder.of(
                                    KingsWillEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.8F, 2.8F)
                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,
                                            location
                                    )
                            )
    );


    // ==================================================
    // The All Seeing Eye
    // ==================================================

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<AllSeeingEyeEntity>
            > ALL_SEEING_EYE = ENTITY_TYPES.register(
            "all_seeing_eye",
            location ->
                    EntityType.Builder.of(
                                    AllSeeingEyeEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(1.0F, 1.0F)
                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,
                                            location
                                    )
                            )
    );


    // ==================================================
    // The Oathbound
    // ==================================================

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<OathboundEntity>
            > OATHBOUND = ENTITY_TYPES.register(
            "oathbound",
            location ->
                    EntityType.Builder.of(
                                    OathboundEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.8F, 2.8F)
                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,
                                            location
                                    )
                            )
    );


    // ==================================================
    // The Last Monarch
    // ==================================================

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<LastMonarchEntity>
            > LAST_MONARCH = ENTITY_TYPES.register(
            "last_monarch",
            location ->
                    EntityType.Builder.of(
                                    LastMonarchEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.8F, 2.8F)
                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,
                                            location
                                    )
                            )
    );


    // ==================================================
    // Herobrine
    // ==================================================

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<HerobrineEntity>
            > HEROBRINE = ENTITY_TYPES.register(
            "herobrine",
            location ->
                    EntityType.Builder.of(
                                    HerobrineEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.6F, 1.95F)
                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,
                                            location
                                    )
                            )
    );


    // ==================================================
    // Null
    // ==================================================

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<NullEntity>
            > NULL = ENTITY_TYPES.register(
            "null",
            location ->
                    EntityType.Builder.of(
                                    NullEntity::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.6F, 1.95F)
                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,
                                            location
                                    )
                            )
    );


    // ==================================================
    // Entity Attributes
    // ==================================================

    public static void registerAttributes(
            EntityAttributeCreationEvent event) {

        // The King's Will
        event.put(
                KINGS_WILL.get(),
                ModEntityAttributes.kingsWill().build()
        );

        // The All Seeing Eye
        event.put(
                ALL_SEEING_EYE.get(),
                ModEntityAttributes.allSeeingEye().build()
        );

        // The Oathbound
        event.put(
                OATHBOUND.get(),
                ModEntityAttributes.oathbound().build()
        );

        // The Last Monarch
        event.put(
                LAST_MONARCH.get(),
                ModEntityAttributes.lastMonarch().build()
        );
    }


    // ==================================================
    // Register Entities
    // ==================================================

    public static void register(IEventBus eventBus) {

        ENTITY_TYPES.register(eventBus);
    }
}