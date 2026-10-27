package io.hxneyw.repo.mixin;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceArray;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceBlockEntity;
import io.hxneyw.repo.content.process.SablePlotBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThermochemicalBoilerInterfaceBlockEntity.class)
@SuppressWarnings({"unused", "DataFlowIssue"})
public abstract class SableThermochemicalBoilerInterfaceActorMixin
        implements BlockEntitySubLevelActor {

    @Override
    public void sable$tick(ServerSubLevel subLevel) {
        ThermochemicalBoilerInterfaceBlockEntity blockEntity =
                (ThermochemicalBoilerInterfaceBlockEntity) (Object) this;
        LevelAccessor plotLevel =
                SablePlotBlockEntityLookup.embeddedLevelAccessor(subLevel);
        BlockPos localPos = SablePlotBlockEntityLookup.embeddedBlockPos(
                subLevel,
                blockEntity.getBlockPos()
        );
        if (plotLevel != null && localPos != null) {
            ThermochemicalBoilerInterfaceArray.exposePhysicsNorthShaft(
                    plotLevel,
                    localPos
            );
        }
    }
}
