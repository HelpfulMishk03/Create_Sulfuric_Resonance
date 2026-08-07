package io.hxneyw.repo.compat.jei;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplay;
import io.hxneyw.repo.compat.jei.animations.AnimatedMoltenRotor;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
public final class MoltenRotorFuelCategory
        implements IRecipeCategory<MoltenRotorFuelDisplay> {

    public static final RecipeType<MoltenRotorFuelDisplay> RECIPE_TYPE =
            RecipeType.create(
                    "sulfuricresonance",
                    "molten_rotor_fuels",
                    MoltenRotorFuelDisplay.class
            );

    private static final int WIDTH = 190;
    private static final int HEIGHT = 151;
    private static final int LABEL_COLOR = 0x555555;
    private static final int VALUE_COLOR = 0x303030;
    private static final int NOTE_COLOR = 0x7A4B24;
    private static final int DIVIDER_COLOR = 0xFFB8B8B8;
    private static final int PANEL_COLOR = 0x12000000;

    private final Component title = Component.translatable(
            "recipe.sulfuricresonance.molten_rotor_fuels"
    );

    private final IDrawable icon;
    private final IDrawable slot;
    private final AnimatedMoltenRotor furnace = new AnimatedMoltenRotor();

    public MoltenRotorFuelCategory(IGuiHelper guiHelper) {
        ItemStack furnaceStack = new ItemStack(
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
        );

        this.icon = guiHelper.createDrawableItemStack(furnaceStack);
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public @NotNull RecipeType<MoltenRotorFuelDisplay> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            MoltenRotorFuelDisplay recipe,
            IFocusGroup focuses
    ) {
        builder.addSlot(
                        RecipeIngredientRole.INPUT,
                        12,
                        25
                )
                .setBackground(this.slot, -1, -1)
                .addItemStacks(recipe.fuelStacks());

        builder.addSlot(
                        RecipeIngredientRole.CATALYST,
                        162,
                        25
                )
                .setBackground(this.slot, -1, -1)
                .addItemStack(
                        new ItemStack(
                                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                        )
                );
    }

    @Override
    public void draw(
            MoltenRotorFuelDisplay recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        this.furnace
                .withHeat(displayHeat(recipe))
                .draw(graphics, 80, 17);

        graphics.fill(
                6,
                59,
                WIDTH - 6,
                HEIGHT - 3,
                PANEL_COLOR
        );

        graphics.fill(
                6,
                59,
                WIDTH - 6,
                60,
                DIVIDER_COLOR
        );

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        MetricRows rows = createRows(recipe);

        drawMetricRow(graphics, font, 66, rows.first());
        drawMetricRow(graphics, font, 81, rows.second());
        drawMetricRow(graphics, font, 96, rows.third());
        drawMetricRow(graphics, font, 111, rows.fourth());
        drawNote(graphics, font, behaviorNote(recipe));
    }

    private static MetricRows createRows(MoltenRotorFuelDisplay recipe) {
        if (recipe.specialBehavior()
                == MoltenRotorFuelDisplay.SpecialBehavior.STICK_BOOST) {
            return new MetricRows(
                    new Metric(
                            label("burn_time_added"),
                            plusDuration(recipe.burnTimeTicks())
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

        if (recipe.isManualHeatBoost()) {
            return new MetricRows(
                    new Metric(
                            label("heat_duration"),
                            plusDuration(recipe.burnTimeTicks())
                    ),
                    new Metric(
                            label("immediate_heat"),
                            Component.translatable(
                                    "jei.sulfuricresonance.molten_rotor_fuels.value.at_least_celsius",
                                    formatNumber(recipe.maximumTemperature())
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
                        duration(recipe.burnTimeTicks())
                ),
                new Metric(
                        label("heating_rate"),
                        heatingRate(recipe.heatingRate())
                ),
                new Metric(
                        label("maximum_temperature"),
                        temperature(recipe.maximumTemperature())
                ),
                new Metric(
                        label("maximum_tier"),
                        tier(recipe.maximumTemperature())
                )
        );
    }

    private static void drawMetricRow(
            GuiGraphics graphics,
            Font font,
            int y,
            Metric metric
    ) {
        graphics.drawString(
                font,
                metric.label(),
                11,
                y,
                LABEL_COLOR,
                false
        );

        int valueX = WIDTH - 11 - font.width(metric.value());

        graphics.drawString(
                font,
                metric.value(),
                Math.max(82, valueX),
                y,
                VALUE_COLOR,
                false
        );
    }

    private static void drawNote(
            GuiGraphics graphics,
            Font font,
            Component note
    ) {
        java.util.List<FormattedCharSequence> lines =
                font.split(note, WIDTH - 16);

        for (int index = 0; index < Math.min(2, lines.size()); index++) {
            FormattedCharSequence line = lines.get(index);
            int noteX = Math.max(8, (WIDTH - font.width(line)) / 2);

            graphics.drawString(
                    font,
                    line,
                    noteX,
                    128 + index * 9,
                    NOTE_COLOR,
                    false
            );
        }
    }

    private static Component label(String name) {
        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.label." + name
        );
    }

    private static Component behaviorNote(MoltenRotorFuelDisplay recipe) {
        String key = switch (recipe.specialBehavior()) {
            case STANDARD -> "standard";
            case STICK_BOOST -> "stick";
            case CINDER_BRIQUETTE -> "cinder_briquette";
            case SOUL_FIRED_REQUIREMENT -> "soul_fired";
            case TNT_RISK -> "tnt";
            case NETHER_STAR_BOOST -> "nether_star";
            case DRAGON_BREATH_BOOST -> "dragon_breath";
        };

        return Component.translatable(
                "jei.sulfuricresonance.molten_rotor_fuels.note." + key
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
                "jei.sulfuricresonance.molten_rotor_fuels.tier." + tier
        );
    }

    private static Component plusDuration(float ticks) {
        return Component.literal("+").append(duration(ticks));
    }

    private static Component duration(float ticks) {
        BigDecimal seconds = BigDecimal.valueOf(Math.max(0.0F, ticks))
                .divide(
                        BigDecimal.valueOf(20L),
                        2,
                        RoundingMode.HALF_UP
                )
                .stripTrailingZeros();

        if (seconds.scale() <= 0 && seconds.longValue() >= 60L) {
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

    private static HeatLevel displayHeat(MoltenRotorFuelDisplay recipe) {
        float maximumTemperature = recipe.specialBehavior()
                == MoltenRotorFuelDisplay.SpecialBehavior.STICK_BOOST
                ? 550.0F
                : recipe.maximumTemperature();

        if (maximumTemperature >= 800.0F) {
            return HeatLevel.SEETHING;
        }

        if (maximumTemperature >= 300.0F) {
            return HeatLevel.KINDLED;
        }

        return HeatLevel.NONE;
    }

    private record Metric(Component label, Component value) {
    }

    private record MetricRows(
            Metric first,
            Metric second,
            Metric third,
            Metric fourth
    ) {
    }
}
