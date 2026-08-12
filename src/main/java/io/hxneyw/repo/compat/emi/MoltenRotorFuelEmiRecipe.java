package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplay;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplayText;
import io.hxneyw.repo.content.registry.AllModBlocks;
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
                .appendTooltip(MoltenRotorFuelDisplayText.behaviorNote(this.display));

        widgets.addSlot(this.furnace, WIDTH - 22, 4)
                .catalyst(true);

        MoltenRotorFuelDisplayText.MetricRows rows = MoltenRotorFuelDisplayText.createRows(this.display);

        drawMetricRow(widgets, 29, rows.first());
        drawMetricRow(widgets, 43, rows.second());
        drawMetricRow(widgets, 57, rows.third());
        drawMetricRow(widgets, 71, rows.fourth());
        drawNote(widgets, MoltenRotorFuelDisplayText.behaviorNote(this.display));
    }


    private static void drawMetricRow(
            WidgetHolder widgets,
            int y,
            MoltenRotorFuelDisplayText.Metric metric
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


}
