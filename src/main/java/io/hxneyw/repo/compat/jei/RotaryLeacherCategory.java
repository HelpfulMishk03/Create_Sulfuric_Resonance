package io.hxneyw.repo.compat.jei;

import io.hxneyw.repo.content.recipes.rotaryleaching.RotaryLeachingRecipe;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.compat.jei.animations.AnimatedRotaryLeacher;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class RotaryLeacherCategory implements IRecipeCategory<RotaryLeachingRecipe> {
    public static final RecipeType<RotaryLeachingRecipe> RECIPE_TYPE = RecipeType.create("sulfuricresonance", "rotary_leaching", RotaryLeachingRecipe.class);
    private final IDrawable icon;
    private final IDrawable slot;
    private final AnimatedRotaryLeacher animation = new AnimatedRotaryLeacher();

    public RotaryLeacherCategory(IGuiHelper helper) {
        ItemStack machine = new ItemStack(AllModBlocks.ROTARY_LEACHER.get());
        icon = helper.createDrawableItemStack(machine);
        slot = helper.getSlotDrawable();
    }

    @Override public @NotNull RecipeType<RotaryLeachingRecipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public @NotNull Component getTitle() { return Component.translatable("recipe.sulfuricresonance.rotary_leaching"); }
    @Override public int getWidth() { return 144; }
    @Override public int getHeight() { return 154; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RotaryLeachingRecipe recipe, @NotNull IFocusGroup focuses) {
        ItemStack[] ingredients = recipe.ingredient().getItems();
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 30).setBackground(slot, -1, -1).addItemStacks(List.of(ingredients))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.sulfuricresonance.rotary_leaching.input_count", recipe.inputCount())));
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 54).setBackground(slot, -1, -1)
                .addFluidStack(AllModFluids.SULFURIC_ACID.get(), recipe.fluidAmount());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 30).setBackground(slot, -1, -1).addItemStack(recipe.result());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 54).setBackground(slot, -1, -1).addItemStack(recipe.byproduct());
    }

    @Override
    public void draw(RotaryLeachingRecipe recipe, @NotNull IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.input_count", recipe.inputCount()), 4, 8, 0x303030, false);

        animation.withRecipe(recipe).draw(graphics, 84, 52);
        drawFlowArrow(graphics, 34);
        drawFlowArrow(graphics, 100);

        graphics.fill(2, 76, 142, 77, 0xFF999999);
        graphics.fill(2, 83, 10, 91, 0xFFBBA65A);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.acid", recipe.fluidAmount()), 14, 83, 0x67562D, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.speed", recipe.minimumSpeed()), 2, 98, 0x303030, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.stress", recipe.minimumSpeed() * 2), 2, 109, 0x303030, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.whisk", 32), 2, 120, 0x303030, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.base_time", recipe.processingTime() / 20.0), 2, 131, 0x303030, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.bulk"), 2, 143, 0x303030, false);
    }

    private static void drawFlowArrow(GuiGraphics graphics, int x) {
        int y = 49;
        int color = 0xFF777777;
        graphics.fill(x, y + 4, x + 9, y + 6, color);
        graphics.fill(x + 7, y, x + 9, y + 10, color);
        graphics.fill(x + 9, y + 2, x + 11, y + 8, color);
        graphics.fill(x + 11, y + 4, x + 13, y + 6, color);
    }
}
