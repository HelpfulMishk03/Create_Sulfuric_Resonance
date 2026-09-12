package io.hxneyw.repo.content.blocks.thermalbattery;

import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ThermalBatteryMenu extends AbstractContainerMenu {

    public static final int BUTTON_HEATED = 0;
    public static final int BUTTON_SUPERHEATED = 1;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final @Nullable ThermalBatteryBlockEntity battery;

    public ThermalBatteryMenu(
            int containerId,
            Inventory inventory,
            RegistryFriendlyByteBuf extraData
    ) {
        this(
                containerId,
                inventory,
                readInitialData(extraData),
                ContainerLevelAccess.NULL,
                null
        );
    }

    public ThermalBatteryMenu(
            int containerId,
            Inventory inventory,
            ThermalBatteryBlockEntity battery
    ) {
        this(
                containerId,
                inventory,
                battery.getMenuData(),
                battery.getLevel() == null
                        ? ContainerLevelAccess.NULL
                        : ContainerLevelAccess.create(
                                battery.getLevel(),
                                battery.getBlockPos()
                        ),
                battery
        );
    }

    public static void writeInitialData(
            ThermalBatteryBlockEntity battery,
            RegistryFriendlyByteBuf buffer
    ) {
        ContainerData data = battery.getMenuData();
        for (int index = 0;
             index < ThermalBatteryBlockEntity.MENU_DATA_COUNT;
             index++) {
            buffer.writeInt(data.get(index));
        }
    }

    private static ContainerData readInitialData(
            RegistryFriendlyByteBuf buffer
    ) {
        SimpleContainerData data = new SimpleContainerData(
                ThermalBatteryBlockEntity.MENU_DATA_COUNT
        );
        for (int index = 0;
             index < ThermalBatteryBlockEntity.MENU_DATA_COUNT;
             index++) {
            data.set(index, buffer.readInt());
        }
        return data;
    }

    @SuppressWarnings("unused")
    private ThermalBatteryMenu(
            int containerId,
            Inventory inventory,
            ContainerData data,
            ContainerLevelAccess access,
            @Nullable ThermalBatteryBlockEntity battery
    ) {
        super(AllModMenus.THERMAL_BATTERY.get(), containerId);
        this.data = data;
        this.access = access;
        this.battery = battery;
        checkContainerDataCount(data, ThermalBatteryBlockEntity.MENU_DATA_COUNT);
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(
                access,
                player,
                AllModBlocks.THERMAL_BATTERY.get()
        );
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int buttonId) {
        if (battery == null || !stillValid(player)) {
            return false;
        }

        boolean changed = switch (buttonId) {
            case BUTTON_HEATED -> battery.setOutputMode(
                    ThermalBatteryBlockEntity.OutputMode.HEATED
            );
            case BUTTON_SUPERHEATED -> battery.setOutputMode(
                    ThermalBatteryBlockEntity.OutputMode.SUPERHEATED
            );
            default -> false;
        };

        if (changed) {
            broadcastChanges();
        }
        return changed;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(
            @NotNull Player player,
            int slotIndex
    ) {
        return ItemStack.EMPTY;
    }

    public float getHeatedHeat() {
        return data.get(0) / 10.0F;
    }

    public float getSuperheatedHeat() {
        return data.get(1) / 10.0F;
    }

    public float getCapacity() {
        return data.get(2) / 10.0F;
    }

    public float getTotalHeat() {
        return getHeatedHeat() + getSuperheatedHeat();
    }

    public ThermalBatteryBlockEntity.OutputMode getOutputMode() {
        int ordinal = data.get(3);
        ThermalBatteryBlockEntity.OutputMode[] values =
                ThermalBatteryBlockEntity.OutputMode.values();
        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : ThermalBatteryBlockEntity.OutputMode.HEATED;
    }

    public int getInputTemperature() {
        return data.get(4);
    }

    public ThermalBatteryBlock.IndicatorState getIndicatorState() {
        int ordinal = data.get(5);
        ThermalBatteryBlock.IndicatorState[] values =
                ThermalBatteryBlock.IndicatorState.values();
        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : ThermalBatteryBlock.IndicatorState.FAULT;
    }

    public int getEstimatedRuntimeTicks() {
        return data.get(6);
    }

}
