package io.hxneyw.repo.content.recipes.rotaryleaching;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RotaryLeachingRecipeRegistry {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CreateSulfuricResonance.MODID, "rotary_leaching");
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, CreateSulfuricResonance.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER, CreateSulfuricResonance.MODID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<RotaryLeachingRecipe>> TYPE = TYPES.register("rotary_leaching", () -> RecipeType.simple(ID));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RotaryLeachingRecipe>> SERIALIZER = SERIALIZERS.register("rotary_leaching", RotaryLeachingRecipeSerializer::new);
    private RotaryLeachingRecipeRegistry() { }
    public static void register(IEventBus modEventBus) { TYPES.register(modEventBus); SERIALIZERS.register(modEventBus); }
}
