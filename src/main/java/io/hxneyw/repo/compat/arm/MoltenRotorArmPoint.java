package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Deposit-only Mechanical Arm target for the Molten Rotor fuel chamber. */
public final class MoltenRotorArmPoint extends ArmInteractionPoint {
   public MoltenRotorArmPoint(
           ArmInteractionPointType type,
           Level level,
           BlockPos pos,
           BlockState state
   ) {
      super(type, level, pos, state);
   }

   @Override
   public Mode getMode() {
      return Mode.DEPOSIT;
   }

   @Override
   protected Vec3 getInteractionPositionVector() {
      BlockState currentState = this.level.getBlockState(this.pos);
      Direction facing = currentState.hasProperty(MoltenRotorBlock.FACING)
              ? currentState.getValue(MoltenRotorBlock.FACING)
              : Direction.NORTH;

      return Vec3.atCenterOf(this.pos).add(
              facing.getStepX() * 0.38,
              -0.14,
              facing.getStepZ() * 0.38
      );
   }


   @Override
   protected Direction getInteractionDirection() {
      BlockState currentState = this.level.getBlockState(this.pos);
      return currentState.hasProperty(MoltenRotorBlock.FACING)
              ? currentState.getValue(MoltenRotorBlock.FACING)
              : Direction.NORTH;
   }

   @Override
   public ItemStack insert(
           ArmBlockEntity arm,
           ItemStack stack,
           boolean simulate
   ) {
      if (!(this.level.getBlockEntity(this.pos)
              instanceof MoltenRotorBlockEntity furnace)) {
         return stack;
      }

      if (stack.isEmpty() || !furnace.insertFuel(stack, true)) {
         return stack;
      }

      if (!simulate && !furnace.insertFuel(stack, false)) {
         return stack;
      }

      ItemStack remainder = stack.copy();
      remainder.shrink(1);
      return remainder;
   }

   @Override
   public ItemStack extract(
           ArmBlockEntity arm,
           int slot,
           int amount,
           boolean simulate
   ) {
      return ItemStack.EMPTY;
   }
}
