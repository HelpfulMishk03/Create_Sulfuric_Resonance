package io.hxneyw.repo.content.blocks.behaviour;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * COMBUSTION HEATING BEHAVIOUR
 * Provides heat to Create machines (Basin, Mixer, etc.)
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
     * Check if this furnace is actively heating
     * Returns TRUE when the furnace IS heating (temp >= 300°C)
     */
    public boolean isActive() {
        return furnace.isCombustionActive();
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