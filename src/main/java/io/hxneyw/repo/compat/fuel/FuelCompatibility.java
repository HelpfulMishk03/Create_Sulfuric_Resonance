package io.hxneyw.repo.compat.fuel;

import io.hxneyw.repo.compat.fuel.evilcraft.EvilCraftFuelCompatibility;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import io.hxneyw.repo.content.recipes.moltenrotorfuel.MoltenRotorFuelRecipe;
import io.hxneyw.repo.content.recipes.moltenrotorfuel.MoltenRotorFuelRecipeRegistry;
import io.hxneyw.repo.content.registry.ModItemTags;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class FuelCompatibility {
    private static final ResourceLocation EVILCRAFT_DARK_STICK =
            ResourceLocation.fromNamespaceAndPath(
                    "evilcraft",
                    "dark_stick"
            );

    private FuelCompatibility() {
    }

    /**
     * Built-in-only resolution. Used by recipe-display code that does not have
     * a level/recipe manager available.
     */
    public static @Nullable ResolvedFuel resolve(ItemStack stack) {
        return resolveBuiltIn(stack);
    }

    /**
     * Runtime resolution. Datapack/KubeJS molten_rotor_fuel recipes have
     * priority over all built-in categories and furnace-fuel fallback.
     */
    public static @Nullable ResolvedFuel resolve(
            ItemStack stack,
            @Nullable Level level
    ) {
        if (stack.isEmpty()) {
            return null;
        }

        CustomResolution custom = resolveCustom(stack, level);
        if (custom.matched()) {
            return custom.fuel();
        }

        return resolveBuiltIn(stack);
    }

    private static CustomResolution resolveCustom(
            ItemStack stack,
            @Nullable Level level
    ) {
        if (level == null) {
            return CustomResolution.NO_MATCH;
        }

        List<RecipeHolder<MoltenRotorFuelRecipe>> recipes = level
                .getRecipeManager()
                .getAllRecipesFor(MoltenRotorFuelRecipeRegistry.TYPE.get())
                .stream()
                .filter(holder -> holder.value().ingredient().test(stack))
                .sorted(Comparator
                        .<RecipeHolder<MoltenRotorFuelRecipe>>comparingInt(
                                holder -> holder.value().priority()
                        )
                        .reversed()
                        .thenComparing(holder -> holder.id().toString()))
                .toList();

        if (recipes.isEmpty()) {
            return CustomResolution.NO_MATCH;
        }

        RecipeHolder<MoltenRotorFuelRecipe> selected = recipes.getFirst();
        return new CustomResolution(
                true,
                selected.value().resolve(selected.id())
        );
    }

    private static @Nullable ResolvedFuel resolveBuiltIn(ItemStack stack) {
        if (stack.isEmpty() || isExplicitlyRejected(stack)) {
            return null;
        }

        if (stack.is(Items.BRIMSTONE_BRIQUETTE.get())) {
            return itemSpecific(
                    stack,
                    FuelType.INFERNAL_COKE,
                    2400.0F,
                    40.0F,
                    1599.0F,
                    4
            );
        }

        if (stack.is(Items.THERMITE_CHARGE.get())) {
            return itemSpecific(
                    stack,
                    FuelType.TNT,
                    1200.0F,
                    65.0F,
                    2000.0F,
                    1
            );
        }

        ResolvedFuel evilCraft = EvilCraftFuelCompatibility.resolve(stack);
        if (evilCraft != null) {
            return evilCraft;
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_SOUL_FIRED)) {
            return ResolvedFuel.fromType(FuelType.SOUL_FIRED_BLAZE_CAKE);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_BLAZE_CAKE)) {
            return ResolvedFuel.fromType(FuelType.BLAZE_CAKE);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_EXPLOSIVE)) {
            return ResolvedFuel.fromType(FuelType.TNT);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_DENSE_FUEL)) {
            return ResolvedFuel.fromType(FuelType.COAL_BLOCK);
        }

        if (stack.is(Items.CINDER_FUEL_BRIQUETTE.get())) {
            return itemSpecific(
                    stack,
                    FuelType.CHARCOAL,
                    FuelType.CHARCOAL.baseBurnTimeTicks * 1.5F,
                    FuelType.CHARCOAL.celsiusPerSecond,
                    FuelType.CHARCOAL.maxTempReachable,
                    FuelType.CHARCOAL.maxStackSize
            );
        }

        if (stack.is(Items.MOLTEN_EMBER_PELLET.get())) {
            return ResolvedFuel.fromType(FuelType.MOLTEN_EMBER_PELLET);
        }

        if (stack.is(Items.CARBON_DEPOSIT_BLOCK_ITEM.get())) {
            return ResolvedFuel.fromType(FuelType.CARBON_DEPOSIT_BLOCK);
        }

        if (stack.is(Items.INFERNAL_CARBON_DEPOSIT_BLOCK_ITEM.get())) {
            return ResolvedFuel.fromType(FuelType.INFERNAL_CARBON_DEPOSIT_BLOCK);
        }

        if (stack.is(Items.COKE.get())) {
            return ResolvedFuel.fromType(FuelType.COKE);
        }

        if (stack.is(Items.INFERNAL_COKE.get())) {
            return ResolvedFuel.fromType(FuelType.INFERNAL_COKE);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_CHARCOAL)) {
            return ResolvedFuel.fromType(FuelType.CHARCOAL);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_COAL)
                || stack.is(ItemTags.COALS)
                || stack.is(CommonFuelTags.COAL_COKE)) {
            return ResolvedFuel.fromType(FuelType.COAL);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_KELP)) {
            return ResolvedFuel.fromType(FuelType.KELP_BLOCK);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_LOGS)
                || stack.is(ItemTags.LOGS_THAT_BURN)
                || stack.is(ItemTags.BAMBOO_BLOCKS)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.WOODEN_STAIRS)
                || stack.is(ItemTags.WOODEN_FENCES)
                || stack.is(ItemTags.FENCE_GATES)
                || stack.is(ItemTags.WOODEN_TRAPDOORS)
                || stack.is(ItemTags.WOODEN_DOORS)) {
            return ResolvedFuel.fromType(FuelType.LOG);
        }

        if (stack.is(ModItemTags.MOLTEN_ROTOR_STICKS)) {
            return ResolvedFuel.fromType(FuelType.STICK);
        }

        int furnaceBurnTime = stack.getBurnTime(RecipeType.SMELTING);
        if (furnaceBurnTime <= 0 || !isStructurallySuitableFuel(stack)) {
            return null;
        }

        if (furnaceBurnTime <= 400) {
            return ResolvedFuel.fromType(FuelType.GENERIC_LOW);
        }

        if (furnaceBurnTime <= 2000) {
            return ResolvedFuel.fromType(FuelType.GENERIC_MEDIUM);
        }

        return ResolvedFuel.fromType(FuelType.GENERIC_HIGH);
    }

    private static ResolvedFuel itemSpecific(
            ItemStack stack,
            FuelType type,
            float burnTimeTicks,
            float heatingRate,
            float maximumTemperature,
            int maximumUnits
    ) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return new ResolvedFuel(
                ResourceLocation.fromNamespaceAndPath(
                        itemId.getNamespace(),
                        "molten_rotor_fuel/" + itemId.getPath()
                ),
                type,
                burnTimeTicks,
                heatingRate,
                maximumTemperature,
                maximumUnits
        );
    }

    private static boolean isExplicitlyRejected(ItemStack stack) {
        return EVILCRAFT_DARK_STICK.equals(
                BuiltInRegistries.ITEM.getKey(stack.getItem())
        );
    }

    private static boolean isStructurallySuitableFuel(ItemStack stack) {
        return stack.is(ItemTags.LOGS_THAT_BURN)
                || stack.is(ItemTags.BAMBOO_BLOCKS)
                || stack.is(ItemTags.PLANKS)
                || stack.is(ItemTags.WOODEN_STAIRS)
                || stack.is(ItemTags.WOODEN_FENCES)
                || stack.is(ItemTags.FENCE_GATES)
                || stack.is(ItemTags.WOODEN_TRAPDOORS)
                || stack.is(ItemTags.WOODEN_DOORS)
                || stack.is(ItemTags.COALS)
                || stack.is(CommonFuelTags.COAL_COKE)
                || stack.is(ModItemTags.MOLTEN_ROTOR_LOGS)
                || stack.is(ModItemTags.MOLTEN_ROTOR_STICKS);
    }

    private record CustomResolution(
            boolean matched,
            @Nullable ResolvedFuel fuel
    ) {
        private static final CustomResolution NO_MATCH =
                new CustomResolution(false, null);
    }
}