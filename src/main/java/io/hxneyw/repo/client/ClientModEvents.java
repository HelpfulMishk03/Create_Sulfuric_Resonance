package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import com.simibubi.create.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import io.hxneyw.repo.client.gui.SulfuricResonanceChamberScreen;
import io.hxneyw.repo.client.animation.CinderFlareAnimationHandler;
import io.hxneyw.repo.client.animation.CinderFlareClientEnumParams;
import io.hxneyw.repo.client.screen.PrecisionSpritzerScreen;
import io.hxneyw.repo.client.renderer.CinderFlareRenderer;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampRenderer;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorRenderer;
import io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalcogwheel.ThermochemicalCogwheelRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalcogwheel.ThermochemicalCogwheelVisual;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerRenderer;
import io.hxneyw.repo.content.particles.AcidDripParticle;
import io.hxneyw.repo.content.particles.CombustionPurpleFlameParticle;
import io.hxneyw.repo.content.particles.PyroclasticFragmentParticle;
import io.hxneyw.repo.content.registry.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

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

    public static final PartialModel THERMOCHEMICAL_BOILER_INTERFACE_PORT_SHAFT =
            partial("block/thermochemical_boiler_interface_port_shaft");

    public static final PartialModel THERMOCHEMICAL_GEARBOX_SHAFT =
            partial("block/thermochemical_gearbox_shaft");

    public static final PartialModel THERMOCHEMICAL_CLUTCH_SHAFT_HALF =
            partial("block/thermochemical_clutch_shaft_half");

    public static final PartialModel THERMOCHEMICAL_CLUTCH_LOCK_HOUSING =
            partial("block/thermochemical_clutch_lock_housing");

    public static final PartialModel THERMOCHEMICAL_CLUTCH_LOCK_PIECE =
            partial("block/thermochemical_clutch_lock_piece");

    public static final PartialModel
            THERMOCHEMICAL_LINK_DRIVE_SHAFT =
            partial("block/thermochemical_link_drive_shaft");

public static final PartialModel THERMOCHEMICAL_COGWHEEL =
        partial("block/thermochemical_cogwheel");

    public static final PartialModel LARGE_THERMOCHEMICAL_COGWHEEL_SHAFTLESS =
            partial("block/large_thermochemical_cogwheel_shaftless");

    public static final PartialModel THERMOCHEMICAL_COGWHEEL_SHAFT =
            partial("block/thermochemical_cogwheel_shaft");

    public static final PartialModel RESONANCE_CHAMBER_BODY =
            partial("block/sulfuric_resonance_chamber");

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

    public static final PartialModel RESONANCE_CHAMBER_PLATFORM =
            partial("block/sulfuric_resonance_chamber_platform");

    public static final PartialModel CATALYST_BED_CONNECTOR =
            partial("block/catalyst_bed_connector");

    public static final PartialModel THERMAL_GAUGE_BASE =
            partial("block/thermal_gauge_base");

    public static final PartialModel THERMAL_GAUGE_NEEDLE =
            partial("block/thermal_gauge_needle");

    public static final PartialModel THERMAL_GAUGE_COVER =
            partial("block/thermal_gauge_cover");

    public static final PartialModel THERMAL_RELAY_SWITCH_SOLID =
            partial("block/thermal_relay_switch_solid");

    public static final PartialModel THERMAL_RELAY_SWITCH_GLASS =
            partial("block/thermal_relay_switch_glass");

    public static final PartialModel THERMAL_WARNING_ALARM_BODY =
            partial("block/thermal_warning_alarm");

    public static final PartialModel THERMAL_WARNING_ALARM_BELL =
            partial("block/thermal_warning_alarm_bell");

    public static final PartialModel THERMAL_WARNING_ALARM_STRIKER =
            partial("block/thermal_warning_alarm_striker");

    public static final PartialModel THERMAL_WARNING_ALARM_MAIN_BULB =
            partial("block/thermal_warning_alarm_main_bulb");

    public static final PartialModel THERMAL_WARNING_ALARM_MAIN_FILAMENT =
            partial("block/thermal_warning_alarm_main_filament");


    public static final PartialModel THERMAL_WARNING_ALARM_STATUS_RED =
            partial("block/thermal_warning_alarm_status_red");

    public static final PartialModel THERMAL_WARNING_ALARM_STATUS_GREEN =
            partial("block/thermal_warning_alarm_status_green");

    public static final PartialModel THERMAL_WARNING_ALARM_VIBRATION_LEFT =
            partial("block/thermal_warning_alarm_vibration_left");

    public static final PartialModel THERMAL_WARNING_ALARM_VIBRATION_RIGHT =
            partial("block/thermal_warning_alarm_vibration_right");

    public static final PartialModel PROCESS_MONITOR_SELECTOR =
            partial("block/process_monitor_selector");

    public static final PartialModel PROCESS_MONITOR_ANTENNA =
            partial("block/process_monitor_antenna");

    public static final PartialModel PROCESS_GAUGE_POINTER =
            partial("block/process_gauge_pointer");

    public static final PartialModel PROCESS_GAUGE_DRUM =
            partial("block/process_gauge_drum");

    public static final PartialModel PROCESS_GAUGE_POINTER_FLOOR =
            partial("block/process_gauge_pointer_floor");

    public static final PartialModel PROCESS_GAUGE_DRUM_FLOOR =
            partial("block/process_gauge_drum_floor");

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

        event.registerSpriteSet(
                ModParticles.PYROCLASTIC_FRAGMENT.get(),
                PyroclasticFragmentParticle.Provider::new
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
        event.register(
                AllModMenus.PRECISION_SPRITZER.get(),
                PrecisionSpritzerScreen::new
        );
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
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
                    AllModBlocks.THERMOCHEMICAL_CLUTCH.get(),
                    RenderType.cutoutMipped()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    AllModBlocks.CATALYST_BED.get(),
                    RenderType.cutout()
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

            ItemBlockRenderTypes.setRenderLayer(
                    AllModBlocks.THERMAL_WARNING_ALARM.get(),
                    RenderType.cutout()
            );

            BlockEntityRenderers.register(
                    AllBlockEntities.MOLTEN_ROTOR.get(),
                    MoltenRotorRenderer::new
            );

            EntityRenderers.register(
                    ModEntities.PYROCLAST_BOMB.get(),
                    ThrownItemRenderer::new
            );

            EntityRenderers.register(
                    ModEntities.CINDER_FLARE.get(),
                    CinderFlareRenderer::new
            );

            EntityRenderers.register(
                    ModEntities.SULFURIC_ACID_FLASK.get(),
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
                AllBlockEntities.THERMOCHEMICAL_BOILER_INTERFACE.get(),
                ThermochemicalBoilerInterfaceRenderer::new
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
                AllBlockEntities.THERMOCHEMICAL_CLUTCH.get(),
                ThermochemicalClutchRenderer::new
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
                AllBlockEntities.THERMAL_WARNING_ALARM.get(),
                ThermalWarningAlarmRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.SULFURIC_RESONANCE_CHAMBER.get(),
                SulfuricResonanceChamberRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.RESONANT_HEAT_INJECTOR.get(),
                ResonantHeatInjectorRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.PROCESS_MONITOR.get(),
                ProcessMonitorRenderer::new
        );

        event.registerBlockEntityRenderer(
                AllBlockEntities.PROCESS_GAUGE.get(),
                ProcessGaugeRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerClientExtensions(
            RegisterClientExtensionsEvent event
    ) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer =
                    new TranslucentMachineItemRenderer();

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        };

        event.registerItem(
                extensions,
                Items.THERMAL_GAUGE_ITEM.get(),
                Items.THERMAL_RELAY_SWITCH_ITEM.get(),
                Items.SULFURIC_RESONANCE_CHAMBER_ITEM.get(),
                Items.THERMAL_WARNING_ALARM_ITEM.get()
        );

        IClientItemExtensions cinderFlareExtensions = new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(
                    @NotNull PoseStack poseStack,
                    @NotNull LocalPlayer player,
                    @NotNull HumanoidArm arm,
                    @NotNull ItemStack itemInHand,
                    float partialTick,
                    float equipProcess,
                    float swingProcess
            ) {
                return CinderFlareAnimationHandler.applyFirstPersonItemTransform(
                        poseStack,
                        player,
                        arm,
                        itemInHand,
                        partialTick,
                        equipProcess,
                        swingProcess
                );
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(
                    LivingEntity entity,
                    @NotNull InteractionHand hand,
                    @NotNull ItemStack stack
            ) {
                if (!entity.isUsingItem()
                        || entity.getUsedItemHand() != hand
                        || hand != InteractionHand.MAIN_HAND
                        || !stack.is(Items.CINDER_FLARE.get())
                        || !entity.getOffhandItem().is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) {
                    return null;
                }

                return CinderFlareClientEnumParams.CINDER_FLARE_LIGHTING.getValue();
            }
        };

        event.registerItem(
                cinderFlareExtensions,
                Items.CINDER_FLARE.get()
        );

        IClientItemExtensions litCinderFlareExtensions = new IClientItemExtensions() {

            @Override

            public boolean applyForgeHandTransform(

                    @NotNull PoseStack poseStack,

                    @NotNull LocalPlayer player,

                    @NotNull HumanoidArm arm,

                    @NotNull ItemStack itemInHand,

                    float partialTick,

                    float equipProcess,

                    float swingProcess

            ) {

                return CinderFlareAnimationHandler.applyFirstPersonItemTransform(

                        poseStack,

                        player,

                        arm,

                        itemInHand,

                        partialTick,

                        equipProcess,

                        swingProcess

                );

            }

        

            @Override

            public HumanoidModel.ArmPose getArmPose(

                    @NotNull LivingEntity entity,

                    @NotNull InteractionHand hand,

                    @NotNull ItemStack stack

            ) {

                if (hand != InteractionHand.MAIN_HAND

                        || !stack.is(Items.LIT_CINDER_FLARE.get())

                        || !CinderFlareAnimationHandler.isThrowing(entity, 0.0F)) {

                    return null;

                }

                return CinderFlareClientEnumParams.CINDER_FLARE_LIGHTING.getValue();

            }

        };

        event.registerItem(

                litCinderFlareExtensions,

                Items.LIT_CINDER_FLARE.get()

        );

        
        IClientItemExtensions flintAndSteelExtensions = new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(
                    @NotNull PoseStack poseStack,
                    @NotNull LocalPlayer player,
                    @NotNull HumanoidArm arm,
                    @NotNull ItemStack itemInHand,
                    float partialTick,
                    float equipProcess,
                    float swingProcess
            ) {
                return CinderFlareAnimationHandler.applyFirstPersonItemTransform(
                        poseStack,
                        player,
                        arm,
                        itemInHand,
                        partialTick,
                        equipProcess,
                        swingProcess
                );
            }
        };

        event.registerItem(
                flintAndSteelExtensions,
                net.minecraft.world.item.Items.FLINT_AND_STEEL
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
                                "block/thermochemical_boiler_interface_port_shaft"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermochemical_clutch_shaft_half"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/sulfuric_resonance_chamber"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_gauge_cover"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_relay_switch_solid"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_relay_switch_glass"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm"
                        )
                )
        );
        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/process_monitor_selector"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/process_gauge_pointer"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/process_gauge_drum"
                        )
                )
        );

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
                                "block/sulfuric_resonance_chamber_platform"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/catalyst_bed_connector"
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
                                "block/thermal_warning_alarm_bell"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm_striker"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm_main_bulb"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm_main_filament"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm_status_red"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm_status_green"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm_vibration_left"
                        )
                )
        );

        event.register(
                ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                "sulfuricresonance",
                                "block/thermal_warning_alarm_vibration_right"
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
