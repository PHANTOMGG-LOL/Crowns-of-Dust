package com.phantom.cod.event;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


/**
 * ============================================================
 * CUSTOM WEAPON INVENTORY PROTECTION
 * ============================================================
 *
 * The weapon selected in the Choice Room belongs to the player
 * until ChoiceRoomManager explicitly clears the ownership.
 *
 * Ownership is stored in Player persistent data.
 *
 * DEATH:
 * - Capture the exact ItemStack before death cleanup.
 * - Restore that exact ItemStack after respawn.
 * - Durability is preserved.
 * - Enchantments are preserved.
 * - Components are preserved.
 *
 * BREAK:
 * - Detect the actual destruction event.
 * - Clear weapon ownership.
 * - Allow the player to choose another weapon.
 *
 * IMPORTANT:
 * - No fresh weapon is generated every tick.
 * - A broken weapon is NOT regenerated.
 */
@EventBusSubscriber(modid = CrownsOfDust.MOD_ID)
public final class CustomWeaponInventoryEvents {

    private CustomWeaponInventoryEvents() {
    }


    // ============================================================
    // PLAYER DATA
    // ============================================================

    private static final String TAG_CHOSEN_WEAPON =
            "cod_chosen_weapon";

    private static final String TAG_DEATH_WEAPON =
            "cod_death_weapon";

    /*
     * This is the marker used by ChoiceRoomManager.
     *
     * If the weapon breaks, we remove this too so the player
     * can immediately make a new choice.
     */
    private static final String TAG_LAST_CHOICE_WEAPON =
            "cod_last_choice_weapon";

    private static final String HEROBRINE =
            "herobrines_legacy";

    private static final String NULL =
            "null_silence";


    // ============================================================
    // LOGIN
    // ============================================================

    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        /*
         * If the player already has their chosen weapon,
         * nothing needs to happen.
         */
        if (!getChosenWeapon(player).isEmpty()
                && hasChosenWeapon(player)) {

            return;
        }

        /*
         * Do NOT generate a new weapon here.
         *
         * If the weapon broke, ownership was already cleared.
         */
        restoreDeathWeapon(player);
    }


    // ============================================================
    // CAPTURE WEAPON BEFORE DEATH
    // ============================================================

    @SubscribeEvent
    public static void onPlayerDeath(
            LivingDeathEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String chosen =
                getChosenWeapon(player);

        if (chosen.isEmpty()) {
            return;
        }

        /*
         * Find the actual weapon currently belonging
         * to the player.
         */
        ItemStack weapon =
                findWeapon(
                        player,
                        chosen
                );

        if (weapon.isEmpty()) {
            return;
        }

        /*
         * Encode the EXACT ItemStack.
         *
         * This preserves:
         *
         * - durability
         * - enchantments
         * - count
         * - components
         * - custom ItemStack data
         */
        Tag encoded =
                ItemStack.CODEC
                        .encodeStart(
                                player.level()
                                        .registryAccess()
                                        .createSerializationContext(
                                                NbtOps.INSTANCE
                                        ),
                                weapon
                        )
                        .getOrThrow();

        if (encoded instanceof CompoundTag compound) {

            player.getPersistentData()
                    .put(
                            TAG_DEATH_WEAPON,
                            compound
                    );
        }
    }


    // ============================================================
    // DEATH / RESPAWN
    // ============================================================

    @SubscribeEvent
    public static void onPlayerClone(
            PlayerEvent.Clone event
    ) {

        /*
         * We only care about actual death.
         *
         * Dimension changes should NOT duplicate weapons.
         */
        if (!event.isWasDeath()) {
            return;
        }

        Player oldPlayer =
                event.getOriginal();

        Player newPlayer =
                event.getEntity();

        String chosen =
                getChosenWeapon(oldPlayer);

        if (chosen.isEmpty()) {
            return;
        }

        /*
         * Ownership survives death.
         */
        setChosenWeapon(
                newPlayer,
                chosen
        );


        // ========================================================
        // VANILLA ALREADY KEPT THE WEAPON
        // ========================================================

        if (hasChosenWeapon(newPlayer)) {

            oldPlayer.getPersistentData()
                    .remove(
                            TAG_DEATH_WEAPON
                    );

            return;
        }


        // ========================================================
        // RESTORE EXACT DEATH SNAPSHOT
        // ========================================================

        CompoundTag savedWeapon =
                oldPlayer.getPersistentData()
                        .getCompoundOrEmpty(
                                TAG_DEATH_WEAPON
                        );

        if (!savedWeapon.isEmpty()) {

            ItemStack restored =
                    ItemStack.CODEC
                            .parse(
                                    newPlayer.level()
                                            .registryAccess()
                                            .createSerializationContext(
                                                    NbtOps.INSTANCE
                                            ),
                                    savedWeapon
                            )
                            .result()
                            .orElse(
                                    ItemStack.EMPTY
                            );

            if (!restored.isEmpty()) {

                newPlayer.getInventory()
                        .add(
                                restored
                        );

                newPlayer.getInventory()
                        .setChanged();

                oldPlayer.getPersistentData()
                        .remove(
                                TAG_DEATH_WEAPON
                        );

                return;
            }
        }


        // ========================================================
        // FALLBACK
        // ========================================================
        //
        // If the serialized snapshot could not be decoded,
        // try to copy the original stack directly.
        //

        ItemStack oldWeapon =
                findWeapon(
                        oldPlayer,
                        chosen
                );

        if (!oldWeapon.isEmpty()) {

            newPlayer.getInventory()
                    .add(
                            oldWeapon.copy()
                    );

            newPlayer.getInventory()
                    .setChanged();
        }

        oldPlayer.getPersistentData()
                .remove(
                        TAG_DEATH_WEAPON
                );
    }


    // ============================================================
    // ACTUAL WEAPON BREAK
    // ============================================================

    @SubscribeEvent
    public static void onPlayerDestroyItem(
            PlayerDestroyItemEvent event
    ) {

        Player player =
                event.getEntity();

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        String chosen =
                getChosenWeapon(
                        serverPlayer
                );

        if (chosen.isEmpty()) {
            return;
        }

        /*
         * This is the exact ItemStack from immediately
         * before it was destroyed.
         */
        ItemStack destroyed =
                event.getOriginal();

        if (!isChosenWeapon(
                serverPlayer,
                destroyed
        )) {

            return;
        }


        // ========================================================
        // WEAPON IS ACTUALLY BROKEN
        // ========================================================

        /*
         * DO NOT regenerate it.
         *
         * The player is now free to choose another weapon.
         */
        clearChosenWeaponOwnership(
                serverPlayer
        );


        /*
         * Also clear Choice Room's "already chosen" marker.
         *
         * This means the player can go directly back to the
         * Choice Room and choose again.
         */
        serverPlayer.getPersistentData()
                .remove(
                        TAG_LAST_CHOICE_WEAPON
                );


        serverPlayer.sendSystemMessage(
                Component.literal(
                        "Your weapon has broken. "
                                + "You may choose a new weapon."
                ).withStyle(style -> style
                        .withColor(ChatFormatting.LIGHT_PURPLE)
                        .withBold(true))
        );
    }


    // ============================================================
    // PREVENT DROPPING
    // ============================================================

    @SubscribeEvent
    public static void onItemToss(
            ItemTossEvent event
    ) {

        ItemStack stack =
                event.getEntity()
                        .getItem();

        Player player =
                event.getPlayer();

        if (!isChosenWeapon(
                player,
                stack
        )) {

            return;
        }


        /*
         * Put it back before cancelling the toss.
         */
        player.getInventory()
                .add(
                        stack.copy()
                );

        player.getInventory()
                .setChanged();

        event.setCanceled(true);


        if (player instanceof ServerPlayer serverPlayer) {

            serverPlayer.sendSystemMessage(
                    Component.literal(
                            "This weapon has chosen you. "
                                    + "You cannot throw it away."
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.DARK_RED)
                            .withBold(true))
            );
        }
    }


    // ============================================================
    // PREVENT CONTAINER INSERTION
    // ============================================================

    @SubscribeEvent
    public static void onItemStackedOnOther(
            ItemStackedOnOtherEvent event
    ) {

        Player player =
                event.getPlayer();

        if (
                isChosenWeapon(
                        player,
                        event.getCarriedItem()
                )
                        ||
                        isChosenWeapon(
                                player,
                                event.getStackedOnItem()
                        )
        ) {

            event.setCanceled(true);
        }
    }


    // ============================================================
    // RECOVER FROM EXTERNAL MENUS
    // ============================================================

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {

        Player player =
                event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }


        /*
         * No weapon ownership = nothing to protect.
         */
        if (getChosenWeapon(player).isEmpty()) {
            return;
        }


        /*
         * We do NOT regenerate missing weapons here.
         *
         * If the weapon broke, PlayerDestroyItemEvent already
         * cleared ownership.
         *
         * This code only handles a weapon being placed inside
         * an external container/menu.
         */
        var menu =
                player.containerMenu;


        for (
                int slotIndex = 0;
                slotIndex < menu.slots.size();
                slotIndex++
        ) {

            var slot =
                    menu.getSlot(
                            slotIndex
                    );

            ItemStack stack =
                    slot.getItem();

            if (!isChosenWeapon(
                    player,
                    stack
            )) {

                continue;
            }


            /*
             * Do not touch the player's normal inventory slots.
             */
            if (
                    slot.container
                            == player.getInventory()
            ) {

                continue;
            }


            ItemStack weapon =
                    stack.copy();

            slot.set(
                    ItemStack.EMPTY
            );

            player.getInventory()
                    .add(
                            weapon
                    );

            player.getInventory()
                    .setChanged();

            menu.broadcastChanges();


            if (player instanceof ServerPlayer serverPlayer) {

                serverPlayer.sendSystemMessage(
                        Component.literal(
                                "This weapon cannot be stored "
                                        + "outside your inventory."
                        ).withStyle(style -> style
                                .withColor(ChatFormatting.DARK_RED)
                                .withBold(true))
                );
            }

            return;
        }
    }


    // ============================================================
    // CHOICE ROOM API
    // ============================================================

    public static void markWeaponChosen(
            ServerPlayer player,
            ItemStack weapon
    ) {

        if (
                weapon.is(
                        ModItems.HEROBRINES_LEGACY.get()
                )
        ) {

            setChosenWeapon(
                    player,
                    HEROBRINE
            );

        } else if (
                weapon.is(
                        ModItems.NULLS_SILENCE.get()
                )
        ) {

            setChosenWeapon(
                    player,
                    NULL
            );
        }


        /*
         * A newly selected weapon must not inherit an old
         * death snapshot.
         */
        player.getPersistentData()
                .remove(
                        TAG_DEATH_WEAPON
                );
    }


    /**
     * Explicitly clears ownership.
     *
     * ChoiceRoomManager calls this when the player is allowed
     * to choose a new weapon.
     */
    public static void clearChosenWeapon(
            ServerPlayer player
    ) {

        clearChosenWeaponOwnership(
                player
        );


        removeAllCopies(
                player,
                ModItems.HEROBRINES_LEGACY.get()
        );


        removeAllCopies(
                player,
                ModItems.NULLS_SILENCE.get()
        );
    }


    // ============================================================
    // CLEAR OWNERSHIP ONLY
    // ============================================================

    private static void clearChosenWeaponOwnership(
            ServerPlayer player
    ) {

        setChosenWeapon(
                player,
                ""
        );

        player.getPersistentData()
                .remove(
                        TAG_DEATH_WEAPON
                );
    }


    // ============================================================
    // RESTORE DEATH SNAPSHOT
    // ============================================================

    private static void restoreDeathWeapon(
            ServerPlayer player
    ) {

        String chosen =
                getChosenWeapon(
                        player
                );

        if (chosen.isEmpty()) {
            return;
        }


        /*
         * Already restored.
         */
        if (hasChosenWeapon(player)) {
            return;
        }


        CompoundTag savedWeapon =
                player.getPersistentData()
                        .getCompoundOrEmpty(
                                TAG_DEATH_WEAPON
                        );

        if (savedWeapon.isEmpty()) {
            return;
        }


        ItemStack restored =
                ItemStack.CODEC
                        .parse(
                                player.level()
                                        .registryAccess()
                                        .createSerializationContext(
                                                NbtOps.INSTANCE
                                        ),
                                savedWeapon
                        )
                        .result()
                        .orElse(
                                ItemStack.EMPTY
                        );

        if (restored.isEmpty()) {
            return;
        }


        player.getInventory()
                .add(
                        restored
                );

        player.getInventory()
                .setChanged();


        player.getPersistentData()
                .remove(
                        TAG_DEATH_WEAPON
                );
    }


    // ============================================================
    // INVENTORY HELPERS
    // ============================================================

    private static boolean isChosenWeapon(
            Player player,
            ItemStack stack
    ) {

        if (stack.isEmpty()) {
            return false;
        }


        String chosen =
                getChosenWeapon(
                        player
                );


        if (
                HEROBRINE.equals(chosen)
        ) {

            return stack.is(
                    ModItems.HEROBRINES_LEGACY.get()
            );
        }


        if (
                NULL.equals(chosen)
        ) {

            return stack.is(
                    ModItems.NULLS_SILENCE.get()
            );
        }


        return false;
    }


    private static boolean hasChosenWeapon(
            Player player
    ) {

        return !findWeapon(
                player,
                getChosenWeapon(
                        player
                )
        ).isEmpty();
    }


    private static ItemStack findWeapon(
            Player player,
            String weapon
    ) {

        /*
         * Normal inventory.
         */
        for (
                ItemStack stack :
                player.getInventory()
                        .getNonEquipmentItems()
        ) {

            if (
                    HEROBRINE.equals(weapon)
                            &&
                            stack.is(
                                    ModItems.HEROBRINES_LEGACY.get()
                            )
            ) {

                return stack;
            }


            if (
                    NULL.equals(weapon)
                            &&
                            stack.is(
                                    ModItems.NULLS_SILENCE.get()
                            )
            ) {

                return stack;
            }
        }


        /*
         * Offhand.
         */
        ItemStack offhand =
                player.getOffhandItem();


        if (
                HEROBRINE.equals(weapon)
                        &&
                        offhand.is(
                                ModItems.HEROBRINES_LEGACY.get()
                        )
        ) {

            return offhand;
        }


        if (
                NULL.equals(weapon)
                        &&
                        offhand.is(
                                ModItems.NULLS_SILENCE.get()
                        )
        ) {

            return offhand;
        }


        return ItemStack.EMPTY;
    }


    private static void removeAllCopies(
            ServerPlayer player,
            Item item
    ) {

        var inventory =
                player.getInventory();


        for (
                int slot = 0;
                slot < inventory.getContainerSize();
                slot++
        ) {

            if (
                    inventory.getItem(slot)
                            .is(item)
            ) {

                inventory.setItem(
                        slot,
                        ItemStack.EMPTY
                );
            }
        }


        inventory.setChanged();
    }


    // ============================================================
    // PLAYER PERSISTENT DATA
    // ============================================================

    private static String getChosenWeapon(
            Player player
    ) {

        return player.getPersistentData()
                .getStringOr(
                        TAG_CHOSEN_WEAPON,
                        ""
                );
    }


    private static void setChosenWeapon(
            Player player,
            String weapon
    ) {

        player.getPersistentData()
                .putString(
                        TAG_CHOSEN_WEAPON,
                        weapon
                );
    }
}