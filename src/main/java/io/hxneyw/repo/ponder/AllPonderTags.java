package io.hxneyw.repo.ponder;

import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class AllPonderTags {
   public static final ResourceLocation FLUIDS = ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "fluids");

   public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
      PonderTagRegistrationHelper<Block> HELPER = helper.withKeyFunction(BuiltInRegistries.BLOCK::getKey);
      helper.registerTag(FLUIDS)
         .addToIndex()
         .item(AllModBlocks.PERFORATED_SPRITZER.get(), true, false)
         .title("Fluid Handling")
         .description("Components that handle fluids in Sulfuric Resonance")
         .register();
      HELPER.addToTag(FLUIDS).add(AllModBlocks.PERFORATED_SPRITZER.get());
      HELPER.addToTag(ResourceLocation.parse("create:fluids")).add(AllModBlocks.PERFORATED_SPRITZER.get());
   }
}
