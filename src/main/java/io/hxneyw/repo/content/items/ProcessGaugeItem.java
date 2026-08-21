package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import io.hxneyw.repo.content.process.ProcessMonitorRef;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProcessGaugeItem extends BlockItem {
    private static final String MONITOR_TAG = "ProcessMonitor";
    private static final String LEGACY_TARGET_TAG = "ProcessTarget";
    private static final String LEGACY_CHANNEL_TAG = "SelectedChannel";

    public ProcessGaugeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level.getBlockEntity(context.getClickedPos())
                instanceof ProcessMonitorBlockEntity monitor) {
            if (!level.isClientSide) {
                bindToMonitor(context.getItemInHand(), level, monitor, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && getMonitorReference(stack) != null) {
            clearMonitorReference(stack);
            player.getInventory().setChanged();
            player.getCooldowns().addCooldown(stack.getItem(), 6);
            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.process_gauge.binding_cleared"
                    ),
                    true
            );
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isBoundToMonitor(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {
        ProcessMonitorRef reference = getMonitorReference(stack);
        if (reference == null) {
            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.process_gauge.unbound"
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.process_gauge.bound"
                    ).withStyle(ChatFormatting.AQUA)
            );
            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.process_gauge.monitor_pos",
                            reference.position().getX(),
                            reference.position().getY(),
                            reference.position().getZ()
                    ).withStyle(ChatFormatting.GRAY)
            );
        }

        tooltipComponents.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.process_gauge.reads_channels"
                ).withStyle(ChatFormatting.GOLD)
        );
        tooltipComponents.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.process_gauge.fault_output"
                ).withStyle(ChatFormatting.RED)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static void bindToMonitor(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull ProcessMonitorBlockEntity monitor,
            @Nullable Player player
    ) {
        setMonitorReference(stack, monitor.createReference(level));
        monitor.pulseBindingContact();

        if (player != null) {
            player.getInventory().setChanged();
            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.process_gauge.bound"
                    ),
                    true
            );
        }
    }

    public static boolean isBoundToMonitor(ItemStack stack) {
        return getMonitorReference(stack) != null;
    }

    public static @Nullable ProcessMonitorRef getMonitorReference(ItemStack stack) {
        CompoundTag root = getCustomTag(stack);
        if (!root.contains(MONITOR_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return ProcessMonitorRef.load(root.getCompound(MONITOR_TAG));
    }

    public static void setMonitorReference(
            ItemStack stack,
            ProcessMonitorRef reference
    ) {
        CompoundTag root = getCustomTag(stack);
        root.remove(LEGACY_TARGET_TAG);
        root.remove(LEGACY_CHANNEL_TAG);
        root.put(MONITOR_TAG, reference.save());
        setCustomTag(stack, root);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static void clearMonitorReference(ItemStack stack) {
        CompoundTag root = getCustomTag(stack);
        root.remove(MONITOR_TAG);
        root.remove(LEGACY_TARGET_TAG);
        root.remove(LEGACY_CHANNEL_TAG);
        setCustomTag(stack, root);
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private static void setCustomTag(ItemStack stack, CompoundTag root) {
        if (root.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
        }
    }

    private static CompoundTag getCustomTag(ItemStack stack) {
        return stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
    }
}
