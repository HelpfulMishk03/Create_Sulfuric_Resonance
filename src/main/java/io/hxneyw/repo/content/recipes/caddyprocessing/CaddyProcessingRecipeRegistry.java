package io.hxneyw.repo.content.recipes.caddyprocessing;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CaddyProcessingRecipeRegistry {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            CreateSulfuricResonance.MODID, "caddy_processing");
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, CreateSulfuricResonance.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER,
                    CreateSulfuricResonance.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CaddyProcessingRecipe>> TYPE =
            TYPES.register("caddy_processing", () -> RecipeType.simple(ID));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CaddyProcessingRecipe>> SERIALIZER =
            SERIALIZERS.register("caddy_processing", CaddyProcessingRecipeSerializer::new);

    private CaddyProcessingRecipeRegistry() { }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
    }
}
