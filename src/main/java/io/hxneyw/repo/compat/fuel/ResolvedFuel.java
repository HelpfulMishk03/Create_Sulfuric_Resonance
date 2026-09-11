package io.hxneyw.repo.compat.fuel;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;
import net.minecraft.resources.ResourceLocation;

public record ResolvedFuel(
        ResourceLocation key,
        FuelType type,
        float burnTimeTicks,
        float heatingRate,
        float maximumTemperature,
        int maximumUnits
) {
    public ResolvedFuel(
            FuelType type,
            float burnTimeTicks,
            float heatingRate,
            float maximumTemperature,
            int maximumUnits
    ) {
        this(
                builtinKey(type),
                type,
                burnTimeTicks,
                heatingRate,
                maximumTemperature,
                maximumUnits
        );
    }

    public static ResolvedFuel fromType(FuelType type) {
        return new ResolvedFuel(
                builtinKey(type),
                type,
                type.baseBurnTimeTicks,
                type.celsiusPerSecond,
                type.maxTempReachable,
                type.maxStackSize
        );
    }

    public static ResourceLocation builtinKey(FuelType type) {
        String path = type == null ? "none" : type.serializedId;
        return ResourceLocation.fromNamespaceAndPath(
                CreateSulfuricResonance.MODID,
                "builtin_fuel/" + path
        );
    }

    public boolean isInvalid() {
        return key == null
                || type == null
                || type == FuelType.NONE
                || burnTimeTicks <= 0.0F
                || heatingRate < 0.0F
                || maximumTemperature <= 0.0F
                || maximumUnits <= 0;
    }
}