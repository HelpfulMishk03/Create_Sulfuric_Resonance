package io.hxneyw.repo.client;

import com.simibubi.create.content.processing.basin.BasinRenderer;
import com.simibubi.create.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampRenderer;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorRenderer;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerRenderer;
import io.hxneyw.repo.content.particles.AcidDripParticle;
import io.hxneyw.repo.content.particles.CombustionPurpleFlameParticle;
import io.hxneyw.repo.content.registry.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;

@EventBusSubscriber(
        modid = "sulfuricresonance",
        value = {Dist.CLIENT}
)
public class ClientModEvents {
   public static final PartialModel ROTOR_SHAFT_LEFT = new PartialModel(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_left"));
   public static final PartialModel ROTOR_SHAFT_RIGHT = new PartialModel(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_right"));
   public static final PartialModel ROTOR_HEAT_NEEDLE = new PartialModel(
           ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/molten_rotor_needle")
   );
   private static final ModelResourceLocation[]
           COMBUSTION_BELT_MODELS = {
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/particle"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/start"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/start_bottom"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/middle"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/middle_bottom"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/end"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/end_bottom"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/diagonal_start"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/diagonal_middle"
                   )
           ),
           ModelResourceLocation.standalone(
                   ResourceLocation.fromNamespaceAndPath(
                           "sulfuricresonance",
                           "block/combustion_belt/diagonal_end"
                   )
           )
   };

   @SubscribeEvent
   public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet(ModParticles.COMBUSTION_PURPLE_FLAME.get(), CombustionPurpleFlameParticle.Provider::new);
      event.registerSpriteSet(ModParticles.ACID_DRIP.get(), AcidDripParticle.Provider::new);
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         ItemBlockRenderTypes.setRenderLayer(AllModFluids.SULFURIC_ACID.get(), RenderType.translucent());
         ItemBlockRenderTypes.setRenderLayer(AllModFluids.SULFURIC_ACID_FLOWING.get(), RenderType.translucent());
         BlockEntityRenderers.register(AllBlockEntities.MOLTEN_ROTOR.get(), MoltenRotorRenderer::new);
         EntityRenderers.register(ModEntities.PYROCLAST_BOMB.get(), ThrownItemRenderer::new);
         BlockEntityRenderers.register(AllBlockEntities.PERFORATED_SPRITZER.get(), PerforatedSpritzerRenderer::new);
         CreateRegistrate.connectedTextures(
                 () -> new SimpleCTBehaviour(ModSpriteShifts.ASHESIL)
         ).accept(AllModBlocks.ASHESIL.get());
         CreateRegistrate.connectedTextures(
                 () -> new GlassPaneCTBehaviour(ModSpriteShifts.ASHESIL)
         ).accept(AllModBlocks.ASHESIL_PANE.get());
      });
   }

   @SubscribeEvent
   public static void registerBlockEntityRenderers(
           EntityRenderersEvent.RegisterRenderers event
   ) {
      event.registerBlockEntityRenderer(
              AllBlockEntities.ASH_CERAMIC_CRUCIBLE.get(),
              BasinRenderer::new

      );

      event.registerBlockEntityRenderer(
              AllBlockEntities.LIVING_EMBER_LAMP.get(),
              LivingEmberLampRenderer::new
      );

   }

   @SubscribeEvent
   public static void registerAdditionalModels(RegisterAdditional event) {
       CombustionBeltClientAssets.init();
      ROTOR_SHAFT_LEFT.invalidate();
      ROTOR_SHAFT_RIGHT.invalidate();
      ROTOR_HEAT_NEEDLE.invalidate();

      event.register(ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(
                      "sulfuricresonance",
                      "item/impeller"
              )
      ));
      event.register(ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(
                      "sulfuricresonance",
                      "block/rotor_shaft_left"
              )
      ));
      event.register(ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(
                      "sulfuricresonance",
                      "block/rotor_shaft_right"
              )
      ));
      event.register(ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(
                      "sulfuricresonance",
                      "block/molten_rotor_needle"
              )
      ));

      for (ModelResourceLocation model : COMBUSTION_BELT_MODELS) {
         event.register(model);
      }
   }
}
