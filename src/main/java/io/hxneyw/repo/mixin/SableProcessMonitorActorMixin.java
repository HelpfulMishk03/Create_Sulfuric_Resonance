package io.hxneyw.repo.mixin;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProcessMonitorBlockEntity.class)
@SuppressWarnings({"unused", "DataFlowIssue"})
public abstract class SableProcessMonitorActorMixin implements BlockEntitySubLevelActor {
    @Override
    public void sable$tick(ServerSubLevel subLevel) {
        ProcessMonitorBlockEntity monitor =
                (ProcessMonitorBlockEntity) (Object) this;
        net.minecraft.world.level.Level level = monitor.getLevel();
        if (level != null) {
            ProcessMonitorBlockEntity.tick(level, monitor);
        }
    }
}
