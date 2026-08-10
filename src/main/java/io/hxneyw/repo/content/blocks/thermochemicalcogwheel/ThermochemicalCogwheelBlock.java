package io.hxneyw.repo.content.blocks.thermochemicalcogwheel;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ThermochemicalCogwheelBlock extends CogWheelBlock
        implements ThermochemicalConnection {

    public ThermochemicalCogwheelBlock(
            boolean large,
            Properties properties
    ) {
        super(large, properties);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        // The resolver verifies the real Create kinetic connection.
        // Returning false here allows both axle connections and cog-to-cog
        // tooth meshing to participate in the thermochemical route.
        return false;
    }

    @Override
    public @NotNull BlockEntityType<? extends KineticBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_COGWHEEL.get();
    }
}