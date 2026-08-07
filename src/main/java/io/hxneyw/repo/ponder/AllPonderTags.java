package io.hxneyw.repo.ponder;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class AllPonderTags {
   public static final ResourceLocation FLUIDS = ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "fluids");
   public static final ResourceLocation REACTIVE_HEAT =
           ResourceLocation.fromNamespaceAndPath(
                   CreateSulfuricResonance.MODID,
                   "reactive_heat"
           );
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
      helper.registerTag(REACTIVE_HEAT)
              .addToIndex()
              .item(
                      AllModBlocks.MOLTEN_ROTOR_FURNACE.get(),
                      true,
                      false
              )
              .title("Reactive Heat")
              .description(
                      "Generate thermochemical heat, route it through physical kinetic connections, renew its reach with Conduits, and deliver it to Combustion Belts."
              )
              .register();
      helper.addToTag(REACTIVE_HEAT)
              .add(
                      BuiltInRegistries.BLOCK.getKey(
                              AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                      )
              )
              .add(
                      BuiltInRegistries.BLOCK.getKey(
                              AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                      )
              )
              .add(
                      BuiltInRegistries.BLOCK.getKey(
                              AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                      )
              )
              .add(
                      BuiltInRegistries.BLOCK.getKey(
                              AllModBlocks.THERMOCHEMICAL_GEARBOX.get()
                      )
              )
              .add(
                      BuiltInRegistries.ITEM.getKey(
                              Items.COMBUSTION_BELT_CONNECTOR.get()
                      )
              );
   }
}
