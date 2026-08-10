package io.hxneyw.repo.content.recipes.sulfuricresonancechamber;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.registry.AllModFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public final class SulfuricResonanceChamberRecipe
        implements Recipe<SingleRecipeInput> {

    private final Ingredient substrate;
    private final ItemStack result;
    private final int acidAmount;
    private final HeatRequirement minimumHeat;
    private final int minimumSpeed;
    private final int processingTime;

    public SulfuricResonanceChamberRecipe(
            Ingredient substrate,
            ItemStack result,
            int acidAmount,
            HeatRequirement minimumHeat,
            int minimumSpeed,
            int processingTime
    ) {
        this.substrate = substrate;
        this.result = result;
        this.acidAmount = acidAmount;
        this.minimumHeat = minimumHeat;
        this.minimumSpeed = minimumSpeed;
        this.processingTime = processingTime;
    }

    public Ingredient substrate() {
        return substrate;
    }

    public ItemStack result() {
        return result;
    }

    public int acidAmount() {
        return acidAmount;
    }

    public HeatRequirement minimumHeat() {
        return minimumHeat;
    }

    public int minimumSpeed() {
        return minimumSpeed;
    }

    public int processingTime() {
        return processingTime;
    }

    public boolean matches(
            ItemStack input,
            FluidStack acid,
            MoltenRotorBlockEntity.RotorHeatLevel heat,
            float speed
    ) {
        return substrate.test(input)
                && acid.getFluid() == AllModFluids.SULFURIC_ACID.get()
                && acid.getAmount() >= acidAmount
                && minimumHeat.accepts(heat)
                && speed >= minimumSpeed;
    }

    @Override
    public boolean matches(
            @NotNull SingleRecipeInput input,
            @NotNull Level level
    ) {
        return substrate.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(
            @NotNull SingleRecipeInput input,
            @NotNull HolderLookup.Provider registries
    ) {
        return result.copy();
    }

    @Override
    public @NotNull ItemStack getResultItem(
            @NotNull HolderLookup.Provider registries
    ) {
        return result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(substrate);
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
        return SulfuricResonanceChamberRecipeRegistry
                .SERIALIZER
                .get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return SulfuricResonanceChamberRecipeRegistry.TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public enum HeatRequirement {
        SUPERHEATED("superheated", 3),
        COMBUSTION("combustion", 4);

        private final String serializedName;
        private final int minimumRank;

        HeatRequirement(
                String serializedName,
                int minimumRank
        ) {
            this.serializedName = serializedName;
            this.minimumRank = minimumRank;
        }

        public String serializedName() {
            return serializedName;
        }

        public boolean accepts(
                MoltenRotorBlockEntity.RotorHeatLevel heat
        ) {
            return heat != null && heat.rank >= minimumRank;
        }

        public static HeatRequirement fromSerializedName(
                String serializedName
        ) {
            for (HeatRequirement value : values()) {
                if (value.serializedName.equals(serializedName)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(
                    "Unknown chamber heat requirement: "
                            + serializedName
            );
        }
    }
}
