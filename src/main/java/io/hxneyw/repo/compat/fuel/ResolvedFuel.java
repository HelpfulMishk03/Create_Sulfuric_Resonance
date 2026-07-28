package io.hxneyw.repo.compat.fuel;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.FuelType;

public record ResolvedFuel(
        FuelType type,
        float burnTimeTicks,
        float heatingRate,
        float maximumTemperature,
        int maximumUnits
) {
    public static ResolvedFuel fromType(FuelType type) {
        return new ResolvedFuel(
                type,
                type.baseBurnTimeTicks,
                type.celsiusPerSecond,
                type.maxTempReachable,
                type.maxStackSize
        );
    }

    public boolean isInvalid() {
        return type != null && type != FuelType.NONE;
    }
}