package io.hxneyw.repo.content.blocks.sulfuricresonancechamber;

import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModMenus;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipeRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SulfuricResonanceChamberMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOT_COUNT = 4;
    private static final int PLAYER_SLOT_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;

    public static final int BUTTON_TOGGLE_MODE = 0;
    public static final int BUTTON_MANUAL_START = 1;
    public static final int BUTTON_TOGGLE_AUDIO = 2;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Inventory playerInventory;
    private final @Nullable SulfuricResonanceChamberBlockEntity chamber;

    public SulfuricResonanceChamberMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(
                        SulfuricResonanceChamberBlockEntity.MENU_DATA_COUNT
                ),
                ContainerLevelAccess.NULL,
                null
        );
    }

    public SulfuricResonanceChamberMenu(
            int containerId,
            Inventory playerInventory,
            SulfuricResonanceChamberBlockEntity chamber
    ) {
        this(
                containerId,
                playerInventory,
                chamber,
                chamber.getMenuData(),
                chamber.getLevel() == null
                        ? ContainerLevelAccess.NULL
                        : ContainerLevelAccess.create(
                                chamber.getLevel(),
                                chamber.getBlockPos()
                        ),
                chamber
        );
    }

    private SulfuricResonanceChamberMenu(
            int containerId,
            Inventory playerInventory,
            Container chamberInventory,
            ContainerData data,
            ContainerLevelAccess access,
            @Nullable SulfuricResonanceChamberBlockEntity chamber
    ) {
        super(AllModMenus.SULFURIC_RESONANCE_CHAMBER.get(), containerId);
        this.data = data;
        this.access = access;
        this.playerInventory = playerInventory;
        this.chamber = chamber;

        checkContainerDataCount(
                data,
                SulfuricResonanceChamberBlockEntity.MENU_DATA_COUNT
        );

        addSlot(createInputSlot(
                chamberInventory,
                SulfuricResonanceChamberBlockEntity.INPUT_1,
                22
        ));
        addSlot(createInputSlot(
                chamberInventory,
                SulfuricResonanceChamberBlockEntity.INPUT_2,
                46
        ));
        addSlot(createInputSlot(
                chamberInventory,
                SulfuricResonanceChamberBlockEntity.INPUT_3,
                70
        ));
        addSlot(new Slot(
                chamberInventory,
                SulfuricResonanceChamberBlockEntity.OUTPUT,
                118,
                50
        ) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        71 + column * 18,
                        163 + row * 18
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    71 + column * 18,
                    221
            ));
        }

        addDataSlots(data);
    }

    private Slot createInputSlot(
            Container chamberInventory,
            int slot,
            int x
    ) {
        return new Slot(chamberInventory, slot, x, 50) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                if (data.get(3) != 0 || data.get(0) > 0) {
                    return false;
                }
                return super.mayPlace(stack);
            }
        };
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(
                access,
                player,
                AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
        );
    }

    @Override
    public boolean clickMenuButton(
            @NotNull Player player,
            int buttonId
    ) {
        if (chamber == null || !stillValid(player)) {
            return false;
        }

        boolean handled = switch (buttonId) {
            case BUTTON_TOGGLE_MODE -> chamber.toggleOperatingMode();
            case BUTTON_MANUAL_START -> chamber.requestManualStart();
            case BUTTON_TOGGLE_AUDIO -> chamber.toggleAudioEnabled();
            default -> false;
        };

        if (handled) {
            broadcastChanges();
        }
        return handled;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(
            @NotNull Player player,
            int slotIndex
    ) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack rawStack = slot.getItem();
        ItemStack original = rawStack.copy();

        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(
                    rawStack,
                    PLAYER_SLOT_START,
                    PLAYER_SLOT_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!smartMoveToMachine(rawStack)) {
                int playerInventoryEnd = PLAYER_SLOT_START + 27;
                if (slotIndex < playerInventoryEnd) {
                    if (!moveItemStackTo(
                            rawStack,
                            playerInventoryEnd,
                            PLAYER_SLOT_END,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(
                        rawStack,
                        PLAYER_SLOT_START,
                        playerInventoryEnd,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (rawStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (rawStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, rawStack);
        return original;
    }

    private boolean smartMoveToMachine(ItemStack stack) {
        if (stack.isEmpty() || data.get(3) != 0 || data.get(0) > 0) {
            return false;
        }

        for (int pass = 0; pass < 2; pass++) {
            for (int machineSlot = 0; machineSlot < 3; machineSlot++) {
                Slot target = slots.get(machineSlot);
                boolean stacking = target.hasItem()
                        && ItemStack.isSameItemSameComponents(
                        target.getItem(),
                        stack
                );

                if (pass == 0 && !stacking) {
                    continue;
                }
                if (pass == 1 && target.hasItem()) {
                    continue;
                }
                if (!isValidRecipePlacement(machineSlot, stack)) {
                    continue;
                }
                if (moveItemStackTo(
                        stack,
                        machineSlot,
                        machineSlot + 1,
                        false
                )) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("resource")
    private boolean isValidRecipePlacement(int slot, ItemStack stack) {
        ItemStack input1 = getMachineStack(0).copy();
        ItemStack input2 = getMachineStack(1).copy();
        ItemStack input3 = getMachineStack(2).copy();
        ItemStack current = getMachineStack(slot);

        if (!current.isEmpty()
                && !ItemStack.isSameItemSameComponents(current, stack)) {
            return false;
        }

        ItemStack candidate = current.isEmpty()
                ? stack.copyWithCount(1)
                : current.copy();

        switch (slot) {
            case 0 -> input1 = candidate;
            case 1 -> input2 = candidate;
            case 2 -> input3 = candidate;
            default -> {
                return false;
            }
        }

        ItemStack finalInput1 = input1;
        ItemStack finalInput2 = input2;
        ItemStack finalInput3 = input3;
        
        return playerInventory.player.level()
                .getRecipeManager()
                .getAllRecipesFor(
                        SulfuricResonanceChamberRecipeRegistry.TYPE.get()
                )
                .stream()
                .anyMatch(holder -> holder.value().matchesPresentInputs(
                        finalInput1,
                        finalInput2,
                        finalInput3
                ));
    }

    public ItemStack getMachineStack(int slot) {
        if (slot < 0 || slot >= MACHINE_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return slots.get(slot).getItem();
    }

    public int getProcessingTicks() {
        return data.get(0);
    }

    public int getProcessingTime() {
        return data.get(1);
    }

    public boolean isProcessing() {
        return data.get(3) != 0;
    }

    public boolean isReady() {
        return data.get(2) != 0;
    }

    public int getAcidAmount() {
        return data.get(4);
    }

    public int getAcidCapacity() {
        return data.get(5);
    }

    public int getSpeed() {
        return data.get(6);
    }

    public int getTemperature() {
        return data.get(7);
    }

    public int getHeatRank() {
        return data.get(8);
    }

    public SulfuricResonanceChamberBlockEntity.ChamberStatus getStatus() {
        return SulfuricResonanceChamberBlockEntity.ChamberStatus
                .fromOrdinal(data.get(9));
    }

    public SulfuricResonanceChamberBlockEntity.OperatingMode
    getOperatingMode() {
        return SulfuricResonanceChamberBlockEntity.OperatingMode
                .fromOrdinal(data.get(10));
    }

    public boolean isAudioEnabled() {
        return data.get(11) != 0;
    }
}
