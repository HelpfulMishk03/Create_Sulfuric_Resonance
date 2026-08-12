package io.hxneyw.repo.compat;

import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipe;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.network.chat.Component;

public final class CombustionBeltDisplayText {

    private CombustionBeltDisplayText() {
    }

    public static Component processingTimeValue(
            CombustionBeltRecipe recipe
    ) {
        int baseTicks = recipe.baseProcessingTicks();
        int ticksPerItem = recipe.processingTicksPerItem();

        if (ticksPerItem <= 0) {
            return Component.translatable(
                    "jei.sulfuricresonance.combustion_belt.value.time.fixed",
                    formatSeconds(baseTicks)
            );
        }

        if (baseTicks <= 0) {
            return Component.translatable(
                    "jei.sulfuricresonance.combustion_belt.value.time.per_item",
                    formatSeconds(ticksPerItem)
            );
        }

        return Component.translatable(
                "jei.sulfuricresonance.combustion_belt.value.time.combined",
                formatSeconds(baseTicks),
                formatSeconds(ticksPerItem)
        );
    }

    public static Component heatComponent(
            CombustionBeltRecipe.HeatRequirement requirement
    ) {
        return switch (requirement) {
            case HEATED -> Component.translatable(
                    "jei.sulfuricresonance.combustion_belt.heat.heated"
            );
            case SUPERHEATED -> Component.translatable(
                    "jei.sulfuricresonance.combustion_belt.heat.superheated"
            );
            case COMBUSTION -> Component.translatable(
                    "jei.sulfuricresonance.combustion_belt.heat.combustion"
            );
        };
    }

    public static int heatColor(
            CombustionBeltRecipe.HeatRequirement requirement
    ) {
        return switch (requirement) {
            case HEATED -> 0xD8872E;
            case SUPERHEATED -> 0xE94B22;
            case COMBUSTION -> 0x9A31D0;
        };
    }

    private static String formatSeconds(int ticks) {
        return BigDecimal.valueOf(Math.max(0, ticks))
                .divide(
                        BigDecimal.valueOf(20L),
                        2,
                        RoundingMode.HALF_UP
                )
                .stripTrailingZeros()
                .toPlainString();
    }
}
