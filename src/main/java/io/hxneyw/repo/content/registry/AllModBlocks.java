package io.hxneyw.repo.content.registry;

import com.simibubi.create.content.decoration.palettes.ConnectedGlassBlock;
import com.simibubi.create.content.decoration.palettes.ConnectedGlassPaneBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.crucible.AshCeramicCrucibleBlock;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlock;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.RubberPaddingBlock;
import io.hxneyw.repo.content.blocks.SulfuricAcidBlock;
import io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerBlock;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlock;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlock;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlock;
import io.hxneyw.repo.content.blocks.thermalwarningalarm.ThermalWarningAlarmBlock;
import io.hxneyw.repo.content.blocks.thermochemicalcogwheel.ThermochemicalCogwheelBlock;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalConduitBlock;
import io.hxneyw.repo.content.blocks.thermochemicalgearbox.ThermochemicalGearboxBlock;
import io.hxneyw.repo.content.blocks.parallelthermochemicalgearbox.ParallelThermochemicalGearboxBlock;
import io.hxneyw.repo.content.blocks.resonantheatinjector.ResonantHeatInjectorBlock;
import io.hxneyw.repo.content.blocks.thermochemicallinkdrive.ThermochemicalLinkDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemicalshaft.ThermochemicalShaftBlock;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public class AllModBlocks {
    public static final Blocks BLOCKS = DeferredRegister.createBlocks("sulfuricresonance");
    public static final DeferredBlock<Block> MOLTEN_ROTOR_FURNACE = BLOCKS.register(
            "molten_rotor_furnace",
            () -> new MoltenRotorBlock(
                    Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
                            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                            .noOcclusion()
                            .lightLevel(state -> {
                                HeatLevel heat =
                                        state.getValue(
                                                MoltenRotorBlock.HEAT_LEVEL
                                        );

                                return switch (heat) {
                                    case NONE -> 0;
                                    case SMOULDERING, FADING -> 8;
                                    case KINDLED -> 12;
                                    case SEETHING -> 15;
                                };
                            })
            )



    );

    public static final DeferredBlock<ThermochemicalLinkDriveBlock>
            THERMOCHEMICAL_LINK_DRIVE =
            BLOCKS.register(
                    "thermochemical_link_drive",
                    () -> new ThermochemicalLinkDriveBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<Block> RUBBER_PADDING = BLOCKS.register(
            "rubber_padding",
            () -> new RubberPaddingBlock(
                    Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(1.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.WOOL)
                            .instrument(NoteBlockInstrument.BASS)
                            .noOcclusion()
                            .dynamicShape()
                            .isValidSpawn((state, level, pos, entityType) -> false)
                            .isSuffocating((state, level, pos) -> false)
            )
    );
    public static final DeferredBlock<Block> SULFUR_BLOCK = BLOCKS.register(
            "sulfur_block",
            () -> new Block(
                    Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.0F, 2.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
                            .instrument(NoteBlockInstrument.HARP)
            )
    );
    public static final DeferredBlock<Block> CARBON_DEPOSIT_BLOCK =
            BLOCKS.registerSimpleBlock(
                    "carbon_deposit_block",
                    Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)
            );

    public static final DeferredBlock<Block> INFERNAL_CARBON_DEPOSIT_BLOCK =
            BLOCKS.registerSimpleBlock(
                    "infernal_carbon_deposit_block",
                    Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(4.0F, 8.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)
            );
    public static final DeferredBlock<LiquidBlock> SULFURIC_ACID_BLOCK = BLOCKS.register(
            "sulfuric_acid",
            () -> new SulfuricAcidBlock(
                    AllModFluids.SULFURIC_ACID.get(),
                    Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .replaceable()
                            .noCollission()
                            .randomTicks()
                            .strength(100.0F)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
            )
    );
    public static final DeferredBlock<Block> PERFORATED_SPRITZER = BLOCKS.register(
            "perforated_spritzer", () -> new PerforatedSpritzerBlock(Properties.of().strength(3.0F).sound(SoundType.METAL).noOcclusion().lightLevel(state -> 0))
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        CreateSulfuricResonance.LOGGER.info("Blocks registered for Sulfuric Resonance");
    }

    public static final DeferredBlock<Block> ASH_BRICK_BLOCK =
            BLOCKS.registerSimpleBlock(
                    "ash_brick_block",
                    Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(2.0f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
            );

    public static final DeferredBlock<SlabBlock> ASH_BRICK_SLAB = BLOCKS.register(
            "ash_brick_slab",
            () -> new SlabBlock(
                    Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(2.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
            )
    );

    public static final DeferredBlock<StairBlock> ASH_BRICK_STAIRS = BLOCKS.register(
            "ash_brick_stairs",
            () -> new StairBlock(
                    ASH_BRICK_BLOCK.get().defaultBlockState(),
                    Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(2.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
            )
    );

    public static final DeferredBlock<RotatedPillarBlock> ASH_BRICK_PILLAR =
            BLOCKS.register(
                    "ash_brick_pillar",
                    () -> new RotatedPillarBlock(
                            Properties.ofFullCopy(
                                    ASH_BRICK_BLOCK.get()
                            )
                    )
            );

    public static final DeferredBlock<WallBlock> ASH_BRICK_WALL = BLOCKS.register(
            "ash_brick_wall",
            () -> new WallBlock(
                    Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(2.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
            )
    );

    public static final DeferredBlock<AshCeramicCrucibleBlock> ASH_CERAMIC_CRUCIBLE =
            BLOCKS.register(
                    "ash_ceramic_crucible",
                    () -> new AshCeramicCrucibleBlock(
                            Properties.of()
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .strength(2.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.DECORATED_POT)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<ConnectedGlassBlock> ASHESIL =
            BLOCKS.register(
                    "ashesil",
                    () -> new ConnectedGlassBlock(
                            Properties.of()
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .strength(0.3F)
                                    .sound(SoundType.GLASS)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<ConnectedGlassPaneBlock> ASHESIL_PANE =
            BLOCKS.register(
                    "ashesil_pane",
                    () -> new ConnectedGlassPaneBlock(
                            Properties.ofFullCopy(
                                            net.minecraft.world.level.block.Blocks.GLASS_PANE
                                    )
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .strength(0.3F)
                                    .sound(SoundType.GLASS)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<ConnectedGlassBlock> TEMPERED_ASHESIL =
            BLOCKS.register(
                    "tempered_ashesil",
                    () -> new ConnectedGlassBlock(
                            Properties.of()
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .strength(0.3F, 3_600_000.0F)
                                    .sound(SoundType.GLASS)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<ConnectedGlassPaneBlock> TEMPERED_ASHESIL_PANE =
            BLOCKS.register(
                    "tempered_ashesil_pane",
                    () -> new ConnectedGlassPaneBlock(
                            Properties.ofFullCopy(
                                            net.minecraft.world.level.block.Blocks.GLASS_PANE
                                    )
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .strength(0.3F, 3_600_000.0F)
                                    .sound(SoundType.GLASS)
                                    .noOcclusion()
                    )
            );

public static final DeferredBlock<ThermochemicalCogwheelBlock>
        THERMOCHEMICAL_COGWHEEL =
        BLOCKS.register(
                "thermochemical_cogwheel",
                () -> new ThermochemicalCogwheelBlock(
                        false,
                        Properties.of()
                                .mapColor(MapColor.DIRT)
                                .strength(1.5F, 6.0F)
                                .sound(SoundType.WOOD)
                                .noOcclusion()
                )
        );

    public static final DeferredBlock<ThermochemicalCogwheelBlock>
            LARGE_THERMOCHEMICAL_COGWHEEL =
            BLOCKS.register(
                    "large_thermochemical_cogwheel",
                    () -> new ThermochemicalCogwheelBlock(
                            true,
                            Properties.of()
                                    .mapColor(MapColor.DIRT)
                                    .strength(1.5F, 6.0F)
                                    .sound(SoundType.WOOD)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<ThermochemicalConduitBlock>
            THERMOCHEMICAL_CONDUIT =
            BLOCKS.register(
                    "thermochemical_conduit",
                    () -> new ThermochemicalConduitBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );



    public static final DeferredBlock<ThermochemicalShaftBlock>
            THERMOCHEMICAL_SHAFT =
            BLOCKS.register(
                    "thermochemical_shaft",
                    () -> new ThermochemicalShaftBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(2.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<ThermochemicalGearboxBlock>
            THERMOCHEMICAL_GEARBOX =
            BLOCKS.register(
                    "thermochemical_gearbox",
                    () -> new ThermochemicalGearboxBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<ParallelThermochemicalGearboxBlock>
            PARALLEL_THERMOCHEMICAL_GEARBOX =
            BLOCKS.register(
                    "parallel_thermochemical_gearbox",
                    () -> new ParallelThermochemicalGearboxBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<LivingEmberLampBlock> LIVING_EMBER_LAMP =
            BLOCKS.register(
                    "living_ember_lamp",
                    () -> new LivingEmberLampBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(2.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                                    .lightLevel(state -> state.getValue(
                                            LivingEmberLampBlock.LIGHT_LEVEL
                                    ))
                    )
            );

    public static final DeferredBlock<ThermalRelaySwitchBlock> THERMAL_RELAY_SWITCH =
            BLOCKS.register(
                    "thermal_relay_switch",
                    () -> new ThermalRelaySwitchBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(2.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );
    public static final DeferredBlock<ThermalGaugeBlock> THERMAL_GAUGE =
            BLOCKS.register(
                    "thermal_gauge",
                    () -> new ThermalGaugeBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(2.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );
    public static final DeferredBlock<ThermalWarningAlarmBlock> THERMAL_WARNING_ALARM =
            BLOCKS.register(
                    "thermal_warning_alarm",
                    () -> new ThermalWarningAlarmBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(2.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );
    public static final DeferredBlock<SulfurBurnerBlock>
            SULFUR_BURNER =
            BLOCKS.register(
                    "sulfur_burner",
                    () -> new SulfurBurnerBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                                    .lightLevel(state -> switch (
                                            state.getValue(SulfurBurnerBlock.HEAT_LEVEL)
                                            ) {
                                        case KINDLED, SEETHING -> 4;
                                        default -> 0;
                                    })
                    )
            );

    public static final DeferredBlock<ResonantHeatInjectorBlock>
            RESONANT_HEAT_INJECTOR =
            BLOCKS.register(
                    "resonant_heat_injector",
                    () -> new ResonantHeatInjectorBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(4.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<SulfuricResonanceChamberBlock>
            SULFURIC_RESONANCE_CHAMBER =
            BLOCKS.register(
                    "sulfuric_resonance_chamber",
                    () -> new SulfuricResonanceChamberBlock(
                            Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 6.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );

}
