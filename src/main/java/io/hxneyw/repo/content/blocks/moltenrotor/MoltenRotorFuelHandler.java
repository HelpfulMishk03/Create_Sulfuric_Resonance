package io.hxneyw.repo.content.blocks.moltenrotor;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Insertion-only item capability for the Molten Rotor.
 *
 * <p>The furnace is a processing sink rather than a conventional inventory:
 * it accepts exactly one fuel unit per successful transaction, exposes no
 * extractable slot, and delegates every rule to the block entity's central
 * fuel validation path.</p>
 */
public final class MoltenRotorFuelHandler implements IItemHandler {
    private final MoltenRotorBlockEntity furnace;
    private final @Nullable Direction side;

    public MoltenRotorFuelHandler(
            MoltenRotorBlockEntity furnace,
            @Nullable Direction side
    ) {
        this.furnace = furnace;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        this.validateSlot(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(
            int slot,
            @NotNull ItemStack stack,
            boolean simulate
    ) {
        this.validateSlot(slot);

        if (stack.isEmpty()
                || !this.furnace.canAutomationInsertFrom(this.side)
                || !this.furnace.insertFuel(stack, true)) {
            return stack;
        }

        if (!simulate && !this.furnace.insertFuel(stack, false)) {
            return stack;
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(
            int slot,
            int amount,
            boolean simulate
    ) {
        this.validateSlot(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        this.validateSlot(slot);
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        this.validateSlot(slot);
        return !stack.isEmpty()
                && this.furnace.canAutomationInsertFrom(this.side)
                && this.furnace.insertFuel(stack, true);
    }

    private void validateSlot(int slot) {
        if (slot != 0) {
            throw new RuntimeException("Molten Rotor fuel handler only has slot 0");
        }
    }
}
