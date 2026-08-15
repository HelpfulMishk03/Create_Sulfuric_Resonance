package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public final class RubberPaddingArmPoint extends ArmInteractionPoint {
   public RubberPaddingArmPoint(
           ArmInteractionPointType type,
           Level level,
           BlockPos pos,
           BlockState state
   ) {
      super(type, level, pos, state);
   }

   @Override
   protected Vec3 getInteractionPositionVector() {
      return Vec3.atLowerCornerOf(this.pos).add(0.5, 0.91, 0.5);
   }

   @Override
   public ItemStack insert(
           ArmBlockEntity arm,
           ItemStack stack,
           boolean simulate
   ) {
      if (stack.isEmpty()) {
         return stack;
      }

      ItemEntity existing = this.findHeldItem();
      int accepted;

      if (existing == null) {
         accepted = Math.min(stack.getCount(), stack.getMaxStackSize());
      } else {
         ItemStack held = existing.getItem();
         if (!ItemStack.isSameItemSameComponents(held, stack)) {
            return stack;
         }

         accepted = Math.clamp(held.getMaxStackSize() - held.getCount(), 0,
                 stack.getCount());
      }

      if (accepted <= 0) {
         return stack;
      }

      if (!simulate) {
         if (existing == null) {
            Vec3 spawnPos = this.getInteractionPositionVector();
            ItemStack deposited = stack.copyWithCount(accepted);
            ItemEntity itemEntity = new ItemEntity(
                    this.level,
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    deposited
            );
            itemEntity.setDeltaMovement(Vec3.ZERO);
            itemEntity.setPickUpDelay(10);
            this.level.addFreshEntity(itemEntity);
         } else {
            ItemStack held = existing.getItem().copy();
            held.grow(accepted);
            existing.setItem(held);
         }
      }

      ItemStack remainder = stack.copy();
      remainder.shrink(accepted);
      return remainder;
   }

   @Override
   public ItemStack extract(
           ArmBlockEntity arm,
           int slot,
           int amount,
           boolean simulate
   ) {
      if (slot != 0 || amount <= 0) {
         return ItemStack.EMPTY;
      }

      ItemEntity itemEntity = this.findHeldItem();
      if (itemEntity == null) {
         return ItemStack.EMPTY;
      }

      ItemStack held = itemEntity.getItem();
      int extractCount = Math.min(amount, held.getCount());
      ItemStack extracted = held.copyWithCount(extractCount);

      if (!simulate) {
         ItemStack remainder = held.copy();
         remainder.shrink(extractCount);
         if (remainder.isEmpty()) {
            itemEntity.discard();
         } else {
            itemEntity.setItem(remainder);
         }
      }

      return extracted;
   }

   @Override
   public int getSlotCount(ArmBlockEntity arm) {
      return this.findHeldItem() == null ? 0 : 1;
   }

   private ItemEntity findHeldItem() {
      Vec3 center = this.getInteractionPositionVector();
      AABB searchBox = new AABB(
              this.pos.getX() + 0.16,
              this.pos.getY() + 0.72,
              this.pos.getZ() + 0.16,
              this.pos.getX() + 0.84,
              this.pos.getY() + 1.22,
              this.pos.getZ() + 0.84
      );

      List<ItemEntity> items = this.level.getEntitiesOfClass(
              ItemEntity.class,
              searchBox,
              item -> item.isAlive() && !item.getItem().isEmpty()
      );

      return items.stream()
              .min(Comparator.comparingDouble(item -> item.distanceToSqr(center)))
              .orElse(null);
   }
}
