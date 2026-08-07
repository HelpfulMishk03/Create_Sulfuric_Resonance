package io.hxneyw.repo.compat.emi;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplay;
import io.hxneyw.repo.compat.fuel.MoltenRotorFuelDisplayRegistry;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.recipes.ModRecipeTypes;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipe;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;

@EmiEntrypoint
public final class SulfuricResonanceEmiPlugin implements EmiPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final EmiStack MOLTEN_ROTOR =
            EmiStack.of(AllModBlocks.MOLTEN_ROTOR_FURNACE.get());

    public static final EmiStack COMBUSTION_BELT =
            EmiStack.of(Items.COMBUSTION_BELT_CONNECTOR.get());

    public static final EmiStack CERAMIC_CRUCIBLE =
            EmiStack.of(AllModBlocks.ASH_CERAMIC_CRUCIBLE.get());

    public static final EmiStack MECHANICAL_MIXER =
            EmiStack.of(AllBlocks.MECHANICAL_MIXER.get());

    public static final EmiRecipeCategory MOLTEN_ROTOR_FUELS =
            new EmiRecipeCategory(
                    ResourceLocation.fromNamespaceAndPath(
                            CreateSulfuricResonance.MODID,
                            "molten_rotor_fuels"
                    ),
                    MOLTEN_ROTOR
            );

    public static final EmiRecipeCategory COMBUSTION_BELT_PROCESSING =
            new EmiRecipeCategory(
                    ResourceLocation.fromNamespaceAndPath(
                            CreateSulfuricResonance.MODID,
                            "combustion_belt"
                    ),
                    COMBUSTION_BELT
            );

    public static final EmiRecipeCategory COMBUSTION_MIXING =
            new EmiRecipeCategory(
                    ResourceLocation.fromNamespaceAndPath(
                            CreateSulfuricResonance.MODID,
                            "combustion_mixing"
                    ),
                    MECHANICAL_MIXER
            );

    @Override
    public void register(EmiRegistry registry) {
        registerCategories(registry);
        registerWorkstations(registry);
        registerMoltenRotorFuels(registry);
        registerProcessingRecipes(registry);
    }

    private static void registerCategories(EmiRegistry registry) {
        registry.addCategory(MOLTEN_ROTOR_FUELS);
        registry.addCategory(COMBUSTION_BELT_PROCESSING);
        registry.addCategory(COMBUSTION_MIXING);
    }

    private static void registerWorkstations(EmiRegistry registry) {
        registry.addWorkstation(MOLTEN_ROTOR_FUELS, MOLTEN_ROTOR);

        registry.addWorkstation(
                COMBUSTION_BELT_PROCESSING,
                COMBUSTION_BELT
        );
        registry.addWorkstation(
                COMBUSTION_BELT_PROCESSING,
                MOLTEN_ROTOR
        );

        registry.addWorkstation(
                COMBUSTION_MIXING,
                CERAMIC_CRUCIBLE
        );
        registry.addWorkstation(
                COMBUSTION_MIXING,
                MECHANICAL_MIXER
        );
        registry.addWorkstation(
                COMBUSTION_MIXING,
                MOLTEN_ROTOR
        );
    }

    private static void registerMoltenRotorFuels(
            EmiRegistry registry
    ) {
        List<MoltenRotorFuelDisplay> displays =
                MoltenRotorFuelDisplayRegistry.createDisplays();

        for (int index = 0; index < displays.size(); index++) {
            registry.addRecipe(
                    new MoltenRotorFuelEmiRecipe(
                            displays.get(index),
                            index
                    )
            );
        }
    }

    private static void registerProcessingRecipes(
            EmiRegistry registry
    ) {
        int beltRecipes = 0;
        int mixingRecipes = 0;

        for (RecipeHolder<?> holder
                : registry.getRecipeManager().getRecipes()) {
            Recipe<?> recipe = holder.value();

            if (recipe instanceof CombustionBeltRecipe beltRecipe) {
                registry.addRecipe(
                        new CombustionBeltEmiRecipe(
                                new RecipeHolder<>(
                                        holder.id(),
                                        beltRecipe
                                )
                        )
                );
                beltRecipes++;
                continue;
            }

            if (recipe instanceof BasinRecipe basinRecipe
                    && recipe.getType()
                    == ModRecipeTypes.COMBUSTION_MIXING.get()) {
                registry.addRecipe(
                        new CombustionMixingEmiRecipe(
                                new RecipeHolder<>(
                                        holder.id(),
                                        basinRecipe
                                )
                        )
                );
                mixingRecipes++;
            }
        }

        LOGGER.info(
                "CSR EMI registered {} Combustion Belt recipes and {} Combustion Mixing recipes",
                beltRecipes,
                mixingRecipes
        );
    }

}
