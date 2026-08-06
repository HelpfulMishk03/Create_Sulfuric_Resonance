package io.hxneyw.repo.compat.jei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory.Info;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplay;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplayRegistry;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.recipes.ModRecipeTypes;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipe;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipeRegistry;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
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
        return ResourceLocation.fromNamespaceAndPath(
                "sulfuricresonance",
                "jei_plugin"
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final mezz.jei.api.recipe.RecipeType<
            RecipeHolder<BasinRecipe>
            > CREATE_MIXING =
            mezz.jei.api.recipe.RecipeType.create(
                    "create",
                    "mixing",
                    (Class) RecipeHolder.class
            );

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final mezz.jei.api.recipe.RecipeType<
            RecipeHolder<BasinRecipe>
            > CREATE_PACKING =
            mezz.jei.api.recipe.RecipeType.create(
                    "create",
                    "packing",
                    (Class) RecipeHolder.class
            );

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final mezz.jei.api.recipe.RecipeType<
            RecipeHolder<BasinRecipe>
            > CREATE_AUTOMATIC_SHAPELESS =
            mezz.jei.api.recipe.RecipeType.create(
                    "create",
                    "automatic_shapeless",
                    (Class) RecipeHolder.class
            );

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final mezz.jei.api.recipe.RecipeType<
            RecipeHolder<BasinRecipe>
            > CREATE_AUTOMATIC_BREWING =
            mezz.jei.api.recipe.RecipeType.create(
                    "create",
                    "automatic_brewing",
                    (Class) RecipeHolder.class
            );

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final mezz.jei.api.recipe.RecipeType<
            RecipeHolder<BasinRecipe>
            > CREATE_AUTOMATIC_PACKING =
            mezz.jei.api.recipe.RecipeType.create(
                    "create",
                    "automatic_packing",
                    (Class) RecipeHolder.class
            );

    @Override
    public void registerCategories(
            IRecipeCategoryRegistration registration
    ) {
        IGuiHelper guiHelper = registration
                .getJeiHelpers()
                .getGuiHelper();

        Info<BasinRecipe> categoryInfo = new Info<>(
                CombustionMixingCategory.RECIPE_TYPE,
                Component.translatable(
                        "recipe.sulfuricresonance.combustion_mixing"
                ),
                new EmptyBackground(177, 103),
                guiHelper.createDrawableItemStack(
                        new ItemStack(
                                AllBlocks.MECHANICAL_MIXER.get()
                        )
                ),
                CombustionMixingJeiPlugin::getAllMixingRecipes,
                Collections.emptyList()
        );

        registration.addRecipeCategories(
                new CombustionMixingCategory(categoryInfo),
                new CombustionBeltCategory(guiHelper),
                new MoltenRotorFuelCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(
            @NotNull IRecipeRegistration registration
    ) {
        registration.addRecipes(
                CombustionMixingCategory.RECIPE_TYPE,
                getAllMixingRecipes()
        );

        registration.addRecipes(
                CombustionBeltCategory.RECIPE_TYPE,
                getAllCombustionBeltRecipes()
        );

        registration.addRecipes(
                MoltenRotorFuelCategory.RECIPE_TYPE,
                getAllMoltenRotorFuels()
        );
    }

    @Override
    public void registerRecipeCatalysts(
            IRecipeCatalystRegistration registration
    ) {
        ItemStack crucible = new ItemStack(
                AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()
        );

        ItemStack moltenRotor = new ItemStack(
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
        );

        registration.addRecipeCatalyst(
                crucible,
                CREATE_MIXING
        );
        registration.addRecipeCatalyst(
                crucible,
                CREATE_PACKING
        );
        registration.addRecipeCatalyst(
                crucible,
                CREATE_AUTOMATIC_SHAPELESS
        );
        registration.addRecipeCatalyst(
                crucible,
                CREATE_AUTOMATIC_BREWING
        );
        registration.addRecipeCatalyst(
                crucible,
                CREATE_AUTOMATIC_PACKING
        );
        registration.addRecipeCatalyst(
                crucible,
                CombustionMixingCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(AllBlocks.MECHANICAL_MIXER.get()),
                CombustionMixingCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                moltenRotor,
                CombustionMixingCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(
                        Items.COMBUSTION_BELT_CONNECTOR.get()
                ),
                CombustionBeltCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                moltenRotor,
                CombustionBeltCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                moltenRotor,
                MoltenRotorFuelCategory.RECIPE_TYPE
        );
    }

    @SuppressWarnings("unchecked")
    private static List<RecipeHolder<BasinRecipe>>
    getAllMixingRecipes() {
        if (Minecraft.getInstance().level == null) {
            return Collections.emptyList();
        }

        net.minecraft.world.item.crafting.RecipeType<BasinRecipe>
                recipeType =
                (net.minecraft.world.item.crafting.RecipeType<BasinRecipe>)
                        (net.minecraft.world.item.crafting.RecipeType<?>)
                                ModRecipeTypes.COMBUSTION_MIXING.get();

        return Minecraft.getInstance()
                .level
                .getRecipeManager()
                .getAllRecipesFor(recipeType);
    }

    private static List<CombustionBeltRecipe>
    getAllCombustionBeltRecipes() {
        if (Minecraft.getInstance().level == null) {
            return Collections.emptyList();
        }

        return Minecraft.getInstance()
                .level
                .getRecipeManager()
                .getAllRecipesFor(
                        CombustionBeltRecipeRegistry.TYPE.get()
                )
                .stream()
                .map(RecipeHolder::value)
                .toList();
    }

    private static List<MoltenRotorFuelDisplay>
    getAllMoltenRotorFuels() {
        return MoltenRotorFuelDisplayRegistry.createDisplays();
    }
}
