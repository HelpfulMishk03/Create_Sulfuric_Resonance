package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Allows Mechanical Arms to interact with Molten Rotor Furnaces
 * Deposit-only - arms can add fuel but not extract
 * BEHAVIOR:
 * - Arm picks up entire stack of fuel (up to 64)
 * - Inserts fuel ONE AT A TIME until furnace is full (32/32)
 * - Holds overflow fuel in hand
 * - Waits for furnace to consume fuel, then inserts more to maintain 32/32
 */
public class MoltenRotorArmPoint extends ArmInteractionPoint {

    public MoltenRotorArmPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    @Override
    public Mode getMode() {
        return Mode.DEPOSIT;
    }

    @Override
    public ItemStack insert(ArmBlockEntity arm, ItemStack stack, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof MoltenRotorBlockEntity furnace))
            return stack;

        MoltenRotorBlockEntity.FuelType fuelType = furnace.getFuelTypeFromItem(stack);
        if (fuelType == null || fuelType == MoltenRotorBlockEntity.FuelType.NONE)
            return stack;

        // SIMULATION: Calculate how many items the furnace can accept
        if (simulate) {
            if (!wouldAcceptFuel(furnace, fuelType)) {
                // Furnace is completely full - can't accept any
                return stack;
            }

            // Calculate effective capacity remaining
            int burnTime = furnace.getDisplayFuelTime();
            int maxCapacity = fuelType.maxStackSize;

            // Special case for sticks
            if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
                maxCapacity = 32;
                // Only works with logs burning
                if (furnace.getActiveFuelType() != MoltenRotorBlockEntity.FuelType.LOG || burnTime <= 0) {
                    return stack;
                }
            }

            // Calculate how much space is available
            int spaceAvailable;
            if (burnTime > 0 && furnace.getActiveFuelType() == fuelType) {
                // Same fuel burning - calculate effective space
                float itemsWorthOfTimeRemaining = burnTime / fuelType.baseBurnTimeTicks;
                int effectiveCurrentCount = (int) Math.ceil(itemsWorthOfTimeRemaining);
                spaceAvailable = maxCapacity - effectiveCurrentCount;
            } else {
                // Fresh start - full capacity
                spaceAvailable = maxCapacity;
            }

            // CRITICAL: Tell the arm we can accept the ENTIRE stack
            // Even if furnace only has space for 5, we return "can accept all 64"
            // This makes the arm pick up the full stack
            // The arm will insert one-by-one and hold the overflow
            return ItemStack.EMPTY; // Accept entire stack (arm will pick it all up)
        }

        // ACTUAL INSERTION: Insert ONE item at a time
        boolean success = furnace.addFuel(fuelType, 1);

        if (!success) {
            return stack; // Keep holding entire stack
        }

        // Success - return remainder (stack minus 1)
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        return remainder;
    }

    @Override
    public ItemStack extract(ArmBlockEntity arm, int amount, boolean simulate) {
        return ItemStack.EMPTY; // Cannot extract from furnaces
    }

    /**
     * EXACTLY mirrors the logic in MoltenRotorBlockEntity.addFuel()
     * Returns true if the furnace WOULD accept this fuel type
     */
    private boolean wouldAcceptFuel(MoltenRotorBlockEntity furnace, MoltenRotorBlockEntity.FuelType fuelType) {
        MoltenRotorBlockEntity.FuelType currentFuel = furnace.getActiveFuelType();
        int currentCount = furnace.getActiveFuelCount();
        int burnTime = furnace.getDisplayFuelTime();

        // CASE 1: New fuel has HIGHER heat output - OVERRIDE
        if (currentFuel != MoltenRotorBlockEntity.FuelType.NONE &&
                burnTime > 0 &&
                fuelType.maxTempReachable > currentFuel.maxTempReachable) {
            return true; // Always can override with higher heat
        }

        // CASE 2: STICKS - Can only add when logs are burning
        if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
            if (currentFuel != MoltenRotorBlockEntity.FuelType.LOG || burnTime <= 0) {
                return false; // Can't add sticks
            }
            // Calculate effective current count for sticks
            float itemsWorthOfTimeRemaining = burnTime / currentFuel.baseBurnTimeTicks;
            int effectiveCurrentCount = (int) Math.ceil(itemsWorthOfTimeRemaining);
            return effectiveCurrentCount < 32; // Can add if under 32
        }

        // CASE 3: Same fuel burning - refill up to max
        if (currentFuel == fuelType && burnTime > 0) {
            // Calculate how many items worth of fuel remain
            float itemsWorthOfTimeRemaining = burnTime / fuelType.baseBurnTimeTicks;
            // Round up - if we have 17.5 items worth, we count as having 18 slots occupied
            int effectiveCurrentCount = (int) Math.ceil(itemsWorthOfTimeRemaining);

            return effectiveCurrentCount < fuelType.maxStackSize; // Room to add more?
        }

        // CASE 4: Different fuel burning (not higher heat) - reject
        if (currentFuel != MoltenRotorBlockEntity.FuelType.NONE &&
                currentFuel != fuelType &&
                burnTime > 0) {
            return false; // Can't mix fuels
        }

        // CASE 5: No fuel burning - start fresh
        // This handles: burnTime <= 0 OR currentFuel == NONE
        return true; // Always can start fresh
    }
}