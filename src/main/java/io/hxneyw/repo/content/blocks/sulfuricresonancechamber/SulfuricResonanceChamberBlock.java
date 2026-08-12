package io.hxneyw.repo.content.blocks.sulfuricresonancechamber;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hit
    ) {
        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) {
                serverPlayer.openMenu(provider);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof SulfuricResonanceChamberBlockEntity chamber)) {
            return null;
        }

        return new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new SulfuricResonanceChamberMenu(
                                containerId,
                                playerInventory,
                                chamber
                        ),
                Component.translatable(
                        "block.sulfuricresonance.sulfuric_resonance_chamber"
                )
        );
    }

    public static Direction heatAndRotationSide(
            BlockState state
    ) {
        // The north-facing model's visible shaft is on local east, which is
        // the player's left when looking at the front of the machine.
        return state.getValue(FACING).getClockWise();
    }

    public static Direction fluidSide(
            BlockState state
    ) {
        // The acid port is the player's right-hand side when looking at the
        // front of the machine. For a north-facing block that is west.
        return state.getValue(FACING).getCounterClockWise();
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
