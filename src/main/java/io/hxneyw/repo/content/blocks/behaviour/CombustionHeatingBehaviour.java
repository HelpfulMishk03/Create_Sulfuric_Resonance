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
 * COMBUSTION HEATING BEHAVIOUR
 * Provides heat to Create machines (Basin, Mixer, etc.) and transmits through shafts
 * <p>
 * Basin Heating: Create automatically detects heat from blocks below that have HEAT_LEVEL property
 * Shaft Heating: Custom feature - transmits heat through connected kinetic components
 */
public class CombustionHeatingBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<CombustionHeatingBehaviour> TYPE = new BehaviourType<>();

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
     * Check if this furnace is actively heating
     */
    public boolean isHeating() {
        return furnace.isCombustionActive();
    }

    /**
     * Get the heat level this furnace provides
     * Used by Create's heating system (Basin, etc.) and for shaft transmission
     */
    public BlazeBurnerBlock.HeatLevel getHeatLevel() {
        if (!isHeating()) {
            return BlazeBurnerBlock.HeatLevel.NONE;
        }

        // Map internal heat tiers to Create's heat levels
        return switch(furnace.getCurrentHeatTier()) {
            case NONE -> BlazeBurnerBlock.HeatLevel.NONE;
            case SMOULDERING, FADING -> BlazeBurnerBlock.HeatLevel.SMOULDERING;
            case KINDLED -> BlazeBurnerBlock.HeatLevel.KINDLED;
            case SEETHING, RADIANT -> BlazeBurnerBlock.HeatLevel.SEETHING;
        };
    }

    /**
     * Check if this furnace is at RADIANT tier (for special combustion recipes)
     */
    public boolean isRadiantTier() {
        return furnace.getCurrentHeatTier() == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;
    }

    /**
     * Get the actual internal heat tier (including RADIANT which Create doesn't have)
     */
    public MoltenRotorBlockEntity.RotorHeatLevel getActualHeatTier() {
        return furnace.getCurrentHeatTier();
    }

    /**
     * Check if this heat level enables combustion recipes (RADIANT only)
     * Use this in custom recipe checks for special high-heat recipes
     */
    public boolean enablesCombustionRecipes() {
        return isRadiantTier();
    }

    /**
     * Get shaft directions that can transmit heat
     * Returns the left and right shaft positions based on facing direction
     */
    public Direction[] getHeatTransmitDirections() {
        BlockState state = furnace.getBlockState();
        Direction facing = state.getValue(com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING);

        return new Direction[] {
                facing.getCounterClockWise(), // Left shaft
                facing.getClockWise()         // Right shaft
        };
    }

    /**
     * Apply heat to adjacent machines through shafts (custom feature)
     * NOTE: This is for future shaft-based heat transmission
     * Basin heating works automatically via the HEAT_LEVEL block state property
     */
    private void applyHeatToAdjacentMachines() {
        if (!isHeating()) return;

        Level level = furnace.getLevel();
        if (level == null) return;

        BlockPos pos = furnace.getBlockPos();
        BlazeBurnerBlock.HeatLevel heatLevel = getHeatLevel();

        // Heat machines connected through shafts
        for (Direction dir : getHeatTransmitDirections()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockEntity adjacentBE = level.getBlockEntity(adjacentPos);

            if (adjacentBE != null) {
                // TODO: Future implementation for shaft-based heating
                // This would require Create API extensions to apply heat through kinetic connections

                // Example future implementation:
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

    /**
     * Static helper for Create's heating system
     * This allows other systems to query heat level from block state
     */
    public static BlazeBurnerBlock.HeatLevel getHeatLevelFromState(BlockState state) {
        if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
        }
        return BlazeBurnerBlock.HeatLevel.NONE;
    }
}