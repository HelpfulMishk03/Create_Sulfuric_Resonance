package io.hxneyw.repo.content.blocks;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.ModBlockEntities;
import io.hxneyw.repo.content.blocks.behaviour.CombustionHeatingBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoltenRotorBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    // Temperature thresholds
    private static final float TEMP_NONE_MIN = 0f;
    private static final float TEMP_SMOULDERING_MIN = 300f;
    private static final float TEMP_FADING_MAX = 350f;
    private static final float TEMP_KINDLED_MIN = 500f;
    private static final float TEMP_SEETHING_MIN = 800f;
    private static final float TEMP_RADIANT_MIN = 1300f;
    private static final float TEMP_ABSOLUTE_MAX = 1599f;

    // State variables
    private float currentTemperature = 20f;
    private int remainingBurnTime = 0;
    // FIX: REMOVED STATIC - each block entity needs its own heat tier!
    private RotorHeatLevel currentHeatTier = RotorHeatLevel.NONE;

    // Active fuel tracking
    private FuelType activeFuelType = FuelType.NONE;
    private int activeFuelCount = 0;
    private float baseHeatingRate = 0f;
    private float currentMaxTemp = 0f;

    // Heat retention system
    private boolean atMaxTemp = false;
    private int heatRetentionTime = 0;
    private float retentionTargetTemp = 0f;

    // Fuel stacking flags
    private boolean hasLavaInStack = false;
    private boolean hasSulfurInStack = false;
    private boolean hasCoalInStack = false;
    private boolean hasCharcoalInStack = false;
    private boolean hasStickInStack = false;

    // Kinetic initialization flag
    private boolean kineticsInitialized = false;

    // FIX: Better update throttling with separate counters
    private int clientUpdateCounter = 0;
    private RotorHeatLevel lastSentHeatTier = RotorHeatLevel.NONE;

    // Heat tiers
    public enum RotorHeatLevel {
        NONE(0, 0f, 0f, "Unheated", 1f),
        SMOULDERING(300, 32f, 128f, "Heated", 1f),
        FADING(325, 24f, 128f, "Heated", 0.8f),
        KINDLED(650, 64f, 1024f, "Heated", 1.5f),
        SEETHING(950, 128f, 2048f, "Superheated", 2.7f),
        RADIANT(1400, 256f, 8192f, "Combustion", 4.8f);

        public final int displayTemp;
        public final float rpmCap;
        public final float baseStressCapacity;
        public final String displayName;
        public final float coolingMultiplier;

        RotorHeatLevel(int temp, float rpm, float su, String name, float coolingMult) {
            this.displayTemp = temp;
            this.rpmCap = rpm;
            this.baseStressCapacity = su;
            this.displayName = name;
            this.coolingMultiplier = coolingMult;
        }
    }

    // Fuel types
    public enum FuelType {
        NONE(0f, 0f, 0, 0f, 1f, 0),
        STICK(2f, 150f, 16, 100f, 1f, 50),
        LOG(8.5f, 475f, 4, 250f, 1f, 200),
        COAL(13f, 550f, 32, 350f, 1f, 400),
        CHARCOAL(14f, 650f, 32, 350f, 0.8f, 500),
        COAL_BLOCK(25f, 900f, 1, 2400f, 1f, 1060),
        KELP_BLOCK(15.5f, 750f, 3, 400f, 1f, 600),
        TNT(25f, 600f, 1, 750f, 1.2f, 100),
        BLAZE_CAKE(40f, 1200f, 1, 3000f, 1f, 1500),
        SOUL_FIRED_BLAZE_CAKE(48.5f, 1599f, 1, 3500f, 1f, 2000);

        public final float celsiusPerSecond;
        public final float maxTempReachable;
        public final int maxStackSize;
        public final float baseBurnTimeTicks;
        public final float burnRateMultiplier;
        public final int retentionTicks;

        FuelType(float cps, float maxTemp, int maxStack, float ticks, float burnMult, int retention) {
            this.celsiusPerSecond = cps;
            this.maxTempReachable = maxTemp;
            this.maxStackSize = maxStack;
            this.baseBurnTimeTicks = ticks;
            this.burnRateMultiplier = burnMult;
            this.retentionTicks = retention;
        }
    }

    public MoltenRotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MoltenRotorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MOLTEN_ROTOR.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new CombustionHeatingBehaviour(this));
    }

    @Override
    public float getGeneratedSpeed() {
        return currentHeatTier.rpmCap;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(getGeneratedSpeed());
        if (speed == 0) return 0f;

        float desiredSU = currentHeatTier.baseStressCapacity;
        float adjustedCapacity = desiredSU / speed;

        this.lastCapacityProvided = adjustedCapacity;
        return adjustedCapacity;
    }

    @Override
    public float calculateStressApplied() {
        return 0f;
    }

    public float getTotalStressOutput() {
        return currentHeatTier.baseStressCapacity;
    }

    public void initializeKinetics() {
        if (kineticsInitialized || level == null || level.isClientSide) {
            return;
        }

        kineticsInitialized = true;
        attachKinetics();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        setChanged();
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        boolean needsUpdate = false;
        RotorHeatLevel previousTier = currentHeatTier;

// PHASE 1: FUEL BURNING
        if (remainingBurnTime > 0) {
            float burnMultiplier = getBurnRateMultiplier();
            remainingBurnTime -= (int) burnMultiplier;
            if (remainingBurnTime < 0) remainingBurnTime = 0;

            float heatingPerTick = baseHeatingRate / 20f;

            // Rain slows heating by 50%
            boolean isRaining = level.isRainingAt(worldPosition.above());
            if (isRaining) {
                heatingPerTick *= 0.5f;  // Half heating rate in rain
            }

            float targetTemp = calculateMaxStackedTemp();

            if (currentTemperature < targetTemp) {
                currentTemperature = Math.min(currentTemperature + heatingPerTick, targetTemp);
                currentTemperature = Math.min(currentTemperature, TEMP_ABSOLUTE_MAX); // CAP AT 1599°C
                needsUpdate = true;
            }

            if (currentTemperature >= targetTemp * 0.79f) {
                retentionTargetTemp = targetTemp;
                heatRetentionTime = activeFuelType.retentionTicks * activeFuelCount;
            }

            if (remainingBurnTime == 0 && currentTemperature >= TEMP_SMOULDERING_MIN) {
                atMaxTemp = true;
                retentionTargetTemp = currentTemperature;
                if (heatRetentionTime == 0) {
                    heatRetentionTime = Math.max(100, (int)(activeFuelType.retentionTicks * 0.3f * activeFuelCount));
                }
            }
        }
        // PHASE 2: HEAT RETENTION
        else if (atMaxTemp && heatRetentionTime > 0) {
            heatRetentionTime--;
            currentTemperature = retentionTargetTemp;
            needsUpdate = true;

            if (heatRetentionTime == 0) {
                atMaxTemp = false;
                retentionTargetTemp = 0f;
            }
        }
        // PHASE 3: COOLING
        else {
            atMaxTemp = false;
            heatRetentionTime = 0;
            retentionTargetTemp = 0f;

            if (currentTemperature > 20f) {
                float baseCoolingRate = 2f / 20f;
                float tierMultiplier = currentHeatTier.coolingMultiplier;

                // FIX: Faster cooling when raining - 3x cooling rate
                boolean isRaining = level.isRainingAt(worldPosition.above());
                if (isRaining) {
                    tierMultiplier *= 2f;
                }

                float coolingRate = baseCoolingRate * tierMultiplier;
                currentTemperature = Math.max(20f, currentTemperature - coolingRate);
                needsUpdate = true;
            }

            if (activeFuelType != FuelType.NONE) {
                activeFuelType = FuelType.NONE;
                activeFuelCount = 0;
                baseHeatingRate = 0f;
                currentMaxTemp = 0f;
                clearFuelStackFlags();
                needsUpdate = true;
            }
        }

        // Update heat tier
        RotorHeatLevel newTier = calculateHeatTierFromTemp();

        if (newTier != previousTier) {
            currentHeatTier = newTier;
            updateBlockVisuals();

            if (tierAffectsRotation(previousTier, newTier)) {
                notifyKineticNetworkOfChange();
            }

            needsUpdate = true;
        }

        if (needsUpdate) {
            setChanged();
        }

        // FIX: Send updates more frequently for smooth temperature display
        // But ONLY update lastSentHeatTier when it actually changes to prevent flickering
        clientUpdateCounter++;
        boolean tierChanged = currentHeatTier != lastSentHeatTier;

        if (tierChanged) {
            // Heat tier changed - update immediately and reset counter
            lastSentHeatTier = currentHeatTier;
            clientUpdateCounter = 0;
            sendData();
        } else if (clientUpdateCounter >= 1) {
            // No tier change - send temperature updates every 5 ticks (4x per second)
            // This keeps temperature smooth without causing goggle flicker
            clientUpdateCounter = 0;
            sendData();
        }
    }

    private boolean tierAffectsRotation(RotorHeatLevel from, RotorHeatLevel to) {
        if (from == RotorHeatLevel.NONE && to != RotorHeatLevel.NONE) return true;
        if (from != RotorHeatLevel.NONE && to == RotorHeatLevel.NONE) return true;
        return from.rpmCap != to.rpmCap;
    }

    private void notifyKineticNetworkOfChange() {
        if (level == null || level.isClientSide || !kineticsInitialized) return;

        super.updateGeneratedRotation();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        setChanged();
    }

    private float getBurnRateMultiplier() {
        return switch(currentHeatTier) {
            case NONE, SMOULDERING, FADING -> 1f;
            case KINDLED -> 2f;
            case SEETHING -> 3f;
            case RADIANT -> 4f;
        };
    }

    private RotorHeatLevel calculateHeatTierFromTemp() {
        if (currentTemperature < TEMP_SMOULDERING_MIN) return RotorHeatLevel.NONE;
        if (currentTemperature >= TEMP_RADIANT_MIN) return RotorHeatLevel.RADIANT;
        if (currentTemperature >= TEMP_SEETHING_MIN) return RotorHeatLevel.SEETHING;
        if (currentTemperature >= TEMP_KINDLED_MIN) return RotorHeatLevel.KINDLED;

        if (currentTemperature >= TEMP_SMOULDERING_MIN
                && currentTemperature < TEMP_FADING_MAX
                && remainingBurnTime == 0
                && !atMaxTemp) {
            return RotorHeatLevel.FADING;
        }

        return RotorHeatLevel.SMOULDERING;
    }

    private void updateBlockVisuals() {
        if (level == null) return;

        BlazeBurnerBlock.HeatLevel visualHeat = switch(currentHeatTier) {
            case NONE -> BlazeBurnerBlock.HeatLevel.NONE;
            case SMOULDERING -> BlazeBurnerBlock.HeatLevel.SMOULDERING;
            case FADING -> BlazeBurnerBlock.HeatLevel.FADING;
            case KINDLED -> BlazeBurnerBlock.HeatLevel.KINDLED;
            case SEETHING, RADIANT -> BlazeBurnerBlock.HeatLevel.SEETHING;
        };

        BlockState currentState = getBlockState();
        BlazeBurnerBlock.HeatLevel currentVisual = currentState.getValue(MoltenRotorBlock.HEAT_LEVEL);

        if (currentVisual != visualHeat) {
            level.setBlock(worldPosition, currentState.setValue(MoltenRotorBlock.HEAT_LEVEL, visualHeat), 2);
        }
    }

    public boolean addFuel(FuelType fuelType, int count) {
        if (level == null || fuelType == FuelType.NONE) return false;

        if (fuelType == FuelType.STICK && activeFuelType != FuelType.LOG) {
            return false;
        }

        // ALLOW fuel changes during heat retention (when remainingBurnTime is 0)
        if (activeFuelType != FuelType.NONE && activeFuelType != fuelType && remainingBurnTime > 0) {
            return false;  // Only block if ACTIVELY burning different fuel
        }

        int canAdd = Math.min(count, fuelType.maxStackSize - activeFuelCount);
        if (canAdd <= 0) return false;

        int ticksToAdd = (int)(fuelType.baseBurnTimeTicks * canAdd);
        remainingBurnTime += ticksToAdd;

        activeFuelType = fuelType;
        activeFuelCount += canAdd;
        baseHeatingRate = fuelType.celsiusPerSecond;
        currentMaxTemp = fuelType.maxTempReachable;

        updateFuelStackFlags(fuelType);

        setChanged();
        sendData();
        return true;
    }

    public void addSpecialFuel(FuelType fuelType, int ticks) {
        if (level == null) return;

        remainingBurnTime += ticks;
        activeFuelType = fuelType;
        activeFuelCount = 1;
        baseHeatingRate = fuelType.celsiusPerSecond;
        currentMaxTemp = fuelType.maxTempReachable;

        updateFuelStackFlags(fuelType);

        setChanged();
        sendData();
    }

    public void addUltimateFuel(int ticks) {
        if (level == null) return;

        remainingBurnTime += ticks;
        currentTemperature = Math.max(currentTemperature, TEMP_RADIANT_MIN);
        currentTemperature = Math.min(currentTemperature, TEMP_ABSOLUTE_MAX); // ADD THIS LINE

        setChanged();
        sendData();
    }

    private void updateFuelStackFlags(FuelType fuelType) {
        switch(fuelType) {
            case COAL -> hasCoalInStack = true;
            case CHARCOAL -> hasCharcoalInStack = true;
            case STICK -> hasStickInStack = true;
        }
    }

    private void clearFuelStackFlags() {
        hasLavaInStack = false;
        hasSulfurInStack = false;
        hasCoalInStack = false;
        hasCharcoalInStack = false;
        hasStickInStack = false;
    }

    private float calculateMaxStackedTemp() {
        float baseMax = currentMaxTemp;


        if (hasStickInStack && activeFuelType == FuelType.LOG) {
            return 550f;
        }

        if (hasCoalInStack && hasCharcoalInStack && activeFuelCount > 16) {
            return Math.min(850f, baseMax);
        }

        return baseMax;
    }

    public RotorHeatLevel getCurrentHeatTier() {
        return currentHeatTier;
    }

    public int getDisplayTemperature() {
        return (int)currentTemperature;
    }

    public int getDisplayFuelTime() {
        return Math.max(remainingBurnTime, 0);
    }

    public int getDisplayCooldownTime() {
        if (currentTemperature <= TEMP_SMOULDERING_MIN || remainingBurnTime > 0 || (atMaxTemp && heatRetentionTime > 0)) {
            return 0;
        }

        float tempDifference = currentTemperature - TEMP_SMOULDERING_MIN;
        float baseCoolingRate = 2f / 20f;
        float tierMultiplier = currentHeatTier.coolingMultiplier;
        float coolingRate = baseCoolingRate * tierMultiplier;

        return (int)(tempDifference / coolingRate);
    }

    public int getDisplayRetentionTime() {
        if (atMaxTemp && heatRetentionTime > 0 && remainingBurnTime == 0) {
            return heatRetentionTime;
        }
        return 0;
    }

    public String getHeatTierName() {
        if (currentHeatTier == RotorHeatLevel.FADING) {
            return "Heated";
        }
        return currentHeatTier.displayName;
    }

    public boolean isCombustionActive() {
        return currentHeatTier.ordinal() >= RotorHeatLevel.SMOULDERING.ordinal();
    }

    public boolean shouldShowStatus() {
        return remainingBurnTime > 0 || currentTemperature > 20f;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal(""));

        int temp = getDisplayTemperature();
        float rpm = getGeneratedSpeed();
        float stress = getTotalStressOutput();
        int fuelTime = getDisplayFuelTime();
        int retentionTime = getDisplayRetentionTime();
        int cooldownTime = getDisplayCooldownTime();
        String heatName = getHeatTierName();

        ChatFormatting color = switch(currentHeatTier) {
            case NONE -> ChatFormatting.GRAY;
            case SMOULDERING, FADING -> ChatFormatting.YELLOW;
            case KINDLED -> ChatFormatting.RED;
            case SEETHING -> ChatFormatting.DARK_RED;
            case RADIANT -> ChatFormatting.DARK_PURPLE;
        };

        tooltip.add(Component.literal("Heat: " + heatName + " (" + temp + "°C)").withStyle(color));

        if (currentHeatTier != RotorHeatLevel.NONE && stress > 0) {
            tooltip.add(Component.literal("Stress Capacity: " + (int)stress + " su")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.literal("Stress Capacity: 0 su").withStyle(ChatFormatting.GRAY));
        }

        if (fuelTime > 0) {
            int seconds = fuelTime / 20;
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;

            if (minutes > 0) {
                tooltip.add(Component.literal("Fuel Remaining: " + minutes + "m " + remainingSeconds + "s")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("Fuel Remaining: " + remainingSeconds + "s")
                        .withStyle(ChatFormatting.GREEN));
            }

            if (atMaxTemp && heatRetentionTime > 0) {
                int retSeconds = heatRetentionTime / 20;
                tooltip.add(Component.literal("Heat Retention: " + retSeconds + "s")
                        .withStyle(ChatFormatting.AQUA));
            }
        } else if (retentionTime > 0) {
            int seconds = retentionTime / 20;
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;

            if (minutes > 0) {
                tooltip.add(Component.literal("Heat Retention: " + minutes + "m " + remainingSeconds + "s")
                        .withStyle(ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.literal("Heat Retention: " + remainingSeconds + "s")
                        .withStyle(ChatFormatting.AQUA));
            }
        } else if (cooldownTime > 0) {
            int seconds = cooldownTime / 20;
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;

            if (minutes > 0) {
                tooltip.add(Component.literal("Cooling Down: " + minutes + "m " + remainingSeconds + "s")
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.add(Component.literal("Cooling Down: " + remainingSeconds + "s")
                        .withStyle(ChatFormatting.YELLOW));
            }
        } else if (currentHeatTier != RotorHeatLevel.NONE) {
            tooltip.add(Component.literal("Fuel Remaining: 0s").withStyle(ChatFormatting.GRAY));
        }

        if (currentHeatTier == RotorHeatLevel.RADIANT) {
            tooltip.add(Component.literal("✦ RADIANT TIER ACTIVE").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.literal("  (Combustion recipes enabled)").withStyle(ChatFormatting.DARK_GRAY));
        }

        return true;
    }

    @Override
    protected void write(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider, boolean clientPacket) {
        super.write(tag, provider, clientPacket);
        tag.putFloat("Temperature", currentTemperature);
        tag.putInt("FuelTime", remainingBurnTime);
        tag.putInt("HeatTier", currentHeatTier.ordinal());
        tag.putInt("FuelType", activeFuelType.ordinal());
        tag.putInt("FuelCount", activeFuelCount);
        tag.putFloat("HeatingRate", baseHeatingRate);
        tag.putFloat("MaxTemp", currentMaxTemp);
        tag.putBoolean("HasLava", hasLavaInStack);
        tag.putBoolean("HasStick", hasStickInStack);
        tag.putBoolean("HasSulfur", hasSulfurInStack);
        tag.putBoolean("HasCoal", hasCoalInStack);
        tag.putBoolean("HasCharcoal", hasCharcoalInStack);
        tag.putBoolean("KineticsInit", kineticsInitialized);
        tag.putBoolean("AtMaxTemp", atMaxTemp);
        tag.putInt("RetentionTime", heatRetentionTime);
        tag.putFloat("RetentionTarget", retentionTargetTemp);
    }

    @Override
    protected void read(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        currentTemperature = tag.getFloat("Temperature");
        remainingBurnTime = tag.getInt("FuelTime");
        activeFuelCount = tag.getInt("FuelCount");
        baseHeatingRate = tag.getFloat("HeatingRate");
        currentMaxTemp = tag.getFloat("MaxTemp");
        hasLavaInStack = tag.getBoolean("HasLava");
        hasSulfurInStack = tag.getBoolean("HasSulfur");
        hasCoalInStack = tag.getBoolean("HasCoal");
        hasStickInStack = tag.getBoolean("HasStick");
        hasCharcoalInStack = tag.getBoolean("HasCharcoal");
        kineticsInitialized = tag.getBoolean("KineticsInit");
        atMaxTemp = tag.getBoolean("AtMaxTemp");
        heatRetentionTime = tag.getInt("RetentionTime");
        retentionTargetTemp = tag.getFloat("RetentionTarget");

        int tierIndex = tag.getInt("HeatTier");
        currentHeatTier = tierIndex >= 0 && tierIndex < RotorHeatLevel.values().length
                ? RotorHeatLevel.values()[tierIndex] : RotorHeatLevel.NONE;

        int fuelIndex = tag.getInt("FuelType");
        activeFuelType = fuelIndex >= 0 && fuelIndex < FuelType.values().length
                ? FuelType.values()[fuelIndex] : FuelType.NONE;

        if (clientPacket) {
            lastSentHeatTier = currentHeatTier;
        }

        if (clientPacket && level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}