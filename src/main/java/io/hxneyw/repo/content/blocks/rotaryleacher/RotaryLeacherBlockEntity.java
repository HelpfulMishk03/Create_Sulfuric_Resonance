package io.hxneyw.repo.content.blocks.rotaryleacher;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.hxneyw.repo.content.recipes.rotaryleaching.RotaryLeachingRecipe;
import io.hxneyw.repo.content.recipes.rotaryleaching.RotaryLeachingRecipeRegistry;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.process.IProcessStateProvider;
import io.hxneyw.repo.content.process.ProcessState;
import java.util.List;
import java.util.EnumMap;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RotaryLeacherBlockEntity extends KineticBlockEntity implements IProcessStateProvider {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int BYPRODUCT = 2;
    public static final int OUTPUT_OVERFLOW = 3;
    public static final int SLOT_COUNT = 4;
    public static final int ACID_CAPACITY = 8500;
    public static final float MAX_ACID_SURFACE_FRACTION = 0.32F;
    private static final float STRESS_PER_RPM = 2.0F;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final EnumMap<Direction, IItemHandler> sidedItemCapabilities = new EnumMap<>(Direction.class);
    private final SmartFluidTank acidTank = new SmartFluidTank(ACID_CAPACITY, ignored -> contentsChanged());
    private UUID processIdentity = UUID.randomUUID();
    private int processingTicks;
    private int processingTime;
    private int processingBatchMultiplier;
    private int processingAcidAmount;
    private @Nullable net.minecraft.resources.ResourceLocation activeRecipeId;

    private final IItemHandler itemCapability = new IItemHandler() {
        @Override public int getSlots() { return SLOT_COUNT; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < SLOT_COUNT ? inventory.get(slot) : ItemStack.EMPTY; }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != INPUT || stack.isEmpty() || !acceptsInput(stack)) return stack;
            ItemStack current = inventory.get(INPUT);
            if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) return stack;
            int count = current.isEmpty() ? 0 : current.getCount();
            int accepted = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), 64) - count);
            if (accepted <= 0) return stack;
            if (!simulate) { if (current.isEmpty()) inventory.set(INPUT, stack.copyWithCount(accepted)); else current.grow(accepted); contentsChanged(); }
            return stack.copyWithCount(stack.getCount() - accepted);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if ((slot != OUTPUT && slot != BYPRODUCT && slot != OUTPUT_OVERFLOW) || amount <= 0) return ItemStack.EMPTY;
            ItemStack current = inventory.get(slot);
            if (current.isEmpty()) return ItemStack.EMPTY;
            int count = Math.min(amount, current.getCount());
            ItemStack result = current.copyWithCount(count);
            if (!simulate) { current.shrink(count); if (current.isEmpty()) inventory.set(slot, ItemStack.EMPTY); contentsChanged(); }
            return result;
        }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return slot == INPUT && acceptsInput(stack); }
    };

    private boolean acceptsInput(ItemStack stack) {
        return level != null && level.getRecipeManager().getAllRecipesFor(RotaryLeachingRecipeRegistry.TYPE.get()).stream()
                .anyMatch(holder -> holder.value().ingredient().test(stack));
    }

    private final IFluidHandler fluidCapability = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return tank == 0 ? acidTank.getFluid() : FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return tank == 0 ? ACID_CAPACITY : 0; }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return tank == 0 && stack.getFluid() == AllModFluids.SULFURIC_ACID.get(); }
        @Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) { return isFluidValid(0, resource) ? acidTank.fill(resource, action) : 0; }
        @Override public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) { return FluidStack.EMPTY; }
        @Override public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) { return FluidStack.EMPTY; }
    };

    public RotaryLeacherBlockEntity(BlockPos pos, BlockState state) { this(AllBlockEntities.ROTARY_LEACHER.get(), pos, state); }

    public RotaryLeacherBlockEntity(BlockEntityType<? extends RotaryLeacherBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public float calculateStressApplied() {
        if (getBlockState().getValue(RotaryLeacherBlock.HALF) != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) return 0;
        return STRESS_PER_RPM;
    }

    @Override public void tick() {
        super.tick();
        if (level == null || level.isClientSide
                || getBlockState().getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) return;
        RecipeHolder<RotaryLeachingRecipe> recipe = findRecipe();
        if (recipe == null || Math.abs(getOperatingSpeed()) < recipe.value().minimumSpeed()) {
            processingTicks = 0;
            processingTime = 0;
            processingBatchMultiplier = 0;
            processingAcidAmount = 0;
            activeRecipeId = null;
            return;
        }
        RotaryLeachingRecipe value = recipe.value();
        if (processingTicks == 0) {
            BatchPlan plan = findBatchPlan(value);
            if (plan == null) {
                processingTime = value.processingTime();
                processingBatchMultiplier = 0;
                processingAcidAmount = 0;
                activeRecipeId = null;
                return;
            }
            activeRecipeId = recipe.id();
            processingBatchMultiplier = plan.multiplier();
            processingAcidAmount = plan.acidAmount();
            processingTime = plan.processingTime();
        }
        processingTicks++;
        if (processingTicks >= processingTime) completeBatch(value);
        if ((level.getGameTime() & 7) == 0) sendData();
    }

    private @Nullable RecipeHolder<RotaryLeachingRecipe> findRecipe() {
        if (level == null) return null;
        List<RecipeHolder<RotaryLeachingRecipe>> matching = level.getRecipeManager().getAllRecipesFor(RotaryLeachingRecipeRegistry.TYPE.get()).stream()
                .filter(holder -> holder.value().ingredient().test(inventory.get(INPUT)))
                .toList();
        if (processingTicks > 0 && activeRecipeId != null) {
            RecipeHolder<RotaryLeachingRecipe> active = matching.stream()
                    .filter(holder -> activeRecipeId.equals(holder.id()))
                    .findFirst().orElse(null);
            if (active != null) return active;
        }
        return matching.stream().findFirst().orElse(null);
    }

    private @Nullable BatchPlan findBatchPlan(RotaryLeachingRecipe recipe) {
        FluidStack fluid = acidTank.getFluid();
        if (fluid.getFluid() != AllModFluids.SULFURIC_ACID.get()) return null;
        int maxMultiplier = Math.min(16, inventory.get(INPUT).getCount() / recipe.inputCount());
        for (int multiplier = maxMultiplier; multiplier >= 1; multiplier--) {
            int acidAmount = acidCost(multiplier);
            if (fluid.getAmount() < acidAmount) continue;
            ItemStack result = scaled(recipe.result(), multiplier);
            ItemStack byproduct = scaled(recipe.byproduct(), multiplier);
            if (outputsFit(result, byproduct)) {
                return new BatchPlan(multiplier, acidAmount, processingTime(multiplier));
            }
        }
        return null;
    }

    private static ItemStack scaled(ItemStack stack, int multiplier) {
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() * multiplier);
    }

    private boolean outputsFit(ItemStack result, ItemStack byproduct) {
        return fitsAcrossOutputSlots(result) && fitsByproduct(byproduct);
    }

    private int acidCost(int multiplier) {
        return interpolate(multiplier, new int[] {1, 2, 4, 8, 16}, new int[] {750, 1400, 2600, 4800, 8500});
    }

    private int processingTime(int multiplier) {
        return interpolate(multiplier, new int[] {1, 2, 4, 8, 16}, new int[] {180, 320, 560, 960, 1500});
    }

    private static int interpolate(int multiplier, int[] sizes, int[] values) {
        for (int i = 1; i < sizes.length; i++) {
            if (multiplier <= sizes[i]) {
                float fraction = (float) (multiplier - sizes[i - 1]) / (sizes[i] - sizes[i - 1]);
                return Math.round(values[i - 1] + fraction * (values[i] - values[i - 1]));
            }
        }
        return values[values.length - 1];
    }

    private boolean fitsAcrossOutputSlots(ItemStack result) {
        int remaining = result.getCount();
        for (int slot : new int[] {OUTPUT, OUTPUT_OVERFLOW}) {
            ItemStack current = inventory.get(slot);
            if (current.isEmpty()) {
                remaining -= result.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(current, result)) {
                remaining -= current.getMaxStackSize() - current.getCount();
            }
        }
        return remaining <= 0;
    }

    private boolean fitsByproduct(ItemStack result) {
        ItemStack current = inventory.get(BYPRODUCT);
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, result)
                && current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private void completeBatch(RotaryLeachingRecipe recipe) {
        int multiplier = Math.max(1, processingBatchMultiplier);
        inventory.get(INPUT).shrink(recipe.inputCount() * multiplier);
        if (inventory.get(INPUT).isEmpty()) inventory.set(INPUT, ItemStack.EMPTY);
        mergeAcrossOutputSlots(scaled(recipe.result(), multiplier));
        mergeByproduct(scaled(recipe.byproduct(), multiplier));
        acidTank.drain(processingAcidAmount, IFluidHandler.FluidAction.EXECUTE);
        processingTicks = 0;
        processingBatchMultiplier = 0;
        processingAcidAmount = 0;
        activeRecipeId = null;
        contentsChanged();
    }

    private void mergeAcrossOutputSlots(ItemStack result) {
        int remaining = result.getCount();
        for (int slot : new int[] {OUTPUT, OUTPUT_OVERFLOW}) {
            ItemStack current = inventory.get(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, result)) {
                int moved = Math.min(remaining, current.getMaxStackSize() - current.getCount());
                current.grow(moved);
                remaining -= moved;
            }
        }
        for (int slot : new int[] {OUTPUT, OUTPUT_OVERFLOW}) {
            if (remaining <= 0) break;
            if (inventory.get(slot).isEmpty()) {
                int moved = Math.min(remaining, result.getMaxStackSize());
                inventory.set(slot, result.copyWithCount(moved));
                remaining -= moved;
            }
        }
    }

    private void mergeByproduct(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (inventory.get(BYPRODUCT).isEmpty()) inventory.set(BYPRODUCT, stack.copy()); else inventory.get(BYPRODUCT).grow(stack.getCount());
    }

    public @Nullable IItemHandler getItemCapability(@Nullable Direction side) {
        if (level != null && getBlockState().hasProperty(RotaryLeacherBlock.HALF)
                && getBlockState().getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
            BlockEntity master = level.getBlockEntity(worldPosition.below());
            if (master instanceof RotaryLeacherBlockEntity leacher) return leacher.getItemCapability(side);
            return null;
        }
        return side == null ? createSidedItemCapability(null) : sidedItemCapabilities.computeIfAbsent(side, this::createSidedItemCapability);
    }
    public IItemHandler getManualItemCapability() { return itemCapability; }
    public void completePonderBatch() {
        if (level == null || getBlockState().getValue(RotaryLeacherBlock.HALF)
                != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) return;
        RecipeHolder<RotaryLeachingRecipe> recipe = findRecipe();
        if (recipe == null) return;
        RotaryLeachingRecipe value = recipe.value();
        BatchPlan plan = findBatchPlan(value);
        if (Math.abs(getOperatingSpeed()) < value.minimumSpeed() || plan == null) return;
        activeRecipeId = recipe.id();
        processingBatchMultiplier = plan.multiplier();
        processingAcidAmount = plan.acidAmount();
        processingTime = plan.processingTime();
        processingTicks = processingTime;
        completeBatch(value);
    }
    public ItemStack extractInputForManual() {
        ItemStack input = inventory.get(INPUT);
        if (input.isEmpty()) return ItemStack.EMPTY;
        ItemStack extracted = input.copy();
        inventory.set(INPUT, ItemStack.EMPTY);
        processingTicks = 0;
        processingTime = 0;
        processingBatchMultiplier = 0;
        processingAcidAmount = 0;
        activeRecipeId = null;
        contentsChanged();
        return extracted;
    }
    private IItemHandler createSidedItemCapability(@Nullable Direction side) {
        return new IItemHandler() {
            @Override public int getSlots() { return itemCapability.getSlots(); }
            @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemCapability.getStackInSlot(slot); }
            @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return itemCapability.insertItem(slot, stack, simulate); }
            @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                return side != null && side.getAxis() != Direction.Axis.Y && isGlassExtractionSide(side)
                        ? itemCapability.extractItem(slot, amount, simulate)
                        : ItemStack.EMPTY;
            }
            @Override public int getSlotLimit(int slot) { return itemCapability.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return itemCapability.isItemValid(slot, stack); }
        };
    }
    private boolean isGlassExtractionSide(Direction side) {
        if (level == null || !getBlockState().hasProperty(RotaryLeacherBlock.FACING)) return false;
        return side != getBlockState().getValue(RotaryLeacherBlock.FACING);
    }
    public @Nullable IFluidHandler getFluidCapability(@Nullable Direction side) {
        if (level == null || side == null || !getBlockState().hasProperty(RotaryLeacherBlock.HALF)
                || getBlockState().getValue(RotaryLeacherBlock.HALF) != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER
                || side != getBlockState().getValue(RotaryLeacherBlock.HORIZONTAL_FACING).getCounterClockWise()) return null;
        BlockEntity master = level.getBlockEntity(worldPosition.below());
        return master instanceof RotaryLeacherBlockEntity leacher ? leacher.getInternalFluidCapability() : null;
    }
    public IFluidHandler getInternalFluidCapability() { return fluidCapability; }
    public int getProcessingTime() { return processingTime; }
    public float getProcessingProgress() { return processingTime <= 0 ? 0 : Math.min(1.0F, (float) processingTicks / processingTime); }
    public ItemStack getVisibleProcessOutput() {
        RecipeHolder<RotaryLeachingRecipe> recipe = processingTicks > 0 ? findRecipe() : null;
        if (recipe != null) return scaled(recipe.value().result(), processingBatchMultiplier);
        return inventory.get(OUTPUT).isEmpty() ? inventory.get(OUTPUT_OVERFLOW).copy() : inventory.get(OUTPUT).copy();
    }
    public float getOperatingSpeed() { return getSpeed(); }

    @Override public @NotNull ProcessState getProcessState() {
        if (getBlockState().hasProperty(RotaryLeacherBlock.HALF)
                && getBlockState().getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
            BlockEntity master = level == null ? null : level.getBlockEntity(worldPosition.below());
            return master instanceof RotaryLeacherBlockEntity leacher ? leacher.getProcessState() : ProcessState.IDLE;
        }

        RecipeHolder<RotaryLeachingRecipe> recipe = findRecipe();
        if (recipe != null) {
            boolean canRun = processingTicks > 0
                    ? processingBatchMultiplier > 0 && Math.abs(getOperatingSpeed()) >= recipe.value().minimumSpeed()
                    : findBatchPlan(recipe.value()) != null && Math.abs(getOperatingSpeed()) >= recipe.value().minimumSpeed();
            return canRun ? ProcessState.PROCESSING : ProcessState.BLOCKED;
        }

        return inventory.get(OUTPUT).isEmpty() && inventory.get(OUTPUT_OVERFLOW).isEmpty() && inventory.get(BYPRODUCT).isEmpty()
                ? ProcessState.IDLE
                : ProcessState.READY;
    }

    @Override public @NotNull UUID getProcessIdentity() {
        if (getBlockState().hasProperty(RotaryLeacherBlock.HALF)
                && getBlockState().getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
            BlockEntity master = level == null ? null : level.getBlockEntity(worldPosition.below());
            if (master instanceof RotaryLeacherBlockEntity leacher) return leacher.getProcessIdentity();
        }
        return processIdentity;
    }

    private static int legacyInputCount(@Nullable net.minecraft.resources.ResourceLocation recipeId) {
        if (recipeId == null) return 4;
        String path = recipeId.getPath();
        int separator = path.lastIndexOf('_');
        if (separator < 0) return 4;
        try {
            return Integer.parseInt(path.substring(separator + 1));
        } catch (NumberFormatException ignored) {
            return 4;
        }
    }

    public int getAcidAmount() { return acidTank.getFluidAmount(); }
    public int getAcidCapacity() { return ACID_CAPACITY; }
    public FluidStack getAcidFluid() { return acidTank.getFluid(); }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("block.sulfuricresonance.rotary_leacher"));
        tooltip.add(Component.translatable("tooltip.sulfuricresonance.rotary_leacher.speed", Math.round(Math.abs(getOperatingSpeed()))));
        tooltip.add(Component.translatable("tooltip.sulfuricresonance.rotary_leacher.acid", getAcidAmount(), ACID_CAPACITY));
        if (processingTime > 0) {
            tooltip.add(Component.translatable("tooltip.sulfuricresonance.rotary_leacher.progress", processingTicks, processingTime));
        }
        return true;
    }
    public List<ItemStack> takeDropsForRemoval() {
        java.util.ArrayList<ItemStack> drops = new java.util.ArrayList<>();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) continue;
            drops.add(stack.copy());
            inventory.set(slot, ItemStack.EMPTY);
        }
        if (!drops.isEmpty()) contentsChanged();
        return drops;
    }

    public void clearItemsForCreativeBreak() {
        takeDropsForRemoval();
    }

    @Override protected void write(CompoundTag tag, Provider provider, boolean clientPacket) {
        super.write(tag, provider, clientPacket);
        ContainerHelper.saveAllItems(tag, inventory, provider);
        tag.put("SulfuricAcid", acidTank.writeToNBT(provider, new CompoundTag()));
        tag.putInt("RotaryLeacherProgress", processingTicks);
        tag.putInt("RotaryLeacherTime", processingTime);
        tag.putInt("RotaryLeacherBatchMultiplier", processingBatchMultiplier);
        tag.putInt("RotaryLeacherBatchAcid", processingAcidAmount);
        if (activeRecipeId != null) tag.putString("RotaryLeacherRecipe", activeRecipeId.toString());
        tag.putUUID("RotaryLeacherProcessIdentity", processIdentity);
    }

    @Override protected void read(CompoundTag tag, Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        for (int slot = 0; slot < SLOT_COUNT; slot++) inventory.set(slot, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inventory, provider);
        acidTank.readFromNBT(provider, tag.getCompound("SulfuricAcid"));
        processingTicks = Math.max(0, tag.getInt("RotaryLeacherProgress"));
        processingTime = Math.max(0, tag.getInt("RotaryLeacherTime"));
        processingBatchMultiplier = Math.max(0, tag.getInt("RotaryLeacherBatchMultiplier"));
        processingAcidAmount = Math.max(0, tag.getInt("RotaryLeacherBatchAcid"));
        activeRecipeId = tag.contains("RotaryLeacherRecipe") ? net.minecraft.resources.ResourceLocation.tryParse(tag.getString("RotaryLeacherRecipe")) : null;
        if (processingTicks > 0 && processingBatchMultiplier == 0) {
            int oldTime = processingTime;
            int legacyInputCount = legacyInputCount(activeRecipeId);
            processingBatchMultiplier = Math.clamp(legacyInputCount / 4, 1, 16);
            processingAcidAmount = acidCost(processingBatchMultiplier);
            processingTime = processingTime(processingBatchMultiplier);
            if (oldTime > 0) processingTicks = Math.round((float) processingTicks * processingTime / oldTime);
            if (acidTank.getFluidAmount() < processingAcidAmount) {
                processingTicks = 0;
                processingBatchMultiplier = 0;
                processingAcidAmount = 0;
                activeRecipeId = null;
            }
        }
        if (tag.hasUUID("RotaryLeacherProcessIdentity")) processIdentity = tag.getUUID("RotaryLeacherProcessIdentity");
    }

    private void contentsChanged() { setChanged(); if (level != null && !level.isClientSide) sendData(); }

    private record BatchPlan(int multiplier, int acidAmount, int processingTime) {}
}
