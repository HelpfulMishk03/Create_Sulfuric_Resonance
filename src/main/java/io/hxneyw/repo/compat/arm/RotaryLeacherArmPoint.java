package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import io.hxneyw.repo.content.blocks.rotaryleacher.RotaryLeacherBlock;
import io.hxneyw.repo.content.blocks.rotaryleacher.RotaryLeacherBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public final class RotaryLeacherArmPoint extends ArmInteractionPoint {
   public RotaryLeacherArmPoint(
           ArmInteractionPointType type,
           Level level,
           BlockPos pos,
           BlockState state
   ) {
      super(type, level, pos, state);
   }

   @Override
   protected Vec3 getInteractionPositionVector() {
      Direction facing = getMachineFacing();
      return Vec3.atCenterOf(pos).add(
              facing.getStepX() * 0.46D,
              0.12D,
              facing.getStepZ() * 0.46D
      );
   }

   @Override
   protected Direction getInteractionDirection() {
      return getMachineFacing();
   }

   @Override
   public ItemStack insert(ArmBlockEntity arm, ItemStack stack, boolean simulate) {
      IItemHandler inventory = getLeacherInventory();
      return inventory == null ? stack : ItemHandlerHelper.insertItem(inventory, stack, simulate);
   }

   @Override
   public ItemStack extract(ArmBlockEntity arm, int slot, int amount, boolean simulate) {
      if (slot != RotaryLeacherBlockEntity.OUTPUT
              && slot != RotaryLeacherBlockEntity.OUTPUT_OVERFLOW
              && slot != RotaryLeacherBlockEntity.BYPRODUCT) {
         return ItemStack.EMPTY;
      }
      IItemHandler inventory = getLeacherInventory();
      return inventory == null ? ItemStack.EMPTY : inventory.extractItem(slot, amount, simulate);
   }

   @Override
   public int getSlotCount(ArmBlockEntity arm) {
      IItemHandler inventory = getLeacherInventory();
      return inventory == null ? 0 : inventory.getSlots();
   }

   private IItemHandler getLeacherInventory() {
      if (level.getBlockEntity(pos) instanceof RotaryLeacherBlockEntity leacher) {
         return leacher.getManualItemCapability();
      }
      return null;
   }

   private Direction getMachineFacing() {
      BlockState state = level.getBlockState(pos);
      return state.hasProperty(RotaryLeacherBlock.HORIZONTAL_FACING)
              ? state.getValue(RotaryLeacherBlock.HORIZONTAL_FACING)
              : Direction.NORTH;
   }
}
