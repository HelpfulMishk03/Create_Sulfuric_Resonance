package io.hxneyw.repo.content.blocks;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.ModBlockEntities;
import io.hxneyw.repo.content.blocks.behaviour.CombustionHeatingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import net.minecraft.ChatFormatting;

import java.util.List;

/**
 * FULLY FIXED MOLTEN ROTOR BLOCK ENTITY
 * All values match the design document exactly
 */
public class MoltenRotorBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    // Temperature thresholds (Document §3)
    private static final float TEMP_NONE_MIN = 0f;
    private static final float TEMP_SMOULDERING_MIN = 300f;
    private static final float TEMP_KINDLED_MIN = 500f;
    private static final float TEMP_SEETHING_MIN = 800f;
    private static final float TEMP_BLAZING_MIN = 1100f;
    private static final float TEMP_RADIANT_MIN = 1500f;

    // State variables
    private float currentTemperature = 20f;
    private int remainingBurnTime = 0;
    private RotorHeatLevel currentHeatTier = RotorHeatLevel.NONE;

    // Active fuel tracking
    private FuelType activeFuelType = FuelType.NONE;
    private int activeFuelCount = 0;
    private float baseHeatingRate = 0f;
    private float currentMaxTemp = 0f;

    // Tier unlocks
    private boolean blazingUnlocked = false;

    // Fuel stacking flags
    private boolean hasLavaInStack = false;
    private boolean hasBlazeRodInStack = false;
    private boolean hasSulfurInStack = false;
    private boolean hasCoalInStack = false;
    private boolean hasCharcoalInStack = false;
    private boolean hasSticksInStack = false;

    // Heat tiers (Document §3)
    public enum RotorHeatLevel {
        NONE(0, 0f, 0f, "Unheated", 1f),
        SMOULDERING(300, 24f, 64f, "Heated", 1f),      // 16-32 RPM range, display as 24
        FADING(400, 24f, 64f, "Heated", 1f),           // Never shown to player
        KINDLED(650, 64f, 1024f, "Heated", 2f),
        SEETHING(950, 128f, 2048f, "Superheated", 3f),
        BLAZING(1300, 200f, 4096f, "Molten", 4f),
        RADIANT(1650, 256f, 8192f, "Combustion", 5f);

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

    // Fuel types with exact values from Document §6.5
    public enum FuelType {
        NONE(0f, 0f, 0, 0f, 1f),
        STICK(5f, 150f, 32, 20f, 1f),
        PLANK(7.5f, 300f, 16, 100f, 1f),
        LOG(8.5f, 385f, 4, 250f, 1f),
        COAL(13f, 550f, 32, 350f, 1f),
        CHARCOAL(14f, 650f, 32, 350f, 1f),
        COAL_BLOCK(25f, 900f, 1, 2400f, 1f),
        KELP_BLOCK(15.5f, 750f, 3, 400f, 1f),
        BLAZE_ROD(15f, 785f, 32, 600f, 1f),
        LAVA(35f, 1000f, 5, 2000f, 1f),
        TNT(25f, 600f, 1, 750f, 1f),
        BLAZE_CAKE(40f, 1200f, 1, 3000f, 1f);

        public final float celsiusPerSecond;
        public final float maxTempReachable;
        public final int maxStackSize;
        public final float baseBurnTimeTicks;
        public final float burnRateMultiplier;

        FuelType(float cps, float maxTemp, int maxStack, float ticks, float burnMult) {
            this.celsiusPerSecond = cps;
            this.maxTempReachable = maxTemp;
            this.maxStackSize = maxStack;
            this.baseBurnTimeTicks = ticks;
            this.burnRateMultiplier = burnMult;
        }
    }

    // Constructors
    public MoltenRotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MoltenRotorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MOLTEN_ROTOR.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        CombustionHeatingBehaviour heatingBehaviour = new CombustionHeatingBehaviour(this);
        behaviours.add(heatingBehaviour);
    }

    // Kinetic generation (Document §4)
    @Override
    public float getGeneratedSpeed() {
        if (currentHeatTier == RotorHeatLevel.SMOULDERING ||
                currentHeatTier == RotorHeatLevel.FADING) {
            return 24f; // 16-32 RPM range, use 24 as middle
        }
        return currentHeatTier.rpmCap;
    }

    public boolean hasShaftTowards(Level world, BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING);
        Direction leftSide = facing.getCounterClockWise();
        Direction rightSide = facing.getClockWise();
        return face == leftSide || face == rightSide;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float rpm = getGeneratedSpeed();
        if (rpm == 0) return 0f;
        float totalSU = getTotalStressOutput();
        return totalSU / Math.abs(rpm);
    }

    // Stress output (Document §5)
    public float getTotalStressOutput() {
        if (currentHeatTier == RotorHeatLevel.SMOULDERING ||
                currentHeatTier == RotorHeatLevel.FADING) {
            return switch(activeFuelType) {
                case STICK, PLANK, LOG, KELP_BLOCK -> 64f;
                case COAL, COAL_BLOCK, BLAZE_ROD, TNT -> 128f;
                case CHARCOAL -> 256f;
                default -> 64f;
            };
        }
        return currentHeatTier.baseStressCapacity;
    }

    // Main tick logic
    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        boolean needsUpdate = false;

        // Fuel consumption with burn rate multipliers (Document §6)
        if (remainingBurnTime > 0) {
            // Apply burn rate multiplier based on heat tier
            float burnMultiplier = getBurnRateMultiplier();
            remainingBurnTime -= (int) burnMultiplier;
            if (remainingBurnTime < 0) remainingBurnTime = 0;

            // Heating
            float heatingPerTick = baseHeatingRate / 20f;
            float targetTemp = calculateMaxStackedTemp();

            if (currentTemperature < targetTemp) {
                currentTemperature = Math.min(currentTemperature + heatingPerTick, targetTemp);
                needsUpdate = true;
            }
        } else {
            // Tier-based cooling (2°C per second base * tier multiplier)
            if (currentTemperature > 20f) {
                float baseCoolingRate = 2f / 20f; // 0.1°C per tick base
                float tierMultiplier = currentHeatTier.coolingMultiplier;
                float coolingRate = baseCoolingRate * tierMultiplier;
                currentTemperature = Math.max(20f, currentTemperature - coolingRate);
                needsUpdate = true;
            }

            // Reset fuel state
            if (activeFuelType != FuelType.NONE) {
                activeFuelType = FuelType.NONE;
                activeFuelCount = 0;
                baseHeatingRate = 0f;
                clearFuelStackFlags();
                needsUpdate = true;
            }
        }

        // Calculate heat tier from temperature
        RotorHeatLevel newTier = calculateHeatTierFromTemp();
        if (newTier != currentHeatTier) {
            currentHeatTier = newTier;
            updateBlockVisuals();
            updateGeneratedRotation();
            needsUpdate = true;
        }

        if (needsUpdate) {
            setChanged();
            sendData(); // Sync to client immediately
        }
    }

    // Burn rate multipliers based on tier (Document §6)
    private float getBurnRateMultiplier() {
        return switch(currentHeatTier) {
            case NONE, SMOULDERING, FADING -> 1f;  // x1 burn rate
            case KINDLED -> 2f;                     // x2 burn rate
            case SEETHING -> 3f;                    // x3 burn rate
            case BLAZING -> 4f;                     // x4 burn rate
            case RADIANT -> 5f;                     // x5 burn rate
        };
    }

    private RotorHeatLevel calculateHeatTierFromTemp() {
        if (currentTemperature < TEMP_SMOULDERING_MIN) {
            return RotorHeatLevel.NONE;
        }

        if (blazingUnlocked && currentTemperature >= TEMP_RADIANT_MIN) {
            return RotorHeatLevel.RADIANT;
        }
        if (blazingUnlocked && currentTemperature >= TEMP_BLAZING_MIN) {
            return RotorHeatLevel.BLAZING;
        }

        if (currentTemperature >= TEMP_SEETHING_MIN) {
            return RotorHeatLevel.SEETHING;
        }
        if (currentTemperature >= TEMP_KINDLED_MIN) {
            return RotorHeatLevel.KINDLED;
        }

        if (currentTemperature >= TEMP_SMOULDERING_MIN && remainingBurnTime < 100) {
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
            case SEETHING, BLAZING, RADIANT -> BlazeBurnerBlock.HeatLevel.SEETHING;
        };

        BlockState currentState = getBlockState();
        BlazeBurnerBlock.HeatLevel currentVisual =
                currentState.getValue(MoltenRotorBlock.HEAT_LEVEL);

        if (currentVisual != visualHeat) {
            level.setBlock(worldPosition,
                    currentState.setValue(MoltenRotorBlock.HEAT_LEVEL, visualHeat),
                    3);
        }
    }

    // Fuel management with proper stacking (Document §6.5)
    public boolean addFuel(FuelType fuelType, int count) {
        if (level == null || fuelType == FuelType.NONE) return false;

        if (activeFuelType != FuelType.NONE && activeFuelType != fuelType) {
            return false;
        }

        int canAdd = Math.min(count, fuelType.maxStackSize - activeFuelCount);
        if (canAdd <= 0) return false;

        // Add burn time based on count
        int ticksToAdd = (int)(fuelType.baseBurnTimeTicks * canAdd);
        remainingBurnTime += ticksToAdd;

        activeFuelType = fuelType;
        activeFuelCount += canAdd;

        // CRITICAL: Heating rate is STATIC per fuel type, NOT multiplied by count
        // This was the bug - heating rate should never change regardless of stack size
        baseHeatingRate = fuelType.celsiusPerSecond;
        currentMaxTemp = fuelType.maxTempReachable;

        // Set stacking flags
        updateFuelStackFlags(fuelType);

        setChanged();
        sendData(); // Sync to client
        return true;
    }

    public void addSpecialFuel(FuelType fuelType, int ticks) {
        if (level == null) return;

        remainingBurnTime += ticks;
        activeFuelType = fuelType;
        activeFuelCount = 1;

        // STATIC heating rate - never multiplied
        baseHeatingRate = fuelType.celsiusPerSecond;
        currentMaxTemp = fuelType.maxTempReachable;

        updateFuelStackFlags(fuelType);

        if (fuelType == FuelType.LAVA && hasBlazeRodInStack) {
            blazingUnlocked = true;
        } else if (fuelType == FuelType.BLAZE_CAKE) {
            blazingUnlocked = true;
        }

        setChanged();
        sendData(); // Sync to client
    }

    public void addUltimateFuel(int ticks) {
        if (level == null) return;
        remainingBurnTime += ticks;
        blazingUnlocked = true;
        setChanged();
        sendData(); // Sync to client
    }

    private void updateFuelStackFlags(FuelType fuelType) {
        switch(fuelType) {
            case STICK -> hasSticksInStack = true;
            case COAL -> hasCoalInStack = true;
            case CHARCOAL -> hasCharcoalInStack = true;
            case BLAZE_ROD -> hasBlazeRodInStack = true;
            case LAVA -> hasLavaInStack = true;
        }
    }

    private void clearFuelStackFlags() {
        hasLavaInStack = false;
        hasBlazeRodInStack = false;
        hasSulfurInStack = false;
        hasCoalInStack = false;
        hasCharcoalInStack = false;
        hasSticksInStack = false;
    }

    // Temperature stacking logic (Document §6.5)
    private float calculateMaxStackedTemp() {
        float baseMax = currentMaxTemp;

        // Lava + Blaze Rod = 1250°C
        if (hasLavaInStack && hasBlazeRodInStack) {
            return 1250f;
        }

        // Lava + Blaze Cake = 1400°C
        if (hasLavaInStack && activeFuelType == FuelType.BLAZE_CAKE) {
            return 1400f;
        }

        // Coal + Charcoal = max 850°C
        if (hasCoalInStack && hasCharcoalInStack && activeFuelCount > 16) {
            return Math.min(850f, baseMax);
        }

        // Lava + TNT = 750°C
        if (hasLavaInStack && activeFuelType == FuelType.TNT) {
            return 750f;
        }

        // Lava + Sulfur = 1150°C (if you add sulfur fuel)
        if (hasLavaInStack && hasSulfurInStack) {
            return 1150f;
        }

        return baseMax;
    }

    // Getters
    public RotorHeatLevel getCurrentHeatTier() {
        return currentHeatTier;
    }

    public int getRemainingBurnTime() {
        return remainingBurnTime;
    }

    public int getDisplayTemperature() {
        return (int)currentTemperature;
    }

    public int getDisplayFuelTime() {
        // Fuel timer shows at SMOULDERING+ (300°C+) when rotation begins
        if (currentTemperature < TEMP_SMOULDERING_MIN) {
            return 0;
        }
        return remainingBurnTime;
    }

    public String getHeatTierName() {
        if (currentHeatTier == RotorHeatLevel.FADING) {
            return "Heated"; // FADING never shown (Document §3)
        }
        return currentHeatTier.displayName;
    }

    public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
        return getBlockState().getValue(MoltenRotorBlock.HEAT_LEVEL);
    }

    public boolean isCombustionActive() {
        return currentHeatTier.ordinal() >= RotorHeatLevel.SMOULDERING.ordinal();
    }

    public boolean shouldShowStatus() {
        if ((currentHeatTier == RotorHeatLevel.SMOULDERING ||
                currentHeatTier == RotorHeatLevel.FADING) &&
                remainingBurnTime == 0) {
            return false;
        }
        return currentHeatTier != RotorHeatLevel.NONE;
    }

    // Goggles overlay (Document §9)
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // Add blank line for spacing
        tooltip.add(Component.literal(""));

        int temp = getDisplayTemperature();
        float rpm = getGeneratedSpeed();
        float stress = getTotalStressOutput();
        int fuelTime = getDisplayFuelTime();
        String heatName = getHeatTierName();

        ChatFormatting color = switch(currentHeatTier) {
            case NONE -> ChatFormatting.GRAY;
            case SMOULDERING, FADING -> ChatFormatting.YELLOW;
            case KINDLED -> ChatFormatting.RED;
            case SEETHING -> ChatFormatting.DARK_RED;
            case BLAZING, RADIANT -> ChatFormatting.DARK_PURPLE;
        };

        tooltip.add(Component.literal("")
                .append(Component.literal("Heat: " + heatName + " (" + temp + "°C)").withStyle(color)));

        if (stress > 0) {
            tooltip.add(Component.literal("")
                    .append(Component.literal("Stress Capacity: ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(String.format("%.1f", stress)).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" su").withStyle(ChatFormatting.GOLD)));
        } else {
            tooltip.add(Component.literal("")
                    .append(Component.literal("Stress Capacity: 0 su").withStyle(ChatFormatting.GRAY)));
        }

        if (rpm > 0) {
            tooltip.add(Component.literal("")
                    .append(Component.literal("Rotation Speed: " + (int)rpm + " RPM").withStyle(ChatFormatting.AQUA)));

            if (currentHeatTier.ordinal() > RotorHeatLevel.SMOULDERING.ordinal()) {
                tooltip.add(Component.literal("")
                        .append(Component.literal("  (Max: " + (int)currentHeatTier.rpmCap + " RPM)").withStyle(ChatFormatting.DARK_GRAY)));
            }
        } else {
            tooltip.add(Component.literal("")
                    .append(Component.literal("Rotation Speed: 0 RPM").withStyle(ChatFormatting.GRAY)));
        }

        if (fuelTime > 0) {
            int seconds = fuelTime / 20;
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;

            if (minutes > 0) {
                tooltip.add(Component.literal("")
                        .append(Component.literal("Fuel Remaining: " + minutes + "m " + remainingSeconds + "s").withStyle(ChatFormatting.GREEN)));
            } else {
                tooltip.add(Component.literal("")
                        .append(Component.literal("Fuel Remaining: " + remainingSeconds + "s").withStyle(ChatFormatting.GREEN)));
            }
        } else if (currentHeatTier != RotorHeatLevel.NONE) {
            tooltip.add(Component.literal("")
                    .append(Component.literal("Fuel Remaining: 0s").withStyle(ChatFormatting.GRAY)));
        }

        if (currentHeatTier == RotorHeatLevel.BLAZING) {
            tooltip.add(Component.literal("")
                    .append(Component.literal("✦ BLAZING TIER ACTIVE").withStyle(ChatFormatting.DARK_PURPLE)));
        } else if (currentHeatTier == RotorHeatLevel.RADIANT) {
            tooltip.add(Component.literal("")
                    .append(Component.literal("✦ RADIANT TIER ACTIVE").withStyle(ChatFormatting.DARK_PURPLE)));
            tooltip.add(Component.literal("")
                    .append(Component.literal("  (Combustion recipes enabled)").withStyle(ChatFormatting.DARK_GRAY)));
        }

        return true;
    }

    // NBT serialization
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
        tag.putBoolean("BlazingUnlocked", blazingUnlocked);
        tag.putBoolean("HasLava", hasLavaInStack);
        tag.putBoolean("HasBlazeRod", hasBlazeRodInStack);
        tag.putBoolean("HasSulfur", hasSulfurInStack);
        tag.putBoolean("HasCoal", hasCoalInStack);
        tag.putBoolean("HasCharcoal", hasCharcoalInStack);
        tag.putBoolean("HasSticks", hasSticksInStack);
    }

    @Override
    protected void read(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        currentTemperature = tag.getFloat("Temperature");
        remainingBurnTime = tag.getInt("FuelTime");
        activeFuelCount = tag.getInt("FuelCount");
        baseHeatingRate = tag.getFloat("HeatingRate");
        currentMaxTemp = tag.getFloat("MaxTemp");
        blazingUnlocked = tag.getBoolean("BlazingUnlocked");
        hasLavaInStack = tag.getBoolean("HasLava");
        hasBlazeRodInStack = tag.getBoolean("HasBlazeRod");
        hasSulfurInStack = tag.getBoolean("HasSulfur");
        hasCoalInStack = tag.getBoolean("HasCoal");
        hasCharcoalInStack = tag.getBoolean("HasCharcoal");
        hasSticksInStack = tag.getBoolean("HasSticks");

        int tierIndex = tag.getInt("HeatTier");
        currentHeatTier = tierIndex >= 0 && tierIndex < RotorHeatLevel.values().length
                ? RotorHeatLevel.values()[tierIndex] : RotorHeatLevel.NONE;

        int fuelIndex = tag.getInt("FuelType");
        activeFuelType = fuelIndex >= 0 && fuelIndex < FuelType.values().length
                ? FuelType.values()[fuelIndex] : FuelType.NONE;

        // If this is a client packet, force visual updates
        if (clientPacket && level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}