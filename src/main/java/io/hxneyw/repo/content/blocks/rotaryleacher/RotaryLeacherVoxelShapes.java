package io.hxneyw.repo.content.blocks.rotaryleacher;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RotaryLeacherVoxelShapes {
    public static final VoxelShape LOWER = Block.box(-1, 0, -1, 17, 16, 17);
    public static final VoxelShape UPPER = Shapes.block();

    private RotaryLeacherVoxelShapes() {}
}
