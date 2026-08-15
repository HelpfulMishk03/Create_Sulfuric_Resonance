package io.hxneyw.repo.compat.jei;

import io.hxneyw.repo.compat.jei.animations.AnimatedCombustionBelt;
import io.hxneyw.repo.compat.CombustionBeltDisplayText;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipe;
import java.util.Arrays;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
public final class CombustionBeltCategory
        implements IRecipeCategory<CombustionBeltRecipe> {

    public static final RecipeType<CombustionBeltRecipe> RECIPE_TYPE =
            RecipeType.create(
                    "sulfuricresonance",
                    "combustion_belt",
                    CombustionBeltRecipe.class
            );

    private static final int WIDTH = 190;
    private static final int HEIGHT = 124;
    private static final int SCENE_X = 94;
    private static final int SCENE_Y = 17;
    private static final int LABEL_COLOR = 0x555555;
    private static final int VALUE_COLOR = 0x303030;
    private static final int DIVIDER_COLOR = 0xFFB8B8B8;
    private static final int PANEL_COLOR = 0x12000000;

    private final Component title = Component.translatable(
            "recipe.sulfuricresonance.combustion_belt"
    );

    private final IDrawable icon;
    private final IDrawable slot;
    private final AnimatedCombustionBelt belt =
            new AnimatedCombustionBelt();

    public CombustionBeltCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(
                        Items.COMBUSTION_BELT_CONNECTOR.get()
                )
        );

        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public @NotNull RecipeType<CombustionBeltRecipe> getRecipeType() {
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
            CombustionBeltRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addSlot(
                        RecipeIngredientRole.INPUT,
                        10,
                        24
                )
                .setBackground(this.slot, -1, -1)
                .addItemStacks(
                        Arrays.asList(
                                recipe.ingredient().getItems()
                        )
                );

        builder.addSlot(
                        RecipeIngredientRole.OUTPUT,
                        162,
                        24
                )
                .setBackground(this.slot, -1, -1)
                .addItemStack(recipe.result().copy());
    }

    @Override
    public void draw(
            CombustionBeltRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        this.belt
                .withDisplayedSegments(recipe.requiredSegments())
                .draw(
                        graphics,
                        SCENE_X,
                        SCENE_Y,
                        displayedIngredient(recipe)
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

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        drawRequirementRow(
                graphics,
                font,
                67,
                Component.translatable(
                        "jei.sulfuricresonance.combustion_belt.label.heat"
                ),
                CombustionBeltDisplayText.heatComponent(recipe.minimumHeat()),
                CombustionBeltDisplayText.heatColor(recipe.minimumHeat())
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
