package io.hxneyw.repo.ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.List;
import java.util.UUID;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ThermalGaugeScenes {

    private ThermalGaugeScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "thermal_gauge.operation",
                "Remote Thermochemical Monitoring"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos furnacePos = util.grid().at(1, 1, 3);
        BlockPos supportPos = util.grid().at(3, 1, 2);
        BlockPos gaugePos = util.grid().at(3, 2, 2);

        Selection furnace = util.select().position(furnacePos);
        Selection support = util.select().position(supportPos);
        Selection gauge = util.select().position(gaugePos);

        UUID furnaceIdentity = UUID.fromString(
                "8ad1d6dd-76e8-47f8-b5d8-96d282afe201"
        );
        UUID networkId = UUID.fromString(
                "438fa9fc-82de-402d-93c2-ef2745ff712a"
        );

        ThermalRelaySwitchItem.FurnaceLink furnaceLink =
                new ThermalRelaySwitchItem.FurnaceLink(
                        furnacePos,
                        "minecraft:overworld",
                        furnaceIdentity
                );

        ItemStack unlinkedGauge = new ItemStack(
                Items.THERMAL_GAUGE_ITEM.get()
        );
        ItemStack linkedGauge = unlinkedGauge.copy();
        ThermalRelaySwitchItem.setConnection(
                linkedGauge,
                networkId,
                furnaceLink
        );
        ItemStack clearedGauge = linkedGauge.copy();
        ThermalRelaySwitchItem.clearConnections(clearedGauge);

        BlockState gaugeState = AllModBlocks.THERMAL_GAUGE.get()
                .defaultBlockState()
                .setValue(
                        BlockStateProperties.ATTACH_FACE,
                        AttachFace.FLOOR
                )
                .setValue(
                        BlockStateProperties.HORIZONTAL_FACING,
                        Direction.NORTH
                );

        scene.world().setBlock(
                furnacePos,
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                        .defaultBlockState(),
                false
        );
        scene.world().setBlock(
                supportPos,
                Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                false
        );
        scene.world().setBlock(
                gaugePos,
                gaugeState,
                false
        );

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(furnace, Direction.DOWN);
        scene.idle(20);

        setFurnaceState(
                scene,
                furnace,
                furnacePos,
                furnaceIdentity,
                MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING,
                325
        );

        scene.overlay().showText(125)
                .text("The Thermal Gauge remotely monitors the temperature and heat tier of a linked Molten Rotor Furnace")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay().showControls(
                        util.vector().topOf(furnacePos),
                        Pointing.DOWN,
                        65
                )
                .rightClick()
                .withItem(unlinkedGauge);
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_gauge_selected_furnace",
                furnace,
                115
        );
        scene.overlay().showText(110)
                .text("Before placement, right-click the furnace with a Thermal Gauge to store its connection")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(furnacePos))
                .placeNearTarget();
        scene.idle(120);

        scene.world().showSection(support, Direction.DOWN);
        scene.idle(8);
        scene.overlay().showControls(
                        util.vector().topOf(supportPos),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(linkedGauge);
        scene.world().showSection(gauge, Direction.DOWN);
        configureGauge(
                scene,
                gauge,
                furnacePos,
                furnaceIdentity,
                networkId,
                List.of(PanelSlot.BOTTOM_LEFT),
                325,
                MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING
        );
        scene.idle(25);

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_gauge_linked_furnace",
                furnace,
                125
        );
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_gauge_linked_panel",
                gauge,
                125
        );
        scene.overlay().showText(120)
                .text("Place the linked Gauge on a solid face; the furnace connection is retained after placement")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(135)
                .text("Its needle follows the furnace temperature continuously, from ambient conditions to Combustion heat")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();

        setHeatStage(
                scene,
                gauge,
                furnace,
                furnacePos,
                furnaceIdentity,
                networkId,
                List.of(PanelSlot.BOTTOM_LEFT),
                MoltenRotorBlockEntity.RotorHeatLevel.KINDLED,
                650
        );
        scene.idle(35);
        setHeatStage(
                scene,
                gauge,
                furnace,
                furnacePos,
                furnaceIdentity,
                networkId,
                List.of(PanelSlot.BOTTOM_LEFT),
                MoltenRotorBlockEntity.RotorHeatLevel.SEETHING,
                950
        );
        scene.idle(35);
        setHeatStage(
                scene,
                gauge,
                furnace,
                furnacePos,
                furnaceIdentity,
                networkId,
                List.of(PanelSlot.BOTTOM_LEFT),
                MoltenRotorBlockEntity.RotorHeatLevel.RADIANT,
                1400
        );
        scene.effects().indicateSuccess(gaugePos);
        scene.idle(75);

        scene.overlay().showText(115)
                .text("The display keeps the full CSR heat distinction, including the Combustion tier")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(125);

        scene.overlay().showControls(
                        util.vector().centerOf(gaugePos),
                        Pointing.DOWN,
                        50
                )
                .rightClick()
                .withItem(linkedGauge);
        configureGauge(
                scene,
                gauge,
                furnacePos,
                furnaceIdentity,
                networkId,
                List.of(
                        PanelSlot.BOTTOM_LEFT,
                        PanelSlot.BOTTOM_RIGHT,
                        PanelSlot.TOP_LEFT,
                        PanelSlot.TOP_RIGHT
                ),
                1400,
                MoltenRotorBlockEntity.RotorHeatLevel.RADIANT
        );
        scene.idle(30);

        scene.overlay().showText(135)
                .text("Additional Gauge items can fill the other panel slots, fitting up to four readouts into one block space")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(145);

        scene.overlay().showText(125)
                .text("Each panel slot stores its own furnace network, allowing compact multi-machine monitoring panels")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay().showControls(
                        util.vector().centerOf(gaugePos),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));
        scene.world().modifyBlock(
                gaugePos,
                state -> state.setValue(
                        BlockStateProperties.HORIZONTAL_FACING,
                        Direction.EAST
                ),
                false
        );
        scene.overlay().showText(110)
                .text("Use a Wrench to rotate the mounted Gauge panel without losing its stored links")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(120);

        scene.overlay().showControls(
                        util.vector().centerOf(gaugePos).add(0.0D, 0.8D, 0.0D),
                        Pointing.DOWN,
                        65
                )
                .whileSneaking()
                .rightClick()
                .withItem(linkedGauge);
        scene.overlay().showText(120)
                .text("Sneak-right-click in the air with a linked Gauge item to clear its stored furnace before reusing it")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showControls(
                        util.vector().centerOf(gaugePos).add(0.0D, 0.8D, 0.0D),
                        Pointing.DOWN,
                        45
                )
                .withItem(clearedGauge);
        scene.idle(55);

        scene.overlay().showControls(
                        util.vector().centerOf(gaugePos),
                        Pointing.DOWN,
                        60
                )
                .whileSneaking()
                .rightClick()
                .withItem(new ItemStack(AllItems.WRENCH.get()));
        scene.overlay().showText(115)
                .text("Sneak-right-click a mounted Thermal Gauge with a Wrench to pick it up while keeping its stored furnace connection")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(125);
        scene.markAsFinished();
    }

    private static void setHeatStage(
            CreateSceneBuilder scene,
            Selection gauge,
            Selection furnace,
            BlockPos furnacePos,
            UUID furnaceIdentity,
            UUID networkId,
            List<PanelSlot> slots,
            MoltenRotorBlockEntity.RotorHeatLevel tier,
            int temperature
    ) {
        setFurnaceState(
                scene,
                furnace,
                furnacePos,
                furnaceIdentity,
                tier,
                temperature
        );
        configureGauge(
                scene,
                gauge,
                furnacePos,
                furnaceIdentity,
                networkId,
                slots,
                temperature,
                tier
        );
    }

    private static void setFurnaceState(
            CreateSceneBuilder scene,
            Selection furnace,
            BlockPos furnacePos,
            UUID furnaceIdentity,
            MoltenRotorBlockEntity.RotorHeatLevel tier,
            int temperature
    ) {
        scene.world().modifyBlockEntity(
                furnacePos,
                MoltenRotorBlockEntity.class,
                blockEntity -> blockEntity.setCreativeMode(true)
        );
        scene.world().modifyBlockEntityNBT(
                furnace,
                MoltenRotorBlockEntity.class,
                nbt -> {
                    nbt.putUUID("FurnaceIdentity", furnaceIdentity);
                    nbt.putBoolean("CreativeMode", true);
                    nbt.putFloat("Temperature", temperature);
                    nbt.putString("HeatTier", tier.serializedId);
                }
        );
        scene.world().modifyBlock(
                furnacePos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        visualHeat(tier)
                ),
                false
        );
        scene.world().setKineticSpeed(
                furnace,
                tier == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                        ? 0.0F
                        : tier.rpmCap
        );
    }

    private static void configureGauge(
            CreateSceneBuilder scene,
            Selection gauge,
            BlockPos furnacePos,
            UUID furnaceIdentity,
            UUID networkId,
            List<PanelSlot> slots,
            int temperature,
            MoltenRotorBlockEntity.RotorHeatLevel tier
    ) {
        scene.world().modifyBlockEntityNBT(
                gauge,
                ThermalGaugeBlockEntity.class,
                nbt -> {
                    ListTag gauges = new ListTag();

                    for (PanelSlot slot : slots) {
                        CompoundTag gaugeTag = new CompoundTag();
                        gaugeTag.putString(
                                "Slot",
                                slot.getSerializedName()
                        );
                        gaugeTag.putUUID(
                                "RelayNetwork",
                                networkId
                        );
                        gaugeTag.putLong(
                                "Position",
                                furnacePos.asLong()
                        );
                        gaugeTag.putString(
                                "Dimension",
                                "minecraft:overworld"
                        );
                        gaugeTag.putUUID(
                                "Identity",
                                furnaceIdentity
                        );
                        gaugeTag.putInt(
                                "DisplayTemperature",
                                temperature
                        );
                        gaugeTag.putBoolean(
                                "NetworkConnected",
                                true
                        );
                        gaugeTag.putString(
                                "HeatTier",
                                tier.serializedId
                        );
                        gauges.add(gaugeTag);
                    }

                    nbt.put("Gauges", gauges);
                }
        );
    }

    private static HeatLevel visualHeat(
            MoltenRotorBlockEntity.RotorHeatLevel tier
    ) {
        return switch (tier) {
            case NONE -> HeatLevel.NONE;
            case SMOULDERING, FADING -> HeatLevel.SMOULDERING;
            case KINDLED -> HeatLevel.KINDLED;
            case SEETHING, RADIANT -> HeatLevel.SEETHING;
        };
    }
}
