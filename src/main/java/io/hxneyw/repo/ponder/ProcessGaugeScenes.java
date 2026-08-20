package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.processgauge.ProcessGaugeBlock;
import io.hxneyw.repo.content.blocks.processgauge.ProcessGaugeBlockEntity;
import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import io.hxneyw.repo.content.items.ProcessGaugeItem;
import io.hxneyw.repo.content.process.ProcessMonitorRef;
import io.hxneyw.repo.content.process.ProcessState;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ProcessGaugeScenes {

    private ProcessGaugeScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "process_gauge.operation",
                "Reading Monitor Channels as Redstone"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);
        scene.rotateCameraY(90);

        BlockPos supportLampPos = util.grid().at(1, 2, 2);
        BlockPos gaugePos = util.grid().at(2, 2, 2);
        BlockPos lowerLampPos = util.grid().at(2, 1, 2);
        BlockPos monitorPos = util.grid().at(4, 1, 2);

        Selection supportLamp = util.select().position(supportLampPos);
        Selection gauge = util.select().position(gaugePos);
        Selection lowerLamp = util.select().position(lowerLampPos);
        Selection monitor = util.select().position(monitorPos);

        UUID monitorIdentity = UUID.fromString(
                "20d2df2c-57fa-4899-8ae2-a8d5e988a301"
        );
        ProcessMonitorRef monitorRef = new ProcessMonitorRef(
                monitorPos,
                "minecraft:overworld",
                monitorIdentity
        );

        ItemStack unlinkedGauge = new ItemStack(Items.PROCESS_GAUGE_ITEM.get());
        ItemStack linkedGauge = unlinkedGauge.copy();
        ProcessGaugeItem.setMonitorReference(linkedGauge, monitorRef);

        BlockState monitorState = AllModBlocks.PROCESS_MONITOR.get()
                .defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        BlockState gaugeState = AllModBlocks.PROCESS_GAUGE.get()
                .defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .setValue(ProcessGaugeBlock.POWERED, false);

        scene.world().setBlock(monitorPos, monitorState, false);
        scene.world().modifyBlockEntityNBT(
                monitor,
                ProcessMonitorBlockEntity.class,
                nbt -> nbt.putUUID("MonitorIdentity", monitorIdentity)
        );
        scene.world().setBlock(
                supportLampPos,
                Blocks.REDSTONE_LAMP.defaultBlockState(),
                false
        );
        scene.world().setBlock(
                lowerLampPos,
                Blocks.REDSTONE_LAMP.defaultBlockState(),
                false
        );
        scene.world().setBlock(gaugePos, gaugeState, false);

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(monitor, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("A Process Gauge reads one linked Process Monitor, not an individual machine")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showControls(
                        util.vector().topOf(monitorPos),
                        Pointing.DOWN,
                        60
                )
                .rightClick()
                .withItem(unlinkedGauge);
        scene.overlay().showText(115)
                .text("Right-click the Monitor with the Gauge item to store that Monitor connection")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(monitorPos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().showSection(supportLamp, Direction.DOWN);
        scene.world().showSection(lowerLamp, Direction.DOWN);
        scene.idle(8);
        scene.overlay().showControls(
                        util.vector().centerOf(gaugePos),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .withItem(linkedGauge);
        scene.world().showSection(gauge, Direction.WEST);
        scene.world().modifyBlockEntityNBT(
                gauge,
                ProcessGaugeBlockEntity.class,
                nbt -> configureGauge(nbt, monitorRef, 0, ProcessState.IDLE, 4, false)
        );
        scene.overlay().showText(115)
                .text("Place the linked Gauge on a wall or floor; it keeps the Monitor reference")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(125);

        scene.overlay().showControls(
                        util.vector().centerOf(gaugePos),
                        Pointing.DOWN,
                        55
                )
                .rightClick();
        scene.world().modifyBlockEntityNBT(
                gauge,
                ProcessGaugeBlockEntity.class,
                nbt -> configureGauge(nbt, monitorRef, 2, ProcessState.BLOCKED, 1, true)
        );
        scene.world().modifyBlock(
                gaugePos,
                state -> state.setValue(ProcessGaugeBlock.POWERED, true),
                false
        );
        scene.overlay().showText(125)
                .text("Empty-hand right-click cycles Channels 1 to 5 without changing any Monitor assignments")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay().showText(120)
                .text("The pointer shows the selected channel and the drum shows that channel's live process state")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(130);

        scene.world().modifyBlock(
                supportLampPos,
                state -> state.setValue(BlockStateProperties.LIT, true),
                false
        );
        scene.world().modifyBlock(
                lowerLampPos,
                state -> state.setValue(BlockStateProperties.LIT, true),
                false
        );
        scene.overlay().showOutline(
                PonderPalette.RED,
                "process_gauge_support_contact",
                supportLamp,
                130
        );
        scene.overlay().showOutline(
                PonderPalette.RED,
                "process_gauge_lower_contact",
                lowerLamp,
                130
        );
        scene.overlay().showText(130)
                .text("READY outputs 5 and BLOCKED outputs 15 through exactly two contacts: the mounted-against face and the lower contact")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(140);

        scene.world().modifyBlockEntityNBT(
                gauge,
                ProcessGaugeBlockEntity.class,
                nbt -> configureGauge(nbt, monitorRef, 3, ProcessState.IDLE, 4, false)
        );
        scene.world().modifyBlock(
                gaugePos,
                state -> state.setValue(ProcessGaugeBlock.POWERED, false),
                false
        );
        scene.world().modifyBlock(
                supportLampPos,
                state -> state.setValue(BlockStateProperties.LIT, false),
                false
        );
        scene.world().modifyBlock(
                lowerLampPos,
                state -> state.setValue(BlockStateProperties.LIT, false),
                false
        );
        scene.overlay().showText(125)
                .text("IDLE, PROCESS, unassigned, OFF, and ERR output 0")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay().showText(125)
                .text("One Gauge can inspect all five Monitor channels; use extra Gauges only when simultaneous outputs are useful")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(gaugePos))
                .placeNearTarget();
        scene.idle(135);

        scene.overlay().showControls(
                        util.vector().topOf(gaugePos),
                        Pointing.DOWN,
                        55
                )
                .rightClick()
                .whileSneaking()
                .withItem(linkedGauge);
        scene.overlay().showText(115)
                .text("Sneak-right-click in the air with a linked Gauge item to clear its stored Monitor connection")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().topOf(gaugePos))
                .placeNearTarget();
        scene.idle(125);
    }

    private static void configureGauge(
            net.minecraft.nbt.CompoundTag nbt,
            ProcessMonitorRef monitorRef,
            int channel,
            ProcessState state,
            int linkStatus,
            boolean active
    ) {
        nbt.put("Monitor", monitorRef.save());
        nbt.putInt("SelectedChannel", channel);
        nbt.putString("ObservedState", state.serializedName());
        nbt.putInt("LinkStatus", linkStatus);
        nbt.putBoolean("Active", active);
    }
}
