package io.hxneyw.repo.mixin;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThermalRelaySwitchBlockEntity.class)
@SuppressWarnings({"unused", "DataFlowIssue"})
public abstract class SableThermalRelayActorMixin implements BlockEntitySubLevelActor {
    @Override
    public void sable$tick(ServerSubLevel subLevel) {
        ThermalRelaySwitchBlockEntity relay =
                (ThermalRelaySwitchBlockEntity) (Object) this;
        net.minecraft.world.level.Level level = relay.getLevel();
        if (level != null) {
            ThermalRelaySwitchBlockEntity.serverTick(level, relay);
        }
    }
}
