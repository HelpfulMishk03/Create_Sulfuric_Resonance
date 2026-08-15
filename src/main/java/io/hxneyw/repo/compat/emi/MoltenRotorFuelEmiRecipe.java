package io.hxneyw.repo.compat.emi;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.compat.emi.animations.EmiAnimatedMoltenRotor;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplay;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplayText;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public final class MoltenRotorFuelEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 151;
    private static final int LABEL_COLOR = 0x555555;
    private static final int VALUE_COLOR = 0x303030;
    private static final int NOTE_COLOR = 0x7A4B24;
    private static final int DIVIDER_COLOR = 0xFFB8B8B8;
    private static final int PANEL_COLOR = 0x12000000;

    private final ResourceLocation id;
    private final MoltenRotorFuelDisplay display;
    private final EmiIngredient fuel;
    private final EmiStack furnace;
    private final EmiAnimatedMoltenRotor furnaceAnimation =
            new EmiAnimatedMoltenRotor();

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
        widgets.addDrawable(
                0,
                0,
                WIDTH,
                HEIGHT,
                (graphics, mouseX, mouseY, delta) -> drawScreen(graphics)
        );

        widgets.addSlot(this.fuel, 12, 25)
                .appendTooltip(
                        MoltenRotorFuelDisplayText.behaviorNote(this.display)
                );

        widgets.addSlot(this.furnace, 162, 25)
                .catalyst(true);
    }

    private void drawScreen(GuiGraphics graphics) {
        this.furnaceAnimation
                .withHeat(displayHeat(this.display))
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

        Font font = Minecraft.getInstance().font;
        MoltenRotorFuelDisplayText.MetricRows rows =
                MoltenRotorFuelDisplayText.createRows(this.display);

        drawMetricRow(graphics, font, 66, rows.first());
        drawMetricRow(graphics, font, 81, rows.second());
        drawMetricRow(graphics, font, 96, rows.third());
        drawMetricRow(graphics, font, 111, rows.fourth());
        drawNote(
                graphics,
                font,
                MoltenRotorFuelDisplayText.behaviorNote(this.display)
        );
    }

    private static void drawMetricRow(
            GuiGraphics graphics,
            Font font,
            int y,
            MoltenRotorFuelDisplayText.Metric metric
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
        List<FormattedCharSequence> lines = font.split(note, WIDTH - 16);

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

    private static HeatLevel displayHeat(MoltenRotorFuelDisplay display) {
        float maximumTemperature = display.specialBehavior()
                == MoltenRotorFuelDisplay.SpecialBehavior.STICK_BOOST
                ? 550.0F
                : display.maximumTemperature();

        if (maximumTemperature >= 800.0F) {
            return HeatLevel.SEETHING;
        }

        if (maximumTemperature >= 300.0F) {
            return HeatLevel.KINDLED;
        }

        return HeatLevel.NONE;
    }
}
