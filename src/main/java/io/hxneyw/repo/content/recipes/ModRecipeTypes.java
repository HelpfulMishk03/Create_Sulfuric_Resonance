package io.hxneyw.repo.content.recipes;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe.Serializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeTypes {

    private static final String COMBUSTION_MIXING_ID =
            "combustion_mixing";

    private static final ResourceLocation COMBUSTION_MIXING_LOCATION =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    COMBUSTION_MIXING_ID
            );

    private static final DeferredRegister<RecipeSerializer<?>>
            SERIALIZER_REGISTER =
            DeferredRegister.create(
                    BuiltInRegistries.RECIPE_SERIALIZER,
                    CreateSulfuricResonance.MODID
            );

    private static final DeferredRegister<RecipeType<?>>
            TYPE_REGISTER =
            DeferredRegister.create(
                    Registries.RECIPE_TYPE,
                    CreateSulfuricResonance.MODID
            );

    public static final DeferredHolder<
            RecipeType<?>,
            RecipeType<CombustionMixingRecipe>
            > COMBUSTION_MIXING =
            TYPE_REGISTER.register(
                    COMBUSTION_MIXING_ID,
                    () -> RecipeType.simple(
                            COMBUSTION_MIXING_LOCATION
                    )
            );

    public static final DeferredHolder<
            RecipeSerializer<?>,
            Serializer<CombustionMixingRecipe>
            > COMBUSTION_MIXING_SERIALIZER =
            SERIALIZER_REGISTER.register(
                    COMBUSTION_MIXING_ID,
                    () -> new Serializer<>(
                            CombustionMixingRecipe::new
                    )
            );

    public static final IRecipeTypeInfo
            COMBUSTION_MIXING_TYPE_INFO =
            new IRecipeTypeInfo() {

                @Override
                public ResourceLocation getId() {
                    return COMBUSTION_MIXING_LOCATION;
                }

                @Override
                @SuppressWarnings("unchecked")
                public <T extends RecipeSerializer<?>>
                T getSerializer() {
                    return (T) COMBUSTION_MIXING_SERIALIZER.get();
                }

                @Override
                @SuppressWarnings("unchecked")
                public <I extends RecipeInput, R extends Recipe<I>>
                RecipeType<R> getType() {
                    return (RecipeType<R>) COMBUSTION_MIXING.get();
                }
            };

    private ModRecipeTypes() {
    }

    public static void register(IEventBus modEventBus) {
        SERIALIZER_REGISTER.register(modEventBus);
        TYPE_REGISTER.register(modEventBus);
    }
}