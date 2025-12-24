package io.hxneyw.repo.content.blocks.behaviour;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom collision shapes for the Molten Rotor Furnace
 * FIXED: Proper rotation for all directions
 */
public class MoltenRotorShapes {

    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    static {
        // Create base shape - this is designed for SOUTH facing
        VoxelShape baseShape = makeBaseShape();

        // Cache rotated versions for all directions
        // The base shape is for SOUTH, so we rotate from there
        SHAPES.put(Direction.SOUTH, baseShape);  // Base direction
        SHAPES.put(Direction.NORTH, rotateHorizontal(baseShape, 180));  // 180° from SOUTH
        SHAPES.put(Direction.EAST, rotateHorizontal(baseShape, 270));   // 90° CCW from SOUTH
        SHAPES.put(Direction.WEST, rotateHorizontal(baseShape, 90));    // 90° CW from SOUTH
        SHAPES.put(Direction.UP, rotateVertical(baseShape, Direction.UP));
        SHAPES.put(Direction.DOWN, rotateVertical(baseShape, Direction.DOWN));
    }

    public static VoxelShape getShape(Direction facing) {
        return SHAPES.getOrDefault(facing, SHAPES.get(Direction.SOUTH));
    }

    /**
     * Base collision shape - YOUR VOXEL DATA
     * This shape is designed for SOUTH facing (gauge on south side)
     */
    private static VoxelShape makeBaseShape() {
        VoxelShape shape = Shapes.empty();

        // YOUR VOXEL DATA HERE (unchanged)
        shape = Shapes.join(shape, Shapes.box(0.5, 0.9375, 0.5625, 0.5625, 1, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.9375, 0.9375, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.125, 0.9375, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.625, 0.9421875, 0.375, 0.8125, 0.9428125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.5625, 0.875, 0.4375, 0.875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.328125, 0.671875, 0.921875, 0.359375, 0.796875, 0.965625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.325, 0.6375, 0.9375, 0.3625, 0.675, 0.96875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.375, 0.375, 1, 0.625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.375, 0.375, 0.125, 0.625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.1875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.9375, 0.375, 0.25, 1, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.9375, 0.25, 0.625, 1, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.9375, 0.375, 0.8125, 1, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.9375, 0.625, 0.75, 1, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.9375, 0.5625, 0.75, 1, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.9375, 0.3125, 0.75, 1, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.5, 0.4375, 1, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.9375, 0.5625, 0.5, 1, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.625, 0.625, 1, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.6875, 0.625, 1, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0, 0.9375, 0.125, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.4375, 0.4375, 1, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.9375, 0.3125, 0.6875, 1, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.3125, 0.3125, 1, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.6875, 0.3125, 1, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.75, 0.4375, 1, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.9375, 0.6875, 0.375, 1, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.1875, 0.875, 0.875, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.875, 0.1875, 0.875, 0.9375, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.8125, 0.8125, 0.875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.75, 0.8125, 0.875, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.75, 0.8125, 0.1875, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.5, 0.8125, 0.8125, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5, 0.8125, 0.25, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.25, 0.8125, 0.1875, 0.75, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.25, 0.8125, 0.9375, 0.75, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.625, 0.8125, 0.75, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.5625, 0.8125, 0.75, 0.625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.5625, 0.8125, 0.4375, 0.625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.8125, 0.9375, 0.25, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.125, 0.9375, 0.8125, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.05625, 0.1875, 0.125, 0.0625, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.375, 0.375, 0.875, 0.625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.1875, 0.125, 0.9375, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.8125, 0.125, 0.9375, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.125, 0.125, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.75, 0.8125, 0.875, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.75, 0.8125, 0.1875, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.125, 0.875, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.8125, 0.1875, 0.9375, 0.875, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.1875, 0.125, 0.875, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.8125, 0.8125, 0.875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.125, 0.875, 0.875, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.874375, 0.1875, 0.125, 0.874375, 0.8125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125625, 0.1875, 0.125, 0.125625, 0.8125, 0.8125), BooleanOp.OR);

        return shape;
    }

    /**
     * Rotate shape horizontally by specified degrees
     */
    private static VoxelShape rotateHorizontal(VoxelShape shape, int degrees) {
        VoxelShape result = shape;
        int rotations = (degrees / 90) % 4;

        for (int i = 0; i < rotations; i++) {
            result = rotate90CW(result);
        }

        return result;
    }

    /**
     * Rotate 90 degrees clockwise around Y-axis
     */
    private static VoxelShape rotate90CW(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.toAabbs().forEach(box -> {
            // 90° CW: (x, y, z) -> (1-z, y, x)
            rotated[0] = Shapes.join(rotated[0], Shapes.box(
                    1 - box.maxZ, box.minY, box.minX,
                    1 - box.minZ, box.maxY, box.maxX
            ), BooleanOp.OR);
        });
        return rotated[0];
    }

    /**
     * Rotate shape for vertical directions (UP, DOWN)
     */
    private static VoxelShape rotateVertical(VoxelShape shape, Direction to) {
        VoxelShape[] rotated = {Shapes.empty()};

        if (to == Direction.UP) {
            shape.toAabbs().forEach(box -> {
                rotated[0] = Shapes.join(rotated[0], Shapes.box(
                        box.minX, box.minZ, 1 - box.maxY,
                        box.maxX, box.maxZ, 1 - box.minY
                ), BooleanOp.OR);
            });
        } else { // DOWN
            shape.toAabbs().forEach(box -> {
                rotated[0] = Shapes.join(rotated[0], Shapes.box(
                        box.minX, 1 - box.maxZ, box.minY,
                        box.maxX, 1 - box.minZ, box.maxY
                ), BooleanOp.OR);
            });
        }

        return rotated[0];
    }
}