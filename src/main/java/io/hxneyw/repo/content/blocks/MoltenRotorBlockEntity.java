package io.hxneyw.repo.content.blocks;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Molten Rotor Furnace Block Entity
 * <p>
 * CRITICAL: Extends GeneratingKineticBlockEntity (NOT KineticBlockEntity!)
 * This is the SAME base class that Water Wheel uses.
 */
public class MoltenRotorBlockEntity extends GeneratingKineticBlockEntity {

    // ========== FUEL & HEAT SYSTEM ==========
    private static final int MAX_HEAT_CAPACITY = 10000; // Same as Blaze Burner

    private int remainingBurnTime = 0;
    private boolean isCombustion = false;

    // Fuel thresholds (in ticks)
    private static final int FUEL_FOR_SMOULDERING = 100;
    private static final int FUEL_FOR_FADING = 1000;
    private static final int FUEL_FOR_KINDLED = 3000;
    private static final int FUEL_FOR_SEETHING = 6000;
    private static final int FUEL_FOR_COMBUSTION = 9000;

    // RPM per heat level
    private static final float RPM_SMOULDERING = 16f;
    private static final float RPM_FADING = 32f;
    private static final float RPM_KINDLED = 48f;
    private static final float RPM_SEETHING = 64f;
    private static final float RPM_COMBUSTION = 80f;

    public MoltenRotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MoltenRotorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOLTEN_ROTOR.get(), pos, state);
    }

    // ========== CRITICAL: This tells Create to generate rotation ==========

    /**
     * This method is called by GeneratingKineticBlockEntity to determine speed.
     * This is THE method that makes rotation work!
     */
    @Override
    public float getGeneratedSpeed() {
        BlazeBurnerBlock.HeatLevel heat = getHeatLevelFromBlock();

        if (heat == BlazeBurnerBlock.HeatLevel.NONE) {
            return 0f; // No rotation when cold
        }

        // Calculate RPM based on heat level
        float rpm = switch (heat) {
            case SMOULDERING -> RPM_SMOULDERING; // 16 RPM
            case FADING -> RPM_FADING;           // 32 RPM
            case KINDLED -> RPM_KINDLED;         // 48 RPM
            case SEETHING -> RPM_SEETHING;       // 64 RPM
            default -> 0f;
        };

        // COMBUSTION mode (your custom level above SEETHING)
        if (isCombustion) {
            rpm = RPM_COMBUSTION; // 80 RPM!
        }

        return rpm;
    }

    // ========== BEHAVIORS (Required for Create integration) ==========

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        // GeneratingKineticBlockEntity handles speed behavior automatically
        // No need to manually add speed behavior like I incorrectly suggested before
    }

    // ========== MAIN TICK LOGIC ==========

    @Override
    public void tick() {
        super.tick(); // CRITICAL: Calls GeneratingKineticBlockEntity.tick()

        if (level == null || level.isClientSide) {
            return;
        }

        boolean needsUpdate = false;

        // Store previous state for comparison
        BlazeBurnerBlock.HeatLevel previousHeat = getHeatLevelFromBlock();
        boolean wasCombustion = isCombustion;

        // Consume fuel
        if (remainingBurnTime > 0) {
            remainingBurnTime--;
            needsUpdate = true;
        }

        // Calculate new heat level
        BlazeBurnerBlock.HeatLevel newHeat = calculateHeatLevel();

        // Update block state if heat level changed
        if (newHeat != previousHeat || wasCombustion != isCombustion) {
            updateBlockState(newHeat);

            // CRITICAL: Tell Create the speed changed!
            updateGeneratedRotation();
            needsUpdate = true;
        }

        if (needsUpdate) {
            setChanged();
            sendData(); // Sync to client
        }
    }

    /**
     * Calculate heat level based on remaining fuel
     */
    private BlazeBurnerBlock.HeatLevel calculateHeatLevel() {
        if (remainingBurnTime <= 0) {
            isCombustion = false;
            return BlazeBurnerBlock.HeatLevel.NONE;
        }

        if (remainingBurnTime >= FUEL_FOR_COMBUSTION) {
            isCombustion = true; // Your custom level!
            return BlazeBurnerBlock.HeatLevel.SEETHING;
        }

        isCombustion = false;

        if (remainingBurnTime >= FUEL_FOR_SEETHING)
            return BlazeBurnerBlock.HeatLevel.SEETHING;
        if (remainingBurnTime >= FUEL_FOR_KINDLED)
            return BlazeBurnerBlock.HeatLevel.KINDLED;
        if (remainingBurnTime >= FUEL_FOR_FADING)
            return BlazeBurnerBlock.HeatLevel.FADING;
        if (remainingBurnTime >= FUEL_FOR_SMOULDERING)
            return BlazeBurnerBlock.HeatLevel.SMOULDERING;

        return BlazeBurnerBlock.HeatLevel.NONE;
    }

    /**
     * Update the block's visual heat level
     */
    private void updateBlockState(BlazeBurnerBlock.HeatLevel newHeat) {
        if (level == null) return;

        BlockState currentState = getBlockState();
        BlazeBurnerBlock.HeatLevel currentHeat = currentState.getValue(MoltenRotorBlock.HEAT_LEVEL);

        if (currentHeat != newHeat) {
            level.setBlock(worldPosition,
                    currentState.setValue(MoltenRotorBlock.HEAT_LEVEL, newHeat),
                    3); // Flag 3 = update clients
        }
    }

    // ========== FUEL MANAGEMENT ==========

    /**
     * Add fuel to the furnace (called from block when player adds coal/etc)
     */
    public void addFuel(int ticks) {
        if (level == null || level.isClientSide) return;

        // Add fuel (capped at max capacity)
        remainingBurnTime = Math.min(remainingBurnTime + ticks, MAX_HEAT_CAPACITY);

        // Immediately recalculate and update heat level
        BlazeBurnerBlock.HeatLevel newHeat = calculateHeatLevel();
        updateBlockState(newHeat);

        // CRITICAL: Tell Create the speed changed!
        updateGeneratedRotation();

        setChanged();
        sendData();
    }

    // ========== GETTERS ==========

    public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
        return getBlockState().getValue(MoltenRotorBlock.HEAT_LEVEL);
    }

    public boolean isCombustion() {
        return isCombustion;
    }

    public int getRemainingBurnTime() {
        return remainingBurnTime;
    }

    // ========== NBT SAVE/LOAD ==========

    @Override
    protected void write(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider, boolean clientPacket) {
        super.write(tag, provider, clientPacket);
        tag.putInt("FuelTime", remainingBurnTime);
        tag.putBoolean("Combustion", isCombustion);
    }

    @Override
    protected void read(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        remainingBurnTime = tag.getInt("FuelTime");
        isCombustion = tag.getBoolean("Combustion");
    }
}