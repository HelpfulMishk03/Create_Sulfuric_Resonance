package io.hxneyw.repo.content.blocks.moltenrotor;

import io.hxneyw.repo.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Owns temperature, heating, cooling, and heat-tier calculation for the
 * Molten Rotor. Kinetic-network updates and visual block-state updates remain
 * orchestrated by the block entity.
 */
public final class MoltenRotorTemperatureController {
    private static final float AMBIENT_TEMPERATURE = 20.0F;
    private static final float MAXIMUM_TEMPERATURE = 1599.0F;
    private static final float NORMAL_COOLING_PER_TICK = 0.1F;

    private final MoltenRotorBlockEntity furnace;
    private final MoltenRotorFuelController fuelController;

    private float currentTemperature = AMBIENT_TEMPERATURE;
    private MoltenRotorBlockEntity.RotorHeatLevel currentHeatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;

    public MoltenRotorTemperatureController(
            MoltenRotorBlockEntity furnace,
            MoltenRotorFuelController fuelController
    ) {
        this.furnace = furnace;
        this.fuelController = fuelController;
    }

    /**
     * Advances one server tick of heating or cooling and recalculates the heat
     * tier. Fuel queue advancement remains owned by the fuel controller.
     */
    public TickResult tick() {
        MoltenRotorBlockEntity.RotorHeatLevel previousTier =
                this.currentHeatTier;
        boolean needsUpdate = false;

        if (this.fuelController.hasFuelRemaining()) {
            float heatingPerTick =
                    this.fuelController.getBaseHeatingRate() / 20.0F;
            float targetTemperature =
                    this.fuelController.getMaximumStackedTemperature();

            this.fuelController.decrementRemainingBurnTime();

            Level level = this.furnace.getLevel();
            if (level != null
                    && Config.RAIN_AFFECTS_MOLTEN_ROTOR.get()
                    && level.isRainingAt(this.furnace.getBlockPos().above())) {
                heatingPerTick *= 0.5F;
            }

            if (this.currentTemperature < targetTemperature) {
                this.currentTemperature = this.clampTemperature(
                        Math.min(
                                this.currentTemperature + heatingPerTick,
                                targetTemperature
                        )
                );
                needsUpdate = true;
            }

            if (this.fuelController.advanceFuelIfBurnedOut()) {
                needsUpdate = true;
            }
        } else if (this.currentTemperature > AMBIENT_TEMPERATURE) {
            this.currentTemperature = this.clampTemperature(
                    this.currentTemperature - this.getCoolingPerTick()
            );
            needsUpdate = true;
        }

        Level level = this.furnace.getLevel();
        if (level != null && level.getGameTime() % 100L == 0L) {
            float oldTemperature = this.currentTemperature;
            this.currentTemperature =
                    this.clampTemperature(this.currentTemperature);

            if (oldTemperature != this.currentTemperature) {
                needsUpdate = true;
            }
        }

        this.currentHeatTier = this.calculateHeatTierFromTemperature();

        return new TickResult(
                needsUpdate,
                previousTier,
                this.currentHeatTier
        );
    }

    public int getDisplayTemperature() {
        return (int) this.currentTemperature;
    }

    public MoltenRotorBlockEntity.RotorHeatLevel getHeatTier() {
        return this.currentHeatTier;
    }

    public String getHeatTierName() {
        return this.currentHeatTier
                == MoltenRotorBlockEntity.RotorHeatLevel.FADING
                ? "Heated"
                : this.currentHeatTier.displayName;
    }

    public boolean isCombustionActive() {
        return this.currentHeatTier.isAtLeast(
                MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING
        );
    }

    public boolean isAboveAmbient() {
        return this.currentTemperature > AMBIENT_TEMPERATURE;
    }

    public float getImpellerRpm() {
        return Math.max(
                (this.currentTemperature - AMBIENT_TEMPERATURE) * 0.5F,
                0.0F
        );
    }

    public int getDisplayCooldownTime(boolean creativeMode) {
        if (!creativeMode
                && this.currentTemperature > AMBIENT_TEMPERATURE
                && !this.fuelController.hasFuelRemaining()) {
            return (int) Math.ceil(
                    (this.currentTemperature - 300.0F)
                            / this.getCoolingPerTick()
            );
        }

        return 0;
    }

    public void setCreativeModeState(boolean creative) {
        if (creative) {
            this.setState(
                    400.0F,
                    MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING
            );
        } else {
            this.setState(
                    AMBIENT_TEMPERATURE,
                    MoltenRotorBlockEntity.RotorHeatLevel.NONE
            );
        }
    }

    public void setState(
            float temperature,
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    ) {
        this.currentTemperature = temperature;
        this.currentHeatTier = heatTier;
    }

    public void raiseToAtLeast(float temperature) {
        this.currentTemperature = this.clampTemperature(
                Math.max(this.currentTemperature, temperature)
        );
    }

    private float getCoolingPerTick() {
        float coolingPerTick = NORMAL_COOLING_PER_TICK;
        Level level = this.furnace.getLevel();

        if (level != null
                && Config.RAIN_AFFECTS_MOLTEN_ROTOR.get()
                && level.isRainingAt(this.furnace.getBlockPos().above())) {
            coolingPerTick *= 2.0F;
        }

        return coolingPerTick;
    }

    private MoltenRotorBlockEntity.RotorHeatLevel
    calculateHeatTierFromTemperature() {
        if (this.currentTemperature < 300.0F) {
            return MoltenRotorBlockEntity.RotorHeatLevel.NONE;
        }

        if (this.currentTemperature >= 1300.0F) {
            return MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;
        }

        if (this.currentTemperature >= 800.0F) {
            return MoltenRotorBlockEntity.RotorHeatLevel.SEETHING;
        }

        if (this.currentTemperature >= 500.0F) {
            return MoltenRotorBlockEntity.RotorHeatLevel.KINDLED;
        }

        return this.currentTemperature >= 300.0F
                && this.currentTemperature < 350.0F
                && !this.fuelController.hasFuelRemaining()
                ? MoltenRotorBlockEntity.RotorHeatLevel.FADING
                : MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING;
    }

    private float clampTemperature(float temperature) {
        return Math.clamp(
                temperature,
                AMBIENT_TEMPERATURE,
                MAXIMUM_TEMPERATURE
        );
    }

    public void write(@NotNull CompoundTag tag) {
        tag.putFloat("Temperature", this.currentTemperature);
        tag.putString("HeatTier", this.currentHeatTier.serializedId);
    }

    public void read(@NotNull CompoundTag tag) {
        this.currentTemperature = tag.getFloat("Temperature");

        if (tag.contains("HeatTier", Tag.TAG_STRING)) {
            this.currentHeatTier =
                    MoltenRotorBlockEntity.RotorHeatLevel.fromSerializedId(
                            tag.getString("HeatTier")
                    );
        } else {
            int tierIndex = tag.getInt("HeatTier");
            this.currentHeatTier = tierIndex >= 0
                    && tierIndex
                    < MoltenRotorBlockEntity.RotorHeatLevel.values().length
                    ? MoltenRotorBlockEntity.RotorHeatLevel.values()[tierIndex]
                    : MoltenRotorBlockEntity.RotorHeatLevel.NONE;
        }
    }

    public record TickResult(
            boolean changed,
            MoltenRotorBlockEntity.RotorHeatLevel previousTier,
            MoltenRotorBlockEntity.RotorHeatLevel currentTier
    ) {
        public boolean tierChanged() {
            return this.previousTier != this.currentTier;
        }
    }
}