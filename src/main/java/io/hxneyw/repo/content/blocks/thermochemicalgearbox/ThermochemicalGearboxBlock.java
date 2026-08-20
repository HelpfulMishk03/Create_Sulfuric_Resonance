package io.hxneyw.repo.content.blocks.thermochemicalgearbox;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermochemicalGearboxBlock
        extends KineticBlock
        implements IWrenchable, IBE<ThermochemicalGearboxBlockEntity>,
        ThermochemicalConnection {

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public ThermochemicalGearboxBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState().setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            @NotNull StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    public @NotNull BlockState getRotatedBlockState(
            @NotNull BlockState state,
            @NotNull Direction targetedFace
    ) {
        return state.setValue(
                FACING,
                state.getValue(FACING).getClockWise()
        );
    }

    @Override
    public @NotNull BlockState rotate(
            @NotNull BlockState state,
            @NotNull Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(state.getValue(FACING))
        );
    }

    @Override
    public @NotNull BlockState mirror(
            @NotNull BlockState state,
            @NotNull Mirror mirror
    ) {
        return rotate(
                state,
                mirror.getRotation(state.getValue(FACING))
        );
    }

    @Override
    public boolean hasShaftTowards(
            @NotNull LevelReader level,
            @NotNull BlockPos position,
            @NotNull BlockState state,
            @NotNull Direction face
    ) {
        return true;
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            @NotNull BlockState state,
            @NotNull Direction face
    ) {
        return false;
    }


    @Override
    public @NotNull Axis getRotationAxis(@NotNull BlockState state) {
        return Axis.Y;
    }

    @Override
    public Class<ThermochemicalGearboxBlockEntity>
    getBlockEntityClass() {
        return ThermochemicalGearboxBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThermochemicalGearboxBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_GEARBOX.get();
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(
            @NotNull BlockState state
    ) {
        return PushReaction.PUSH_ONLY;
    }

    @Override
    public float getParticleTargetRadius() {
        return 0.75F;
    }

}
