package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeltBlockEntity.class)
@SuppressWarnings({"unused", "DataFlowIssue"})
public abstract class SableBeltBlockEntityActorMixin implements BlockEntitySubLevelActor {
    @Override
    public void sable$tick(ServerSubLevel subLevel) {
        ((BeltBlockEntity) (Object) this).tick();
    }
}
