package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.compat.CombustionBeltDisplayText;
import io.hxneyw.repo.compat.emi.animations.EmiAnimatedCombustionBelt;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipe;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public final class CombustionBeltEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 124;
    private static final int SCENE_X = 94;
    private static final int SCENE_Y = 17;
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
    private final EmiAnimatedCombustionBelt beltAnimation =
            new EmiAnimatedCombustionBelt();

    public CombustionBeltEmiRecipe(
            RecipeHolder<CombustionBeltRecipe> holder
    ) {
        this.id = holder.id();
        this.recipe = holder.value();
        this.input = EmiIngredient.of(this.recipe.ingredient());
        this.output = EmiStack.of(this.recipe.result().copy());
        this.furnace = SulfuricResonanceEmiPlugin.MOLTEN_ROTOR.copy();
        this.belt = SulfuricResonanceEmiPlugin.COMBUSTION_BELT.copy();
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
                        drawScreen(graphics)
        );

        widgets.addSlot(this.input, 10, 24);
        widgets.addSlot(this.output, 162, 24)
                .recipeContext(this);
    }

    private void drawScreen(GuiGraphics graphics) {
        this.beltAnimation
                .withDisplayedSegments(this.recipe.requiredSegments())
                .draw(
                        graphics,
                        SCENE_X,
                        SCENE_Y,
                        displayedIngredient(this.recipe)
                );

        graphics.fill(
                6,
                59,
                WIDTH - 6,
                HEIGHT - 5,
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

        drawRequirementRow(
                graphics,
                font,
                67,
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.label.heat"
                ),
                CombustionBeltDisplayText.heatComponent(
                        this.recipe.minimumHeat()
                ),
                CombustionBeltDisplayText.heatColor(
                        this.recipe.minimumHeat()
                )
        );

        drawRequirementRow(
                graphics,
                font,
                82,
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.label.belt"
                ),
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.value.segments",
                        this.recipe.requiredSegments()
                ),
                VALUE_COLOR
        );

        drawProcessingTime(
                graphics,
                font,
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.label.time"
                ),
                CombustionBeltDisplayText.processingTimeValue(this.recipe)
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
                97,
                LABEL_COLOR,
                false
        );

        graphics.drawString(
                font,
                value,
                WIDTH - 12 - font.width(value),
                109,
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

    private static ItemStack displayedIngredient(
            CombustionBeltRecipe recipe
    ) {
        ItemStack[] items = recipe.ingredient().getItems();

        if (items.length == 0) {
            return ItemStack.EMPTY;
        }

        int index = (int) (
                Util.getMillis() / 1000L
                        % items.length
        );

        ItemStack displayed = items[index].copy();
        displayed.setCount(1);
        return displayed;
    }
}
