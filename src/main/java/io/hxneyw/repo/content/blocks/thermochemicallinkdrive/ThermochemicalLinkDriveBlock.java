package io.hxneyw.repo.content.blocks.thermochemicallinkdrive;

import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ThermochemicalLinkDriveBlock
        extends ChainDriveBlock
        implements ThermochemicalConnection {

    public ThermochemicalLinkDriveBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        // The resolver verifies the real native Create connection.
        return false;
    }
}