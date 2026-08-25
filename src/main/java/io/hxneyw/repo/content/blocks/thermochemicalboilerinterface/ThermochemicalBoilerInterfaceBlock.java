package io.hxneyw.repo.content.blocks.thermochemicalboilerinterface;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public final class ThermochemicalBoilerInterfaceBlock
        extends DirectionalKineticBlock
        implements IBE<ThermochemicalBoilerInterfaceBlockEntity>,
        IWrenchable,
        ThermochemicalConnection {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty PORT_NORTH = BooleanProperty.create("port_north");
    public static final BooleanProperty PORT_EAST = BooleanProperty.create("port_east");
    public static final BooleanProperty PORT_SOUTH = BooleanProperty.create("port_south");
    public static final BooleanProperty PORT_WEST = BooleanProperty.create("port_west");
    public static final BooleanProperty INPUT_ACTIVE = BooleanProperty.create("input_active");

    public ThermochemicalBoilerInterfaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(PORT_NORTH, false)
                .setValue(PORT_EAST, false)
                .setValue(PORT_SOUTH, false)
                .setValue(PORT_WEST, false)
                .setValue(INPUT_ACTIVE, false));
    }

    public static Direction inputSide(BlockState state) {
        return state.getValue(FACING);
    }

    public static boolean hasPort(BlockState state, Direction side) {
        BooleanProperty property = portProperty(side);
        return property != null && state.getValue(property);
    }

    public static BlockState setPort(BlockState state, Direction side, boolean enabled) {
        BooleanProperty property = portProperty(side);
        return property == null ? state : state.setValue(property, enabled);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return inputSide(state).getAxis();
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos position,
            BlockState state,
            Direction face
    ) {
        return false;
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return !state.getValue(INPUT_ACTIVE)
                || face != inputSide(state)
                || !hasPort(state, face);
    }

    @Override
    public BlockState getRotatedBlockState(
            BlockState originalState,
            Direction targetedFace
    ) {
        return originalState;
    }

    @Override
    public @NotNull InteractionResult onWrenched(
            @NotNull BlockState state,
            @NotNull UseOnContext context
    ) {
        Direction side = context.getClickedFace();
        if (side.getAxis() == Axis.Y) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos position = context.getClickedPos();
        if (!ThermochemicalBoilerInterfaceArray.isPortEligible(level, position, side)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            ThermochemicalBoilerInterfaceArray.selectPort(
                    level,
                    position,
                    side
            );
            IWrenchable.playRotateSound(level, position);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        return updateConnections(
                defaultBlockState().setValue(
                        FACING,
                        context.getHorizontalDirection().getOpposite()
                ),
                context.getLevel(),
                context.getClickedPos()
        );
    }

    @Override
    public @NotNull BlockState updateShape(
            @NotNull BlockState state,
            @NotNull Direction direction,
            @NotNull BlockState neighbourState,
            @NotNull LevelAccessor level,
            @NotNull BlockPos position,
            @NotNull BlockPos neighbourPosition
    ) {
        if (direction.getAxis() == Axis.Y) {
            return state;
        }
        BlockState updated = updateConnections(state, level, position);
        if (isInterface(level, position.relative(direction))) {
            updated = setPort(updated, direction, false);
            if (updated.getValue(INPUT_ACTIVE)
                    && updated.getValue(FACING) == direction) {
                updated = updated.setValue(INPUT_ACTIVE, false);
            }
        }
        return updated;
    }

    @Override
    protected void createBlockStateDefinition(
            @NotNull StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder);
        builder.add(
                NORTH,
                EAST,
                SOUTH,
                WEST,
                PORT_NORTH,
                PORT_EAST,
                PORT_SOUTH,
                PORT_WEST,
                INPUT_ACTIVE
        );
    }

    @Override
    public void onPlace(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos position,
            @NotNull BlockState oldState,
            boolean isMoving
    ) {
        super.onPlace(state, level, position, oldState, isMoving);
        if (!level.isClientSide) {
            notifyBoilerAbove(level, position);
            ThermochemicalBoilerInterfaceArray.requestRefresh(level, position);
        }
    }

    @Override
    public void onRemove(
            BlockState state,
            @NotNull Level level,
            @NotNull BlockPos position,
            BlockState newState,
            boolean isMoving
    ) {
        boolean removed = !state.is(newState.getBlock());
        super.onRemove(state, level, position, newState, isMoving);
        if (removed && !level.isClientSide) {
            notifyBoilerAbove(level, position);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                ThermochemicalBoilerInterfaceArray.requestRefresh(
                        level,
                        position.relative(side)
                );
            }
        }
    }

    private static void notifyBoilerAbove(Level level, BlockPos position) {
        BlockEntity above = level.getBlockEntity(position.above());
        if (above instanceof FluidTankBlockEntity tank) {
            FluidTankBlockEntity controller = tank.getControllerBE();
            if (controller != null) {
                controller.boiler.needsHeatLevelUpdate = true;
                controller.updateBoilerTemperature();
                controller.notifyUpdate();
            }
        }
    }

    private BlockState updateConnections(
            BlockState state,
            LevelReader level,
            BlockPos position
    ) {
        return state
                .setValue(NORTH, isInterface(level, position.north()))
                .setValue(EAST, isInterface(level, position.east()))
                .setValue(SOUTH, isInterface(level, position.south()))
                .setValue(WEST, isInterface(level, position.west()));
    }

    public static boolean hasValidInputNeighbour(
            LevelReader level,
            BlockPos position,
            Direction side
    ) {
        if (side.getAxis() == Axis.Y) {
            return false;
        }
        BlockPos neighbourPos = position.relative(side);
        BlockState neighbourState = level.getBlockState(neighbourPos);
        if (neighbourState.getBlock() instanceof ThermochemicalBoilerInterfaceBlock) {
            return false;
        }
        if (!(neighbourState.getBlock() instanceof IRotate rotate)
                || !rotate.hasShaftTowards(
                level,
                neighbourPos,
                neighbourState,
                side.getOpposite()
        )) {
            return false;
        }
        if (!(neighbourState.getBlock() instanceof ThermochemicalConnection thermochemical)) {
            return false;
        }
        return !thermochemical.doesNotHaveThermochemicalConnection(
                neighbourState,
                side.getOpposite()
        );
    }

    public static boolean isInterface(LevelReader level, BlockPos position) {
        return level.getBlockState(position).getBlock()
                instanceof ThermochemicalBoilerInterfaceBlock;
    }

    private static BooleanProperty portProperty(Direction side) {
        return switch (side) {
            case NORTH -> PORT_NORTH;
            case EAST -> PORT_EAST;
            case SOUTH -> PORT_SOUTH;
            case WEST -> PORT_WEST;
            default -> null;
        };
    }

    @Override
    public @NotNull Class<ThermochemicalBoilerInterfaceBlockEntity> getBlockEntityClass() {
        return ThermochemicalBoilerInterfaceBlockEntity.class;
    }

    @Override
    public @NotNull BlockEntityType<? extends ThermochemicalBoilerInterfaceBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_BOILER_INTERFACE.get();
    }
}
