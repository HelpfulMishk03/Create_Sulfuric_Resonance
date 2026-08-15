package io.hxneyw.repo.content.blocks.thermochemical;

import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ThermochemicalHeatTickHelper {

    private ThermochemicalHeatTickHelper() {
    }

    public static void tick(
            BlockEntity blockEntity,
            ThermochemicalHeatData heatData,
            Runnable syncAction
    ) {
        Level level = blockEntity.getLevel();

        if (level == null || level.isClientSide) {
            return;
        }

        ThermochemicalHeatResolver.Result result =
                ThermochemicalHeatResolver.resolve(blockEntity);

        if (!heatData.update(
                result.heatTier(),
                result.sourcePos(),
                result.pathLength(),
                result.spanLimit(),
                result.remainingAllowance(),
                result.temperature()
        )) {
            return;
        }

        blockEntity.setChanged();
        syncAction.run();
    }
}
