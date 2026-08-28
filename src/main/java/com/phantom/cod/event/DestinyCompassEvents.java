package com.phantom.cod.event;

import com.phantom.cod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

public final class DestinyCompassEvents {

    private DestinyCompassEvents() {
    }

    // ==================================================
    // PLAYER LOGIN
    // ==================================================

    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {

        Player player = event.getEntity();

        // Already has one
        if (hasDestinyCompass(player)) {
            return;
        }

        // Give one
        player.getInventory().add(
                createDestinyCompass()
        );
    }


    // ==================================================
// PLAYER CLONE / DEATH
// ==================================================

    @SubscribeEvent
    public static void onPlayerClone(
            PlayerEvent.Clone event
    ) {

        // --------------------------------------------------
        // Only handle actual death.
        // --------------------------------------------------

        if (!event.isWasDeath()) {
            return;
        }

        Player oldPlayer =
                event.getOriginal();

        Player newPlayer =
                event.getEntity();

        // --------------------------------------------------
        // IMPORTANT:
        //
        // Minecraft may have already transferred the
        // player's inventory to the new player.
        //
        // If the new player already has the compass,
        // DO NOT give another one.
        // --------------------------------------------------

        if (hasDestinyCompass(newPlayer)) {
            return;
        }

        // --------------------------------------------------
        // The new player doesn't have one.
        //
        // Try to recover the original compass.
        // --------------------------------------------------

        ItemStack compass =
                findDestinyCompass(oldPlayer);

        if (!compass.isEmpty()) {

            newPlayer.getInventory().add(
                    compass.copy()
            );

            return;
        }

        // --------------------------------------------------
        // Safety fallback:
        //
        // If the original compass is genuinely gone,
        // give the player a fresh one.
        // --------------------------------------------------

        newPlayer.getInventory().add(
                createDestinyCompass()
        );
    }

    // ==================================================
    // PREVENT DROPPING DESTINY COMPASS
    // ==================================================

    @SubscribeEvent
    public static void onItemToss(
            ItemTossEvent event
    ) {

        ItemStack stack =
                event.getEntity().getItem();

        if (!stack.is(
                ModItems.DESTINY_COMPASS.get()
        )) {
            return;
        }

        // --------------------------------------------------
        // Restore compass to inventory
        // --------------------------------------------------

        Player player =
                event.getPlayer();

        player.getInventory().add(
                stack.copy()
        );

        // --------------------------------------------------
        // Prevent dropped item from spawning
        // --------------------------------------------------

        event.setCanceled(true);

        // --------------------------------------------------
        // Message
        // --------------------------------------------------

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {

            serverPlayer.sendSystemMessage(
                    Component.literal(
                            "The compass decides your destiny... "
                                    + "you cannot throw it."
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.DARK_RED)
                            .withBold(true))
            );
        }
    }


    // ==================================================
    // PREVENT PUTTING INTO CONTAINERS
    // ==================================================

    @SubscribeEvent
    public static void onItemStackedOnOther(
            ItemStackedOnOtherEvent event
    ) {

        // --------------------------------------------------
        // Destiny Compass being dragged/clicked
        // --------------------------------------------------

        if (event.getCarriedItem().is(
                ModItems.DESTINY_COMPASS.get()
        )) {

            event.setCanceled(true);
            return;
        }

        // --------------------------------------------------
        // Destiny Compass already inside the slot
        // --------------------------------------------------

        if (event.getStackedOnItem().is(
                ModItems.DESTINY_COMPASS.get()
        )) {

            event.setCanceled(true);
        }
    }

    // ==================================================
    // PREVENT QUICK-MOVING INTO CONTAINERS
    // ==================================================

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {

        Player player =
                event.getEntity();

        // --------------------------------------------------
        // Server side only
        // --------------------------------------------------

        if (player.level().isClientSide()) {
            return;
        }

        // --------------------------------------------------
        // If compass is already in player's inventory,
        // it is where it belongs.
        // --------------------------------------------------

        if (hasDestinyCompass(player)) {
            return;
        }

        // --------------------------------------------------
        // Search open menu for a misplaced compass.
        // --------------------------------------------------

        var menu =
                player.containerMenu;

        for (int slotIndex = 0;
             slotIndex < menu.slots.size();
             slotIndex++) {

            var slot =
                    menu.getSlot(slotIndex);

            ItemStack stack =
                    slot.getItem();

            if (!stack.is(
                    ModItems.DESTINY_COMPASS.get()
            )) {
                continue;
            }

            // --------------------------------------------------
            // We only get here if the player currently has
            // no Destiny Compass in their own inventory.
            //
            // Therefore this compass must have been moved
            // somewhere outside the player's inventory.
            // --------------------------------------------------

            ItemStack compass =
                    stack.copy();

            slot.set(
                    ItemStack.EMPTY
            );

            // --------------------------------------------------
            // Return it to the player.
            // --------------------------------------------------

            player.getInventory().add(
                    compass
            );

            // --------------------------------------------------
            // Synchronize inventory/menu.
            // --------------------------------------------------

            menu.broadcastChanges();

            // --------------------------------------------------
            // Message
            // --------------------------------------------------

            if (player instanceof ServerPlayer serverPlayer) {

                serverPlayer.sendSystemMessage(
                        Component.literal(
                                "The compass decides your destiny... "
                                        + "you cannot store it."
                        ).withStyle(style -> style
                                .withColor(ChatFormatting.GOLD)
                                .withBold(true))
                );
            }

            return;
        }
    }


    // ==================================================
    // CREATE DESTINY COMPASS
    // ==================================================

    private static ItemStack createDestinyCompass() {

        ItemStack compass =
                new ItemStack(
                        ModItems.DESTINY_COMPASS.get()
                );

        // --------------------------------------------------
        // Lore
        // --------------------------------------------------

        compass.set(
                net.minecraft.core.component.DataComponents.LORE,
                new ItemLore(
                        List.of(

                                Component.literal(
                                        "Choose your destiny and go with it."
                                ),

                                Component.literal(
                                        "Right-click to choose your destiny."
                                )

                        )
                )
        );

        return compass;
    }


    // ==================================================
    // FIND DESTINY COMPASS
    // ==================================================

    private static ItemStack findDestinyCompass(
            Player player
    ) {

        // --------------------------------------------------
        // Main inventory + hotbar
        // --------------------------------------------------

        for (ItemStack stack :
                player.getInventory()
                        .getNonEquipmentItems()) {

            if (stack.is(
                    ModItems.DESTINY_COMPASS.get()
            )) {

                return stack;
            }
        }

        // --------------------------------------------------
        // Offhand
        // --------------------------------------------------

        ItemStack offhand =
                player.getOffhandItem();

        if (offhand.is(
                ModItems.DESTINY_COMPASS.get()
        )) {

            return offhand;
        }

        return ItemStack.EMPTY;
    }


    // ==================================================
    // HAS DESTINY COMPASS
    // ==================================================

    private static boolean hasDestinyCompass(
            Player player
    ) {

        return !findDestinyCompass(player)
                .isEmpty();
    }
}