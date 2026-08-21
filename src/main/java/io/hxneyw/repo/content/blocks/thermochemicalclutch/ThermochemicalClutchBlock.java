package io.hxneyw.repo.content.blocks.thermochemicalclutch;

import com.simibubi.create.content.kinetics.transmission.ClutchBlock;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ThermochemicalClutchBlock
        extends ClutchBlock
        implements ThermochemicalConnection {

    public ThermochemicalClutchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            @NotNull BlockState state,
            @NotNull Direction face
    ) {
        return state.getValue(POWERED)
                || face.getAxis() != state.getValue(AXIS);
    }

    @Override
    public BlockEntityType<? extends SplitShaftBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_CLUTCH.get();
    }
}
