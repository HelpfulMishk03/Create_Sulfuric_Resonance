package io.hxneyw.repo.client;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(
        modid = "sulfuricresonance",
        value = Dist.CLIENT
)
public class UniversalTooltipHandler {

   @SubscribeEvent
   public static void onItemTooltip(ItemTooltipEvent event) {
      Item item = event.getItemStack().getItem();
      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

      if (!"sulfuricresonance".equals(itemId.getNamespace())) {
         return;
      }

      addSulfuricResonanceTooltip(event, itemId);

      TooltipModifier modifier = TooltipModifier.REGISTRY.get(item);

      if (modifier != null) {
         modifier.modify(event);
         return;
      }

      ItemDescription description =
              ItemDescription.create(item, Palette.STANDARD_CREATE);

      if (description != null) {
         event.getToolTip().addAll(1, description.getCurrentLines());
      }
   }

   private static void addSulfuricResonanceTooltip(
           ItemTooltipEvent event,
           ResourceLocation itemId
   ) {
      String path = itemId.getPath();
      List<Component> lines = new ArrayList<>();

      switch (path) {
         case "sulfur" -> {
            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.sulfur.reactive"
            ).withStyle(ChatFormatting.YELLOW));

            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.sulfur.uses"
            ).withStyle(ChatFormatting.GRAY));
         }

         case "sulfur_block" -> {
            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.sulfur_block.storage"
            ).withStyle(ChatFormatting.GRAY));

            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.sulfur_block.unpack"
            ).withStyle(ChatFormatting.GRAY));
         }

         case "sulfuric_acid_bucket" -> {
            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.acid.corrosive"
            ).withStyle(ChatFormatting.DARK_RED));

            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.acid.reactions"
            ).withStyle(ChatFormatting.GOLD));

            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.acid.industrial"
            ).withStyle(ChatFormatting.GRAY));

            lines.add(Component.translatable(
                    "tooltip.sulfuricresonance.acid.warning"
            ).withStyle(ChatFormatting.RED));
         }
      }

      if (!lines.isEmpty()) {
         event.getToolTip().addAll(1, lines);
      }

      if (event.getFlags().isAdvanced()) {
         switch (path) {
            case "sulfur" -> event.getToolTip().add(
                    Component.literal("c:sulfur / c:dusts/sulfur")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );

            case "sulfuric_acid_bucket" -> event.getToolTip().add(
                    Component.literal("c:buckets/sulfuric_acid")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
         }
      }
   }
}