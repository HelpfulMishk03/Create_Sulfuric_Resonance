package io.hxneyw.repo.mixin;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import io.hxneyw.repo.content.process.SablePlotBlockEntityLookup;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEmberLampBlockEntity.class)
@SuppressWarnings({"unused", "DataFlowIssue"})
public abstract class SableLivingEmberLampActorMixin implements BlockEntitySubLevelActor {
    @Override
    public void sable$tick(ServerSubLevel subLevel) {
        LivingEmberLampBlockEntity lamp =
                (LivingEmberLampBlockEntity) (Object) this;
        if (lamp.getLevel() == null) {
            return;
        }
        net.minecraft.world.level.LevelAccessor plotLevel =
                SablePlotBlockEntityLookup.embeddedLevelAccessor(subLevel);
        if (plotLevel == null) {
            return;
        }
        net.minecraft.core.BlockPos localPos =
                SablePlotBlockEntityLookup.embeddedBlockPos(
                        subLevel,
                        lamp.getBlockPos()
                );
        if (localPos == null) {
            return;
        }
        LivingEmberLampBlockEntity.serverTick(
                lamp.getLevel(),
                plotLevel,
                localPos,
                lamp,
                java.util.Objects.requireNonNullElse(
                        SablePlotBlockEntityLookup.embeddedLightEngine(subLevel),
                        lamp.getLevel().getLightEngine())
        );
    }
}
