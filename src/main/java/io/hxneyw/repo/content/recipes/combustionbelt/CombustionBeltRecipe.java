package io.hxneyw.repo.content.recipes.combustionbelt;

import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltExposure;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class CombustionBeltRecipe
        implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final HeatRequirement minimumHeat;
    private final int requiredSegments;
    private final int baseProcessingTicks;
    private final int processingTicksPerItem;

    public CombustionBeltRecipe(
            Ingredient ingredient,
            ItemStack result,
            HeatRequirement minimumHeat,
            int requiredSegments,
            int baseProcessingTicks,
            int processingTicksPerItem
    ) {
        this.ingredient = ingredient;
        this.result = result;
        this.minimumHeat = minimumHeat;
        this.requiredSegments = requiredSegments;
        this.baseProcessingTicks = baseProcessingTicks;
        this.processingTicksPerItem =
                processingTicksPerItem;
    }

    public Ingredient ingredient() {
        return this.ingredient;
    }

    public ItemStack result() {
        return this.result;
    }

    public HeatRequirement minimumHeat() {
        return this.minimumHeat;
    }

    public int requiredSegments() {
        return this.requiredSegments;
    }

    public int baseProcessingTicks() {
        return this.baseProcessingTicks;
    }

    public int processingTicksPerItem() {
        return this.processingTicksPerItem;
    }

    public int requiredProcessingTicks(
            int inputCount
    ) {
        long required =
                (long) this.baseProcessingTicks
                        + (long) this.processingTicksPerItem
                        * Math.max(1, inputCount);

        return (int) Math.min(
                Integer.MAX_VALUE,
                required
        );
    }

    @Override
    public boolean matches(
            @NotNull SingleRecipeInput input,
            @NotNull Level level
    ) {
        return this.ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(
            @NotNull SingleRecipeInput input,
            @NotNull HolderLookup.Provider registries
    ) {
        return this.result.copy();
    }

    @Override
    public @NotNull ItemStack getResultItem(
            @NotNull HolderLookup.Provider registries
    ) {
        return this.result;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients =
                NonNullList.create();

        ingredients.add(this.ingredient);
        return ingredients;
    }

    @Override
    public boolean canCraftInDimensions(
            int width,
            int height
    ) {
        return width * height >= 1;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CombustionBeltRecipeRegistry
                .SERIALIZER
                .get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return CombustionBeltRecipeRegistry
                .TYPE
                .get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public enum HeatRequirement {
        HEATED(
                "heated",
                1,
                CombustionBeltExposure
                        .ExposureBand.HEATED
        ),
        SUPERHEATED(
                "superheated",
                3,
                CombustionBeltExposure
                        .ExposureBand.SUPERHEATED
        ),
        COMBUSTION(
                "combustion",
                4,
                CombustionBeltExposure
                        .ExposureBand.COMBUSTION
        );

        private final String serializedName;
        private final int rank;
        private final CombustionBeltExposure.ExposureBand
                exposureBand;

        HeatRequirement(
                String serializedName,
                int rank,
                CombustionBeltExposure.ExposureBand
                        exposureBand
        ) {
            this.serializedName = serializedName;
            this.rank = rank;
            this.exposureBand = exposureBand;
        }

        public String serializedName() {
            return this.serializedName;
        }


        public CombustionBeltExposure.ExposureBand
        exposureBand() {
            return this.exposureBand;
        }

        public boolean accepts(
                MoltenRotorBlockEntity.RotorHeatLevel
                        heatTier
        ) {
            return heatTier != null
                    && heatTier.rank >= this.rank;
        }

        public static HeatRequirement
        fromSerializedName(
                String serializedName
        ) {
            for (HeatRequirement requirement : values()) {
                if (requirement.serializedName.equals(
                        serializedName
                )) {
                    return requirement;
                }
            }

            throw new IllegalArgumentException(
                    "Unknown Combustion Belt heat requirement: "
                            + serializedName
            );
        }
    }
}
