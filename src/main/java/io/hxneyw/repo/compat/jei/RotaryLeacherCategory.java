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
    @Override public int getWidth() { return 160; }
    @Override public int getHeight() { return 152; }
    @Override public @NotNull IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RotaryLeachingRecipe recipe, @NotNull IFocusGroup focuses) {
        ItemStack[] ingredients = recipe.ingredient().getItems();
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 30).setBackground(slot, -1, -1).addItemStacks(List.of(ingredients))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.sulfuricresonance.rotary_leaching.input_count", recipe.inputCount())));
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 54).setBackground(slot, -1, -1)
                .addFluidStack(AllModFluids.SULFURIC_ACID.get(), recipe.fluidAmount());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 30).setBackground(slot, -1, -1).addItemStack(recipe.result());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 54).setBackground(slot, -1, -1).addItemStack(recipe.byproduct());
    }

    @Override
    public void draw(RotaryLeachingRecipe recipe, @NotNull IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.input_count", recipe.inputCount()), 4, 8, 0x303030, false);

        animation.withRecipe(recipe).draw(graphics, 90, 54);

        graphics.fill(4, 78, 156, 79, 0xFF999999);
        graphics.fill(4, 85, 12, 93, 0xFFBBA65A);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.acid", recipe.fluidAmount()), 16, 85, 0x67562D, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.speed", recipe.minimumSpeed()), 4, 98, 0x303030, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.stress", recipe.minimumSpeed() * 2), 4, 112, 0x303030, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.whisk", 32), 4, 126, 0x303030, false);
        graphics.drawString(font, Component.translatable("jei.sulfuricresonance.rotary_leaching.time", recipe.processingTime() / 20.0), 4, 140, 0x303030, false);
    }
}
