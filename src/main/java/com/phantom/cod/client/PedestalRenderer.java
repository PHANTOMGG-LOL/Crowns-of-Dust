package com.phantom.cod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import com.phantom.cod.block.PedestalBlockEntity;
import com.phantom.cod.registry.ModItems;
import com.phantom.cod.temple.TempleStone;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PedestalRenderer
        implements BlockEntityRenderer<
        PedestalBlockEntity,
        PedestalRenderer.State> {

    private final ItemModelResolver itemModelResolver;


    public PedestalRenderer(
            BlockEntityRendererProvider.Context context) {

        this.itemModelResolver =
                context.itemModelResolver();
    }


    // ==================================================
    // RENDER STATE
    // ==================================================

    public static class State
            extends BlockEntityRenderState {

        // ----------------------------------------------
        // CORE
        // ----------------------------------------------

        public boolean coreInserted;

        public final ItemStackRenderState core =
                new ItemStackRenderState();


        // ----------------------------------------------
        // TEMPLE STONE
        // ----------------------------------------------

        public TempleStone templeStone =
                TempleStone.NONE;

        public final ItemStackRenderState stone =
                new ItemStackRenderState();


        // ----------------------------------------------
        // CHOICE ROOM WEAPON
        // ----------------------------------------------

        public PedestalBlockEntity.ChoiceWeapon
                choiceWeapon =
                PedestalBlockEntity.ChoiceWeapon.NONE;

        public final ItemStackRenderState weapon =
                new ItemStackRenderState();


        // ----------------------------------------------
        // ROTATION
        // ----------------------------------------------

        public float rotation;
    }


    @Override
    public State createRenderState() {

        return new State();
    }


    // ==================================================
    // EXTRACT RENDER STATE
    // ==================================================

    @Override
    public void extractRenderState(
            PedestalBlockEntity blockEntity,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {

        BlockEntityRenderer.super.extractRenderState(
                blockEntity,
                state,
                partialTick,
                cameraPosition,
                crumblingOverlay
        );


        // ==================================================
        // CORE
        // ==================================================

        state.coreInserted =
                blockEntity.isCoreInserted();


        // ==================================================
        // TEMPLE STONE
        // ==================================================

        state.templeStone =
                blockEntity.getTempleStone();


        // ==================================================
        // CHOICE ROOM WEAPON
        // ==================================================

        state.choiceWeapon =
                blockEntity.getChoiceWeapon();


        // ==================================================
        // CLIENT LEVEL
        // ==================================================

        if (!(blockEntity.getLevel()
                instanceof ClientLevel level)) {

            return;
        }


        // ==================================================
        // CORE
        // ==================================================

        if (state.coreInserted) {

            ItemStack coreStack =
                    new ItemStack(
                            ModItems.THE_CORE.get()
                    );

            itemModelResolver.updateForTopItem(
                    state.core,
                    coreStack,
                    ItemDisplayContext.FIXED,
                    level,
                    null,
                    0
            );
        }


        // ==================================================
        // TEMPLE STONE
        // ==================================================

        if (state.templeStone !=
                TempleStone.NONE) {

            ItemStack stoneStack =
                    createStoneStack(
                            state.templeStone
                    );

            if (!stoneStack.isEmpty()) {

                itemModelResolver.updateForTopItem(
                        state.stone,
                        stoneStack,
                        ItemDisplayContext.FIXED,
                        level,
                        null,
                        0
                );
            }
        }


        // ==================================================
        // CHOICE ROOM WEAPON
        // ==================================================

        if (state.choiceWeapon !=
                PedestalBlockEntity.ChoiceWeapon.NONE) {

            ItemStack weaponStack =
                    createWeaponStack(
                            state.choiceWeapon
                    );

            if (!weaponStack.isEmpty()) {

                itemModelResolver.updateForTopItem(
                        state.weapon,
                        weaponStack,
                        ItemDisplayContext.FIXED,
                        level,
                        null,
                        0
                );
            }
        }


        // ==================================================
        // ROTATION
        // ==================================================

        state.rotation =
                ((level.getGameTime()
                        + partialTick)
                        * 4.0F)
                        % 360.0F;
    }


    // ==================================================
    // CREATE STONE STACK
    // ==================================================

    private static ItemStack createStoneStack(
            TempleStone stone) {

        return switch (stone) {

            case VALOR ->
                    new ItemStack(
                            ModItems.STONE_OF_VALOR.get()
                    );

            case WISDOM ->
                    new ItemStack(
                            ModItems.STONE_OF_WISDOM.get()
                    );

            case UNITY ->
                    new ItemStack(
                            ModItems.STONE_OF_UNITY.get()
                    );

            case RESOLVE ->
                    new ItemStack(
                            ModItems.STONE_OF_RESOLVE.get()
                    );

            case NONE ->
                    ItemStack.EMPTY;
        };
    }


    // ==================================================
    // CREATE WEAPON STACK
    // ==================================================

    private static ItemStack createWeaponStack(
            PedestalBlockEntity.ChoiceWeapon weapon) {

        return switch (weapon) {

            case DIAMOND_SWORD ->
                    new ItemStack(
                            ModItems.HEROBRINES_LEGACY.get()
                    );

            case NETHERITE_SWORD ->
                    new ItemStack(
                            ModItems.NULLS_SILENCE.get()
                    );

            case NONE ->
                    ItemStack.EMPTY;
        };
    }


    // ==================================================
    // RENDER BOUNDING BOX
    // ==================================================

    @Override
    public AABB getRenderBoundingBox(
            PedestalBlockEntity blockEntity) {

        return new AABB(
                blockEntity.getBlockPos()
        ).inflate(2.5D);
    }


    // ==================================================
    // SUBMIT
    // ==================================================

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState) {


        // ==================================================
        // NOTHING TO RENDER
        // ==================================================

        if (!state.coreInserted
                && state.templeStone ==
                TempleStone.NONE
                && state.choiceWeapon ==
                PedestalBlockEntity.ChoiceWeapon.NONE) {

            return;
        }


        // ==================================================
        // CORE
        // ==================================================

        if (state.coreInserted) {

            poseStack.pushPose();

            poseStack.translate(
                    0.5D,
                    1.35D,
                    0.5D
            );

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            state.rotation
                    )
            );

            poseStack.scale(
                    0.75F,
                    0.75F,
                    0.75F
            );

            state.core.submit(
                    poseStack,
                    collector,
                    state.lightCoords,
                    0,
                    0
            );

            poseStack.popPose();
        }


        // ==================================================
        // TEMPLE STONE
        // ==================================================

        if (state.templeStone !=
                TempleStone.NONE) {

            poseStack.pushPose();

            poseStack.translate(
                    0.5D,
                    1.35D,
                    0.5D
            );

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            state.rotation
                    )
            );

            poseStack.scale(
                    0.75F,
                    0.75F,
                    0.75F
            );

            state.stone.submit(
                    poseStack,
                    collector,
                    state.lightCoords,
                    0,
                    0
            );

            poseStack.popPose();
        }


        // ==================================================
        // CHOICE ROOM WEAPON
        // ==================================================

        if (state.choiceWeapon !=
                PedestalBlockEntity.ChoiceWeapon.NONE) {

            poseStack.pushPose();


            // ----------------------------------------------
            // POSITION
            // ----------------------------------------------

            poseStack.translate(
                    0.5D,
                    1.55D,
                    0.5D
            );


            // ----------------------------------------------
            // ROTATION
            // ----------------------------------------------

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            state.rotation
                    )
            );


            // ----------------------------------------------
            // 22.5 DEGREE TILT
            // ----------------------------------------------

            poseStack.mulPose(
                    Axis.ZP.rotationDegrees(
                            22.5F
                    )
            );


            // ----------------------------------------------
            // LARGE DISPLAY
            // ----------------------------------------------

            poseStack.scale(
                    1.25F,
                    1.25F,
                    1.25F
            );


            // ----------------------------------------------
            // RENDER WEAPON
            // ----------------------------------------------

            state.weapon.submit(
                    poseStack,
                    collector,
                    state.lightCoords,
                    0,
                    0
            );


            poseStack.popPose();
        }
    }
}