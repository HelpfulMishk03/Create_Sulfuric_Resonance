package io.hxneyw.repo.content.recipes.sulfuricresonancechamber;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class SulfuricResonanceChamberRecipeRegistry {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "sulfuric_resonance_chamber"
            );

    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(
                    Registries.RECIPE_TYPE,
                    CreateSulfuricResonance.MODID
            );

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(
                    Registries.RECIPE_SERIALIZER,
                    CreateSulfuricResonance.MODID
            );

    public static final Supplier<
            RecipeType<SulfuricResonanceChamberRecipe>
            > TYPE = TYPES.register(
                    "sulfuric_resonance_chamber",
                    () -> RecipeType.simple(ID)
            );

    public static final Supplier<
            RecipeSerializer<SulfuricResonanceChamberRecipe>
            > SERIALIZER = SERIALIZERS.register(
                    "sulfuric_resonance_chamber",
                    SulfuricResonanceChamberRecipeSerializer::new
            );

    private SulfuricResonanceChamberRecipeRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
    }
}
