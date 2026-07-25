package io.hxneyw.repo.content.registry;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AllVoxelShapes {
   private static VoxelShape rotateHorizontal(VoxelShape shape, int degrees) {
      VoxelShape result = shape;
      int rotations = degrees / 90 % 4;

      for (int i = 0; i < rotations; i++) {
         result = rotate90CW(result);
      }

      return result;
   }

   private static VoxelShape rotate90CW(VoxelShape shape) {
      VoxelShape[] rotated = new VoxelShape[]{Shapes.empty()};
      shape.toAabbs()
         .forEach(box -> rotated[0] = Shapes.join(rotated[0], Shapes.box(1.0 - box.maxZ, box.minY, box.minX, 1.0 - box.minZ, box.maxY, box.maxX), BooleanOp.OR));
      return rotated[0];
   }

   private static VoxelShape rotateVertical(VoxelShape shape, Direction to) {
      VoxelShape[] rotated = new VoxelShape[]{Shapes.empty()};
      if (to == Direction.UP) {
         shape.toAabbs()
            .forEach(
               box -> rotated[0] = Shapes.join(rotated[0], Shapes.box(box.minX, box.minZ, 1.0 - box.maxY, box.maxX, box.maxZ, 1.0 - box.minY), BooleanOp.OR)
            );
      } else {
         shape.toAabbs()
            .forEach(
               box -> rotated[0] = Shapes.join(rotated[0], Shapes.box(box.minX, 1.0 - box.maxZ, box.minY, box.maxX, 1.0 - box.minZ, box.maxY), BooleanOp.OR)
            );
      }

      return rotated[0];
   }

   public static class MoltenRotor {
      private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

      public static VoxelShape getShape(Direction facing) {
         return SHAPES.getOrDefault(facing, SHAPES.get(Direction.SOUTH));
      }

      private static VoxelShape makeMoltenRotorShape() {
         VoxelShape shape = Shapes.empty();
         shape = Shapes.join(shape, Shapes.box(0.4375, 0.9375, 0.375, 0.5, 1.0, 0.4375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0, 0.9375, 0.125, 0.0625), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.1875, 0.9375, 0.8125, 0.875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.625, 0.625, 0.0571875, 0.8125, 0.8125, 0.0578125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.5625, 0.5625, 0.0625, 0.875, 0.875, 0.125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.6375, 0.6375, 0.03125, 0.675, 0.675, 0.0625), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.1875, 0.9375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.75, 0.9375, 0.3125, 0.8125, 1.0, 0.625), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.6875, 0.6875, 1.0, 0.75), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.1875, 0.9375, 0.4375, 0.25, 1.0, 0.625), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.3125, 0.3125, 1.0, 0.375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.375, 0.3125, 1.0, 0.4375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.25, 0.9375, 0.625, 0.3125, 1.0, 0.6875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.4375, 0.625, 1.0, 0.5), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.5, 0.9375, 0.375, 0.5625, 1.0, 0.4375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.3125, 0.4375, 1.0, 0.375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.375, 0.9375, 0.25, 0.4375, 1.0, 0.3125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.9375, 0.9375, 0.125, 1.0), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.5, 0.625, 1.0, 0.5625), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.3125, 0.9375, 0.625, 0.375, 1.0, 0.6875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.6875, 0.9375, 0.625, 0.75, 1.0, 0.6875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.6875, 0.9375, 0.25, 0.75, 1.0, 0.3125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.5625, 0.9375, 0.1875, 0.625, 1.0, 0.25), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.625, 0.9375, 0.25, 0.6875, 1.0, 0.3125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.1875, 0.875, 0.875, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.125, 0.875, 0.1875, 0.875, 0.9375, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.125, 0.875, 0.875, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.125, 0.75, 0.125, 0.1875, 0.8125, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.8125, 0.75, 0.125, 0.875, 0.8125, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.1875, 0.5, 0.125, 0.25, 0.8125, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.75, 0.5, 0.125, 0.8125, 0.8125, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.8125, 0.25, 0.125, 0.9375, 0.75, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.25, 0.125, 0.1875, 0.75, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.25, 0.625, 0.125, 0.75, 0.8125, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.25, 0.5625, 0.125, 0.3125, 0.625, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.5625, 0.5625, 0.125, 0.75, 0.625, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.125, 0.9375, 0.25, 0.1875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.875, 0.9375, 0.8125, 0.875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.33125, 0.1875, 0.78125, 0.3625, 0.225, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.33125, 0.225, 0.79375, 0.34375, 0.24375, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.34375, 0.225, 0.78125, 0.3625, 0.24375, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.33125, 0.1875, 0.8125, 0.425, 0.28125, 0.874375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.3625, 0.1875, 0.78125, 0.425, 0.28125, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.46875, 0.45, 0.71875, 0.5290625, 0.5125, 0.76875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.475, 0.45625, 0.69375, 0.5240625, 0.5053125, 0.71875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.4625, 0.1875, 0.71875, 0.5375, 0.45, 0.79375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.32235125, 0.2, 0.6816525, 0.37235125, 0.2625, 0.8285275), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.575, 0.1875, 0.8125, 0.66875, 0.28125, 0.874375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.65625, 0.225, 0.79375, 0.66875, 0.24375, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.6375, 0.225, 0.78125, 0.65625, 0.24375, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.575, 0.1875, 0.78125, 0.6375, 0.28125, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.6375, 0.1875, 0.78125, 0.66875, 0.225, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.9375, 0.1875, 0.1875, 0.94375, 0.8125, 0.875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.05625, 0.1875, 0.1875, 0.0625, 0.8125, 0.875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.875, 0.8125, 0.1875, 0.9375, 0.8125, 0.875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.1875, 0.125, 0.8125, 0.875), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.8125, 0.75, 0.8125, 0.875, 0.8125, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.125, 0.75, 0.8125, 0.1875, 0.8125, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.875, 0.8125, 0.1875, 0.9375, 0.875, 0.8125), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.1875, 0.125, 0.875, 0.8125), BooleanOp.OR);
         return Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.8125, 0.875, 0.875, 0.875), BooleanOp.OR);
      }

      static {
         VoxelShape baseShape = makeMoltenRotorShape();
         SHAPES.put(Direction.NORTH, baseShape);
         SHAPES.put(Direction.SOUTH, AllVoxelShapes.rotateHorizontal(baseShape, 180));
         SHAPES.put(Direction.EAST, AllVoxelShapes.rotateHorizontal(baseShape, 90));
         SHAPES.put(Direction.WEST, AllVoxelShapes.rotateHorizontal(baseShape, 270));
         SHAPES.put(Direction.UP, AllVoxelShapes.rotateVertical(baseShape, Direction.UP));
         SHAPES.put(Direction.DOWN, AllVoxelShapes.rotateVertical(baseShape, Direction.DOWN));
      }
   }

   public static class RubberPadding {
      private static final VoxelShape SHAPE = makeRubberPaddingShape();

      public static VoxelShape getShape() {
         return SHAPE;
      }

      private static VoxelShape makeRubberPaddingShape() {
         VoxelShape shape = Shapes.empty();
         shape = Shapes.join(shape, Shapes.box(0.9375, 0.0, 0.9375, 1.0, 0.1875, 1.0), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.25, 0.0625, 0.9375, 0.3125, 0.9375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0, 0.0, 0.0, 0.0625, 0.1875, 0.0625), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0, 0.0, 0.0625, 0.0625, 0.25, 0.9375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.0, 0.0, 0.9375, 0.25, 0.9375), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0, 0.0, 0.9375, 0.9375, 0.1875, 1.0), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.0625, 0.1875, 0.9375, 0.9375, 0.25, 1.0), BooleanOp.OR);
         shape = Shapes.join(shape, Shapes.box(0.9375, 0.0, 0.0, 1.0, 0.1875, 0.0625), BooleanOp.OR);
         return Shapes.join(shape, Shapes.box(0.9375, 0.0, 0.0625, 1.0, 0.25, 0.9375), BooleanOp.OR);
      }
   }
}
