package io.hxneyw.repo.content.items;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NetherwoodDustItem extends Item {
   public NetherwoodDustItem(Properties properties) {
      super(properties);
   }

   public boolean onEntityItemUpdate(@NotNull ItemStack stack, @NotNull ItemEntity entity) {
      return false;
   }
}
