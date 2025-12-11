package io.hxneyw.repo.content.blocks.behaviour;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * COMBUSTION HEATING BEHAVIOUR (Document §7) - FIXED
 * <p>
 * Transmits heat through shafts to adjacent machines at SMOULDERING+
 * <p>
 * NOTE: Full Create integration (mixer/press heating) requires deeper API access.
 * This provides the foundation for future implementation.
 */
public class CombustionHeatingBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<CombustionHeatingBehaviour> TYPE =
            new BehaviourType<>();

    private final MoltenRotorBlockEntity furnace;

    public CombustionHeatingBehaviour(MoltenRotorBlockEntity furnace) {
        super(furnace);
        this.furnace = furnace;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    /**
     * Check if this furnace can provide heat
     * Used internally for heat transmission logic
     */
    private boolean isHeating() {
        return furnace.isCombustionActive();
    }

    /**
     * Get the heat level to transmit to adjacent machines
     * Public for future Create integration
     */
    public BlazeBurnerBlock.HeatLevel getTransmittedHeat() {
        if (!isHeating()) {
            return BlazeBurnerBlock.HeatLevel.NONE;
        }

        // Map internal heat tier to Create's heat levels
        return switch(furnace.getCurrentHeatTier()) {
            case NONE -> BlazeBurnerBlock.HeatLevel.NONE;
            case SMOULDERING, FADING -> BlazeBurnerBlock.HeatLevel.SMOULDERING;
            case KINDLED -> BlazeBurnerBlock.HeatLevel.KINDLED;
            case SEETHING, BLAZING, RADIANT -> BlazeBurnerBlock.HeatLevel.SEETHING;
        };
    }

    /**
     * Check if this furnace can enable combustion recipes (RADIANT only)
     * Public for future recipe system integration
     */
    public boolean enablesCombustionRecipes() {
        return furnace.getCurrentHeatTier() == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;
    }

    /**
     * Get shaft directions that can transmit heat
     * Public for future shaft heat transfer system
     */
    public Direction[] getHeatTransmitDirections() {
        BlockState state = furnace.getBlockState();
        Direction facing = state.getValue(com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING);

        return new Direction[] {
                facing.getCounterClockWise(), // Left shaft (clockwise)
                facing.getClockWise()         // Right shaft (counter-clockwise)
        };
    }

    /**
     * Apply heat to adjacent machines through shafts
     * Called each tick when heating is active
     * <p>
     * TODO: Requires deeper Create API integration to actually heat mixers/presses
     */
    private void applyHeatToAdjacentMachines() {
        if (!isHeating()) return;

        Level level = furnace.getLevel();
        if (level == null) return;

        BlockPos pos = furnace.getBlockPos();
        BlazeBurnerBlock.HeatLevel heatLevel = getTransmittedHeat();

        // Heat machines connected through shafts
        for (Direction dir : getHeatTransmitDirections()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockEntity adjacentBE = level.getBlockEntity(adjacentPos);

            if (adjacentBE != null) {
                // TODO: Future implementation for Create integration
                // This requires access to Create's internal heating system

                // Example for future implementation:
                // if (adjacentBE instanceof MixerBlockEntity mixer) {
                //     mixer.applyDirectHeat(heatLevel);
                // }
                // if (adjacentBE instanceof MechanicalPressBlockEntity press) {
                //     press.applyHeatLevel(heatLevel);
                // }

                // For now, this just checks if we're heating - actual heat
                // transfer will be implemented when Create's API is extended
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (furnace.getLevel() != null && !furnace.getLevel().isClientSide) {
            applyHeatToAdjacentMachines();
        }
    }
}