package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity;
import io.hxneyw.repo.content.process.ProcessState;
import io.hxneyw.repo.content.process.ProcessTargetRef;
import io.hxneyw.repo.content.registry.AllModBlocks;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ProcessMonitorScenes {

    private ProcessMonitorScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title(
                "process_monitor.operation",
                "Monitoring Five Process Channels"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.86F);
        scene.setSceneOffsetY(-0.45F);

        BlockPos monitorPos = util.grid().at(1, 1, 2);
        BlockPos chamberPos = util.grid().at(3, 1, 2);
        Selection monitor = util.select().position(monitorPos);
        Selection chamber = util.select().position(chamberPos);

        UUID monitorIdentity = UUID.fromString(
                "432d2d54-9b75-4a8a-80f1-58c4193a5b01"
        );
        UUID chamberIdentity = UUID.fromString(
                "29c8da92-780d-4cd7-b45b-2848977cf502"
        );

        BlockState monitorState = AllModBlocks.PROCESS_MONITOR.get()
                .defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        scene.world().setBlock(monitorPos, monitorState, false);
        scene.world().setBlock(
                chamberPos,
                AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get().defaultBlockState(),
                false
        );

        scene.world().modifyBlockEntityNBT(
                monitor,
                ProcessMonitorBlockEntity.class,
                nbt -> nbt.putUUID("MonitorIdentity", monitorIdentity)
        );
        scene.world().modifyBlockEntityNBT(
                chamber,
                SulfuricResonanceChamberBlockEntity.class,
                nbt -> nbt.putUUID("ProcessIdentity", chamberIdentity)
        );

        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(monitor, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(120)
                .text("The Process Monitor owns five independent machine channels")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showControls(
                        util.vector().topOf(monitorPos),
                        Pointing.DOWN,
                        55
                )
                .rightClick();
        scene.world().modifyBlockEntityNBT(
                monitor,
                ProcessMonitorBlockEntity.class,
                nbt -> nbt.putInt("SelectedChannel", 2)
        );
        scene.overlay().showText(110)
                .text("Empty-hand right-click cycles the selector through Channels 1 to 5")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(monitorPos))
                .placeNearTarget();
        scene.idle(120);

        scene.overlay().showControls(
                        util.vector().topOf(monitorPos),
                        Pointing.DOWN,
                        60
                )
                .leftClick()
                .whileSneaking();
        scene.world().modifyBlockEntityNBT(
                monitor,
                ProcessMonitorBlockEntity.class,
                nbt -> nbt.putInt("BindingPulseTicks", 8)
        );
        scene.overlay().showText(125)
                .text("Sneak-left-click with an empty main hand arms only the currently selected channel for linking")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(125);

        scene.world().showSection(chamber, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "process_monitor_link_target",
                chamber,
                120
        );
        scene.overlay().showControls(
                        util.vector().topOf(chamberPos),
                        Pointing.DOWN,
                        60
                )
                .rightClick();
        scene.overlay().showText(120)
                .text("Right-click a process-capable machine to assign it to that armed channel")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(chamberPos))
                .placeNearTarget();
        scene.idle(130);

        ProcessTargetRef target = new ProcessTargetRef(
                chamberPos,
                "minecraft:overworld",
                chamberIdentity
        );
        scene.world().modifyBlockEntityNBT(
                chamber,
                SulfuricResonanceChamberBlockEntity.class,
                nbt -> nbt.putInt("ChamberStatus", 6)
        );
        scene.world().modifyBlockEntityNBT(
                monitor,
                ProcessMonitorBlockEntity.class,
                nbt -> configureLinkedChannel(nbt, monitorIdentity, 2, target)
        );
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "process_monitor_linked_monitor",
                monitor,
                120
        );
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "process_monitor_linked_machine",
                chamber,
                120
        );
        scene.overlay().showText(120)
                .text("The selected channel now reports that machine's live process state")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(130);

        scene.overlay().showText(130)
                .text("Other channels remain independent and can be assigned to other process machines")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(140);

        scene.overlay().showText(145)
                .text("-- means unassigned, OFF means temporarily unavailable, and ERR means the original machine became invalid")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(155);

        scene.overlay().showText(145)
                .text("OFF can recover when the assigned machine becomes available again; ERR stays latched until that channel is manually rebound")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(155);

        scene.overlay().showText(125)
                .text("The Monitor keeps ownership of all five machine assignments; Process Gauges only read them")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(monitorPos))
                .placeNearTarget();
        scene.idle(135);
        scene.markAsFinished();
    }

    private static void configureLinkedChannel(
            CompoundTag nbt,
            UUID monitorIdentity,
            int selectedChannel,
            ProcessTargetRef target
    ) {
        nbt.putUUID("MonitorIdentity", monitorIdentity);
        nbt.putInt("SelectedChannel", selectedChannel);

        ListTag channels = new ListTag();
        for (int channel = 0; channel < ProcessMonitorBlockEntity.CHANNEL_COUNT; channel++) {
            CompoundTag channelTag = new CompoundTag();
            channelTag.putInt("Index", channel);
            channelTag.putInt("State", ProcessState.IDLE.ordinal());
            channelTag.putInt("Availability", 0);

            if (channel == selectedChannel) {
                channelTag.put("Target", target.save());
                channelTag.putInt("State", ProcessState.READY.ordinal());
                channelTag.putInt("Availability", 1);
            }
            channels.add(channelTag);
        }
        nbt.put("Channels", channels);
    }
}
