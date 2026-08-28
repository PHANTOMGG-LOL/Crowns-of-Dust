package com.phantom.cod.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DestinyCompassItem extends Item {

    public DestinyCompassItem(Properties properties) {
        super(properties);
    }

    // ==================================================
    // ENCHANTMENT GLINT
    // ==================================================

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    // ==================================================
    // USE
    // ==================================================

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        return InteractionResult.SUCCESS;
    }
}