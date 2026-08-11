package io.hxneyw.repo.content.blocks.sulfuricresonancechamber;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

public class SulfuricResonanceChamberBlock
        extends HorizontalDirectionalBlock
        implements IBE<SulfuricResonanceChamberBlockEntity>,
        IRotate,
        ThermochemicalConnection {

    public static final MapCodec<SulfuricResonanceChamberBlock>
            CODEC = simpleCodec(
            SulfuricResonanceChamberBlock::new
    );

    public SulfuricResonanceChamberBlock(
            Properties properties
    ) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected @NotNull MapCodec<? extends SulfuricResonanceChamberBlock>
    codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        return defaultBlockState()
                .setValue(
                        FACING,
                        context.getHorizontalDirection().getOpposite()
                );
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos position,
            BlockState state,
            Direction face
    ) {
        return face == heatAndRotationSide(state);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return face != heatAndRotationSide(state);
    }

    @Override
    public Axis getRotationAxis(
            BlockState state
    ) {
        return heatAndRotationSide(state).getAxis();
    }

    @Override
    public Class<SulfuricResonanceChamberBlockEntity>
    getBlockEntityClass() {
        return SulfuricResonanceChamberBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SulfuricResonanceChamberBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.SULFURIC_RESONANCE_CHAMBER.get();
    }

    public static Direction heatAndRotationSide(
            BlockState state
    ) {
        return state.getValue(FACING).getCounterClockWise();
    }

    public static Direction fluidSide(
            BlockState state
    ) {
        return state.getValue(FACING).getClockWise();
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    net.minecraft.world.level.block.Block,
                    BlockState
                    > builder
    ) {
        builder.add(FACING);
    }
}
