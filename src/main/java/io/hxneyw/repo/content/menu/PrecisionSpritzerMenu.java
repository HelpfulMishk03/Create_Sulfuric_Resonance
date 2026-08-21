package io.hxneyw.repo.content.menu;

import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity.PrecisionFilterMode;
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

public final class PrecisionSpritzerMenu extends AbstractContainerMenu {

    public static final int DATA_MODE = 0;
    public static final int DATA_ITEM_START = 1;
    public static final int DATA_ENTITY_START = DATA_ITEM_START + PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES;
    public static final int DATA_COUNT = 1 + PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES * 2;

    public static final int BUTTON_MODE_ITEM = 0;
    public static final int BUTTON_MODE_ENTITY = 1;
    public static final int BUTTON_CLEAR = 2;
    public static final int BUTTON_ITEM_BASE = 1000;
    public static final int BUTTON_ENTITY_BASE = 1000000;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    @Nullable
    private final PerforatedSpritzerBlockEntity spritzer;

    public PrecisionSpritzerMenu(
            int containerId,
            @NotNull Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                null,
                createClientData(),
                ContainerLevelAccess.NULL
        );
    }

    private static ContainerData createClientData() {
        SimpleContainerData data = new SimpleContainerData(DATA_COUNT);
        for (int i = DATA_ITEM_START; i < DATA_COUNT; i++) {
            data.set(i, -1);
        }
        return data;
    }

    public PrecisionSpritzerMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @NotNull PerforatedSpritzerBlockEntity spritzer
    ) {
        this(
                containerId,
                playerInventory,
                spritzer,
                spritzer.getPrecisionMenuData(),
                ContainerLevelAccess.create(
                        Objects.requireNonNull(spritzer.getLevel()),
                        spritzer.getBlockPos()
                )
        );
    }

    private PrecisionSpritzerMenu(
            int containerId,
            @NotNull Inventory playerInventory,
            @Nullable PerforatedSpritzerBlockEntity spritzer,
            @NotNull ContainerData data,
            @NotNull ContainerLevelAccess access
    ) {
        super(AllModMenus.PRECISION_SPRITZER.get(), containerId);
        Objects.requireNonNull(playerInventory);
        this.spritzer = spritzer;
        this.data = data;
        this.access = access;
        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(
                this.access,
                player,
                AllModBlocks.PERFORATED_SPRITZER.get()
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
        if (this.spritzer == null || !this.stillValid(player)) {
            return false;
        }

        if (buttonId == BUTTON_MODE_ITEM) {
            this.spritzer.setPrecisionFilterMode(PrecisionFilterMode.ITEM);
        } else if (buttonId == BUTTON_MODE_ENTITY) {
            this.spritzer.setPrecisionFilterMode(PrecisionFilterMode.ENTITY);
        } else if (buttonId == BUTTON_CLEAR) {
            this.spritzer.clearCurrentPrecisionFilter();
        } else if (buttonId >= BUTTON_ENTITY_BASE) {
            this.spritzer.toggleEntityFilterByRegistryId(buttonId - BUTTON_ENTITY_BASE);
        } else if (buttonId >= BUTTON_ITEM_BASE) {
            this.spritzer.toggleItemFilterByRegistryId(buttonId - BUTTON_ITEM_BASE);
        } else {
            return false;
        }

        this.broadcastChanges();
        return true;
    }

    public PrecisionFilterMode getMode() {
        return PrecisionFilterMode.fromOrdinal(this.data.get(DATA_MODE));
    }

    public int getItemFilterRegistryId(int index) {
        if (index < 0 || index >= PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES) {
            return -1;
        }
        return this.data.get(DATA_ITEM_START + index);
    }

    public int getEntityFilterRegistryId(int index) {
        if (index < 0 || index >= PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES) {
            return -1;
        }
        return this.data.get(DATA_ENTITY_START + index);
    }

    public boolean isItemFilterSelected(int registryId) {
        for (int i = 0; i < PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES; i++) {
            if (this.getItemFilterRegistryId(i) == registryId) {
                return true;
            }
        }
        return false;
    }

    public boolean isEntityFilterSelected(int registryId) {
        for (int i = 0; i < PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES; i++) {
            if (this.getEntityFilterRegistryId(i) == registryId) {
                return true;
            }
        }
        return false;
    }

    public int getSelectedItemCount() {
        int count = 0;
        for (int i = 0; i < PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES; i++) {
            if (this.getItemFilterRegistryId(i) >= 0) {
                count++;
            }
        }
        return count;
    }

    public int getSelectedEntityCount() {
        int count = 0;
        for (int i = 0; i < PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES; i++) {
            if (this.getEntityFilterRegistryId(i) >= 0) {
                count++;
            }
        }
        return count;
    }
}
