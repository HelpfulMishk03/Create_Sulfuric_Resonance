package io.hxneyw.repo.compat.fuel;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record MoltenRotorFuelDisplay(
        List<ItemStack> fuelStacks,
        FuelType fuelType,
        float burnTimeTicks,
        float heatingRate,
        float maximumTemperature,
        int maximumUnits,
        SpecialBehavior specialBehavior
) {
    public MoltenRotorFuelDisplay {
        fuelStacks = fuelStacks.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(stack -> stack.copyWithCount(1))
                .toList();
    }

    public boolean isManualHeatBoost() {
        return this.specialBehavior == SpecialBehavior.NETHER_STAR_BOOST
                || this.specialBehavior == SpecialBehavior.DRAGON_BREATH_BOOST;
    }

    public enum SpecialBehavior {
        STANDARD,
        STICK_BOOST,
        CINDER_BRIQUETTE,
        SOUL_FIRED_REQUIREMENT,
        TNT_RISK,
        NETHER_STAR_BOOST,
        DRAGON_BREATH_BOOST
    }
}
