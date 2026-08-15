package io.hxneyw.repo.client;

import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import com.simibubi.create.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import io.hxneyw.repo.client.gui.SulfuricResonanceChamberScreen;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampRenderer;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorRenderer;
import io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalcogwheel.ThermochemicalCogwheelRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalcogwheel.ThermochemicalCogwheelVisual;
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
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(
        modid = "sulfuricresonance",
        value = {Dist.CLIENT}
)
public class ClientModEvents {

    public static final PartialModel ROTOR_SHAFT_LEFT =
            partial("block/rotor_shaft_left");

    public static final PartialModel ROTOR_SHAFT_RIGHT =
            partial("block/rotor_shaft_right");

    public static final PartialModel ROTOR_HEAT_NEEDLE =
            partial("block/molten_rotor_needle");

    public static final PartialModel THERMOCHEMICAL_CONDUIT_SHAFT =
            partial("block/thermochemical_conduit_shaft");

    public static final PartialModel THERMOCHEMICAL_SHAFT =
            partial("block/thermochemical_shaft");

    public static final PartialModel THERMOCHEMICAL_GEARBOX_SHAFT =
            partial("block/thermochemical_gearbox_shaft");

    public static final PartialModel
            THERMOCHEMICAL_LINK_DRIVE_SHAFT =
            partial("block/thermochemical_link_drive_shaft");

public static final PartialModel THERMOCHEMICAL_COGWHEEL =
        partial("block/thermochemical_cogwheel");

    public static final PartialModel LARGE_THERMOCHEMICAL_COGWHEEL_SHAFTLESS =
            partial("block/large_thermochemical_cogwheel_shaftless");

    public static final PartialModel THERMOCHEMICAL_COGWHEEL_SHAFT =
            partial("block/thermochemical_cogwheel_shaft");

    public static final PartialModel RESONANCE_CHAMBER_SHAFT =
            partial("block/sulfuric_resonance_chamber_shaft");

    public static final PartialModel RESONANT_HEAT_INJECTOR_SHAFT =
            partial("block/resonant_heat_injector_shaft");

    public static final PartialModel RESONANCE_CHAMBER_WINDOW =
            partial("block/sulfuric_resonance_chamber_window");

    public static final PartialModel RESONANCE_CHAMBER_RING_TOP =
            partial("block/sulfuric_resonance_chamber_ring_top");

    public static final PartialModel RESONANCE_CHAMBER_RING_BOTTOM =
            partial("block/sulfuric_resonance_chamber_ring_bottom");

    public static final PartialModel THERMAL_GAUGE_BASE =
            partial("block/thermal_gauge_base");

    public static final PartialModel THERMAL_GAUGE_NEEDLE =
            partial("block/thermal_gauge_needle");

    private static PartialModel partial(String path) {
        return PartialModel.of(
                ResourceLocation.fromNamespaceAndPath(
                        "sulfuricresonance",
                        path
                )
        );
    }

    @SubscribeEvent
    public static void registerParticleFactories(
            RegisterParticleProvidersEvent event
    ) {
        event.registerSpriteSet(
                ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                CombustionPurpleFlameParticle.Provider::new
        );

        event.registerSpriteSet(
                ModParticles.ACID_DRIP.get(),
                AcidDripParticle.Provider::new
        );
    }

    @SubscribeEvent
    public static void registerMenuScreens(
            RegisterMenuScreensEvent event
    ) {
        event.register(
                AllModMenus.SULFURIC_RESONANCE_CHAMBER.get(),
                SulfuricResonanceChamberScreen::new
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            SimpleBlockEntityVisualizer
                    .builder(AllBlockEntities.THERMOCHEMICAL_COGWHEEL.get())
                    .factory(ThermochemicalCogwheelVisual::create)
                    .skipVanillaRender(blockEntity -> true)
                    .apply();

            CreateRegistrate.blockModel(
                    () -> BracketedKineticBlockModel::new
            ).accept(
                    AllModBlocks.THERMOCHEMICAL_COGWHEEL.get()
            );

            CreateRegistrate.blockModel(
                    () -> BracketedKineticBlockModel::new
            ).accept(
                    AllModBlocks.LARGE_THERMOCHEMICAL_COGWHEEL.get()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    AllModFluids.SULFURIC_ACID.get(),
                    RenderType.translucent()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    AllModFluids.SULFURIC_ACID_FLOWING.get(),
                    RenderType.translucent()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    AllModBlocks.ASHESIL.get(),
                    RenderType.translucent()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    AllModBlocks.ASHESIL_PANE.get(),
                    RenderType.translucent()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    AllModBlocks.TEMPERED_ASHESIL.get(),
                    RenderType.translucent()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    AllModBlocks.TEMPERED_ASHESIL_PANE.get(),
                    RenderType.translucent()
            );

            BlockEntityRenderers.register(
                    AllBlockEntities.MOLTEN_ROTOR.get(),
                    MoltenRotorRenderer::new
            );

            EntityRenderers.register(
                    ModEntities.PYROCLAST_BOMB.get(),
                    ThrownItemRenderer::new
            );

            BlockEntityRenderers.register(
                    AllBlockEntities.PERFORATED_SPRITZER.get(),
                    PerforatedSpritzerRenderer::new
            );

            CreateRegistrate.connectedTextures(
                    () -> new SimpleCTBehaviour(
                            ModSpriteShifts.ASHESIL
                    )
            ).accept(AllModBlocks.ASHESIL.get());

            CreateRegistrate.connectedTextures(
                    () -> new GlassPaneCTBehaviour(
                            ModSpriteShifts.ASHESIL
                    )
            ).accept(AllModBlocks.ASHESIL_PANE.get());
            CreateRegistrate.connectedTextures(
                    () -> new SimpleCTBehaviour(
                            ModSpriteShifts.TEMPERED_ASHESIL
                    )
            ).accept(AllModBlocks.TEMPERED_ASHESIL.get());

            CreateRegistrate.connectedTextures(
                    () -> new GlassPaneCTBehaviour(
                            ModSpriteShifts.TEMPERED_ASHESIL
                    )
            ).accept(AllModBlocks.TEMPERED_ASHESIL_PANE.get());
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
                AllBlockEntities.SULFUR_BURNER.get(),
                SulfurBurnerRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.THERMOCHEMICAL_CONDUIT.get(),
                ThermochemicalConduitRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.THERMOCHEMICAL_SHAFT.get(),
                ThermochemicalShaftRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.THERMOCHEMICAL_COGWHEEL.get(),
                ThermochemicalCogwheelRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.THERMOCHEMICAL_GEARBOX.get(),
                ThermochemicalGearboxRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.PARALLEL_THERMOCHEMICAL_GEARBOX.get(),
                ParallelThermochemicalGearboxRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.THERMOCHEMICAL_LINK_DRIVE.get(),
                ThermochemicalLinkDriveRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.LIVING_EMBER_LAMP.get(),
                LivingEmberLampRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.THERMAL_GAUGE.get(),
                ThermalGaugeRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.SULFURIC_RESONANCE_CHAMBER.get(),
                SulfuricResonanceChamberRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.RESONANT_HEAT_INJECTOR.get(),
                ResonantHeatInjectorRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerAdditionalModels(
            RegisterAdditional event
    ) {
        CombustionBeltClientAssets.init();

        registerStandalone(event);
    }

    private static void registerStandalone(
            RegisterAdditional event
    ) {
        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/sulfuric_resonance_chamber_shaft"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/resonant_heat_injector_shaft"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/sulfuric_resonance_chamber_window"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_gauge_base"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_gauge_needle"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "item/impeller"
                        )
                )
        );
    }
}
