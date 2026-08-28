package com.phantom.cod.temple;

import com.phantom.cod.registry.ModItems;

import net.minecraft.world.item.Item;

public enum TempleStone {

    NONE,

    VALOR,

    WISDOM,

    UNITY,

    RESOLVE;


    // ==================================================
    // ITEM → STONE
    // ==================================================

    public static TempleStone fromItem(Item item) {

        if (item == ModItems.STONE_OF_VALOR.get()) {
            return VALOR;
        }

        if (item == ModItems.STONE_OF_WISDOM.get()) {
            return WISDOM;
        }

        if (item == ModItems.STONE_OF_UNITY.get()) {
            return UNITY;
        }

        if (item == ModItems.STONE_OF_RESOLVE.get()) {
            return RESOLVE;
        }

        return NONE;
    }


    // ==================================================
    // VALID STONE
    // ==================================================

    public boolean isValid() {

        return this != NONE;
    }
}