package io.hxneyw.repo.mixin;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThermalBatteryBlockEntity.class)
@SuppressWarnings({"unused", "DataFlowIssue"})
public abstract class SableThermalBatteryActorMixin implements BlockEntitySubLevelActor {
    @Override
    public void sable$tick(ServerSubLevel subLevel) {
        ((ThermalBatteryBlockEntity) (Object) this).tick();
    }
}
