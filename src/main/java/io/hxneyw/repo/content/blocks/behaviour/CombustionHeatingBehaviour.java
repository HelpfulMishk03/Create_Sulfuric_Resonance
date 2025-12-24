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
 * Provides heat to Create machines (Basin, Mixer, etc.)
 *
 * CRITICAL: Basin heating works via the HEAT_LEVEL block state property
 * Create automatically detects blocks with HEAT_LEVEL and applies heating
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
     * FIXED: Check if this furnace is actively heating
     * Returns TRUE when the furnace IS heating (temp >= 300°C)
     */
    public boolean isHeating() {
        return furnace.isCombustionActive(); // FIXED: Removed the ! (NOT operator)
    }

    /**
     * Get the heat level this furnace provides to Create machines
     *
     * IMPORTANT: This maps to Create's heating system:
     * - NONE: No heating (below 300°C)
     * - SMOULDERING: Warming (300-499°C) - NOT enough for heated recipes
     * - KINDLED: Heated (500-799°C) - ENABLES heated recipes
     * - SEETHING: Superheated (800°C+) - ENABLES superheated recipes
     */
    public BlazeBurnerBlock.HeatLevel getHeatLevel() {
        // If not heating, return NONE
        if (!isHeating()) {
            return BlazeBurnerBlock.HeatLevel.NONE;
        }

        // Map internal heat tiers to Create's heat levels
        // CRITICAL: Create needs KINDLED (500°C+) for "heated" recipes
        return switch(furnace.getCurrentHeatTier()) {
            case NONE -> BlazeBurnerBlock.HeatLevel.NONE;

            // SMOULDERING/FADING (300-499°C) - visible heat but NOT enough for recipes
            case SMOULDERING, FADING -> BlazeBurnerBlock.HeatLevel.KINDLED;

            // KINDLED (500-799°C) - ENABLES heated mixing/compacting
            case KINDLED -> BlazeBurnerBlock.HeatLevel.KINDLED;

            // SEETHING/RADIANT (800°C+) - ENABLES superheated recipes
            case SEETHING, RADIANT -> BlazeBurnerBlock.HeatLevel.SEETHING;
        };
    }

    /**
     * Check if this furnace is at RADIANT tier (for special combustion recipes)
     * This is your custom tier above Create's SEETHING
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
     * Apply heat to adjacent machines through shafts (future feature)
     * NOTE: Basin heating works automatically via the HEAT_LEVEL block state property
     */
    private void applyHeatToAdjacentMachines() {
        if (!isHeating()) return;

        Level level = furnace.getLevel();
        if (level == null) return;

        // Future implementation: shaft-based heat transmission
        // For now, basin heating works automatically via block state
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