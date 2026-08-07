package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplay;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public final class MoltenRotorFuelEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 112;
    private static final int LABEL_COLOR = 0x555555;
    private static final int VALUE_COLOR = 0x303030;
    private static final int NOTE_COLOR = 0x7A4B24;

    private final ResourceLocation id;
    private final MoltenRotorFuelDisplay display;
    private final EmiIngredient fuel;
    private final EmiStack furnace;

    public MoltenRotorFuelEmiRecipe(
            MoltenRotorFuelDisplay display,
            int index
    ) {
        this.display = display;
        this.id = ResourceLocation.fromNamespaceAndPath(
                CreateSulfuricResonance.MODID,
                "/molten_rotor_fuels/" + index
        );

        List<EmiStack> fuelStacks = display.fuelStacks()
                .stream()
                .map(EmiStack::of)
                .toList();

        this.fuel = EmiIngredient.of(fuelStacks);
        this.furnace = EmiStack.of(
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
        );
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SulfuricResonanceEmiPlugin.MOLTEN_ROTOR_FUELS;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(this.fuel);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(this.furnace);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(this.fuel, 4, 4)
                .appendTooltip(behaviorNote(this.display));

        widgets.addSlot(this.furnace, WIDTH - 22, 4)
                .catalyst(true);

        MetricRows rows = createRows(this.display);

        drawMetricRow(widgets, 29, rows.first());
        drawMetricRow(widgets, 43, rows.second());
        drawMetricRow(widgets, 57, rows.third());
        drawMetricRow(widgets, 71, rows.fourth());
        drawNote(widgets, behaviorNote(this.display));
    }

    private static MetricRows createRows(
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
                                    formatNumber(display.maximumTemperature())
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

    private static void drawMetricRow(
            WidgetHolder widgets,
            int y,
            Metric metric
    ) {
        Font font = Minecraft.getInstance().font;

        widgets.addText(
                metric.label(),
                4,
                y,
                LABEL_COLOR,
                false
        );

        widgets.addText(
                metric.value(),
                Math.max(76, WIDTH - 4 - font.width(metric.value())),
                y,
                VALUE_COLOR,
                false
        );
    }

    private static void drawNote(
            WidgetHolder widgets,
            Component note
    ) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines =
                font.split(note, WIDTH - 8);

        for (int index = 0;
             index < Math.min(2, lines.size());
             index++) {
            FormattedCharSequence line = lines.get(index);
            int x = Math.max(4, (WIDTH - font.width(line)) / 2);

            widgets.addText(
                    line,
                    x,
                    89 + index * 9,
                    NOTE_COLOR,
                    false
            );
        }
    }

    private static Component label(String name) {
        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.label."
                        + name
        );
    }

    private static Component behaviorNote(
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

        if (seconds.scale() <= 0
                && seconds.longValue() >= 60L) {
            long totalSeconds = seconds.longValue();
            long minutes = totalSeconds / 60L;
            long remainingSeconds = totalSeconds % 60L;

            if (remainingSeconds == 0L) {
                return Component.translatable(
                        "jei.sulfuricresonance.molten_rotor_fuels.value.minutes",
                        minutes
                );
            }

            return Component.translatable(
                    "jei.sulfuricresonance.molten_rotor_fuels.value.minutes_seconds",
                    minutes,
                    remainingSeconds
            );
        }

        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.value.seconds",
                seconds.toPlainString()
        );
    }

    private static String formatNumber(float value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private record Metric(
            Component label,
            Component value
    ) {
    }

    private record MetricRows(
            Metric first,
            Metric second,
            Metric third,
            Metric fourth
    ) {
    }
}
