package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RubberPaddingArmPoint extends ArmInteractionPoint {
   public RubberPaddingArmPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
      super(type, level, pos, state);
   }

   protected Vec3 getInteractionPositionVector() {
      return Vec3.atLowerCornerOf(this.pos).add(0.5, 0.875, 0.5);
   }

   public ItemStack insert(ArmBlockEntity arm, ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
         return stack;
      } else {
         if (!simulate) {
            Vec3 spawnPos = this.getInteractionPositionVector();
            ItemEntity itemEntity = new ItemEntity(this.level, spawnPos.x, spawnPos.y, spawnPos.z, stack.copy());
            itemEntity.setDeltaMovement(0.0, -0.1, 0.0);
            itemEntity.setPickUpDelay(10);
            this.level.addFreshEntity(itemEntity);
         }

         return ItemStack.EMPTY;
      }
   }

   public ItemStack extract(ArmBlockEntity arm, int slot, int amount, boolean simulate) {
      AABB searchBox = new AABB(this.pos).inflate(0.5, 0.5, 0.5);
      List<ItemEntity> items = this.level.getEntitiesOfClass(ItemEntity.class, searchBox);
      if (items.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemEntity itemEntity = items.getFirst();
         ItemStack stackOnPadding = itemEntity.getItem();
         if (stackOnPadding.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            int extractCount = Math.min(amount, stackOnPadding.getCount());
            ItemStack extracted = stackOnPadding.copy();
            extracted.setCount(extractCount);
            if (!simulate) {
               stackOnPadding.shrink(extractCount);
               if (stackOnPadding.isEmpty()) {
                  itemEntity.discard();
               } else {
                  itemEntity.setItem(stackOnPadding);
               }
            }

            return extracted;
         }
      }
   }

   public int getSlotCount(ArmBlockEntity armBlockEntity) {
      AABB searchBox = new AABB(this.pos).inflate(0.5, 0.5, 0.5);
      List<ItemEntity> items = this.level.getEntitiesOfClass(ItemEntity.class, searchBox);
      return items.size();
   }
}
