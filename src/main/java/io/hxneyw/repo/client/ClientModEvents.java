package io.hxneyw.repo.client;

import io.hxneyw.repo.content.blocks.client.MoltenRotorRenderer;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerRenderer;
import io.hxneyw.repo.content.particles.AcidDripParticle;
import io.hxneyw.repo.content.particles.CombustionPurpleFlameParticle;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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

   @SubscribeEvent
   public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)ModParticles.COMBUSTION_PURPLE_FLAME.get(), CombustionPurpleFlameParticle.Provider::new);
      event.registerSpriteSet((ParticleType)ModParticles.ACID_DRIP.get(), AcidDripParticle.Provider::new);
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         ItemBlockRenderTypes.setRenderLayer((Fluid)AllModFluids.SULFURIC_ACID.get(), RenderType.translucent());
         ItemBlockRenderTypes.setRenderLayer((Fluid)AllModFluids.SULFURIC_ACID_FLOWING.get(), RenderType.translucent());
         BlockEntityRenderers.register((BlockEntityType)AllBlockEntities.MOLTEN_ROTOR.get(), MoltenRotorRenderer::new);
         EntityRenderers.register((EntityType)ModEntities.PYROCLAST_BOMB.get(), ThrownItemRenderer::new);
         BlockEntityRenderers.register((BlockEntityType)AllBlockEntities.PERFORATED_SPRITZER.get(), PerforatedSpritzerRenderer::new);
      });
   }

   @SubscribeEvent
   public static void registerAdditionalModels(RegisterAdditional event) {
      event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "item/impeller")));
      event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_left")));
      event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_right")));
      event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/molten_rotor_needle")));
   }
}
