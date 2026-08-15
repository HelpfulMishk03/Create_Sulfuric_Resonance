package io.hxneyw.repo.compat.jei;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplay;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplayText;
import io.hxneyw.repo.compat.jei.animations.AnimatedMoltenRotor;
import io.hxneyw.repo.content.registry.AllModBlocks;
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
        MoltenRotorFuelDisplayText.MetricRows rows = MoltenRotorFuelDisplayText.createRows(recipe);

        drawMetricRow(graphics, font, 66, rows.first());
        drawMetricRow(graphics, font, 81, rows.second());
        drawMetricRow(graphics, font, 96, rows.third());
        drawMetricRow(graphics, font, 111, rows.fourth());
        drawNote(graphics, font, MoltenRotorFuelDisplayText.behaviorNote(recipe));
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

}
