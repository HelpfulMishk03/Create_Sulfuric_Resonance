package io.hxneyw.repo.compat.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
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
    public RotaryLeachingEmiRecipe(RecipeHolder<RotaryLeachingRecipe> holder) { id = holder.id(); recipe = holder.value(); }
    @Override public EmiRecipeCategory getCategory() { return SulfuricResonanceEmiPlugin.ROTARY_LEACHING; }
    @Override public @Nullable ResourceLocation getId() { return id; }
    @Override public List<EmiIngredient> getInputs() { return List.of(EmiIngredient.of(recipe.ingredient(), recipe.inputCount()), EmiStack.of(AllModFluids.SULFURIC_ACID.get(), recipe.fluidAmount())); }
    @Override public List<EmiStack> getOutputs() { return List.of(EmiStack.of(recipe.result()), EmiStack.of(recipe.byproduct())); }
    @Override public List<EmiIngredient> getCatalysts() { return List.of(SulfuricResonanceEmiPlugin.ROTARY_LEACHER); }
    @Override public int getDisplayWidth() { return 224; }
    @Override public int getDisplayHeight() { return 142; }
    @Override public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiIngredient.of(recipe.ingredient(), recipe.inputCount()), 4, 32);
        widgets.addTank(EmiStack.of(AllModFluids.SULFURIC_ACID.get(), recipe.fluidAmount()), 4, 54, 18, 18, 4000);
        widgets.addSlot(EmiStack.of(recipe.result()), 199, 32).recipeContext(this);
        widgets.addSlot(EmiStack.of(recipe.byproduct()), 199, 54).recipeContext(this);
        widgets.addDrawable(0, 0, 224, 142, (graphics, mouseX, mouseY, delta) -> draw(graphics));
    }
    private void draw(GuiGraphics graphics) {
        graphics.fill(0, 0, 224, 142, 0x11000000);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("jei.sulfuricresonance.rotary_leaching.speed", recipe.minimumSpeed()), 4, 82, 0x303030, false);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("jei.sulfuricresonance.rotary_leaching.stress"), 4, 98, 0x303030, false);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, Component.translatable("jei.sulfuricresonance.rotary_leaching.time", recipe.processingTime() / 20.0), 4, 114, 0x303030, false);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(112, 49, 200);
        pose.scale(2.25F, 2.25F, 2.25F);
        graphics.renderItem(new net.minecraft.world.item.ItemStack(io.hxneyw.repo.content.registry.AllModBlocks.ROTARY_LEACHER.get()), -8, -8);
        pose.popPose();
    }
}
