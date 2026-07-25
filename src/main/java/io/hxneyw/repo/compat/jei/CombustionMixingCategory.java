package io.hxneyw.repo.compat.jei;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import io.hxneyw.repo.compat.jei.animations.AnimatedMoltenRotor;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.apache.commons.lang3.mutable.MutableInt;

@ParametersAreNonnullByDefault
public class CombustionMixingCategory extends BasinCategory {
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static final RecipeType<RecipeHolder<BasinRecipe>> RECIPE_TYPE = RecipeType.create("sulfuricresonance", "combustion_mixing", (Class)RecipeHolder.class);
   private final AnimatedMixer mixer = new AnimatedMixer();
   private final AnimatedMoltenRotor heater = new AnimatedMoltenRotor();

   public CombustionMixingCategory(Info<BasinRecipe> info) {
      super(info, true);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe, IFocusGroup focuses) {
      List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(recipe.getIngredients());
      int size = condensedIngredients.size() + recipe.getFluidIngredients().size();
      int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
      int i = 0;

      for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
         List<ItemStack> stacks = new ArrayList<>();

         for (ItemStack itemStack : pair.getFirst().getItems()) {
            ItemStack copy = itemStack.copy();
            copy.setCount(pair.getSecond().getValue());
            stacks.add(copy);
         }

         builder.addSlot(
                         RecipeIngredientRole.INPUT,
                         17 + xOffset + i % 3 * 19,
                         51 - i / 3 * 19
                 )
                 .setBackground(getRenderedSlot(), -1, -1)
                 .addItemStacks(stacks)
                 .addRichTooltipCallback((view, tooltip) -> {
                    int count = pair.getSecond().getValue();
                    if (count > 1) {
                       tooltip.add(Component.literal("x" + count));
                    }
                 });
         i++;
      }

      for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
         int x = 17 + xOffset + i % 3 * 19;
         int y = 51 - i / 3 * 19;
         addFluidSlot(builder, x, y, fluidIngredient);
         i++;
      }

      size = recipe.getRollableResults().size() + recipe.getFluidResults().size();
      i = 0;

      for (ProcessingOutput result : recipe.getRollableResults()) {
         int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : (i % 2 == 0 ? 10 : -9));
         int yPosition = -19 * (i / 2) + 51;
         builder.addSlot(
                         RecipeIngredientRole.OUTPUT,
                         xPosition,
                         yPosition
                 )
                 .setBackground(getRenderedSlot(result), -1, -1)
                 .addItemStack(result.getStack())
                 .addRichTooltipCallback(addStochasticTooltip(result));
         i++;
      }

      for (FluidStack fluidResult : recipe.getFluidResults()) {
         int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : (i % 2 == 0 ? 10 : -9));
         int yPosition = -19 * (i / 2) + 51;
         addFluidSlot(builder, xPosition, yPosition, fluidResult);
         i++;
      }

      HeatCondition requiredHeat = recipe.getRequiredHeat();
      if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
         builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81)
                 .addItemStack(
                         new ItemStack(AllModBlocks.MOLTEN_ROTOR_FURNACE.get())
                 );
      }

      if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
         builder.addSlot(RecipeIngredientRole.CATALYST, 153, 81)
                 .addItemStack(
                         new ItemStack(Items.SOUL_FIRED_BLAZE_CAKE.get())
                 );
      }
   }

   public void draw(BasinRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
      HeatCondition requiredHeat = recipe.getRequiredHeat();
      boolean noHeat = requiredHeat == HeatCondition.NONE;
      int vRows = (1 + recipe.getFluidResults().size() + recipe.getRollableResults().size()) / 2;
      int centerX = this.getBackground().getWidth() / 2 + 3;
      if (vRows <= 2) {
         AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (vRows - 1) + 32);
      }

      AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
      shadow.render(graphics, 81, 58 + (noHeat ? 10 : 30));
      if (requiredHeat != HeatCondition.NONE) {
         this.heater.withHeat(requiredHeat.visualizeAsBlazeBurner()).draw(graphics, centerX, 55);
      }

      this.mixer.draw(graphics, centerX, 34);
      AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;
      heatBar.render(graphics, 4, 80);
      Minecraft mc = Minecraft.getInstance();
      String text;
      int color = 0;
      if (requiredHeat == HeatCondition.SUPERHEATED) {
         text = "Combustion";
         color = 11141375;
      } else {
         text = CreateLang.translateDirect(
                 requiredHeat.getTranslationKey()
         ).getString();}

      graphics.drawString(mc.font, text, 9, 86, color, false);
   }
}
