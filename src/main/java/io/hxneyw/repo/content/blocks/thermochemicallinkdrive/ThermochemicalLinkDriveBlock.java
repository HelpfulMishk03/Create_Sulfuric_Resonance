package io.hxneyw.repo.content.blocks.thermochemicallinkdrive;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


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
        return false;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_LINK_DRIVE.get();
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction face,
                                           BlockState neighbour, LevelAccessor level, BlockPos currentPos,
                                           BlockPos facingPos) {
        if (neighbour.getBlock() instanceof ChainDriveBlock
                && !(neighbour.getBlock() instanceof ThermochemicalLinkDriveBlock)) {
            neighbour = Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, face, neighbour, level, currentPos, facingPos);
    }

}
