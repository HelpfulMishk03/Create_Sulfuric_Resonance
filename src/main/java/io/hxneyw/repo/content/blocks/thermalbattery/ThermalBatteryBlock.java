package io.hxneyw.repo.content.blocks.thermalbattery;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.items.ThermalBatteryItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class ThermalBatteryBlock
        extends DirectionalKineticBlock
        implements IBE<ThermalBatteryBlockEntity>,
        IWrenchable,
        ThermochemicalConnection {

    public static final EnumProperty<IndicatorState> INDICATOR =
            EnumProperty.create("indicator", IndicatorState.class);
    public static final BooleanProperty POWERED =
            BlockStateProperties.POWERED;

    public ThermalBatteryBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(INDICATOR, IndicatorState.HEATED)
                        .setValue(POWERED, false)
        );
    }


    public static Direction interfaceSide(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return interfaceSide(state).getAxis();
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos position,
            BlockState state,
            Direction face
    ) {
        return face == interfaceSide(state);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return face != interfaceSide(state);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(
            @NotNull BlockPlaceContext context
    ) {
        Direction playerFacing = context.getNearestLookingDirection().getOpposite();
        Direction preferredInterface = getPreferredFacing(context);

        Direction selected = preferredInterface != null
                ? preferredInterface
                : playerFacing;

        return stateForInterfaceSide(selected).setValue(
                POWERED,
                context.getLevel().hasNeighborSignal(context.getClickedPos())
        );
    }

    @Override
    public @Nullable Direction getPreferredFacing(
            BlockPlaceContext context
    ) {
        BlockPos placementPos = context.getClickedPos();
        Direction nearest = context.getNearestLookingDirection();

        if (hasValidInterfaceNeighbour(
                context.getLevel(),
                placementPos,
                nearest
        )) {
            return nearest;
        }

        Direction preferred = null;

        for (Direction side : Direction.values()) {
            if (!hasValidInterfaceNeighbour(
                    context.getLevel(),
                    placementPos,
                    side
            )) {
                continue;
            }

            if (preferred != null) {
                return null;
            }

            preferred = side;
        }

        if (preferred != null) {
            return preferred;
        }

        Direction createPreferred = super.getPreferredFacing(context);
        if (createPreferred != null
                && hasValidInterfaceNeighbour(
                context.getLevel(),
                placementPos,
                createPreferred
        )) {
            return createPreferred;
        }

        return null;
    }

    private boolean hasValidInterfaceNeighbour(
            Level level,
            BlockPos placementPos,
            Direction side
    ) {
        return exposesThermochemicalShaftTowards(
                level,
                placementPos.relative(side),
                side.getOpposite()
        );
    }

    static boolean exposesThermochemicalShaftTowards(
            Level level,
            BlockPos position,
            Direction face
    ) {
        if (!level.isLoaded(position)) {
            return false;
        }

        BlockState state = level.getBlockState(position);
        if (!(state.getBlock() instanceof IRotate rotate)
                || !rotate.hasShaftTowards(
                level,
                position,
                state,
                face
        )) {
            return false;
        }

        if (state.getBlock() instanceof ThermochemicalConnection connection) {
            return !connection.doesNotHaveThermochemicalConnection(
                    state,
                    face
            );
        }

                if (level.getBlockEntity(position) instanceof BeltBlockEntity belt
                && belt.hasPulley()
                && belt instanceof CombustionBeltAccessor accessor
                && accessor.sulfuricresonance$isCombustionBelt()
                && accessor.sulfuricresonance$isThermochemicalPulley()) {
            return true;
        }
return level.getBlockEntity(position)
                instanceof MoltenRotorBlockEntity;
    }

    private BlockState stateForInterfaceSide(Direction side) {
        return defaultBlockState()
                .setValue(FACING, side)
                .setValue(INDICATOR, IndicatorState.HEATED);
    }

    @Override
    public void setPlacedBy(
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @Nullable LivingEntity placer,
            @NotNull ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide
                && level.getBlockEntity(pos)
                instanceof ThermalBatteryBlockEntity battery) {
            battery.restoreStoredState(stack);
        }
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(
            @NotNull BlockState state,
            @NotNull HitResult target,
            @NotNull LevelReader level,
            @NotNull BlockPos pos,
            @NotNull Player player
    ) {
        if (level.getBlockEntity(pos)
                instanceof ThermalBatteryBlockEntity battery) {
            return ThermalBatteryItem.createStoredStack(battery);
        }
        return new ItemStack(
                io.hxneyw.repo.content.Items.THERMAL_BATTERY_ITEM.get()
        );
    }

    @Override
    public BlockState getRotatedBlockState(
            BlockState originalState,
            Direction targetedFace
    ) {
        Direction next = switch (originalState.getValue(FACING)) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.UP;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.NORTH;
        };
        return originalState.setValue(FACING, next);
    }

    @Override
    protected boolean areStatesKineticallyEquivalent(
            BlockState oldState,
            BlockState newState
    ) {
        return super.areStatesKineticallyEquivalent(oldState, newState)
                && oldState.getValue(FACING) == newState.getValue(FACING);
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder);
        builder.add(INDICATOR, POWERED);
    }


    @Override
    public void neighborChanged(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Block block,
            @NotNull BlockPos fromPos,
            boolean isMoving
    ) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) {
            return;
        }

        BlockState current = level.getBlockState(pos);
        if (!current.is(this)) {
            return;
        }

        boolean powered = level.hasNeighborSignal(pos);
        if (current.getValue(POWERED) != powered) {
            level.setBlock(
                    pos,
                    current.setValue(POWERED, powered),
                    Block.UPDATE_CLIENTS
            );
            if (level.getBlockEntity(pos)
                    instanceof ThermalBatteryBlockEntity battery) {
                battery.onRedstonePowerChanged();
            }
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull BlockHitResult hit
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null
                    && level.getBlockEntity(pos)
                    instanceof ThermalBatteryBlockEntity battery) {
                serverPlayer.openMenu(
                        provider,
                        buffer -> ThermalBatteryMenu.writeInitialData(
                                battery,
                                buffer
                        )
                );
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
                instanceof ThermalBatteryBlockEntity battery)) {
            return null;
        }

        return new SimpleMenuProvider(
                (containerId, inventory, player) ->
                        new ThermalBatteryMenu(containerId, inventory, battery),
                Component.translatable(
                        "block.sulfuricresonance.thermal_battery"
                )
        );
    }

    @Override
    public @NotNull Class<ThermalBatteryBlockEntity> getBlockEntityClass() {
        return ThermalBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThermalBatteryBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMAL_BATTERY.get();
    }

    @Override
    protected @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        return Shapes.block();
    }

    public enum IndicatorState implements StringRepresentable {
        HEATED("heated"),
        SUPERHEATED("superheated"),
        FAULT("fault");

        private final String serializedName;

        IndicatorState(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public @NotNull String getSerializedName() {
            return serializedName;
        }
    }
}
