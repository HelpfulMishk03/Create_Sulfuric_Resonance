package io.hxneyw.repo.content.blocks.thermochemicalconduit;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ThermochemicalConduitBlock
        extends RotatedPillarKineticBlock
        implements IBE<ThermochemicalConduitBlockEntity> {

    /*
     * Central housing:
     * 8 × 8 × 8 pixels.
     */
    private static final VoxelShape BODY =
            Block.box(
                    4, 4, 4,
                    12, 12, 12
            );

    /*
     * Four-pixel shaft passage extending completely through
     * the block along the selected axis.
     */
    private static final VoxelShape X_SHAPE =
            Shapes.or(
                    BODY,
                    Block.box(
                            0, 6, 6,
                            16, 10, 10
                    )
            );

    private static final VoxelShape Y_SHAPE =
            Shapes.or(
                    BODY,
                    Block.box(
                            6, 0, 6,
                            10, 16, 10
                    )
            );

    private static final VoxelShape Z_SHAPE =
            Shapes.or(
                    BODY,
                    Block.box(
                            6, 6, 0,
                            10, 10, 16
                    )
            );

    public ThermochemicalConduitBlock(Properties properties) {
        super(properties);
    }

    /*
     * Only the two faces aligned with the conduit axis
     * expose kinetic shaft connections.
     */
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
            BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return getShapeForAxis(state.getValue(AXIS));
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos position,
            @NotNull CollisionContext context
    ) {
        return getShapeForAxis(state.getValue(AXIS));
    }

    private static VoxelShape getShapeForAxis(Axis axis) {
        return switch (axis) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
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