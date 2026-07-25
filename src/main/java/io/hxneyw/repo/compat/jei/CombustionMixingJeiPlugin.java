package io.hxneyw.repo.compat.jei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory.Info;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import io.hxneyw.repo.content.recipes.ModRecipeTypes;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
@SuppressWarnings("unused")
public class CombustionMixingJeiPlugin implements IModPlugin {
   @NotNull
   @Override
   public ResourceLocation getPluginUid() {
      return ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "jei_plugin");
   }

   @Override
   public void registerCategories(IRecipeCategoryRegistration registration) {
      Info<BasinRecipe> categoryInfo = new Info<>(
              CombustionMixingCategory.RECIPE_TYPE,
              Component.translatable(
                      "recipe.sulfuricresonance.combustion_mixing"
              ),
              new EmptyBackground(177, 103),
              registration.getJeiHelpers()
                      .getGuiHelper()
                      .createDrawableItemStack(
                              new ItemStack(AllBlocks.MECHANICAL_MIXER.get())
                      ),
              CombustionMixingJeiPlugin::getAllRecipes,
              Collections.emptyList()
      );

      registration.addRecipeCategories(
              new CombustionMixingCategory(categoryInfo)
      );
   }

   @Override
   public void registerRecipes(@NotNull IRecipeRegistration registration) {
      List<RecipeHolder<BasinRecipe>> recipes = getAllRecipes();
      registration.addRecipes(CombustionMixingCategory.RECIPE_TYPE, recipes);
   }

   @Override
   public void registerRecipeCatalysts(
           IRecipeCatalystRegistration registration
   ) {
      registration.addRecipeCatalyst(
              new ItemStack(AllBlocks.MECHANICAL_MIXER.get()),
              CombustionMixingCategory.RECIPE_TYPE
      );

      registration.addRecipeCatalyst(
              new ItemStack(AllBlocks.BASIN.get()),
              CombustionMixingCategory.RECIPE_TYPE
      );

      registration.addRecipeCatalyst(
              new ItemStack(AllModBlocks.MOLTEN_ROTOR_FURNACE.get()),
              CombustionMixingCategory.RECIPE_TYPE
      );
   }

   @SuppressWarnings("unchecked")
   private static List<RecipeHolder<BasinRecipe>> getAllRecipes() {
      if (Minecraft.getInstance().level == null) {
         return Collections.emptyList();
      }

      net.minecraft.world.item.crafting.RecipeType<BasinRecipe> recipeType =
              (net.minecraft.world.item.crafting.RecipeType<BasinRecipe>)
                      (net.minecraft.world.item.crafting.RecipeType<?>)
                              ModRecipeTypes.COMBUSTION_MIXING.get();

      return Minecraft.getInstance()
              .level
              .getRecipeManager()
              .getAllRecipesFor(recipeType);
   }
}
