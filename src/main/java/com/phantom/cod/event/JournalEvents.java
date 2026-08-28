package com.phantom.cod.event;

import com.phantom.cod.item.JournalHelper;
import com.phantom.cod.registry.ModItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class JournalEvents {

    private static final String RECEIVED_FIRST_JOURNAL =
            "cod_received_first_journal";

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag persistentData = player.getPersistentData();

        if (persistentData.getBoolean(RECEIVED_FIRST_JOURNAL).orElse(false)) {
            return;
        }

        // ==================================================
        // Give Journal I
        // ==================================================

        ItemStack journal = JournalHelper.createFirstRecord();

        player.getInventory().placeItemBackInInventory(journal);

        // ==================================================
        // Give The Core
        // ==================================================

        ItemStack core = new ItemStack(ModItems.THE_CORE.get());

        player.getInventory().placeItemBackInInventory(core);

        // ==================================================
        // Mark as received
        // ==================================================

        persistentData.putBoolean(
                RECEIVED_FIRST_JOURNAL,
                true
        );
    }
}