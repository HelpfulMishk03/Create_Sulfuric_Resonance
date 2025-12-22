package io.hxneyw.repo.compat.jei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.ModBlocks;
import io.hxneyw.repo.content.recipes.CombustionMixingRecipe;
import io.hxneyw.repo.content.recipes.ModRecipeTypes;
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

import java.util.Collections;
import java.util.List;

@JeiPlugin
public class CombustionMixingJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CreateSulfuricResonance.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CombustionMixingCategory(
                        new CreateRecipeCategory.Info<>(
                                CombustionMixingCategory.RECIPE_TYPE,
                                Component.translatable("recipe.sulfuricresonance.combustion_mixing"),
                                registration.getJeiHelpers().getGuiHelper().createDrawableItemStack(
                                        new ItemStack(ModBlocks.MOLTEN_ROTOR_FURNACE.get())
                                ),
                                null,
                                CombustionMixingJeiPlugin::getAllRecipes,
                                Collections.emptyList()
                        )
                )
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<BasinRecipe>> recipes = getAllRecipes();

        CreateSulfuricResonance.LOGGER.info("========================================");
        CreateSulfuricResonance.LOGGER.info("[JEI] Registering Combustion Mixing recipes");
        CreateSulfuricResonance.LOGGER.info("[JEI] Found {} recipes", recipes.size());

        for (RecipeHolder<BasinRecipe> holder : recipes) {
            BasinRecipe recipe = holder.value();
            CreateSulfuricResonance.LOGGER.info("[JEI] Recipe ID: {}", holder.id());
            CreateSulfuricResonance.LOGGER.info("[JEI]   Type: {}", recipe.getClass().getSimpleName());
            CreateSulfuricResonance.LOGGER.info("[JEI]   Ingredients: {}", recipe.getIngredients().size());
            CreateSulfuricResonance.LOGGER.info("[JEI]   Results: {}", recipe.getRollableResults().size());
            CreateSulfuricResonance.LOGGER.info("[JEI]   Is CombustionMixingRecipe? {}", recipe instanceof CombustionMixingRecipe);
        }
        CreateSulfuricResonance.LOGGER.info("========================================");

        registration.addRecipes(CombustionMixingCategory.RECIPE_TYPE, recipes);
    }



    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Register Molten Rotor as catalyst
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.MOLTEN_ROTOR_FURNACE.get()),
                CombustionMixingCategory.RECIPE_TYPE
        );

        // IMPORTANT: Register Basin + Mixer as catalysts
        // This makes "Combustion Mixing" show when pressing U on Mixer/Basin
        registration.addRecipeCatalyst(
                new ItemStack(AllBlocks.BASIN.get()),
                CombustionMixingCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(AllBlocks.MECHANICAL_MIXER.get()),
                CombustionMixingCategory.RECIPE_TYPE
        );
    }

    private static List<RecipeHolder<BasinRecipe>> getAllRecipes() {
        if (Minecraft.getInstance().level == null) {
            return Collections.emptyList();
        }

        // Get all Combustion Mixing recipes and cast them to BasinRecipe
        // (safe because CombustionMixingRecipe extends BasinRecipe)
        return Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.COMBUSTION_MIXING.get())
                .stream()
                .map(holder -> (RecipeHolder<BasinRecipe>)(RecipeHolder<?>)holder)
                .toList();
    }
}
