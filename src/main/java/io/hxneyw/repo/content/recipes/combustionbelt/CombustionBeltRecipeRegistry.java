package io.hxneyw.repo.content.recipes.combustionbelt;

import io.hxneyw.repo.CreateSulfuricResonance;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CombustionBeltRecipeRegistry {

    private static final ResourceLocation COMBUSTION_BELT_ID =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "combustion_belt"
            );

    private static final DeferredRegister<RecipeType<?>>
            RECIPE_TYPES =
            DeferredRegister.create(
                    Registries.RECIPE_TYPE,
                    CreateSulfuricResonance.MODID
            );

    private static final DeferredRegister<RecipeSerializer<?>>
            RECIPE_SERIALIZERS =
            DeferredRegister.create(
                    Registries.RECIPE_SERIALIZER,
                    CreateSulfuricResonance.MODID
            );

    public static final Supplier<
            RecipeType<CombustionBeltRecipe>
            > TYPE =
            RECIPE_TYPES.register(
                    "combustion_belt",
                    () -> RecipeType.simple(
                            COMBUSTION_BELT_ID
                    )
            );

    public static final Supplier<
            RecipeSerializer<CombustionBeltRecipe>
            > SERIALIZER =
            RECIPE_SERIALIZERS.register(
                    "combustion_belt",
                    CombustionBeltRecipeSerializer::new
            );

    private CombustionBeltRecipeRegistry() {
    }

    public static void register(
            IEventBus modEventBus
    ) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
