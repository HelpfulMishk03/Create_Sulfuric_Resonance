package io.hxneyw.repo.client;

import io.hxneyw.repo.content.blocks.client.MoltenRotorRenderer;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.particles.CombustionPurpleFlameParticle;
import io.hxneyw.repo.content.registry.ModBlockEntities;
import io.hxneyw.repo.content.registry.ModParticles;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

//import net.minecraft.client.renderer.blockentity.BlockEntityRenderez@EventBusSubscriber(modid = "sulfuricresonance", value = Dist.CLIENT)
@EventBusSubscriber(modid = "sulfuricresonance", value = Dist.CLIENT)
public class ClientModEvents {
    // Define partial models
    public static final PartialModel ROTOR_SHAFT_LEFT = new PartialModel(
            ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_left")
    );

    public static final PartialModel ROTOR_SHAFT_RIGHT = new PartialModel(
            ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_right")
    );

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                CombustionPurpleFlameParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModBlockEntities.MOLTEN_ROTOR.get(), MoltenRotorRenderer::new);
            EntityRenderers.register(ModEntities.PYROCLAST_BOMB.get(), ThrownItemRenderer::new);
        });
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "impeller")
        ));

        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_left")
        ));

        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/rotor_shaft_right")
        ));
    }

}
