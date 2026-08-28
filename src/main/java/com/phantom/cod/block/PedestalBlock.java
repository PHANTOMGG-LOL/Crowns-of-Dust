package com.phantom.cod.block;

import com.mojang.serialization.MapCodec;
import com.phantom.cod.registry.ModBlockEntities;
import com.phantom.cod.registry.ModItems;

import com.phantom.cod.temple.ChoiceRoomManager;
import com.phantom.cod.temple.TemplePedestalLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PedestalBlock extends BaseEntityBlock {

    public static final MapCodec<PedestalBlock> CODEC =
            simpleCodec(PedestalBlock::new);

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    // ==================================================
    // CODEC
    // ==================================================

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ==================================================
    // BLOCK ENTITY
    // ==================================================

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state) {

        return new PedestalBlockEntity(pos, state);
    }

    // ==================================================
    // TICKER
    // ==================================================

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type) {

        return createTickerHelper(
                type,
                ModBlockEntities.PEDESTAL.get(),
                PedestalBlockEntity::tick
        );
    }

    // ==================================================
    // RIGHT CLICK
    // ==================================================

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {

        // Get the pedestal BlockEntity.
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof PedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }

        // ==================================================
        // TEMPLE PEDESTAL
        // ==================================================

        if (pedestal.isTemplePedestal()) {

            if (!level.isClientSide()) {

                TemplePedestalLogic.handleStone(
                        (net.minecraft.server.level.ServerLevel) level,
                        pedestal,
                        player,
                        stack
                );
            }

            return InteractionResult.SUCCESS;
        }

        // ==================================================
        // CHOICE ROOM WEAPON PEDESTAL
        // ==================================================

        if (pedestal.hasChoiceWeapon()) {

            if (!level.isClientSide()) {

                ChoiceRoomManager.handleWeaponChoice(
                        (ServerLevel) level,
                        pedestal,
                        (ServerPlayer) player
                );
            }

            return InteractionResult.SUCCESS;
        }

        // ==================================================
        // COOLDOWN
        // ==================================================

        if (pedestal.isOnCooldown()) {

            if (!level.isClientSide()) {

                long remaining =
                        pedestal.getRemainingCooldown();

                long totalSeconds =
                        remaining / 20L;

                long minutes =
                        totalSeconds / 60L;

                long seconds =
                        totalSeconds % 60L;

                String time =
                        String.format(
                                "%02d:%02d",
                                minutes,
                                seconds
                        );

                player.sendSystemMessage(
                        Component.literal(
                                "The arena sleeps once more. Return in: " + time
                        ).withStyle(style -> style
                                        .withColor(ChatFormatting.GOLD)
                                        .withBold(true))
                );
            }

            return InteractionResult.SUCCESS;
        }

        // ==================================================
        // ALREADY ACTIVATED
        // ==================================================

        if (pedestal.isCoreInserted()) {

            if (!level.isClientSide()) {
                player.sendSystemMessage(
                        Component.literal(
                                "The Core has already been awakened."
                        ).withStyle(style -> style
                                .withColor(ChatFormatting.YELLOW)
                                .withBold(true))
                );
            }

            return InteractionResult.SUCCESS;
        }

        // ==================================================
        // CHECK CORE
        // ==================================================

        if (!stack.is(ModItems.THE_CORE.get())) {

            if (!level.isClientSide()) {
                player.sendSystemMessage(
                        Component.literal(
                                "The pedestal requires a Core."
                        ).withStyle(style -> style
                                .withColor(ChatFormatting.AQUA)
                                .withBold(true))
                );
            }

            return InteractionResult.SUCCESS;
        }

        // ==================================================
        // ACTIVATE PEDESTAL
        // ==================================================

        if (!level.isClientSide()) {

            // ==============================================
            // HISTORIC PLACE CHECK
            // ==============================================

            if (!pedestal.isInHistoricPlace()) {

                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(
                                "The pedestal is worthless here. "
                                        + "Its power belongs to its historic place."
                        ).withStyle(style -> style
                                .withColor(ChatFormatting.GOLD)
                                .withBold(true))
                );

                return InteractionResult.SUCCESS;
            }


            // ==============================================
            // CONSUME CORE
            // ==============================================

            if (!player.isCreative()) {
                stack.shrink(1);
            }


            // ==============================================
            // START ACTIVATION
            // ==============================================

            pedestal.activate();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random) {

        // Spawn multiple particles around the pedestal.
        for (int i = 0; i < 2; i++) {

            double centerX = pos.getX() + 0.5D;
            double centerY = pos.getY() + 1.0D;
            double centerZ = pos.getZ() + 0.5D;

            // Random angle around the pedestal.
            double angle =
                    random.nextDouble() * Math.PI * 2.0D;

            // Keep particles slightly outside the pedestal.
            double radius =
                    0.45D + random.nextDouble() * 0.25D;

            double x =
                    centerX + Math.cos(angle) * radius;

            double z =
                    centerZ + Math.sin(angle) * radius;

            // Height around the pedestal.
            double y =
                    centerY + random.nextDouble() * 1.0D;

            level.addParticle(
                    ParticleTypes.END_ROD,
                    x,
                    y,
                    z,

                    // Slight horizontal movement.
                    (random.nextDouble() - 0.5D) * 0.01D,

                    // Slowly rise.
                    0.015D + random.nextDouble() * 0.02D,

                    (random.nextDouble() - 0.5D) * 0.01D
            );
        }
    }
}