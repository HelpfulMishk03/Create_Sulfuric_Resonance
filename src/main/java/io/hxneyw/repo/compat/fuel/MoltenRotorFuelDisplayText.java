package io.hxneyw.repo.compat.fuel;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.network.chat.Component;

public final class MoltenRotorFuelDisplayText {

    private MoltenRotorFuelDisplayText() {
    }

    public static MetricRows createRows(
            MoltenRotorFuelDisplay display
    ) {
        if (display.specialBehavior()
                == MoltenRotorFuelDisplay.SpecialBehavior.STICK_BOOST) {
            return new MetricRows(
                    new Metric(
                            label("burn_time_added"),
                            plusDuration(display.burnTimeTicks())
                    ),
                    new Metric(
                            label("log_heating_rate"),
                            heatingRate(FuelType.LOG.celsiusPerSecond)
                    ),
                    new Metric(
                            label("log_maximum"),
                            temperature(550.0F)
                    ),
                    new Metric(
                            label("maximum_tier"),
                            tier(550.0F)
                    )
            );
        }

        if (display.isManualHeatBoost()) {
            return new MetricRows(
                    new Metric(
                            label("heat_duration"),
                            plusDuration(display.burnTimeTicks())
                    ),
                    new Metric(
                            label("immediate_heat"),
                            Component.translatable(
                                    "jei.sulfuricresonance.molten_rotor_fuels.value.at_least_celsius",
                                    formatNumber(
                                            display.maximumTemperature()
                                    )
                            )
                    ),
                    new Metric(
                            label("maximum_tier"),
                            Component.translatable(
                                    "jei.sulfuricresonance.molten_rotor_fuels.tier.combustion"
                            )
                    ),
                    new Metric(
                            label("insertion"),
                            Component.translatable(
                                    "jei.sulfuricresonance.molten_rotor_fuels.value.manual_only"
                            )
                    )
            );
        }

        return new MetricRows(
                new Metric(
                        label("burn_time"),
                        duration(display.burnTimeTicks())
                ),
                new Metric(
                        label("heating_rate"),
                        heatingRate(display.heatingRate())
                ),
                new Metric(
                        label("maximum_temperature"),
                        temperature(display.maximumTemperature())
                ),
                new Metric(
                        label("maximum_tier"),
                        tier(display.maximumTemperature())
                )
        );
    }

    public static Component behaviorNote(
            MoltenRotorFuelDisplay display
    ) {
        String key = switch (display.specialBehavior()) {
            case STANDARD -> "standard";
            case STICK_BOOST -> "stick";
            case CINDER_BRIQUETTE -> "cinder_briquette";
            case SOUL_FIRED_REQUIREMENT -> "soul_fired";
            case TNT_RISK -> "tnt";
            case NETHER_STAR_BOOST -> "nether_star";
            case DRAGON_BREATH_BOOST -> "dragon_breath";
        };

        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.note."
                        + key
        );
    }

    public static String formatNumber(float value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static Component label(String name) {
        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.label."
                        + name
        );
    }

    private static Component heatingRate(float rate) {
        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.value.celsius_per_second",
                formatNumber(rate)
        );
    }

    private static Component temperature(float value) {
        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.value.celsius",
                formatNumber(value)
        );
    }

    private static Component tier(float maximumTemperature) {
        String tier = maximumTemperature >= 1300.0F
                ? "combustion"
                : maximumTemperature >= 800.0F
                ? "superheated"
                : maximumTemperature >= 300.0F
                ? "heated"
                : "unheated";

        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.tier."
                        + tier
        );
    }

    private static Component plusDuration(float ticks) {
        return Component.literal("+").append(duration(ticks));
    }

    private static Component duration(float ticks) {
        BigDecimal seconds =
                BigDecimal.valueOf(Math.max(0.0F, ticks))
                        .divide(
                                BigDecimal.valueOf(20L),
                                2,
                                RoundingMode.HALF_UP
                        )
                        .stripTrailingZeros();

        if (seconds.compareTo(BigDecimal.valueOf(60L)) >= 0) {
            BigDecimal[] parts =
                    seconds.divideAndRemainder(
                            BigDecimal.valueOf(60L)
                    );

            long minutes = parts[0].longValue();
            BigDecimal remainingSeconds =
                    parts[1].stripTrailingZeros();

            if (remainingSeconds.compareTo(BigDecimal.ZERO) == 0) {
                return Component.translatable(
                        "jei.sulfuricresonance.molten_rotor_fuels.value.minutes",
                        minutes
                );
            }

            return Component.translatable(
                    "jei.sulfuricresonance.molten_rotor_fuels.value.minutes_seconds",
                    minutes,
                    remainingSeconds.toPlainString()
            );
        }

        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.value.seconds",
                seconds.toPlainString()
        );
    }

    public record Metric(
            Component label,
            Component value
    ) {
    }

    public record MetricRows(
            Metric first,
            Metric second,
            Metric third,
            Metric fourth
    ) {
    }
}
