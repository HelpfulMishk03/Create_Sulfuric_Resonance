package io.hxneyw.repo.compat.jei;

import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.recipes.precisionspraying.PrecisionSprayingDisplay;
import io.hxneyw.repo.content.registry.AllModFluids;
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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
public final class PrecisionSprayingCategory
        implements IRecipeCategory<PrecisionSprayingDisplay> {

    public static final RecipeType<PrecisionSprayingDisplay> RECIPE_TYPE =
            RecipeType.create(
                    "sulfuricresonance",
                    "precision_spraying",
                    PrecisionSprayingDisplay.class
            );

    private static final int WIDTH = 182;
    private static final int HEIGHT = 70;
    private static final int TEXT = 0x404040;
    private static final int MUTED = 0x666666;
    private static final int TARGET_X = 8;
    private static final int FILTER_X = 42;
    private static final int ACID_X = 76;
    private static final int OUTPUT_X = 156;
    private static final int SLOT_Y = 22;
    private final Component title = Component.translatable(
            "recipe.sulfuricresonance.precision_spraying"
    );
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public PrecisionSprayingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(Items.PRECISION_SPRITZER.get())
        );
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public @NotNull RecipeType<PrecisionSprayingDisplay> getRecipeType() {
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
            PrecisionSprayingDisplay recipe,
            IFocusGroup focuses
    ) {
        builder.addSlot(RecipeIngredientRole.INPUT, TARGET_X, SLOT_Y)
                .setBackground(this.slot, -1, -1)
                .addItemStack(recipe.input().copy())
                .addRichTooltipCallback((slotView, tooltip) ->
                        tooltip.add(Component.translatable(
                                "jei.sulfuricresonance.precision_spraying.target"
                        ))
                );

        builder.addSlot(RecipeIngredientRole.CATALYST, FILTER_X, SLOT_Y)
                .setBackground(this.slot, -1, -1)
                .addItemStack(recipe.input().copy())
                .addRichTooltipCallback((slotView, tooltip) ->
                        tooltip.add(Component.translatable(
                                "jei.sulfuricresonance.precision_spraying.filter_field"
                        ))
                );

        builder.addSlot(RecipeIngredientRole.INPUT, ACID_X, SLOT_Y)
                .setBackground(this.slot, -1, -1)
                .addFluidStack(
                        AllModFluids.SULFURIC_ACID.get(),
                        recipe.fluidAmount()
                )
                .addRichTooltipCallback((slotView, tooltip) ->
                        tooltip.add(Component.translatable(
                                "jei.sulfuricresonance.precision_spraying.acid",
                                recipe.fluidAmount()
                        ))
                );

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
                .setBackground(this.slot, -1, -1)
                .addItemStack(recipe.output().copy());
    }

    @Override
    public void draw(
            PrecisionSprayingDisplay recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.literal("+"), 31, 27, MUTED, false);
        graphics.drawString(font, Component.literal("+"), 65, 27, MUTED, false);
        this.arrow.draw(graphics, 112, SLOT_Y);
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.total_acid",
                        recipe.fluidAmount()
                ),
                ACID_X + 8,
                46,
                MUTED
        );
        graphics.drawCenteredString(
                font,
                Component.translatable("jei.sulfuricresonance.precision_spraying.stage"),
                WIDTH / 2,
                58,
                TEXT
        );
    }
}
