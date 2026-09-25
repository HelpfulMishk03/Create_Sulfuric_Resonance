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
    public static final int SLOT_COUNT = 3;
    public static final int ACID_CAPACITY = 3500;
    private static final float STRESS_PER_RPM = 2.0F;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final EnumMap<Direction, IItemHandler> sidedItemCapabilities = new EnumMap<>(Direction.class);
    private final SmartFluidTank acidTank = new SmartFluidTank(ACID_CAPACITY, ignored -> contentsChanged());
    private UUID processIdentity = UUID.randomUUID();
    private int processingTicks;
    private int processingTime;
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
            if ((slot != OUTPUT && slot != BYPRODUCT) || amount <= 0) return ItemStack.EMPTY;
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
            activeRecipeId = null;
            return;
        }
        RotaryLeachingRecipe value = recipe.value();
        if (!outputsFit(value) || inventory.get(INPUT).getCount() < value.inputCount() || !hasAcid(value)) {
            processingTicks = 0;
            processingTime = value.processingTime();
            activeRecipeId = recipe.id();
            return;
        }
        if (!recipe.id().equals(activeRecipeId)) {
            activeRecipeId = recipe.id();
            processingTicks = 0;
            processingTime = value.processingTime();
        }
        processingTicks++;
        if (processingTicks >= value.processingTime()) completeBatch(value);
        if ((level.getGameTime() & 7) == 0) sendData();
    }

    private @Nullable RecipeHolder<RotaryLeachingRecipe> findRecipe() {
        if (level == null) return null;
        return level.getRecipeManager().getAllRecipesFor(RotaryLeachingRecipeRegistry.TYPE.get()).stream()
                .filter(holder -> holder.value().ingredient().test(inventory.get(INPUT)))
                .filter(holder -> activeRecipeId == null || activeRecipeId.equals(holder.id()))
                .findFirst().orElse(null);
    }

    private boolean hasAcid(RotaryLeachingRecipe recipe) {
        FluidStack fluid = acidTank.getFluid();
        return fluid.getFluid() == AllModFluids.SULFURIC_ACID.get() && fluid.getAmount() >= recipe.fluidAmount();
    }

    private boolean outputsFit(RotaryLeachingRecipe recipe) {
        return fits(OUTPUT, recipe.result()) && fits(BYPRODUCT, recipe.byproduct());
    }

    private boolean fits(int slot, ItemStack result) {
        ItemStack current = inventory.get(slot);
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, result)
                && current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private void completeBatch(RotaryLeachingRecipe recipe) {
        inventory.get(INPUT).shrink(recipe.inputCount());
        if (inventory.get(INPUT).isEmpty()) inventory.set(INPUT, ItemStack.EMPTY);
        merge(OUTPUT, recipe.result());
        merge(BYPRODUCT, recipe.byproduct());
        acidTank.drain(recipe.fluidAmount(), IFluidHandler.FluidAction.EXECUTE);
        processingTicks = 0;
        contentsChanged();
    }

    private void merge(int slot, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (inventory.get(slot).isEmpty()) inventory.set(slot, stack.copy()); else inventory.get(slot).grow(stack.getCount());
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
                || side != getBlockState().getValue(RotaryLeacherBlock.HORIZONTAL_FACING).getClockWise()) return null;
        BlockEntity master = level.getBlockEntity(worldPosition.below());
        return master instanceof RotaryLeacherBlockEntity leacher ? leacher.getInternalFluidCapability() : null;
    }
    public IFluidHandler getInternalFluidCapability() { return fluidCapability; }
    public int getProcessingTime() { return processingTime; }
    public float getOperatingSpeed() { return getSpeed(); }

    @Override public @NotNull ProcessState getProcessState() {
        if (getBlockState().hasProperty(RotaryLeacherBlock.HALF)
                && getBlockState().getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) {
            BlockEntity master = level == null ? null : level.getBlockEntity(worldPosition.below());
            return master instanceof RotaryLeacherBlockEntity leacher ? leacher.getProcessState() : ProcessState.IDLE;
        }

        RecipeHolder<RotaryLeachingRecipe> recipe = findRecipe();
        if (recipe != null) {
            RotaryLeachingRecipe value = recipe.value();
            boolean canRun = inventory.get(INPUT).getCount() >= value.inputCount()
                    && hasAcid(value)
                    && Math.abs(getOperatingSpeed()) >= value.minimumSpeed()
                    && outputsFit(value);
            return canRun ? ProcessState.PROCESSING : ProcessState.BLOCKED;
        }

        return inventory.get(OUTPUT).isEmpty() && inventory.get(BYPRODUCT).isEmpty()
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

    public int getStressCost() { return Math.round(Math.abs(getOperatingSpeed()) * STRESS_PER_RPM); }
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
        tooltip.add(Component.translatable("tooltip.sulfuricresonance.rotary_leacher.su_cost", getStressCost()));
        if (processingTime > 0) {
            tooltip.add(Component.translatable("tooltip.sulfuricresonance.rotary_leacher.progress", processingTicks, processingTime));
        }
        return true;
    }
    public List<ItemStack> getDropsForRemoval() {
        java.util.ArrayList<ItemStack> drops = new java.util.ArrayList<>();
        for (ItemStack stack : inventory) if (!stack.isEmpty()) drops.add(stack.copy());
        return drops;
    }

    public void clearItemsForCreativeBreak() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) inventory.set(slot, ItemStack.EMPTY);
        setChanged();
    }

    @Override protected void write(CompoundTag tag, Provider provider, boolean clientPacket) {
        super.write(tag, provider, clientPacket);
        ContainerHelper.saveAllItems(tag, inventory, provider);
        tag.put("SulfuricAcid", acidTank.writeToNBT(provider, new CompoundTag()));
        tag.putInt("RotaryLeacherProgress", processingTicks);
        tag.putInt("RotaryLeacherTime", processingTime);
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
        activeRecipeId = tag.contains("RotaryLeacherRecipe") ? net.minecraft.resources.ResourceLocation.tryParse(tag.getString("RotaryLeacherRecipe")) : null;
        if (tag.hasUUID("RotaryLeacherProcessIdentity")) processIdentity = tag.getUUID("RotaryLeacherProcessIdentity");
    }

    private void contentsChanged() { setChanged(); if (level != null && !level.isClientSide) sendData(); }
}
