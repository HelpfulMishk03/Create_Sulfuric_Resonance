package io.hxneyw.repo.content.menu;

import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModMenus;
import java.util.Objects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ThermalRelaySwitchMenu
        extends AbstractContainerMenu {

    public static final int DATA_MODE = 0;
    public static final int DATA_LOW_FUEL_SCOPE = 1;
    public static final int DATA_HEATED_REDSTONE = 2;
    public static final int DATA_HEATED_GLOW = 3;
    public static final int DATA_SUPERHEATED_REDSTONE = 4;
    public static final int DATA_SUPERHEATED_GLOW = 5;
    public static final int DATA_COMBUSTION_REDSTONE = 6;
    public static final int DATA_COMBUSTION_GLOW = 7;
    public static final int DATA_LOW_FUEL_REDSTONE = 8;
    public static final int DATA_LOW_FUEL_GLOW = 9;
    public static final int DATA_CURRENT_HEAT_BAND = 10;
    public static final int DATA_CURRENT_POWER = 11;
    public static final int DATA_CURRENT_GLOW = 12;
    public static final int DATA_LINKED_COUNT = 13;
    public static final int DATA_LOW_FUEL_ACTIVE = 14;
    public static final int DATA_COUNT = 15;

    public static final int BUTTON_MODE_CUSTOM_HEAT = 0;
    public static final int BUTTON_MODE_LOW_FUEL = 1;

    public static final int BUTTON_SCOPE_LOW_HEAT = 10;
    public static final int BUTTON_SCOPE_HIGH_HEAT = 11;
    public static final int BUTTON_SCOPE_BOTH = 12;

    public static final int BUTTON_HEATED_REDSTONE_DOWN = 100;
    public static final int BUTTON_HEATED_REDSTONE_UP = 101;
    public static final int BUTTON_HEATED_GLOW_DOWN = 102;
    public static final int BUTTON_HEATED_GLOW_UP = 103;

    public static final int BUTTON_SUPERHEATED_REDSTONE_DOWN = 110;
    public static final int BUTTON_SUPERHEATED_REDSTONE_UP = 111;
    public static final int BUTTON_SUPERHEATED_GLOW_DOWN = 112;
    public static final int BUTTON_SUPERHEATED_GLOW_UP = 113;

    public static final int BUTTON_COMBUSTION_REDSTONE_DOWN = 120;
    public static final int BUTTON_COMBUSTION_REDSTONE_UP = 121;
    public static final int BUTTON_COMBUSTION_GLOW_DOWN = 122;
    public static final int BUTTON_COMBUSTION_GLOW_UP = 123;

    public static final int BUTTON_LOW_FUEL_REDSTONE_DOWN = 130;
    public static final int BUTTON_LOW_FUEL_REDSTONE_UP = 131;
    public static final int BUTTON_LOW_FUEL_GLOW_DOWN = 132;
    public static final int BUTTON_LOW_FUEL_GLOW_UP = 133;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    @Nullable
    private final ThermalRelaySwitchBlockEntity relay;


    public ThermalRelaySwitchMenu(
            int containerId,
            @NotNull Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                null,
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }


    public ThermalRelaySwitchMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @NotNull ThermalRelaySwitchBlockEntity relay
    ) {
        this(
                containerId,
                playerInventory,
                relay,
                relay.getMenuData(),
                ContainerLevelAccess.create(
                        Objects.requireNonNull(
                                relay.getLevel()
                        ),
                        relay.getBlockPos()
                )
        );
    }

    private ThermalRelaySwitchMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @Nullable ThermalRelaySwitchBlockEntity relay,
            @NotNull ContainerData data,
            @NotNull ContainerLevelAccess access
    ) {
        super(
                AllModMenus.THERMAL_RELAY_SWITCH.get(),
                containerId
        );

        this.relay = relay;
        this.data = data;
        this.access = access;

        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(
            @NotNull Player player
    ) {
        return AbstractContainerMenu.stillValid(
                this.access,
                player,
                AllModBlocks.THERMAL_RELAY_SWITCH.get()
        );
    }

    @Override
    public @NotNull ItemStack quickMoveStack(
            @NotNull Player player,
            int index
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(
            @NotNull Player player,
            int buttonId
    ) {
        if (this.relay == null
                || !stillValid(player)) {
            return false;
        }

        boolean handled =
                this.relay.handleMenuButton(buttonId);

        if (handled) {
            broadcastChanges();
        }

        return handled;
    }

    public ThermalRelaySwitchBlockEntity.RelayMode
    getMode() {
        return ThermalRelaySwitchBlockEntity
                .RelayMode.fromOrdinal(
                        this.data.get(DATA_MODE)
                );
    }

    public ThermalRelaySwitchBlockEntity.LowFuelScope
    getLowFuelScope() {
        return ThermalRelaySwitchBlockEntity
                .LowFuelScope.fromOrdinal(
                        this.data.get(
                                DATA_LOW_FUEL_SCOPE
                        )
                );
    }

    public int getHeatedRedstone() {
        return this.data.get(DATA_HEATED_REDSTONE);
    }

    public int getHeatedGlow() {
        return this.data.get(DATA_HEATED_GLOW);
    }

    public int getSuperheatedRedstone() {
        return this.data.get(
                DATA_SUPERHEATED_REDSTONE
        );
    }

    public int getSuperheatedGlow() {
        return this.data.get(
                DATA_SUPERHEATED_GLOW
        );
    }

    public int getCombustionRedstone() {
        return this.data.get(
                DATA_COMBUSTION_REDSTONE
        );
    }

    public int getCombustionGlow() {
        return this.data.get(
                DATA_COMBUSTION_GLOW
        );
    }

    public int getLowFuelRedstone() {
        return this.data.get(
                DATA_LOW_FUEL_REDSTONE
        );
    }

    public int getLowFuelGlow() {
        return this.data.get(
                DATA_LOW_FUEL_GLOW
        );
    }

    public ThermalRelaySwitchBlockEntity.HeatBand
    getCurrentHeatBand() {
        return ThermalRelaySwitchBlockEntity
                .HeatBand.fromOrdinal(
                        this.data.get(
                                DATA_CURRENT_HEAT_BAND
                        )
                );
    }

    public int getCurrentPower() {
        return this.data.get(DATA_CURRENT_POWER);
    }

    public int getCurrentGlow() {
        return this.data.get(DATA_CURRENT_GLOW);
    }

    public int getLinkedCount() {
        return this.data.get(DATA_LINKED_COUNT);
    }

    public boolean isLowFuelWarningActive() {
        return this.data.get(
                DATA_LOW_FUEL_ACTIVE
        ) != 0;
    }
}
