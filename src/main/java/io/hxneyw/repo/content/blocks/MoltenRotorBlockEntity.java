package io.hxneyw.repo.content.blocks;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.registry.ModBlockEntities;
import io.hxneyw.repo.content.blocks.behaviour.CombustionHeatingBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class MoltenRotorBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {
    // Temperature constants
    private static final float TEMP_NONE_MIN = 0f, TEMP_SMOULDERING_MIN = 300f, TEMP_FADING_MAX = 350f;
    private static final float TEMP_KINDLED_MIN = 500f, TEMP_SEETHING_MIN = 800f, TEMP_RADIANT_MIN = 1300f, TEMP_ABSOLUTE_MAX = 1599f;

    // Cooldown: 10 seconds per 50°C above ambient
    private static final float COOLDOWN_TEMP_INTERVAL = 50f;
    private static final int COOLDOWN_SECONDS_PER_INTERVAL = 10;

    // State
    private float currentTemperature = 20f, baseHeatingRate = 0f, currentMaxTemp = 0f;
    private int remainingBurnTime = 0, activeFuelCount = 0, clientUpdateCounter = 0;
    private int lastNotifiedFuelCount = 0; // Track when to notify neighbors
    private RotorHeatLevel currentHeatTier = RotorHeatLevel.NONE, lastSentHeatTier = RotorHeatLevel.NONE;
    private FuelType activeFuelType = FuelType.NONE;
    private boolean creativeMode = false, kineticsInitialized = false;
    private boolean hasLavaInStack = false, hasSulfurInStack = false, hasCoalInStack = false, hasCharcoalInStack = false, hasStickInStack = false;
    public int tntCooldown = 0;


    public enum RotorHeatLevel {
        NONE(0, 0f, 0f, "Unheated"), SMOULDERING(300, 32f, 128f, "Heated"), FADING(325, 24f, 128f, "Heated"),
        KINDLED(650, 64f, 1024f, "Heated"), SEETHING(950, 128f, 2048f, "Superheated"), RADIANT(1400, 256f, 8192f, "Combustion");
        public final int displayTemp; public final float rpmCap, baseStressCapacity; public final String displayName;
        RotorHeatLevel(int temp, float rpm, float su, String name) {
            this.displayTemp = temp; this.rpmCap = rpm; this.baseStressCapacity = su; this.displayName = name;
        }
    }

    public enum FuelType {
        NONE(0f, 0f, 0, 0f, 1f), STICK(2.5f, 150f, 16, 100f, 1f), LOG(8.5f, 475f, 8, 700f, 1f),
        COAL(13f, 550f, 32, 350f, 1f), CHARCOAL(14f, 650f, 32, 350f, 0.8f), COAL_BLOCK(25f, 900f, 1, 2400f, 1f),
        KELP_BLOCK(15.5f, 750f, 3, 400f, 1f), TNT(25f, 600f, 1, 750f, 1.2f), BLAZE_CAKE(40f, 1200f, 1, 3000f, 1f),
        SOUL_FIRED_BLAZE_CAKE(48.5f, 1599f, 1, 3500f, 1f);
        public final float celsiusPerSecond, maxTempReachable, baseBurnTimeTicks, burnRateMultiplier;
        public final int maxStackSize;
        FuelType(float cps, float maxTemp, int maxStack, float ticks, float burnMult) {
            this.celsiusPerSecond = cps; this.maxTempReachable = maxTemp; this.maxStackSize = maxStack;
            this.baseBurnTimeTicks = ticks; this.burnRateMultiplier = burnMult;
        }
    }

    public MoltenRotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    public MoltenRotorBlockEntity(BlockPos pos, BlockState state) { this(ModBlockEntities.MOLTEN_ROTOR.get(), pos, state); }



    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) { super.addBehaviours(behaviours); behaviours.add(new CombustionHeatingBehaviour(this)); }

// REPLACE these methods in MoltenRotorBlockEntity.java:


    @Override
    public float getGeneratedSpeed() {
        float baseSpeed = currentHeatTier.rpmCap;
        if (baseSpeed == 0) return 0;

        Direction facing = getBlockState().getValue(MoltenRotorBlock.FACING);

        return switch (facing) {
            case NORTH -> -baseSpeed;  // Invert for North
            case EAST -> -baseSpeed;
            case SOUTH -> baseSpeed;
            case WEST -> baseSpeed;
            default -> baseSpeed;
        };
    }


    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(getGeneratedSpeed());
        if (speed == 0) return 0f;
        float desiredSU = currentHeatTier.baseStressCapacity;
        return this.lastCapacityProvided = desiredSU / speed;
    }

    @Override public float calculateStressApplied() { return 0f; }
    public boolean isCreativeMode() { return creativeMode; }
    public float getTotalStressOutput() { return currentHeatTier.baseStressCapacity; }
    public RotorHeatLevel getCurrentHeatTier() { return currentHeatTier; }
    public int getDisplayTemperature() { return (int)currentTemperature; }
    public int getDisplayFuelTime() { return Math.max(remainingBurnTime, 0); }
    public int getDisplayRetentionTime() { return 0; } // REMOVED - always returns 0
    public String getHeatTierName() { return currentHeatTier == RotorHeatLevel.FADING ? "Heated" : currentHeatTier.displayName; }
    public boolean isCombustionActive() { return currentHeatTier.ordinal() >= RotorHeatLevel.SMOULDERING.ordinal(); }
    public boolean shouldShowStatus() { return remainingBurnTime > 0 || currentTemperature > 20f; }
    public int getActiveFuelCount() { return activeFuelCount; }
    public FuelType getActiveFuelType() { return activeFuelType; }

    public void setCreativeMode(boolean creative) {
        this.creativeMode = creative;
        if (creative) {
            currentHeatTier = RotorHeatLevel.SMOULDERING; currentTemperature = TEMP_SMOULDERING_MIN + 100f;
            remainingBurnTime = 0;
            activeFuelType = FuelType.NONE; activeFuelCount = 0;
            updateBlockVisuals(); notifyKineticNetworkOfChange(); setChanged(); sendData();
        }
    }

    public void cycleCreativeTier() {
        if (!creativeMode) return;
        currentHeatTier = switch(currentHeatTier) {
            case NONE, SMOULDERING, FADING -> RotorHeatLevel.SEETHING;
            case KINDLED, SEETHING -> RotorHeatLevel.RADIANT;
            case RADIANT -> RotorHeatLevel.SMOULDERING;
        };
        currentTemperature = switch(currentHeatTier) {
            case SMOULDERING -> TEMP_SMOULDERING_MIN + 100f;
            case SEETHING -> TEMP_SEETHING_MIN + 200f;
            case RADIANT -> TEMP_RADIANT_MIN + 150f;
            default -> 20f;
        };
        updateBlockVisuals(); notifyKineticNetworkOfChange(); setChanged(); sendData();
        if (level instanceof ServerLevel sl) {
            var particle = currentHeatTier == RotorHeatLevel.SMOULDERING ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
            int count = switch(currentHeatTier) { case SMOULDERING -> 15; case SEETHING -> 25; case RADIANT -> 35; default -> 10; };
            sl.sendParticles(particle, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, count, 0.3, 0.3, 0.3, 0.05);
        }
    }

    public void initializeKinetics() {
        if (kineticsInitialized || level == null || level.isClientSide) return;
        kineticsInitialized = true; attachKinetics();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock()); setChanged();
    }

    public final IItemHandler fuelHandler = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            FuelType fuelType = getFuelTypeFromItem(stack);
            if (fuelType == null || fuelType == FuelType.NONE) return stack;
            if (simulate) return stack.getCount() > 1 ? stack.copyWithCount(stack.getCount() - 1) : ItemStack.EMPTY;
            if (!addFuel(fuelType, 1)) return stack;
            ItemStack remainder = stack.copy(); remainder.shrink(1); return remainder;
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return getFuelTypeFromItem(stack) != null; }
    };
    //ARM INTERACTION POINT FUEL WHITELIST
    public FuelType getFuelTypeFromItem(ItemStack stack) {
        if (stack.is(Items.STICK)) return FuelType.STICK;
        if (stack.is(ItemTags.LOGS)) return FuelType.LOG;
        if (stack.is(Items.COAL)) return FuelType.COAL;
        if (stack.is(Items.CHARCOAL)) return FuelType.CHARCOAL;
        if (stack.is(Items.COAL_BLOCK)) return FuelType.COAL_BLOCK;
        if (stack.is(Items.DRIED_KELP_BLOCK)) return FuelType.KELP_BLOCK;
        if (stack.is(Items.TNT)) return FuelType.TNT;
        if (stack.is(AllItems.BLAZE_CAKE)) return FuelType.BLAZE_CAKE;
        if (stack.is(io.hxneyw.repo.content.Items.SOUL_FIRED_BLAZE_CAKE)) return FuelType.SOUL_FIRED_BLAZE_CAKE;
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        if (tntCooldown > 0) {
            tntCooldown--;
        }

        if (creativeMode) {
            RotorHeatLevel prev = currentHeatTier;
            updateBlockVisuals();
            if (currentHeatTier != prev) notifyKineticNetworkOfChange();
            setChanged();
            if (++clientUpdateCounter >= 20) { clientUpdateCounter = 0; sendData(); }
            return;
        }

        boolean needsUpdate = false;
        RotorHeatLevel previousTier = currentHeatTier;

        // PHASE 1: FUEL BURNING
        if (remainingBurnTime > 0) {
            remainingBurnTime -= 1;
            if (remainingBurnTime < 0) remainingBurnTime = 0;

            float heatingPerTick = baseHeatingRate / 20f;
            if (level.isRainingAt(worldPosition.above())) heatingPerTick *= 0.5f;

            float targetTemp = calculateMaxStackedTemp();
            if (currentTemperature < targetTemp) {
                currentTemperature = Math.min(Math.min(currentTemperature + heatingPerTick, targetTemp), TEMP_ABSOLUTE_MAX);
                needsUpdate = true;
            }

            // When fuel runs out, just clear the fuel type
            if (remainingBurnTime == 0) {
                activeFuelType = FuelType.NONE;
                activeFuelCount = 0;
                baseHeatingRate = currentMaxTemp = 0f;
                clearFuelStackFlags();
                needsUpdate = true;
            }
        }
        // PHASE 2: COOLING (no fuel - simplified cooling)
        else {
            if (currentTemperature > 20f) {
                // Simple cooling: 2°C per second base rate
                float coolingPerTick = 2f / 20f;
                if (level.isRainingAt(worldPosition.above())) coolingPerTick *= 2f;
                currentTemperature = Math.max(20f, currentTemperature - coolingPerTick);
                needsUpdate = true;
            }
        }

        RotorHeatLevel newTier = calculateHeatTierFromTemp();
        if (newTier != previousTier) {
            currentHeatTier = newTier;
            updateBlockVisuals();
            if (tierAffectsRotation(previousTier, newTier)) notifyKineticNetworkOfChange();
            needsUpdate = true;
        }
        if (needsUpdate) setChanged();

        boolean tierChanged = currentHeatTier != lastSentHeatTier;
        if (tierChanged) {
            lastSentHeatTier = currentHeatTier;
            clientUpdateCounter = 0;
            sendData();
        } else if (++clientUpdateCounter >= 1) {
            clientUpdateCounter = 0;
            sendData();
        }
    }

    public int getDisplayCooldownTime() {
        // Calculate ACTUAL time to cool from current temp to ambient
        if (creativeMode || currentTemperature <= 20f || remainingBurnTime > 0) return 0;

        // Calculate actual cooling rate per tick
        float coolingPerTick = 2f / 20f; // 2°C per second = 0.1°C per tick
        if (level != null && level.isRainingAt(worldPosition.above())) {
            coolingPerTick *= 2f; // Rain doubles to 4°C per second
        }

        // Calculate how many ticks needed to cool from current temp to ambient (300°C)
        float tempDifference = currentTemperature - 300f;
        int ticksNeeded = (int)Math.ceil(tempDifference / coolingPerTick);

        return ticksNeeded; // Return ticks directly
    }

    private boolean tierAffectsRotation(RotorHeatLevel from, RotorHeatLevel to) {
        return (from == RotorHeatLevel.NONE) != (to == RotorHeatLevel.NONE) || from.rpmCap != to.rpmCap;
    }

    private void notifyKineticNetworkOfChange() {
        if (level == null || level.isClientSide || !kineticsInitialized) return;
        super.updateGeneratedRotation(); level.updateNeighborsAt(worldPosition, getBlockState().getBlock()); setChanged();
    }

    private RotorHeatLevel calculateHeatTierFromTemp() {
        if (currentTemperature < TEMP_SMOULDERING_MIN) return RotorHeatLevel.NONE;
        if (currentTemperature >= TEMP_RADIANT_MIN) return RotorHeatLevel.RADIANT;
        if (currentTemperature >= TEMP_SEETHING_MIN) return RotorHeatLevel.SEETHING;
        if (currentTemperature >= TEMP_KINDLED_MIN) return RotorHeatLevel.KINDLED;
        return (currentTemperature >= TEMP_SMOULDERING_MIN && currentTemperature < TEMP_FADING_MAX && remainingBurnTime == 0)
                ? RotorHeatLevel.FADING : RotorHeatLevel.SMOULDERING;
    }

    private void updateBlockVisuals() {
        if (level == null) return;
        // FIXED: SMOULDERING and FADING now map to KINDLED to enable heated recipes at 300°C
        BlazeBurnerBlock.HeatLevel visualHeat = switch(currentHeatTier) {
            case NONE -> BlazeBurnerBlock.HeatLevel.NONE;
            case SMOULDERING, FADING -> BlazeBurnerBlock.HeatLevel.KINDLED;  // ← 300°C = HEATED recipes
            case KINDLED -> BlazeBurnerBlock.HeatLevel.KINDLED;              // ← 500°C = HEATED recipes
            case SEETHING, RADIANT -> BlazeBurnerBlock.HeatLevel.SEETHING;   // ← 800°C+ = SUPERHEATED recipes
        };
        BlockState state = getBlockState();
        if (state.getValue(MoltenRotorBlock.HEAT_LEVEL) != visualHeat) {
            // Update block state with flag 3 (notify neighbors + send to clients)
            level.setBlock(worldPosition, state.setValue(MoltenRotorBlock.HEAT_LEVEL, visualHeat), 3);
            // Force notify blocks above (Basins check heat from below)
            level.updateNeighborsAt(worldPosition.above(), getBlockState().getBlock());
        }
    }

    public boolean addFuel(FuelType fuelType, int count) {
        if (level == null || fuelType == FuelType.NONE) return false;

        FuelType currentFuel = activeFuelType;
        int currentCount = activeFuelCount;
        int burnTime = remainingBurnTime;

        // CASE 1: New fuel has HIGHER heat output - OVERRIDE current fuel
        if (currentFuel != FuelType.NONE &&
                burnTime > 0 &&
                fuelType.maxTempReachable > currentFuel.maxTempReachable) {

            activeFuelType = fuelType;
            activeFuelCount = Math.min(count, fuelType.maxStackSize);
            remainingBurnTime = (int)(fuelType.baseBurnTimeTicks * count);
            baseHeatingRate = fuelType.celsiusPerSecond;
            currentMaxTemp = fuelType.maxTempReachable;

            clearFuelStackFlags();
            updateFuelStackFlags(fuelType);
            setChanged();
            sendData();
            return true;
        }



        // CASE 2: STICKS - Can only add when logs are burning
        if (fuelType == FuelType.STICK) {
            if (currentFuel != FuelType.LOG || burnTime <= 0 || currentCount >= 32) {
                return false;
            }
            count = Math.min(count, 32 - currentCount);
            if (count <= 0) return false;

            remainingBurnTime += (int)(fuelType.baseBurnTimeTicks * count);
            activeFuelCount += count;
            updateFuelStackFlags(fuelType);
            setChanged();
            sendData();
            return true;
        }

        // CASE 3: Same fuel burning - refill up to max
        if (currentFuel == fuelType && burnTime > 0) {
            // Calculate how many items worth of fuel remain
            float itemsWorthOfTimeRemaining = remainingBurnTime / fuelType.baseBurnTimeTicks;
            // Round up - if we have 17.5 items worth, we count as having 18 slots occupied
            int effectiveCurrentCount = (int) Math.ceil(itemsWorthOfTimeRemaining);

            // Calculate how many we can add
            int canAdd = Math.min(count, fuelType.maxStackSize - effectiveCurrentCount);
            if (canAdd <= 0) return false;

            // Add the fuel time
            remainingBurnTime += (int)(fuelType.baseBurnTimeTicks * canAdd);

            // Update the count to reflect the new total
            float newItemsWorth = remainingBurnTime / fuelType.baseBurnTimeTicks;
            activeFuelCount = Math.min((int) Math.ceil(newItemsWorth), fuelType.maxStackSize);

            setChanged();
            sendData();
            return true;
        }

        // CASE 4: Different fuel burning (not higher heat) - reject
        if (currentFuel != FuelType.NONE && currentFuel != fuelType && burnTime > 0) {
            return false;
        }

        // CASE 5: No fuel burning - start fresh
        count = Math.min(count, fuelType.maxStackSize);
        if (count <= 0) return false;

        activeFuelType = fuelType;
        activeFuelCount = count;
        remainingBurnTime = (int)(fuelType.baseBurnTimeTicks * count);
        baseHeatingRate = fuelType.celsiusPerSecond;
        currentMaxTemp = fuelType.maxTempReachable;

        updateFuelStackFlags(fuelType);
        setChanged();
        sendData();
        return true;
    }


    public void addSpecialFuel(FuelType fuelType, int ticks) {
        if (level == null) return;
        remainingBurnTime += ticks; activeFuelType = fuelType; activeFuelCount = 1;
        baseHeatingRate = fuelType.celsiusPerSecond; currentMaxTemp = fuelType.maxTempReachable;
        updateFuelStackFlags(fuelType); setChanged(); sendData();
    }

    public void addUltimateFuel(int ticks) {
        if (level == null) return;
        remainingBurnTime += ticks;
        currentTemperature = Math.min(Math.max(currentTemperature, TEMP_RADIANT_MIN), TEMP_ABSOLUTE_MAX);
        setChanged(); sendData();
    }

    private void updateFuelStackFlags(FuelType fuelType) {
        switch(fuelType) { case COAL -> hasCoalInStack = true; case CHARCOAL -> hasCharcoalInStack = true; case STICK -> hasStickInStack = true; default -> {} }
    }

    private void clearFuelStackFlags() { hasLavaInStack = hasSulfurInStack = hasCoalInStack = hasCharcoalInStack = hasStickInStack = false; }

    private float calculateMaxStackedTemp() {
        return (hasStickInStack && activeFuelType == FuelType.LOG) ? 550f : currentMaxTemp;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal(""));
        if (creativeMode) {
            tooltip.add(Component.literal("★ CREATIVE MODE").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            tooltip.add(Component.literal("  (Infinite heat - right-click to cycle)").withStyle(ChatFormatting.DARK_GRAY));
            ChatFormatting color = switch(currentHeatTier) {
                case NONE -> ChatFormatting.GRAY; case SMOULDERING, FADING -> ChatFormatting.YELLOW;
                case KINDLED -> ChatFormatting.RED; case SEETHING -> ChatFormatting.DARK_RED; case RADIANT -> ChatFormatting.DARK_PURPLE;
            };
            tooltip.add(Component.literal("Heat: " + getHeatTierName() + " (" + getDisplayTemperature() + "°C)").withStyle(color));
            tooltip.add(Component.literal("Stress Capacity: " + (int)getTotalStressOutput() + " su").withStyle(ChatFormatting.GOLD));
            if (currentHeatTier == RotorHeatLevel.RADIANT) {
                tooltip.add(Component.literal("✦ RADIANT TIER ACTIVE").withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.literal("  (Combustion recipes enabled)").withStyle(ChatFormatting.DARK_GRAY));
            }
            return true;
        }

        int temp = getDisplayTemperature(), fuelTime = getDisplayFuelTime(), cooldownTime = getDisplayCooldownTime();
        float stress = getTotalStressOutput();
        String heatName = getHeatTierName();
        ChatFormatting color = switch(currentHeatTier) {
            case NONE -> ChatFormatting.GRAY; case SMOULDERING, FADING -> ChatFormatting.YELLOW;
            case KINDLED -> ChatFormatting.RED; case SEETHING -> ChatFormatting.DARK_RED; case RADIANT -> ChatFormatting.DARK_PURPLE;
        };
        tooltip.add(Component.literal("Heat: " + heatName + " (" + temp + "°C)").withStyle(color));
        tooltip.add(Component.literal("Stress Capacity: " + (currentHeatTier != RotorHeatLevel.NONE && stress > 0 ? (int)stress : 0) + " su")
                .withStyle(currentHeatTier != RotorHeatLevel.NONE && stress > 0 ? ChatFormatting.GOLD : ChatFormatting.GRAY));

        if (fuelTime > 0) {
            int sec = fuelTime / 20, min = sec / 60, remSec = sec % 60;
            tooltip.add(Component.literal("Fuel Remaining: " + (min > 0 ? min + "m " : "") + remSec + "s").withStyle(ChatFormatting.GREEN));
        } else if (cooldownTime > 0) {
            int sec = cooldownTime / 20, min = sec / 60, remSec = sec % 60;
            tooltip.add(Component.literal("Cooling Down: " + (min > 0 ? min + "m " : "") + remSec + "s").withStyle(ChatFormatting.YELLOW));
        } else if (currentHeatTier != RotorHeatLevel.NONE) {
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
        tag.putFloat("Temperature", currentTemperature); tag.putInt("FuelTime", remainingBurnTime);
        tag.putInt("HeatTier", currentHeatTier.ordinal()); tag.putInt("FuelType", activeFuelType.ordinal());
        tag.putInt("FuelCount", activeFuelCount); tag.putFloat("HeatingRate", baseHeatingRate);
        tag.putFloat("MaxTemp", currentMaxTemp); tag.putBoolean("HasLava", hasLavaInStack);
        tag.putBoolean("HasStick", hasStickInStack); tag.putBoolean("HasSulfur", hasSulfurInStack);
        tag.putBoolean("HasCoal", hasCoalInStack); tag.putBoolean("HasCharcoal", hasCharcoalInStack);
        tag.putBoolean("KineticsInit", kineticsInitialized);
        tag.putBoolean("CreativeMode", creativeMode);
        tag.putInt("LastNotifiedFuel", lastNotifiedFuelCount);
    }

    @Override
    protected void read(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        currentTemperature = tag.getFloat("Temperature"); remainingBurnTime = tag.getInt("FuelTime");
        activeFuelCount = tag.getInt("FuelCount"); baseHeatingRate = tag.getFloat("HeatingRate");
        currentMaxTemp = tag.getFloat("MaxTemp"); hasLavaInStack = tag.getBoolean("HasLava");
        hasSulfurInStack = tag.getBoolean("HasSulfur"); hasCoalInStack = tag.getBoolean("HasCoal");
        hasStickInStack = tag.getBoolean("HasStick"); hasCharcoalInStack = tag.getBoolean("HasCharcoal");
        kineticsInitialized = tag.getBoolean("KineticsInit");
        creativeMode = tag.getBoolean("CreativeMode");
        lastNotifiedFuelCount = tag.getInt("LastNotifiedFuel");
        int tierIndex = tag.getInt("HeatTier");
        currentHeatTier = tierIndex >= 0 && tierIndex < RotorHeatLevel.values().length ? RotorHeatLevel.values()[tierIndex] : RotorHeatLevel.NONE;
        int fuelIndex = tag.getInt("FuelType");
        activeFuelType = fuelIndex >= 0 && fuelIndex < FuelType.values().length ? FuelType.values()[fuelIndex] : FuelType.NONE;
        if (clientPacket) { lastSentHeatTier = currentHeatTier;
            if (level != null && level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}