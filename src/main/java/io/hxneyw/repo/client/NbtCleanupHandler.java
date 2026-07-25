package io.hxneyw.repo.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre;

@EventBusSubscriber(
   modid = "sulfuricresonance"
)
public class NbtCleanupHandler {
   private static final String[] RUBBER_PADDING_KEYS = new String[]{
      "RubberPaddingBounced", "RubberPaddingBounceCount", "RubberPaddingLastBounceTime", "RubberPaddingLastYPos", "RubberPaddingSettling"
   };

   @SubscribeEvent
   public static void onItemPickup(Pre event) {
      ItemEntity itemEntity = event.getItemEntity();
      CompoundTag persistentData = itemEntity.getPersistentData();

      for (String key : RUBBER_PADDING_KEYS) {
         if (persistentData.contains(key)) {
            persistentData.remove(key);
         }
      }
   }
}
