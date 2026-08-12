package io.hxneyw.repo.content.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ThermalGaugeItem extends ThermalRelaySwitchItem {

    public ThermalGaugeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown() || !hasConnections(stack)) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            clearConnections(stack);
            player.getInventory().setChanged();
            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.thermal_gauge.network_removed"
                    ),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
        );
    }
}
