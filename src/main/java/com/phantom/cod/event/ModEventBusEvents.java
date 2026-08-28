package com.phantom.cod.event;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.entity.ModEntityAttributes;
import com.phantom.cod.registry.ModEntities;

import com.phantom.cod.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = CrownsOfDust.MOD_ID)
public class ModEventBusEvents {

    // ==================================================
    // Entity Attribute Registration
    // ==================================================

    @SubscribeEvent
    public static void registerAttributes(
            EntityAttributeCreationEvent event) {

        event.put(
                ModEntities.KINGS_WILL.get(),
                ModEntityAttributes.kingsWill().build()
        );

        event.put(
                ModEntities.ALL_SEEING_EYE.get(),
                ModEntityAttributes.allSeeingEye().build()
        );

        event.put(
                ModEntities.OATHBOUND.get(),
                ModEntityAttributes.oathbound().build()
        );

        event.put(
                ModEntities.LAST_MONARCH.get(),
                ModEntityAttributes.lastMonarch().build()
        );

        // Herobrine attributes.
        event.put(
                ModEntities.HEROBRINE.get(),
                ModEntityAttributes.herobrine().build()
        );

        // Null attributes.
        event.put(
                ModEntities.NULL.get(),
                ModEntityAttributes.nullEntity().build()
        );
    }


    // ==================================================
    // Recipe Unlock
    // ==================================================

    @SubscribeEvent
    public static void onPlayerJoin(
            PlayerEvent.PlayerLoggedInEvent event) {

        // Only run this on the server.
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Recipes that should automatically be unlocked.
        List<String> recipeIds = List.of(
                "core"
        );

        // Recipes we successfully find.
        List<RecipeHolder<?>> recipesToUnlock =
                new ArrayList<>();

        for (String recipeId : recipeIds) {

            ResourceKey<Recipe<?>> key =
                    ResourceKey.create(
                            Registries.RECIPE,
                            Identifier.fromNamespaceAndPath(
                                    CrownsOfDust.MOD_ID,
                                    recipeId
                            )
                    );

            Optional<RecipeHolder<?>> recipe =
                    player.level()
                            .getServer()
                            .getRecipeManager()
                            .byKey(key);

            recipe.ifPresent(
                    recipesToUnlock::add
            );
        }

        // Unlock the recipes for the player.
        if (!recipesToUnlock.isEmpty()) {

            player.awardRecipes(
                    recipesToUnlock
            );
        }
    }

    // ==================================================
// Creative Inventory
// ==================================================

    @SubscribeEvent
    public static void addCreativeItems(
            BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {

            event.accept(
                    ModItems.PEDESTAL
            );
        }
    }
}