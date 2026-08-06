package io.hxneyw.repo.compat.fuel;

import com.simibubi.create.AllItems;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MoltenRotorFuelDisplayRegistry {

    private MoltenRotorFuelDisplayRegistry() {
    }

    public static List<MoltenRotorFuelDisplay> createDisplays() {
        Map<DisplayKey, List<ItemStack>> groupedFuelStacks =
                new LinkedHashMap<>();

        BuiltInRegistries.ITEM.stream()
                .sorted(Comparator.comparing(item ->
                        BuiltInRegistries.ITEM.getKey(item).toString()
                ))
                .forEach(item -> addResolvedFuel(
                        item,
                        groupedFuelStacks
                ));

        List<MoltenRotorFuelDisplay> displays = new ArrayList<>();

        for (Map.Entry<DisplayKey, List<ItemStack>> entry
                : groupedFuelStacks.entrySet()) {
            DisplayKey key = entry.getKey();

            displays.add(new MoltenRotorFuelDisplay(
                    entry.getValue(),
                    key.fuelType(),
                    key.burnTimeTicks(),
                    key.heatingRate(),
                    key.maximumTemperature(),
                    key.maximumUnits(),
                    key.specialBehavior()
            ));
        }

        displays.add(new MoltenRotorFuelDisplay(
                List.of(new ItemStack(Items.NETHER_STAR)),
                FuelType.NONE,
                6000.0F,
                0.0F,
                1300.0F,
                1,
                MoltenRotorFuelDisplay.SpecialBehavior.NETHER_STAR_BOOST
        ));

        displays.add(new MoltenRotorFuelDisplay(
                List.of(new ItemStack(Items.DRAGON_BREATH)),
                FuelType.NONE,
                4000.0F,
                0.0F,
                1300.0F,
                1,
                MoltenRotorFuelDisplay.SpecialBehavior.DRAGON_BREATH_BOOST
        ));

        displays.sort(
                Comparator.comparingInt(
                                MoltenRotorFuelDisplayRegistry::sortOrder
                        )
                        .thenComparing(
                                MoltenRotorFuelDisplayRegistry::firstItemId
                        )
        );

        return List.copyOf(displays);
    }

    private static void addResolvedFuel(
            Item item,
            Map<DisplayKey, List<ItemStack>> groupedFuelStacks
    ) {
        if (item == Items.AIR
                || item == AllItems.CREATIVE_BLAZE_CAKE.get()) {
            return;
        }

        ItemStack stack = new ItemStack(item);
        ResolvedFuel resolvedFuel = FuelCompatibility.resolve(stack);

        if (resolvedFuel == null || resolvedFuel.isInvalid()) {
            return;
        }

        MoltenRotorFuelDisplay.SpecialBehavior behavior =
                determineBehavior(stack, resolvedFuel.type());

        DisplayKey key = new DisplayKey(
                resolvedFuel.type(),
                resolvedFuel.burnTimeTicks(),
                resolvedFuel.heatingRate(),
                resolvedFuel.maximumTemperature(),
                resolvedFuel.maximumUnits(),
                behavior
        );

        groupedFuelStacks.computeIfAbsent(
                key,
                ignored -> new ArrayList<>()
        ).add(stack);
    }

    private static MoltenRotorFuelDisplay.SpecialBehavior
    determineBehavior(ItemStack stack, FuelType fuelType) {
        if (stack.is(io.hxneyw.repo.content.Items
                .CINDER_FUEL_BRIQUETTE.get())) {
            return MoltenRotorFuelDisplay.SpecialBehavior.CINDER_BRIQUETTE;
        }

        if (stack.is(Items.TNT)) {
            return MoltenRotorFuelDisplay.SpecialBehavior.TNT_RISK;
        }

        if (fuelType == FuelType.STICK) {
            return MoltenRotorFuelDisplay.SpecialBehavior.STICK_BOOST;
        }

        if (fuelType == FuelType.SOUL_FIRED_BLAZE_CAKE) {
            return MoltenRotorFuelDisplay.SpecialBehavior
                    .SOUL_FIRED_REQUIREMENT;
        }

        return MoltenRotorFuelDisplay.SpecialBehavior.STANDARD;
    }

    private static int sortOrder(MoltenRotorFuelDisplay display) {
        return switch (display.specialBehavior()) {
            case STICK_BOOST -> 0;
            case CINDER_BRIQUETTE -> 31;
            case TNT_RISK -> 71;
            case SOUL_FIRED_REQUIREMENT -> 91;
            case NETHER_STAR_BOOST -> 100;
            case DRAGON_BREATH_BOOST -> 101;
            case STANDARD -> switch (display.fuelType()) {
                case STICK -> 0;
                case LOG -> 10;
                case COAL -> 20;
                case CHARCOAL -> 30;
                case COAL_BLOCK -> 40;
                case KELP_BLOCK -> 50;
                case GENERIC_LOW -> 60;
                case GENERIC_MEDIUM -> 61;
                case GENERIC_HIGH -> 62;
                case TNT -> 70;
                case BLAZE_CAKE -> 80;
                case SOUL_FIRED_BLAZE_CAKE -> 90;
                case NONE -> 999;
            };
        };
    }

    private static String firstItemId(MoltenRotorFuelDisplay display) {
        if (display.fuelStacks().isEmpty()) {
            return "";
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(
                display.fuelStacks().getFirst().getItem()
        );

        return itemId.toString();
    }

    private record DisplayKey(
            FuelType fuelType,
            float burnTimeTicks,
            float heatingRate,
            float maximumTemperature,
            int maximumUnits,
            MoltenRotorFuelDisplay.SpecialBehavior specialBehavior
    ) {
    }
}
