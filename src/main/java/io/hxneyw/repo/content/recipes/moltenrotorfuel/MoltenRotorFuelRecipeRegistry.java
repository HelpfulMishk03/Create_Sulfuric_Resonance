package io.hxneyw.repo.content.recipes.moltenrotorfuel;

import io.hxneyw.repo.CreateSulfuricResonance;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MoltenRotorFuelRecipeRegistry {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "molten_rotor_fuel"
            );

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(
                    Registries.RECIPE_TYPE,
                    CreateSulfuricResonance.MODID
            );

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(
                    Registries.RECIPE_SERIALIZER,
                    CreateSulfuricResonance.MODID
            );

    public static final Supplier<RecipeType<MoltenRotorFuelRecipe>> TYPE =
            RECIPE_TYPES.register(
                    "molten_rotor_fuel",
                    () -> RecipeType.simple(ID)
            );

    public static final Supplier<RecipeSerializer<MoltenRotorFuelRecipe>>
            SERIALIZER = SERIALIZERS.register(
            "molten_rotor_fuel",
            MoltenRotorFuelRecipeSerializer::new
    );

    private MoltenRotorFuelRecipeRegistry() {
    }

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
