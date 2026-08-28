package com.phantom.cod.registry;

import com.phantom.cod.CrownsOfDust;

import com.phantom.cod.item.DestinyCompassItem;
import com.phantom.cod.item.HerobrinesLegacyItem;
import com.phantom.cod.item.NullsSilenceItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    // ==================================================
    // Item Registry
    // ==================================================

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(
                    CrownsOfDust.MOD_ID
            );


    // ==================================================
    // Stone of Valor
    // ==================================================

    public static final DeferredItem<Item> STONE_OF_VALOR =
            ITEMS.registerSimpleItem(
                    "stone_of_valor"
            );


    // ==================================================
    // Stone of Wisdom
    // ==================================================

    public static final DeferredItem<Item> STONE_OF_WISDOM =
            ITEMS.registerSimpleItem(
                    "stone_of_wisdom"
            );


    // ==================================================
    // Stone of Unity
    // ==================================================

    public static final DeferredItem<Item> STONE_OF_UNITY =
            ITEMS.registerSimpleItem(
                    "stone_of_unity"
            );


    // ==================================================
    // Stone of Resolve
    // ==================================================

    public static final DeferredItem<Item> STONE_OF_RESOLVE =
            ITEMS.registerSimpleItem(
                    "stone_of_resolve"
            );


    // ==================================================
    // The Core
    // ==================================================

    public static final DeferredItem<Item> THE_CORE =
            ITEMS.registerSimpleItem(
                    "core"
            );


    // ==================================================
    // Pedestal Block Item
    // ==================================================

    public static final DeferredItem<BlockItem> PEDESTAL =
            ITEMS.registerSimpleBlockItem(
                    ModBlocks.PEDESTAL
            );


    // ==================================================
    // Destiny Compass
    // ==================================================

    public static final DeferredItem<DestinyCompassItem> DESTINY_COMPASS =
            ITEMS.registerItem(
                    "destiny_compass",
                    DestinyCompassItem::new
            );


    // ==================================================
    // Herobrine's Legacy
    // ==================================================
    //
    // Netherite-style sword combat:
    //
    // Attack Damage:
    //     +8 total
    //
    // Attack Speed:
    //     1.6 attacks/second
    //
    // Durability:
    //     10,000
    //
    // The actual abilities remain inside
    // HerobrinesLegacyItem.
    // ==================================================

    public static final DeferredItem<HerobrinesLegacyItem> HEROBRINES_LEGACY =
            ITEMS.registerItem(
                    "herobrine_legacy",
                    HerobrinesLegacyItem::new,
                    props -> props
                            .sword(
                                    // Sword material.
                                    //
                                    // The material's own damage contribution
                                    // is combined with this sword's bonus.
                                    //
                                    // We use the vanilla Netherite material
                                    // so the weapon behaves like a
                                    // Netherite sword.
                                    net.minecraft.world.item.ToolMaterial.NETHERITE,

                                    // Sword attack damage bonus.
                                    3,

                                    // Attack speed modifier.
                                    -2.4F
                            )
                            .durability(10_000)
            );


    // ==================================================
    // Null's Silence
    // ==================================================
    //
    // Same Netherite-style combat statistics.
    //
    // Attack Damage:
    //     +8 total
    //
    // Attack Speed:
    //     1.6 attacks/second
    //
    // Durability:
    //     10,000
    // ==================================================

    public static final DeferredItem<NullsSilenceItem> NULLS_SILENCE =
            ITEMS.registerItem(
                    "null_silence",
                    NullsSilenceItem::new,
                    props -> props
                            .sword(
                                    net.minecraft.world.item.ToolMaterial.NETHERITE,

                                    3,

                                    -2.4F
                            )
                            .durability(10_000)
            );


    // ==================================================
    // Register
    // ==================================================

    public static void register(IEventBus eventBus) {

        ITEMS.register(eventBus);
    }
}