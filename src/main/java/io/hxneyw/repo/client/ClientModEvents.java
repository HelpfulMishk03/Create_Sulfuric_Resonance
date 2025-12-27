package io.hxneyw.repo.client;

import io.hxneyw.repo.content.blocks.client.MoltenRotorRenderer;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.registry.ModBlockEntities;
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

@EventBusSubscriber(modid = "sulfuricresonance", value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register block entity renderer
            BlockEntityRenderers.register(ModBlockEntities.MOLTEN_ROTOR.get(), MoltenRotorRenderer::new);

            // Register entity renderer
            EntityRenderers.register(
                    ModEntities.PYROCLAST_BOMB.get(),
                    ThrownItemRenderer::new
            );
        });
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Register the impeller model so it's loaded by Minecraft
        event.register(ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "impeller")
        ));
    }
}