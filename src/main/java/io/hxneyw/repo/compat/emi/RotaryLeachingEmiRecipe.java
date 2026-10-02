package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.compat.emi.animations.EmiAnimatedRotaryLeacher;
import io.hxneyw.repo.content.recipes.rotaryleaching.RotaryLeachingRecipe;
import io.hxneyw.repo.content.registry.AllModFluids;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public final class RotaryLeachingEmiRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final RotaryLeachingRecipe recipe;
    private final EmiAnimatedRotaryLeacher animation = new EmiAnimatedRotaryLeacher();
    public RotaryLeachingEmiRecipe(RecipeHolder<RotaryLeachingRecipe> holder) { id = holder.id(); recipe = holder.value(); }
    @Override public EmiRecipeCategory getCategory() { return SulfuricResonanceEmiPlugin.ROTARY_LEACHING; }
    @Override public @Nullable ResourceLocation getId() { return id; }
    @Override public List<EmiIngredient> getInputs() { return List.of(EmiIngredient.of(recipe.ingredient(), recipe.inputCount()), EmiStack.of(AllModFluids.SULFURIC_ACID.get(), recipe.fluidAmount())); }
    @Override public List<EmiStack> getOutputs() { return List.of(EmiStack.of(recipe.result()), EmiStack.of(recipe.byproduct())); }
    @Override public List<EmiIngredient> getCatalysts() { return List.of(SulfuricResonanceEmiPlugin.ROTARY_LEACHER); }
    @Override public int getDisplayWidth() { return 224; }
    @Override public int getDisplayHeight() { return 154; }
    @Override public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, 224, 154, (graphics, mouseX, mouseY, delta) -> draw(graphics));
        widgets.addSlot(EmiIngredient.of(recipe.ingredient(), recipe.inputCount()), 4, 32);
        widgets.addTank(EmiStack.of(AllModFluids.SULFURIC_ACID.get(), recipe.fluidAmount()), 4, 54, 18, 18, io.hxneyw.repo.content.blocks.rotaryleacher.RotaryLeacherBlockEntity.ACID_CAPACITY);
        widgets.addSlot(EmiStack.of(recipe.result()), 199, 32).recipeContext(this);
        widgets.addSlot(EmiStack.of(recipe.byproduct()), 199, 54).recipeContext(this);
    }
    private void draw(GuiGraphics graphics) {
        graphics.fill(0, 0, 224, 154, 0x11000000);
        animation.withRecipe(recipe).draw(graphics, 130, 52);
        drawFlowArrow(graphics, 59);
        drawFlowArrow(graphics, 164);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("jei.sulfuricresonance.rotary_leaching.speed", recipe.minimumSpeed()), 4, 82, 0x303030, false);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("jei.sulfuricresonance.rotary_leaching.stress", recipe.minimumSpeed() * 2), 4, 98, 0x303030, false);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("jei.sulfuricresonance.rotary_leaching.base_time", recipe.processingTime() / 20.0), 4, 114, 0x303030, false);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("jei.sulfuricresonance.rotary_leaching.bulk"), 4, 130, 0x303030, false);
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
