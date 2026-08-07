package io.hxneyw.repo.content.blocks.thermochemical;

import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import java.util.function.Supplier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ThermochemicalHeatUpdater {

    private ThermochemicalHeatUpdater() {
    }

    public static boolean update(
            @Nullable Level level,
            ThermochemicalHeatData heatData,
            Supplier<ThermochemicalHeatResolver.Result> resolver
    ) {
        if (level == null || level.isClientSide) {
            return false;
        }

        ThermochemicalHeatResolver.Result result =
                resolver.get();

        return heatData.update(
                result.heatTier(),
                result.sourcePos(),
                result.pathLength(),
                result.spanLimit(),
                result.remainingAllowance(),
                result.temperature()
        );
    }
}
