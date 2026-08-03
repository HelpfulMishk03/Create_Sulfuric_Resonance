package io.hxneyw.repo.content.blocks.moltenrotor;

import io.hxneyw.repo.compat.fuel.FuelCompatibility;
import io.hxneyw.repo.compat.fuel.ResolvedFuel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Owns the Molten Rotor's active fuel, queued fuel, burn timing, and
 * fuel-specific save data. Temperature tiers and kinetic output remain in the
 * block entity.
 */
public final class MoltenRotorFuelController {
    private final MoltenRotorBlockEntity furnace;

    private float baseHeatingRate = 0.0F;
    private float currentMaxTemp = 0.0F;
    private int remainingBurnTime = 0;
    private int activeFuelCount = 0;
    private int activeLogStickCount = 0;
    private final List<ItemStack> activeLogStickStacks = new ArrayList<>();
    private int renderedFuelUnitCount = 0;
    private MoltenRotorBlockEntity.FuelType activeFuelType =
            MoltenRotorBlockEntity.FuelType.NONE;
    private ItemStack activeFuelStack = ItemStack.EMPTY;
    private boolean hasLavaInStack = false;
    private boolean hasSulfurInStack = false;
    private boolean hasCoalInStack = false;
    private boolean hasCharcoalInStack = false;
    private boolean hasStickInStack = false;
    private final List<ItemStack> pendingFuel = new ArrayList<>();

    public MoltenRotorFuelController(MoltenRotorBlockEntity furnace) {
        this.furnace = furnace;
    }

    private MoltenRotorBlockEntity.FuelType getFuelTypeFromItem(ItemStack stack) {
        ResolvedFuel resolvedFuel = FuelCompatibility.resolve(stack);

        if (resolvedFuel == null || resolvedFuel.isInvalid()) {
            return null;
        }

        return resolvedFuel.type();
    }

    public boolean insertFuel(ItemStack stack, boolean simulate) {
        if (this.furnace.isCreativeMode()) {
            return false;
        }

        if (this.furnace.getLevel() == null || stack.isEmpty()) {
            return false;
        }

        MoltenRotorBlockEntity.FuelType fuelType =
                this.getFuelTypeFromItem(stack);

        if (fuelType == null
                || fuelType == MoltenRotorBlockEntity.FuelType.NONE) {
            return false;
        }

        if (fuelType
                == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE
                && !this.furnace.getCurrentHeatTier().isAtLeast(
                        MoltenRotorBlockEntity.RotorHeatLevel.SEETHING
                )) {
            return false;
        }

        if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
            return this.insertLogBoostStick(stack, simulate);
        }

        if (!this.hasActiveOrPendingFuel()) {
            if (!simulate) {
                this.startFuel(stack);
                this.furnace.syncFuelState();
            }

            return true;
        }

        if (this.isSpecialFuel(fuelType)) {
            boolean soulCakeAfterBlazeCake =
                    fuelType
                            == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE
                            && this.activeFuelType
                            == MoltenRotorBlockEntity.FuelType.BLAZE_CAKE
                            && this.furnace.getCurrentHeatTier().isAtLeast(
                                    MoltenRotorBlockEntity.RotorHeatLevel.SEETHING
                            )
                            && !this.hasPendingSpecialFuel();

            if (!soulCakeAfterBlazeCake
                    && ((this.activeFuelType
                    != MoltenRotorBlockEntity.FuelType.NONE
                    && this.isSpecialFuel(this.activeFuelType))
                    || this.hasPendingSpecialFuel())) {
                return false;
            }

            if (!simulate) {
                this.enqueueFuel(stack);
                this.furnace.syncFuelState();
            }

            return true;
        }

        /*
         * Special fuels keep their exclusive queue rules.
         * Ordinary fuels may be mixed in insertion order.
         */
        if (this.hasPendingSpecialFuel()) {
            return false;
        }

        ResolvedFuel resolvedFuel = FuelCompatibility.resolve(stack);

        if (resolvedFuel == null || resolvedFuel.isInvalid()) {
            return false;
        }

        int maximumUnits = resolvedFuel.maximumUnits();

        if (this.getFuelUnitCount(fuelType) >= maximumUnits) {
            return false;
        }

        if (this.getFuelUnitCount(fuelType) >= fuelType.maxStackSize) {
            return false;
        }

        if (!simulate) {
            this.enqueueFuel(stack);
            this.furnace.syncFuelState();
        }

        return true;
    }

    float getBaseHeatingRate() {
        return this.baseHeatingRate;
    }

    float getMaximumStackedTemperature() {
        return this.calculateMaxStackedTemp();
    }

    void decrementRemainingBurnTime() {
        if (this.remainingBurnTime > 0) {
            this.remainingBurnTime--;
        }
    }

    boolean advanceFuelIfBurnedOut() {
        if (this.remainingBurnTime != 0) {
            return false;
        }

        this.clearActiveFuel();
        this.startNextPendingFuel();
        return true;
    }

    private boolean insertLogBoostStick(ItemStack stack, boolean simulate) {
        if (this.activeFuelType != MoltenRotorBlockEntity.FuelType.LOG
                || this.remainingBurnTime <= 0) {
            return false;
        }

        int logUnits = this.getFuelUnitCount(
                MoltenRotorBlockEntity.FuelType.LOG
        );

        if (logUnits + this.activeLogStickCount >= 32) {
            return false;
        }

        if (!simulate) {
            this.remainingBurnTime +=
                    (int) MoltenRotorBlockEntity.FuelType.STICK.baseBurnTimeTicks;
            this.activeLogStickCount++;
            this.activeLogStickStacks.add(stack.copyWithCount(1));
            this.activeFuelCount = logUnits + this.activeLogStickCount;
            this.updateFuelStackFlags(
                    MoltenRotorBlockEntity.FuelType.STICK
            );
            this.furnace.syncFuelState();
        }

        return true;
    }

    private boolean hasActiveOrPendingFuel() {
        return this.activeFuelType
                != MoltenRotorBlockEntity.FuelType.NONE
                && this.remainingBurnTime > 0
                || !this.pendingFuel.isEmpty();
    }

    private boolean isSpecialFuel(
            MoltenRotorBlockEntity.FuelType fuelType
    ) {
        return fuelType == MoltenRotorBlockEntity.FuelType.BLAZE_CAKE
                || fuelType
                == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE;
    }

    private boolean hasPendingSpecialFuel() {
        for (ItemStack queuedStack : this.pendingFuel) {
            MoltenRotorBlockEntity.FuelType queuedType =
                    this.getFuelTypeFromItem(queuedStack);

            if (queuedType != null && this.isSpecialFuel(queuedType)) {
                return true;
            }
        }

        return false;
    }

    private int getFuelUnitCount(
            MoltenRotorBlockEntity.FuelType fuelType
    ) {
        int count = this.activeFuelType == fuelType
                && this.remainingBurnTime > 0 ? 1 : 0;

        for (ItemStack queuedStack : this.pendingFuel) {
            if (this.getFuelTypeFromItem(queuedStack) == fuelType) {
                count += queuedStack.getCount();
            }
        }

        return count;
    }

    private void enqueueFuel(ItemStack stack) {
        ItemStack queuedUnit = stack.copyWithCount(1);

        for (ItemStack queuedStack : this.pendingFuel) {
            if (ItemStack.isSameItemSameComponents(queuedStack, queuedUnit)
                    && queuedStack.getCount()
                    < queuedStack.getMaxStackSize()) {
                queuedStack.grow(1);
                return;
            }
        }

        this.pendingFuel.add(queuedUnit);
    }

    private void startFuel(ItemStack fuelStack) {
        ResolvedFuel resolvedFuel = FuelCompatibility.resolve(fuelStack);

        if (resolvedFuel == null || resolvedFuel.isInvalid()) {
            return;
        }

        MoltenRotorBlockEntity.FuelType fuelType = resolvedFuel.type();

        this.activeFuelStack = fuelStack.copyWithCount(1);
        this.activeFuelType = fuelType;
        this.activeFuelCount = 1;
        this.activeLogStickCount = 0;
        this.activeLogStickStacks.clear();
        this.remainingBurnTime = (int) resolvedFuel.burnTimeTicks();
        this.baseHeatingRate = resolvedFuel.heatingRate();
        this.currentMaxTemp = resolvedFuel.maximumTemperature();

        this.clearFuelStackFlags();
        this.updateFuelStackFlags(fuelType);
    }

    private void startNextPendingFuel() {
        if (this.pendingFuel.isEmpty()) {
            return;
        }

        ItemStack queuedStack = this.pendingFuel.getFirst();
        ItemStack nextFuel = queuedStack.copyWithCount(1);

        queuedStack.shrink(1);

        if (queuedStack.isEmpty()) {
            this.pendingFuel.removeFirst();
        }

        this.startFuel(nextFuel);
    }

    private void clearActiveFuel() {
        this.activeFuelStack = ItemStack.EMPTY;
        this.activeFuelType = MoltenRotorBlockEntity.FuelType.NONE;
        this.activeFuelCount = 0;
        this.activeLogStickCount = 0;
        this.activeLogStickStacks.clear();
        this.baseHeatingRate = 0.0F;
        this.currentMaxTemp = 0.0F;
        this.clearFuelStackFlags();
    }

    public void clearAllFuel() {
        this.clearActiveFuel();
        this.pendingFuel.clear();
    }

    public List<ItemStack> drainPendingFuelForDrop() {
        List<ItemStack> drops = new ArrayList<>();

        for (ItemStack queuedStack : this.pendingFuel) {
            drops.add(queuedStack.copy());
        }

        this.pendingFuel.clear();
        this.furnace.markFuelStateChanged();
        return drops;
    }

    public void addBurnTime(int ticks) {
        this.remainingBurnTime += ticks;
    }

    private void updateFuelStackFlags(
            MoltenRotorBlockEntity.FuelType fuelType
    ) {
        if (fuelType == MoltenRotorBlockEntity.FuelType.COAL) {
            this.hasCoalInStack = true;
        } else if (fuelType
                == MoltenRotorBlockEntity.FuelType.CHARCOAL) {
            this.hasCharcoalInStack = true;
        } else if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
            this.hasStickInStack = true;
        }
    }

    private void clearFuelStackFlags() {
        this.hasLavaInStack = false;
        this.hasSulfurInStack = false;
        this.hasCoalInStack = false;
        this.hasCharcoalInStack = false;
        this.hasStickInStack = false;
    }

    private float calculateMaxStackedTemp() {
        return this.hasStickInStack
                && this.activeFuelType == MoltenRotorBlockEntity.FuelType.LOG
                ? 550.0F
                : this.currentMaxTemp;
    }

    public boolean hasFuelRemaining() {
        return this.remainingBurnTime > 0;
    }

    public int getRemainingBurnTime() {
        return this.remainingBurnTime;
    }

    public int getDisplayFuelTime() {
        return Math.max(this.remainingBurnTime, 0);
    }

    public boolean isFuelQueueEmpty() {
        return this.pendingFuel.isEmpty();
    }

    public ItemStack getRenderedFuelStack() {
        return this.activeFuelStack.copy();
    }


    public MoltenRotorBlockEntity.FuelType getRenderedFuelType() {
        return this.activeFuelType;
    }

    public List<ItemStack> getRenderedLogStickStacks() {
        List<ItemStack> renderedStacks = new ArrayList<>();

        for (int index = 0;
             index < this.activeLogStickStacks.size() && index < 4;
             index++) {
            renderedStacks.add(
                    this.activeLogStickStacks.get(index).copy()
            );
        }

        /* Preserve visuals for saves made before exact stick stacks existed. */
        while (renderedStacks.size()
                < Math.min(this.activeLogStickCount, 4)) {
            renderedStacks.add(
                    new ItemStack(net.minecraft.world.item.Items.STICK)
            );
        }

        return renderedStacks;
    }

    public int getRenderedFuelUnitCount() {
        if (this.activeFuelType == MoltenRotorBlockEntity.FuelType.NONE
                || this.remainingBurnTime <= 0) {
            return 0;
        }

        Level level = this.furnace.getLevel();

        if (level != null && level.isClientSide) {
            return this.renderedFuelUnitCount;
        }

        return this.getFuelUnitCount(this.activeFuelType);
    }

    public Component getActiveFuelDisplayName() {
        if (!this.activeFuelStack.isEmpty()) {
            return this.activeFuelStack.getHoverName();
        }

        return Component.literal(this.activeFuelType.getDisplayName());
    }

    private int getQueuedFuelCount() {
        int count = 0;

        for (ItemStack queuedStack : this.pendingFuel) {
            count += queuedStack.getCount();
        }

        return count;
    }

    public void addQueuedFuelTooltip(List<Component> tooltip) {
        if (this.pendingFuel.isEmpty()) {
            return;
        }

        tooltip.add(
                Component.literal(
                        "Queued Fuel: " + this.getQueuedFuelCount()
                ).withStyle(ChatFormatting.GRAY)
        );

        for (ItemStack queuedStack : this.pendingFuel) {
            tooltip.add(
                    Component.literal("  ")
                            .append(queuedStack.getHoverName())
                            .append(
                                    Component.literal(
                                            " ×" + queuedStack.getCount()
                                    )
                            )
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    public void write(
            @NotNull CompoundTag tag,
            @NotNull Provider provider
    ) {
        tag.putInt("FuelTime", this.remainingBurnTime);
        tag.putString("ActiveFuelType", this.activeFuelType.serializedId);

        if (!this.activeFuelStack.isEmpty()) {
            tag.put(
                    "ActiveFuelStack",
                    this.activeFuelStack.save(provider)
            );
        }

        tag.putInt("FuelCount", this.activeFuelCount);
        tag.putInt("ActiveLogStickCount", this.activeLogStickCount);

        ListTag activeLogStickTag = new ListTag();

        for (ItemStack stickStack : this.activeLogStickStacks) {
            activeLogStickTag.add(stickStack.save(provider));
        }

        tag.put("ActiveLogStickStacks", activeLogStickTag);
        tag.putInt(
                "RenderedFuelUnits",
                this.activeFuelType != MoltenRotorBlockEntity.FuelType.NONE
                        && this.remainingBurnTime > 0
                        ? this.getFuelUnitCount(this.activeFuelType)
                        : 0
        );

        ListTag pendingFuelTag = new ListTag();

        for (ItemStack queuedStack : this.pendingFuel) {
            pendingFuelTag.add(queuedStack.save(provider));
        }

        tag.put("PendingFuel", pendingFuelTag);
        tag.putFloat("HeatingRate", this.baseHeatingRate);
        tag.putFloat("MaxTemp", this.currentMaxTemp);
        tag.putBoolean("HasLava", this.hasLavaInStack);
        tag.putBoolean("HasStick", this.hasStickInStack);
        tag.putBoolean("HasSulfur", this.hasSulfurInStack);
        tag.putBoolean("HasCoal", this.hasCoalInStack);
        tag.putBoolean("HasCharcoal", this.hasCharcoalInStack);
    }

    public void read(
            @NotNull CompoundTag tag,
            @NotNull Provider provider
    ) {
        this.remainingBurnTime = tag.getInt("FuelTime");
        this.activeFuelCount = tag.getInt("FuelCount");
        this.activeLogStickCount = tag.getInt("ActiveLogStickCount");
        this.activeLogStickStacks.clear();

        if (tag.contains("ActiveLogStickStacks", Tag.TAG_LIST)) {
            ListTag activeLogStickTag =
                    tag.getList("ActiveLogStickStacks", Tag.TAG_COMPOUND);

            for (int index = 0; index < activeLogStickTag.size(); index++) {
                ItemStack stickStack = ItemStack.parseOptional(
                        provider,
                        activeLogStickTag.getCompound(index)
                );

                if (!stickStack.isEmpty()) {
                    this.activeLogStickStacks.add(stickStack);
                }
            }
        }

        this.renderedFuelUnitCount = tag.getInt("RenderedFuelUnits");
        this.pendingFuel.clear();

        if (tag.contains("PendingFuel", Tag.TAG_LIST)) {
            ListTag pendingFuelTag =
                    tag.getList("PendingFuel", Tag.TAG_COMPOUND);

            for (int index = 0; index < pendingFuelTag.size(); index++) {
                ItemStack queuedStack = ItemStack.parseOptional(
                        provider,
                        pendingFuelTag.getCompound(index)
                );

                if (!queuedStack.isEmpty()) {
                    this.pendingFuel.add(queuedStack);
                }
            }
        }

        this.activeFuelStack = ItemStack.EMPTY;

        if (tag.contains("ActiveFuelStack", Tag.TAG_COMPOUND)) {
            this.activeFuelStack = ItemStack.parseOptional(
                    provider,
                    tag.getCompound("ActiveFuelStack")
            );
        }

        this.baseHeatingRate = tag.getFloat("HeatingRate");
        this.currentMaxTemp = tag.getFloat("MaxTemp");
        this.hasLavaInStack = tag.getBoolean("HasLava");
        this.hasSulfurInStack = tag.getBoolean("HasSulfur");
        this.hasCoalInStack = tag.getBoolean("HasCoal");
        this.hasStickInStack = tag.getBoolean("HasStick");
        this.hasCharcoalInStack = tag.getBoolean("HasCharcoal");

        if (tag.contains("ActiveFuelType", Tag.TAG_STRING)) {
            this.activeFuelType =
                    MoltenRotorBlockEntity.FuelType.fromSerializedId(
                            tag.getString("ActiveFuelType")
                    );
        } else {
            int fuelIndex = tag.getInt("FuelType");
            this.activeFuelType = fuelIndex >= 0
                    && fuelIndex
                    < MoltenRotorBlockEntity.FuelType.values().length
                    ? MoltenRotorBlockEntity.FuelType.values()[fuelIndex]
                    : MoltenRotorBlockEntity.FuelType.NONE;
            this.pendingFuel.clear();
        }
    }
}
