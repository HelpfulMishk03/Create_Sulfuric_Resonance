package io.hxneyw.repo.content.blocks.thermochemicalconduit;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ThermochemicalConduitBlockEntity
        extends KineticBlockEntity {

    public ThermochemicalConduitBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_CONDUIT.get(),
                position,
                state
        );
    }
}