package io.hxneyw.repo.content.blocks.sulfuricresonancechamber;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipe;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipeRegistry;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModFluids;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SulfuricResonanceChamberBlockEntity
        extends KineticBlockEntity {

    public static final int ACID_CAPACITY = 1000;
    public static final float MINIMUM_SPEED = 32.0F;

    private final NonNullList<ItemStack> inventory =
            NonNullList.withSize(2, ItemStack.EMPTY);

    private final SmartFluidTank sulfuricAcid =
            new SmartFluidTank(
                    ACID_CAPACITY,
                    ignored -> onContentsChanged()
            );

    private final IItemHandler itemCapability =
            new IItemHandler() {
                @Override
                public int getSlots() {
                    return inventory.size();
                }

                @Override
                public @NotNull ItemStack getStackInSlot(int slot) {
                    return inventory.get(slot);
                }

                @Override
                public @NotNull ItemStack insertItem(
                        int slot,
                        @NotNull ItemStack stack,
                        boolean simulate
                ) {
                    if (slot != 0 || stack.isEmpty()) {
                        return stack;
                    }

                    ItemStack current = inventory.get(slot);
                    if (!current.isEmpty()
                            && !ItemStack.isSameItemSameComponents(
                            current,
                            stack
                    )) {
                        return stack;
                    }

                    int accepted = Math.min(
                            stack.getCount(),
                            stack.getMaxStackSize() - current.getCount()
                    );
                    if (accepted <= 0) {
                        return stack;
                    }

                    if (!simulate) {
                        if (current.isEmpty()) {
                            inventory.set(slot, stack.copyWithCount(accepted));
                        } else {
                            current.grow(accepted);
                        }
                        onContentsChanged();
                    }

                    return stack.copyWithCount(stack.getCount() - accepted);
                }

                @Override
                public @NotNull ItemStack extractItem(
                        int slot,
                        int amount,
                        boolean simulate
                ) {
                    if (slot != 1 || amount <= 0) {
                        return ItemStack.EMPTY;
                    }

                    ItemStack current = inventory.get(slot);
                    int extracted = Math.min(amount, current.getCount());
                    if (extracted <= 0) {
                        return ItemStack.EMPTY;
                    }

                    ItemStack result = current.copyWithCount(extracted);
                    if (!simulate) {
                        current.shrink(extracted);
                        onContentsChanged();
                    }
                    return result;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return 64;
                }

                @Override
                public boolean isItemValid(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    return slot == 0;
                }
            };

    private final IFluidHandler fluidCapability =
            new IFluidHandler() {
                @Override
                public int getTanks() {
                    return sulfuricAcid.getTanks();
                }

                @Override
                public @NotNull FluidStack getFluidInTank(int tank) {
                    return sulfuricAcid.getFluidInTank(tank);
                }

                @Override
                public int getTankCapacity(int tank) {
                    return sulfuricAcid.getTankCapacity(tank);
                }

                @Override
                public boolean isFluidValid(
                        int tank,
                        @NotNull FluidStack stack
                ) {
                    return stack.getFluid()
                            == AllModFluids.SULFURIC_ACID.get()
                            || stack.getFluid()
                            == AllModFluids.SULFURIC_ACID_FLOWING.get();
                }

                @Override
                public int fill(
                        @NotNull FluidStack resource,
                        @NotNull FluidAction action
                ) {
                    if (!isFluidValid(0, resource)) {
                        return 0;
                    }
                    return sulfuricAcid.fill(resource, action);
                }

                @Override
                public @NotNull FluidStack drain(
                        @NotNull FluidStack resource,
                        @NotNull FluidAction action
                ) {
                    return FluidStack.EMPTY;
                }

                @Override
                public @NotNull FluidStack drain(
                        int maxDrain,
                        @NotNull FluidAction action
                ) {
                    return FluidStack.EMPTY;
                }
            };

    private MoltenRotorBlockEntity.RotorHeatLevel heatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    private int temperature;
    private int processingTicks;
    private boolean ready;
    private boolean processing;

    public SulfuricResonanceChamberBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.SULFURIC_RESONANCE_CHAMBER.get(),
                position,
                state
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        updateHeat();

        boolean previousReady = ready;
        boolean previousProcessing = processing;

        ready = hasSufficientHeat()
                && Math.abs(getSpeed()) >= MINIMUM_SPEED
                && !sulfuricAcid.getFluid().isEmpty();

        RecipeHolder<SulfuricResonanceChamberRecipe> holder =
                findActiveRecipe();

        if (holder == null) {
            processingTicks = 0;
            processing = false;
        } else {
            SulfuricResonanceChamberRecipe recipe = holder.value();
            processing = true;
            processingTicks++;

            if (processingTicks >= recipe.processingTime()) {
                completeRecipe(recipe);
                processingTicks = 0;
                processing = false;
            }
        }

        if (previousReady != ready
                || previousProcessing != processing) {
            setChanged();
            sendData();
        }
    }

    private void updateHeat() {
        io.hxneyw.repo.content.blocks.thermochemicalconduit
                .ThermochemicalHeatResolver.Result result =
                io.hxneyw.repo.content.blocks.thermochemicalconduit
                        .ThermochemicalHeatResolver.resolve(this);

        heatTier = result.heatTier();
        temperature = result.temperature();
    }

    private @Nullable RecipeHolder<SulfuricResonanceChamberRecipe>
    findActiveRecipe() {
        if (!ready || level == null) {
            return null;
        }

        return level.getRecipeManager()
                .getAllRecipesFor(
                        SulfuricResonanceChamberRecipeRegistry.TYPE.get()
                )
                .stream()
                .filter(holder -> holder.value().matches(
                        inventory.get(0),
                        sulfuricAcid.getFluid(),
                        heatTier,
                        Math.abs(getSpeed())
                ))
                .filter(holder -> canAcceptOutput(holder.value().result()))
                .findFirst()
                .orElse(null);
    }

    private boolean canAcceptOutput(
            ItemStack result
    ) {
        ItemStack output = inventory.get(1);
        return output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount()
                <= output.getMaxStackSize();
    }

    private void completeRecipe(
            SulfuricResonanceChamberRecipe recipe
    ) {
        ItemStack input = inventory.get(0);
        ItemStack output = inventory.get(1);
        ItemStack result = recipe.result().copy();

        if (!recipe.matches(
                input,
                sulfuricAcid.getFluid(),
                heatTier,
                Math.abs(getSpeed())
        )) {
            return;
        }

        if (!output.isEmpty()
                && (!ItemStack.isSameItemSameComponents(output, result)
                || output.getCount() + result.getCount()
                > output.getMaxStackSize())) {
            return;
        }

        FluidStack drained = sulfuricAcid.drain(
                recipe.acidAmount(),
                FluidAction.EXECUTE
        );
        if (drained.getAmount() != recipe.acidAmount()) {
            return;
        }

        input.shrink(1);
        if (output.isEmpty()) {
            inventory.set(1, result);
        } else {
            output.grow(result.getCount());
        }
        onContentsChanged();
    }

    private boolean hasSufficientHeat() {
        return heatTier.rank >= 3;
    }

    private void onContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            sendData();
        }
    }

    public IItemHandler getItemCapability() {
        return itemCapability;
    }

    public @Nullable IFluidHandler getFluidCapability(
            @Nullable net.minecraft.core.Direction side
    ) {
        // TEMPORARY DIAGNOSTIC: accept acid from any horizontal side.
        // Restore the side-specific check after identifying the correct port.
        return side == null || side.getAxis().isHorizontal()
                ? fluidCapability
                : null;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isProcessing() {
        return processing;
    }

    public int getProcessingTicks() {
        return processingTicks;
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable(
                "block.sulfuricresonance.sulfuric_resonance_chamber"
        ));
        tooltip.add(Component.translatable(
                "tooltip.sulfuricresonance.sulfuric_resonance_chamber.heat",
                Component.translatable(
                        "tooltip.sulfuricresonance.thermochemical.heat."
                                + heatTier.serializedId
                )
        ));
        tooltip.add(Component.translatable(
                "tooltip.sulfuricresonance.sulfuric_resonance_chamber.speed",
                Math.round(Math.abs(getSpeed()))
        ));
        tooltip.add(Component.translatable(
                "tooltip.sulfuricresonance.sulfuric_resonance_chamber.acid",
                sulfuricAcid.getFluidAmount(),
                ACID_CAPACITY
        ));
        tooltip.add(Component.translatable(
                ready
                        ? "tooltip.sulfuricresonance.sulfuric_resonance_chamber.ready"
                        : "tooltip.sulfuricresonance.sulfuric_resonance_chamber.waiting"
        ));
        return true;
    }

    @Override
    protected void write(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.write(tag, provider, clientPacket);
        ContainerHelper.saveAllItems(tag, inventory, provider);
        tag.put(
                "SulfuricAcid",
                sulfuricAcid.writeToNBT(provider, new CompoundTag())
        );
        tag.putString("HeatTier", heatTier.serializedId);
        tag.putInt("Temperature", temperature);
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putBoolean("Ready", ready);
        tag.putBoolean("Processing", processing);
    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);
        ContainerHelper.loadAllItems(tag, inventory, provider);
        sulfuricAcid.readFromNBT(
                provider,
                tag.getCompound("SulfuricAcid")
        );
        heatTier = MoltenRotorBlockEntity.RotorHeatLevel
                .fromSerializedId(tag.getString("HeatTier"));
        temperature = tag.getInt("Temperature");
        processingTicks = tag.getInt("ProcessingTicks");
        ready = tag.getBoolean("Ready");
        processing = tag.getBoolean("Processing");
    }
}
