package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.compat.CombustionBeltDisplayText;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipe;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public final class CombustionBeltEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 112;
    private static final int LABEL_COLOR = 0x555555;
    private static final int VALUE_COLOR = 0x303030;
    private static final int DIVIDER_COLOR = 0xFFB8B8B8;
    private static final int PANEL_COLOR = 0x12000000;

    private final ResourceLocation id;
    private final CombustionBeltRecipe recipe;
    private final EmiIngredient input;
    private final EmiStack output;
    private final EmiStack furnace;
    private final EmiStack belt;

    public CombustionBeltEmiRecipe(
            RecipeHolder<CombustionBeltRecipe> holder
    ) {
        this.id = holder.id();
        this.recipe = holder.value();
        this.input = EmiIngredient.of(this.recipe.ingredient());
        this.output = EmiStack.of(this.recipe.result().copy());
        this.furnace =
                SulfuricResonanceEmiPlugin.MOLTEN_ROTOR.copy();
        this.belt =
                SulfuricResonanceEmiPlugin.COMBUSTION_BELT.copy();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SulfuricResonanceEmiPlugin
                .COMBUSTION_BELT_PROCESSING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(this.input);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(this.belt, this.furnace);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(this.output);
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
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(
                0,
                0,
                WIDTH,
                HEIGHT,
                (graphics, mouseX, mouseY, delta) ->
                        drawBackground(graphics, this.recipe)
        );

        widgets.addSlot(this.input, 10, 18);

        widgets.addSlot(this.belt, 72, 18)
                .catalyst(true);

        widgets.addSlot(this.furnace, 100, 18)
                .catalyst(true);

        widgets.addSlot(this.output, 162, 18)
                .recipeContext(this);
    }

    private static void drawBackground(
            GuiGraphics graphics,
            CombustionBeltRecipe recipe
    ) {
        Font font = Minecraft.getInstance().font;

        graphics.drawString(
                font,
                Component.literal("→"),
                42,
                23,
                VALUE_COLOR,
                false
        );

        graphics.drawString(
                font,
                Component.literal("+"),
                93,
                23,
                VALUE_COLOR,
                false
        );

        graphics.drawString(
                font,
                Component.literal("→"),
                136,
                23,
                VALUE_COLOR,
                false
        );

        graphics.fill(
                6,
                51,
                WIDTH - 6,
                HEIGHT - 5,
                PANEL_COLOR
        );

        graphics.fill(
                6,
                51,
                WIDTH - 6,
                52,
                DIVIDER_COLOR
        );

        drawRequirementRow(
                graphics,
                font,
                60,
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.label.heat"
                ),
                CombustionBeltDisplayText.heatComponent(recipe.minimumHeat()),
                CombustionBeltDisplayText.heatColor(recipe.minimumHeat())
        );

        drawRequirementRow(
                graphics,
                font,
                75,
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.label.belt"
                ),
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.value.segments",
                        recipe.requiredSegments()
                ),
                VALUE_COLOR
        );

        drawProcessingTime(
                graphics,
                font,
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.label.time"
                ),
                CombustionBeltDisplayText.processingTimeValue(recipe)
        );
    }

    private static void drawProcessingTime(
            GuiGraphics graphics,
            Font font,
            Component label,
            Component value
    ) {
        graphics.drawString(
                font,
                label,
                12,
                90,
                LABEL_COLOR,
                false
        );

        graphics.drawString(
                font,
                value,
                WIDTH - 12 - font.width(value),
                99,
                VALUE_COLOR,
                false
        );
    }

    private static void drawRequirementRow(
            GuiGraphics graphics,
            Font font,
            int y,
            Component label,
            Component value,
            int valueColor
    ) {
        graphics.drawString(
                font,
                label,
                12,
                y,
                LABEL_COLOR,
                false
        );

        int valueX = WIDTH - 12 - font.width(value);

        graphics.drawString(
                font,
                value,
                Math.max(73, valueX),
                y,
                valueColor,
                false
        );
    }


}
