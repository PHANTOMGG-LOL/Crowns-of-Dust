package com.phantom.cod.temple;

import com.phantom.cod.block.PedestalBlockEntity;
import com.phantom.cod.network.ModNetwork;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TemplePedestalLogic {

    private TemplePedestalLogic() {
    }

    // ==================================================
    // HANDLE STONE
    // ==================================================

    public static boolean handleStone(
            ServerLevel level,
            PedestalBlockEntity pedestal,
            Player player,
            ItemStack stack) {

        // ==================================================
        // CHECK TEMPLE COOLDOWN
        // ==================================================

        if (TempleManager.isOnCooldown(level)) {

            long remaining =
                    TempleManager.getRemainingCooldown(level);

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
                            "The Temple remains silent. "
                                    + "Return in: " + time
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.RED)
                            .withBold(true))
            );

            return true;
        }

        // ==================================================
        // CHECK WHETHER ANOTHER PLAYER OWNS THE RITUAL
        // ==================================================

        if (TempleManager.isClaimed()
                && !TempleManager.isOwner(player)) {

            player.sendSystemMessage(
                    Component.literal(
                            "The Temple has already chosen its bearer."
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.RED)
                            .withBold(true))
            );

            return true;
        }

        // ==================================================
        // CHECK STONE
        // ==================================================

        TempleStone stone =
                TempleStone.fromItem(
                        stack.getItem()
                );

        if (!stone.isValid()) {

            player.sendSystemMessage(
                    Component.literal(
                            "The pedestal awaits an ancient stone."
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.GOLD)
                            .withBold(true))
            );

            return true;
        }

        // ==================================================
        // CHECK PEDESTAL
        // ==================================================

        if (pedestal.hasTempleStone()) {

            player.sendSystemMessage(
                    Component.literal(
                            "A stone already rests upon this pedestal."
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.RED)
                            .withBold(true))
            );

            return true;
        }

        // ==================================================
        // CHECK DUPLICATE STONE
        // ==================================================

        if (TempleManager.isStoneAlreadyPlaced(
                level,
                pedestal.getBlockPos(),
                stone
        )) {

            player.sendSystemMessage(
                    Component.literal(
                            "This stone has already been placed."
                    ).withStyle(style -> style
                            .withColor(ChatFormatting.DARK_RED)
                            .withBold(true))
            );

            return true;
        }

        // ==================================================
        // CLAIM TEMPLE
        // ==================================================

        if (!TempleManager.isClaimed()) {

            TempleManager.claimTemple(
                    level,
                    pedestal.getBlockPos(),
                    player
            );
        }

        // ==================================================
        // INSERT STONE
        // ==================================================

        pedestal.setTempleStone(stone);

        // ==================================================
        // TEMPLE STONE ACTIVATION SHAKE
        // ==================================================

        ModNetwork.ScreenShakePayload payload =
                new ModNetwork.ScreenShakePayload(
                        100,
                        1.35F,
                        pedestal.getBlockPos().getX(),
                        pedestal.getBlockPos().getY(),
                        pedestal.getBlockPos().getZ(),
                        2
                );

        PacketDistributor.sendToPlayersNear(
                level,
                null,
                pedestal.getBlockPos().getX(),
                pedestal.getBlockPos().getY(),
                pedestal.getBlockPos().getZ(),
                32.0D,
                payload
        );

        // ==================================================
        // CONSUME ITEM
        // ==================================================

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        // ==================================================
        // MESSAGE
        // ==================================================

        player.sendSystemMessage(
                Component.literal(
                        "The ancient stone has been accepted."
                ).withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withBold(true))
        );

        // ==================================================
        // CHECK TEMPLE COMPLETION
        // ==================================================

        if (TempleManager.hasAllFourStones(
                level,
                pedestal.getBlockPos()
        )) {

            TempleManager.completeTemple(
                    level,
                    pedestal.getBlockPos(),
                    player
            );
        }

        return true;
    }
}