package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MoltenRotorArmPoint extends ArmInteractionPoint {
   public MoltenRotorArmPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
      super(type, level, pos, state);
   }

   public Mode getMode() {
      return Mode.DEPOSIT;
   }

   public ItemStack insert(ArmBlockEntity arm, ItemStack stack, boolean simulate) {
      if (this.level.getBlockEntity(this.pos) instanceof MoltenRotorBlockEntity furnace) {
         MoltenRotorBlockEntity.FuelType fuelType = furnace.getFuelTypeFromItem(stack);
         if (fuelType == null || fuelType == MoltenRotorBlockEntity.FuelType.NONE) {
            return stack;
         } else if (!simulate) {
            boolean success = furnace.insertFuel(stack, false);
            if (!success) {
               return stack;
            } else {
               ItemStack remainder = stack.copy();
               remainder.shrink(1);
               return remainder;
            }
         } else {
            return !this.wouldAcceptFuel(furnace, stack) ? stack : ItemStack.EMPTY;
         }
      } else {
         return stack;
      }
   }

   public ItemStack extract(ArmBlockEntity arm, int amount, boolean simulate) {
      return ItemStack.EMPTY;
   }

   private boolean wouldAcceptFuel(MoltenRotorBlockEntity furnace, ItemStack stack) {
      return furnace.insertFuel(stack, true);
   }
}
