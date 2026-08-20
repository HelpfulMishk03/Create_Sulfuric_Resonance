package io.hxneyw.repo.ponder;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class SulfuricResonancePonderPlugin implements PonderPlugin {

    private static final int REGISTERED_SCENES = 25;

    @NotNull
    @Override
    public String getModId() {
        return CreateSulfuricResonance.MODID;
    }

    @Override
    public void registerScenes(
            @NotNull PonderSceneRegistrationHelper<ResourceLocation> helper
    ) {
        CreateSulfuricResonance.LOGGER.info(
                "Registering Ponder scenes..."
        );

        try {
            ResourceLocation thermochemicalShaftId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMOCHEMICAL_SHAFT.get()
                    );

            helper.addStoryBoard(
                    thermochemicalShaftId,
                    "thermoshaft/shaft",
                    ThermochemicalShaftScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation thermochemicalGearboxId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMOCHEMICAL_GEARBOX.get()
                    );

            helper.addStoryBoard(
                    thermochemicalGearboxId,
                    "thermogearbox/gearbox",
                    ThermochemicalGearboxScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation combustionBeltConnectorId =
                    BuiltInRegistries.ITEM.getKey(
                            Items.COMBUSTION_BELT_CONNECTOR.get()
                    );

            helper.addStoryBoard(
                    combustionBeltConnectorId,
                    "combustionbelt/belt",
                    CombustionBeltScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation thermochemicalConduitId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMOCHEMICAL_CONDUIT.get()
                    );

            helper.addStoryBoard(
                    thermochemicalConduitId,
                    "thermoconduit/conduit",
                    ThermochemicalConduitScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation spritzerId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.PERFORATED_SPRITZER.get()
                    );

            helper.addStoryBoard(
                    spritzerId,
                    "perforated_spritzer/intro",
                    PerforatedSpritzerScenes::intro,
                    AllPonderTags.FLUIDS
            );

            helper.addStoryBoard(
                    spritzerId,
                    "perforated_spritzer/mob_automation",
                    PerforatedSpritzerScenes::mobAutomation,
                    AllPonderTags.FLUIDS
            );

            ResourceLocation precisionSpritzerId =
                    BuiltInRegistries.ITEM.getKey(
                            io.hxneyw.repo.content.Items.PRECISION_SPRITZER.get()
                    );

            helper.addStoryBoard(
                    precisionSpritzerId,
                    "perforated_spritzer/intro",
                    PerforatedSpritzerScenes::intro,
                    AllPonderTags.FLUIDS
            );

            ResourceLocation moltenRotorId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                    );

            helper.addStoryBoard(
                    moltenRotorId,
                    "molten_rotor/operation",
                    MoltenRotorScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation thermalRelayId =
                    ResourceLocation.fromNamespaceAndPath(
                            CreateSulfuricResonance.MODID,
                            "thermal_relay_switch"
                    );

            helper.forComponents(thermalRelayId)
                    .addStoryBoard(
                            "relayswitch/thermal_relay_switch",
                            ThermalRelayScenes::thermalRelaySwitch
                    );

            ResourceLocation sulfurId =
                    ResourceLocation.fromNamespaceAndPath(
                            CreateSulfuricResonance.MODID,
                            "sulfur"
                    );

            helper.addStoryBoard(
                    sulfurId,
                    "sulfur/compatibility",
                    SulfurScenes::compatibility
            );

            ResourceLocation sulfurBurnerId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.SULFUR_BURNER.get()
                    );

            helper.addStoryBoard(
                    sulfurBurnerId,
                    "sulfur_burner/operation",
                    SulfurScenes::burner,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation resonantHeatInjectorId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.RESONANT_HEAT_INJECTOR.get()
                    );

            helper.addStoryBoard(
                    resonantHeatInjectorId,
                    "resonant_heat_injector/operation",
                    ResonantHeatInjectorScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation thermochemicalLinkDriveId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMOCHEMICAL_LINK_DRIVE.get()
                    );

            helper.addStoryBoard(
                    thermochemicalLinkDriveId,
                    "thermochemical_link_drive/operation",
                    ThermochemicalLinkDriveScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation thermochemicalClutchId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMOCHEMICAL_CLUTCH.get()
                    );

            helper.addStoryBoard(
                    thermochemicalClutchId,
                    "thermochemical_clutch/operation",
                    ThermochemicalClutchScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation thermalGaugeId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMAL_GAUGE.get()
                    );

            helper.addStoryBoard(
                    thermalGaugeId,
                    "thermal_gauge/operation",
                    ThermalGaugeScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation livingEmberLampId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.LIVING_EMBER_LAMP.get()
                    );

            helper.addStoryBoard(
                    livingEmberLampId,
                    "living_ember_lamp/operation",
                    LivingEmberLampScenes::operation
            );

            ResourceLocation sulfuricResonanceChamberId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
                    );

            helper.addStoryBoard(
                    sulfuricResonanceChamberId,
                    "sulfuric_resonance_chamber/operation",
                    SulfuricResonanceChamberScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation thermochemicalCogwheelId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMOCHEMICAL_COGWHEEL.get()
                    );

            helper.addStoryBoard(
                    thermochemicalCogwheelId,
                    "thermochemical_cogwheel/operation",
                    ThermochemicalCogwheelScenes::smallCogwheel,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation largeThermochemicalCogwheelId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.LARGE_THERMOCHEMICAL_COGWHEEL.get()
                    );

            helper.addStoryBoard(
                    largeThermochemicalCogwheelId,
                    "large_thermochemical_cogwheel/operation",
                    ThermochemicalCogwheelScenes::largeCogwheel,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation parallelThermochemicalGearboxId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.PARALLEL_THERMOCHEMICAL_GEARBOX.get()
                    );

            helper.addStoryBoard(
                    parallelThermochemicalGearboxId,
                    "parallel_thermochemical_gearbox/operation",
                    ParallelThermochemicalGearboxScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation ashCeramicCrucibleId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()
                    );

            helper.addStoryBoard(
                    ashCeramicCrucibleId,
                    "ash_ceramic_crucible/operation",
                    AshCeramicCrucibleScenes::operation,
                    AllPonderTags.REACTIVE_HEAT
            );

            ResourceLocation rubberPaddingId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.RUBBER_PADDING.get()
                    );

            helper.addStoryBoard(
                    rubberPaddingId,
                    "rubber_padding/operation",
                    RubberPaddingScenes::operation,
                    AllPonderTags.INTELLIGENT_INDUSTRY
            );

            ResourceLocation sulfuricAcidBucketId =
                    BuiltInRegistries.ITEM.getKey(
                            Items.SULFURIC_ACID_BUCKET.get()
                    );

            helper.addStoryBoard(
                    sulfuricAcidBucketId,
                    "sulfuric_acid/operation",
                    SulfuricAcidScenes::operation,
                    AllPonderTags.FLUIDS
            );

            ResourceLocation processMonitorId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.PROCESS_MONITOR.get()
                    );

            helper.addStoryBoard(
                    processMonitorId,
                    "process_monitor/operation",
                    ProcessMonitorScenes::operation,
                    AllPonderTags.INTELLIGENT_INDUSTRY
            );

            ResourceLocation processGaugeId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.PROCESS_GAUGE.get()
                    );

            helper.addStoryBoard(
                    processGaugeId,
                    "process_gauge/operation",
                    ProcessGaugeScenes::operation,
                    AllPonderTags.INTELLIGENT_INDUSTRY
            );

            ResourceLocation thermalWarningAlarmId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.THERMAL_WARNING_ALARM.get()
                    );

            helper.addStoryBoard(
                    thermalWarningAlarmId,
                    "thermal_warning_alarm/operation",
                    ThermalWarningAlarmScenes::operation,
                    AllPonderTags.INTELLIGENT_INDUSTRY
            );

            CreateSulfuricResonance.LOGGER.info(
                    "Loaded {} Ponder scenes",
                    REGISTERED_SCENES
            );
        } catch (Exception exception) {
            CreateSulfuricResonance.LOGGER.error(
                    "Failed to register Ponder scenes",
                    exception
            );
        }
    }

    @Override
    public void registerTags(
            @NotNull PonderTagRegistrationHelper<ResourceLocation> helper
    ) {
        try {
            AllPonderTags.register(helper);
        } catch (Exception exception) {
            CreateSulfuricResonance.LOGGER.error(
                    "Failed to register Ponder tags",
                    exception
            );
        }
    }
}