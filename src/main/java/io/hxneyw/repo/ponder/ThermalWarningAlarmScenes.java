package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.thermalwarningalarm.ThermalWarningAlarmBlock;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.UUID;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ThermalWarningAlarmScenes {

    private ThermalWarningAlarmScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "thermal_warning_alarm.operation",
                "Warning Before Thermochemical Heat Fails"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos furnacePos = util.grid().at(1, 1, 3);
        BlockPos alarmPos = util.grid().at(3, 1, 2);
        Selection furnace = util.select().position(furnacePos);
        Selection alarm = util.select().position(alarmPos);

        UUID furnaceIdentity = UUID.fromString(
                "71ae2fd6-b99e-454d-b704-687b3ab26801"
        );
        UUID networkId = UUID.fromString(
                "d9521697-bdf8-4c4f-956c-4ea113823802"
        );
        ThermalRelaySwitchItem.FurnaceLink furnaceLink =
                new ThermalRelaySwitchItem.FurnaceLink(
                        furnacePos,
                        "minecraft:overworld",
                        furnaceIdentity
                );

        ItemStack unlinkedAlarm = new ItemStack(
                Items.THERMAL_WARNING_ALARM_ITEM.get()
        );
        ItemStack linkedAlarm = unlinkedAlarm.copy();
        ThermalRelaySwitchItem.setConnection(
                linkedAlarm,
                networkId,
                furnaceLink
        );

        BlockState alarmState = AllModBlocks.THERMAL_WARNING_ALARM.get()
                .defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(ThermalWarningAlarmBlock.CONNECTED, false)
                .setValue(ThermalWarningAlarmBlock.ALARMING, false);

        scene.world().setBlock(
                furnacePos,
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get().defaultBlockState(),
                false
        );
        scene.world().setBlock(alarmPos, alarmState, false);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(furnace, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("The Thermal Warning Alarm watches a linked Molten Rotor before its usable heat disappears")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showControls(
                        util.vector().topOf(furnacePos),
                        Pointing.DOWN,
                        60
                )
                .rightClick()
                .withItem(unlinkedAlarm);
        scene.overlay().showText(115)
                .text("Before placement, right-click the Molten Rotor with the Alarm item to copy its thermal-network link")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(furnacePos))
                .placeNearTarget();
        scene.idle(125);

        scene.overlay().showControls(
                        util.vector().topOf(alarmPos),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(linkedAlarm);
        scene.world().showSection(alarm, Direction.DOWN);
        scene.world().modifyBlock(
                alarmPos,
                state -> state.setValue(ThermalWarningAlarmBlock.CONNECTED, true),
                false
        );
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_warning_alarm_furnace",
                furnace,
                125
        );
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_warning_alarm_device",
                alarm,
                125
        );
        scene.overlay().showText(135)
                .text("Place the linked Alarm and its green indicator confirms a valid saved network connection; cross-dimensional links can resolve normally")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(alarmPos))
                .placeNearTarget();
        scene.idle(145);

        scene.overlay().showText(140)
                .text("If that valid target is temporarily unavailable or unloaded, the Alarm keeps the connection state instead of force-loading the target")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(alarmPos))
                .placeNearTarget();
        scene.idle(150);

        scene.world().modifyBlock(
                alarmPos,
                state -> state.setValue(ThermalWarningAlarmBlock.ALARMING, true),
                false
        );
        scene.overlay().showText(135)
                .text("With no fuel queued, the Alarm warns during the final 200 fuel ticks or the final 200 ticks of heated cooldown")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(alarmPos))
                .placeNearTarget();
        scene.idle(145);

        scene.overlay().showText(110)
                .text("The striker rapidly oscillates, the bell sounds, and the warning assembly flashes while the warning remains active")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(alarmPos))
                .placeNearTarget();
        scene.idle(130);

        scene.effects().indicateRedstone(alarmPos);
        scene.overlay().showText(110)
                .text("While alarming, the block outputs redstone strength 15 on every side")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(alarmPos))
                .placeNearTarget();
        scene.idle(120);

        scene.overlay().showText(105)
                .text("Queued fuel or a creative Rotor suppresses the warning")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();
        scene.idle(115);

        scene.world().modifyBlock(
                alarmPos,
                state -> state
                        .setValue(ThermalWarningAlarmBlock.CONNECTED, false)
                        .setValue(ThermalWarningAlarmBlock.ALARMING, false),
                false
        );
        scene.overlay().showControls(
                        util.vector().topOf(alarmPos),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .whileSneaking();
        scene.overlay().showText(120)
                .text("Sneak-right-click the placed Alarm to clear its network connection; disconnected status pulses red")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(alarmPos))
                .placeNearTarget();
        scene.idle(130);
        scene.markAsFinished();
    }
}
