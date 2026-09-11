package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public final class ThermalBatteryItem extends BlockItem {

    public ThermalBatteryItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {
        int percent = getStoredHeatPercent(stack);
        tooltipComponents.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.thermal_battery.stored_heat",
                        percent
                ).withStyle(percent > 0
                        ? ChatFormatting.GOLD
                        : ChatFormatting.DARK_GRAY)
        );
        tooltipComponents.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.thermal_battery.redstone_output"
                ).withStyle(ChatFormatting.GRAY)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static ItemStack createStoredStack(
            float heatedHeat,
            float superheatedHeat,
            ThermalBatteryBlockEntity.OutputMode outputMode
    ) {
        ItemStack stack = new ItemStack(
                io.hxneyw.repo.content.Items.THERMAL_BATTERY_ITEM.get()
        );
        writeStoredState(stack, heatedHeat, superheatedHeat, outputMode);
        return stack;
    }

    public static ItemStack createStoredStack(
            ThermalBatteryBlockEntity battery
    ) {
        return createStoredStack(
                battery.getHeatedHeat(),
                battery.getSuperheatedHeat(),
                battery.getOutputMode()
        );
    }

    public static ItemStack createFullyChargedStack() {
        ItemStack stack = createStoredStack(
                0.0F,
                ThermalBatteryBlockEntity.MAX_STORED_HEAT,
                ThermalBatteryBlockEntity.OutputMode.SUPERHEATED
        );
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.translatable(
                        "item.sulfuricresonance.thermal_battery.fully_charged"
                )
        );
        return stack;
    }

    public static void writeStoredState(
            ItemStack stack,
            float heatedHeat,
            float superheatedHeat,
            ThermalBatteryBlockEntity.OutputMode outputMode
    ) {
        float heated = Math.max(0.0F, heatedHeat);
        float superheated = Math.max(0.0F, superheatedHeat);
        float total = heated + superheated;
        if (total > ThermalBatteryBlockEntity.MAX_STORED_HEAT) {
            float scale = ThermalBatteryBlockEntity.MAX_STORED_HEAT / total;
            heated *= scale;
            superheated *= scale;
        }

        CompoundTag stored = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        stored.putFloat("HeatedHeat", heated);
        stored.putFloat("SuperheatedHeat", superheated);
        stored.putString(
                "OutputMode",
                (outputMode == null
                        ? ThermalBatteryBlockEntity.OutputMode.HEATED
                        : outputMode).serializedName()
        );
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(stored));
    }

    public static int getStoredHeatPercent(ItemStack stack) {
        CompoundTag stored = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        float heat = Math.max(0.0F, stored.getFloat("HeatedHeat"))
                + Math.max(0.0F, stored.getFloat("SuperheatedHeat"));
        return Mth.clamp(
                Math.round(
                        heat
                                / ThermalBatteryBlockEntity.MAX_STORED_HEAT
                                * 100.0F
                ),
                0,
                100
        );
    }
}
