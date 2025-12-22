package io.hxneyw.repo.content.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom collision shapes for the Molten Rotor Furnace
 * Auto-generated from Blockbench, then rotated for all directions
 */
public class MoltenRotorShapes {

    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    static {
        // Create base shape (facing NORTH)
        VoxelShape baseShape = makeBaseShape();

        // Cache rotated versions for all directions
        SHAPES.put(Direction.NORTH, baseShape);
        SHAPES.put(Direction.SOUTH, rotateShape(baseShape, Direction.SOUTH));
        SHAPES.put(Direction.EAST, rotateShape(baseShape, Direction.EAST));
        SHAPES.put(Direction.WEST, rotateShape(baseShape, Direction.WEST));
        SHAPES.put(Direction.UP, rotateShapeVertical(baseShape, Direction.UP));
        SHAPES.put(Direction.DOWN, rotateShapeVertical(baseShape, Direction.DOWN));
    }

    public static VoxelShape getShape(Direction facing) {
        return SHAPES.getOrDefault(facing, SHAPES.get(Direction.NORTH));
    }

    /**
     * Base collision shape - auto-generated from your Blockbench model
     */
    private static VoxelShape makeBaseShape() {
        VoxelShape shape = Shapes.empty();

        // Generated from Blockbench - all coordinates preserved
        shape = Shapes.join(shape, Shapes.box(0.625, 0.625, 0.05718749999999995, 0.8125, 0.8125, 0.057812500000000044), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.5625, 0.0625, 0.875, 0.875, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.375, 0.375, 0.125, 0.625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.1875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.9375, 0.25, 0.6875, 1, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.9375, 0.25, 0.75, 1, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.9375, 0.3125, 0.8125, 1, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.1875, 0.625, 1, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.9375, 0.625, 0.75, 1, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.6875, 0.6875, 1, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.9375, 0.625, 0.375, 1, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.625, 0.3125, 1, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.9375, 0.4375, 0.25, 1, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.375, 0.3125, 1, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.3125, 0.3125, 1, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.4375, 0.625, 1, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.5, 0.625, 1, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.9375, 0.375, 0.5625, 1, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.3125, 0.4375, 1, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.9375, 0.375, 0.5, 1, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.25, 0.4375, 1, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.9375, 0.9375, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.125, 0.9375, 0.25, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.25, 0.125, 0.1875, 0.75, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.25, 0.125, 0.9375, 0.75, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.5, 0.125, 0.8125, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.75, 0.125, 0.875, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5, 0.125, 0.25, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.75, 0.125, 0.1875, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.625, 0.125, 0.75, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.5625, 0.125, 0.75, 0.625, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.5625, 0.125, 0.3125, 0.625, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.125, 0.875, 0.875, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6375, 0.6375, 0.03125, 0.675, 0.675, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.640625, 0.671875, 0.034375000000000044, 0.671875, 0.796875, 0.078125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0, 0.9375, 0.125, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.375, 0.375, 1, 0.625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.375, 0.375, 0.875, 0.625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.1875, 0.9375, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.1875, 0.1875, 0.9437500000000001, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.875, 0.9375, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.1875, 0.0625, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.1875, 0.125, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.8125, 0.1875, 0.9375, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.75, 0.1875, 0.125, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.75, 0.1875, 0.9375, 0.8125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.1875, 0.875, 0.875, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.8125, 0.875, 0.8125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.875, 0.1875, 0.875, 0.9375, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);

        return shape;
    }

    /**
     * Rotate shape for horizontal directions (SOUTH, EAST, WEST)
     */
    private static VoxelShape rotateShape(VoxelShape shape, Direction to) {
        if (to == Direction.NORTH) return shape;

        int rotations = switch (to) {
            case SOUTH -> 2;
            case WEST -> 1;
            case EAST -> 3;
            default -> 0;
        };

        for (int i = 0; i < rotations; i++) {
            shape = rotateShapeOnce(shape);
        }

        return shape;
    }

    /**
     * Rotate 90 degrees clockwise around Y-axis
     */
    private static VoxelShape rotateShapeOnce(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.toAabbs().forEach(box -> {
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
    private static VoxelShape rotateShapeVertical(VoxelShape shape, Direction to) {
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