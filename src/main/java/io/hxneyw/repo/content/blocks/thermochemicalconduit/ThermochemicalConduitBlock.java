package io.hxneyw.repo.content.blocks.thermochemicalconduit;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ThermochemicalConduitBlock
        extends RotatedPillarKineticBlock
        implements IBE<ThermochemicalConduitBlockEntity>,
        ThermochemicalConnection {

    private static final VoxelShape BODY = Block.box(4, 4, 4, 12, 12, 12);

    public ThermochemicalConduitBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos position,
            BlockState state,
            Direction face
    ) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return face.getAxis() != state.getValue(AXIS);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<ThermochemicalConduitBlockEntity>
    getBlockEntityClass() {
        return ThermochemicalConduitBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThermochemicalConduitBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_CONDUIT.get();
    }

    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return BODY;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return BODY;
    }

    @Override
    public float getParticleTargetRadius() {
        return 0.45F;
    }

    @Override
    public float getParticleInitialRadius() {
        return 0.2F;
    }
}
