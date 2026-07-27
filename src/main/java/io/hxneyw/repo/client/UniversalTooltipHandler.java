package io.hxneyw.repo.client;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(
        modid = "sulfuricresonance",
        value = {Dist.CLIENT}
)
public class UniversalTooltipHandler {
   @SubscribeEvent
   public static void onItemTooltip(ItemTooltipEvent event) {
      Item item = event.getItemStack().getItem();
      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
      if (itemId.getNamespace().equals("sulfuricresonance")) {
         TooltipModifier modifier = TooltipModifier.REGISTRY.get(item);
         if (modifier != null) {
            modifier.modify(event);
         } else {
            ItemDescription description = ItemDescription.create(item, Palette.STANDARD_CREATE);
            if (description != null) {
               event.getToolTip().addAll(1, description.getCurrentLines());
            }
         }
      }
   }
}
