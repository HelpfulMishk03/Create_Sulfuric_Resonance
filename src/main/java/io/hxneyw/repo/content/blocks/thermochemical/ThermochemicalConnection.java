package io.hxneyw.repo.content.blocks.thermochemical;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface ThermochemicalConnection {
    boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    );
}
