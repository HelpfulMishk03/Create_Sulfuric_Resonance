package io.hxneyw.repo.content.blocks.sulfuricresonancechamber;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipe;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipeRegistry;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SulfuricResonanceChamberBlockEntity extends KineticBlockEntity implements Container {

    public static final int ACID_CAPACITY = 1500;
    public static final int INPUT_1 = 0;
    public static final int INPUT_2 = 1;
    public static final int INPUT_3 = 2;
    public static final int OUTPUT = 3;
    public static final int SLOT_COUNT = 4;

    public static final int MENU_DATA_COUNT = 10;

    private final NonNullList<ItemStack> inventory =
            NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);


    private final SmartFluidTank sulfuricAcid = new SmartFluidTank(
            ACID_CAPACITY,
            ignored -> onContentsChanged()
    );

    private final IItemHandler itemCapability = new IItemHandler() {
        @Override
        public int getSlots() {
            return inventory.size();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= inventory.size()) {
                return ItemStack.EMPTY;
            }
            return inventory.get(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(
                int slot,
                @NotNull ItemStack stack,
                boolean simulate
        ) {
            if (slot < INPUT_1
                    || slot > INPUT_3
                    || stack.isEmpty()
                    || !isItemValid(slot, stack)) {
                return stack;
            }

            ItemStack current = inventory.get(slot);
            if (!current.isEmpty()
                    && !ItemStack.isSameItemSameComponents(current, stack)) {
                return stack;
            }

            int currentCount = current.isEmpty() ? 0 : current.getCount();
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            int accepted = Math.min(stack.getCount(), limit - currentCount);
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

            if (slot != OUTPUT || amount <= 0) {
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
                if (current.isEmpty()) {
                    inventory.set(slot, ItemStack.EMPTY);
                }
                onContentsChanged();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < INPUT_1 || slot > INPUT_3 || stack.isEmpty()) {
                return false;
            }
            return canInsertIntoInput(slot, stack);
        }
    };

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return inventory.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = ContainerHelper.removeItem(inventory, slot, amount);
        if (!removed.isEmpty()) {
            onContentsChanged();
        }
        return removed;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = ContainerHelper.takeItem(inventory, slot);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        ItemStack stored = stack.copy();
        if (stored.getCount() > 64) {
            stored.setCount(64);
        }
        inventory.set(slot, stored);
        onContentsChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            inventory.set(slot, ItemStack.EMPTY);
        }
        onContentsChanged();
    }

    private final IFluidHandler fluidCapability = new IFluidHandler() {
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
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return stack.getFluid() == AllModFluids.SULFURIC_ACID.get()
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
    private int processingTime;
    private boolean ready;
    private boolean processing;
    private ChamberStatus status = ChamberStatus.IDLE;

    private static final float RING_DEGREES_PER_TICK = 3.0F;
    private float clientPreviousRingAngle;
    private float clientRingAngle;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> processingTicks;
                case 1 -> processingTime;
                case 2 -> ready ? 1 : 0;
                case 3 -> processing ? 1 : 0;
                case 4 -> sulfuricAcid.getFluidAmount();
                case 5 -> ACID_CAPACITY;
                case 6 -> Math.round(Math.abs(getSpeed()));
                case 7 -> temperature;
                case 8 -> getDisplayHeatBand(heatTier);
                case 9 -> status.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {

        }

        @Override
        public int getCount() {
            return MENU_DATA_COUNT;
        }
    };

    public SulfuricResonanceChamberBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(AllBlockEntities.SULFURIC_RESONANCE_CHAMBER.get(), position, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) {
            return;
        }

        if (level.isClientSide) {
            clientPreviousRingAngle = clientRingAngle;
            if (processing) {
                clientRingAngle += RING_DEGREES_PER_TICK;
                if (clientRingAngle >= 360.0F) {
                    clientRingAngle -= 360.0F;
                    clientPreviousRingAngle -= 360.0F;
                }
            }
            return;
        }

        MoltenRotorBlockEntity.RotorHeatLevel previousHeatTier = heatTier;
        int previousTemperature = temperature;
        updateHeat();

        boolean previousReady = ready;
        boolean previousProcessing = processing;
        ChamberStatus previousStatus = status;

        RecipeHolder<SulfuricResonanceChamberRecipe> inputHolder =
                findInputRecipe();

        if (inputHolder == null) {
            processingTime = 0;
            ready = false;
            processing = false;
            if (!hasAnyInput()) {
                status = ChamberStatus.IDLE;
            } else if (hasPotentialInputRecipe()) {
                status = ChamberStatus.MISSING_INGREDIENTS;
            } else {
                status = ChamberStatus.NO_VALID_RECIPE;
            }
            resetInterruptedProgress();
        } else {
            SulfuricResonanceChamberRecipe recipe = inputHolder.value();
            processingTime = recipe.processingTime();
            status = determineStatus(recipe);
            ready = status == ChamberStatus.READY;

            if (ready) {
                processing = true;
                status = ChamberStatus.PROCESSING;
                processingTicks++;

                if (processingTicks >= recipe.processingTime()) {
                    if (completeRecipe(recipe)) {
                        processingTicks = 0;
                        processing = false;
                        ready = false;
                    } else {

                        processing = false;
                        ready = false;
                        processingTicks = 0;
                    }
                }
            } else {
                processing = false;
                resetInterruptedProgress();
            }
        }

        if (previousReady != ready
                || previousProcessing != processing
                || previousStatus != status
                || previousHeatTier != heatTier
                || previousTemperature != temperature) {
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

    private static int getDisplayHeatBand(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    ) {
        if (heatTier == null) {
            return 0;
        }

        return switch (heatTier) {
            case NONE -> 0;
            case SMOULDERING, FADING, KINDLED -> 1;
            case SEETHING -> 2;
            case RADIANT -> 3;
        };
    }

    private ChamberStatus determineStatus(
            SulfuricResonanceChamberRecipe recipe
    ) {
        if (isOutputBlocked(recipe.result())) {
            return ChamberStatus.OUTPUT_BLOCKED;
        }

        FluidStack acid = sulfuricAcid.getFluid();
        if (isInvalidSulfuricAcid(acid)
                || acid.getAmount() < recipe.acidAmount()) {
            return ChamberStatus.MISSING_ACID;
        }

        if (!recipe.minimumHeat().accepts(heatTier)) {
            return ChamberStatus.INSUFFICIENT_HEAT;
        }

        if (Math.abs(getSpeed()) < recipe.minimumSpeed()) {
            return ChamberStatus.INSUFFICIENT_SPEED;
        }

        return ChamberStatus.READY;
    }

    private @Nullable RecipeHolder<SulfuricResonanceChamberRecipe>
    findInputRecipe() {
        if (level == null) {
            return null;
        }

        return level.getRecipeManager()
                .getAllRecipesFor(SulfuricResonanceChamberRecipeRegistry.TYPE.get())
                .stream()
                .filter(holder -> holder.value().matchesInputs(
                        inventory.get(INPUT_1),
                        inventory.get(INPUT_2),
                        inventory.get(INPUT_3)
                ))
                .findFirst()
                .orElse(null);
    }

    private static boolean isInvalidSulfuricAcid(FluidStack stack) {
        return stack.isEmpty()
                || stack.getFluid() != AllModFluids.SULFURIC_ACID.get()
                && stack.getFluid() != AllModFluids.SULFURIC_ACID_FLOWING.get();
    }

    private boolean isOutputBlocked(ItemStack result) {
        ItemStack output = inventory.get(OUTPUT);
        return !output.isEmpty()
                && (!ItemStack.isSameItemSameComponents(output, result)
                || output.getCount() + result.getCount()
                > output.getMaxStackSize());
    }

    private boolean completeRecipe(
            SulfuricResonanceChamberRecipe recipe
    ) {
        if (!recipe.matchesInputs(
                inventory.get(INPUT_1),
                inventory.get(INPUT_2),
                inventory.get(INPUT_3)
        )) {
            return false;
        }

        ItemStack result = recipe.result().copy();
        if (result.isEmpty() || isOutputBlocked(result)) {
            return false;
        }

        FluidStack acid = sulfuricAcid.getFluid();
        if (isInvalidSulfuricAcid(acid)
                || acid.getAmount() < recipe.acidAmount()) {
            return false;
        }

        FluidStack simulatedDrain = sulfuricAcid.drain(
                recipe.acidAmount(),
                FluidAction.SIMULATE
        );
        if (simulatedDrain.getAmount() != recipe.acidAmount()) {
            return false;
        }

        sulfuricAcid.drain(
                recipe.acidAmount(),
                FluidAction.EXECUTE
        );

        inventory.get(INPUT_1).shrink(1);
        recipe.catalyst().ifPresent(
                ignored -> inventory.get(INPUT_2).shrink(1)
        );
        recipe.auxiliary().ifPresent(
                ignored -> inventory.get(INPUT_3).shrink(1)
        );

        for (int slot = INPUT_1; slot <= INPUT_3; slot++) {
            if (inventory.get(slot).isEmpty()) {
                inventory.set(slot, ItemStack.EMPTY);
            }
        }

        ItemStack output = inventory.get(OUTPUT);
        if (output.isEmpty()) {
            inventory.set(OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }

        onContentsChanged();
        playCompletionEffects();
        return true;
    }

    private void playCompletionEffects() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.45D;
        double z = worldPosition.getZ() + 0.5D;

        serverLevel.playSound(
                null,
                worldPosition,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                0.9F,
                0.72F
        );

        serverLevel.sendParticles(
                ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                x,
                y,
                z,
                7,
                0.16D,
                0.08D,
                0.16D,
                0.002D
        );

        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                x,
                y + 0.05D,
                z,
                5,
                0.13D,
                0.10D,
                0.13D,
                0.01D
        );
    }

    private void resetInterruptedProgress() {
        if (processingTicks != 0) {
            processingTicks = 0;
            setChanged();
        }
    }

    private boolean hasAnyInput() {
        return !inventory.get(INPUT_1).isEmpty()
                || !inventory.get(INPUT_2).isEmpty()
                || !inventory.get(INPUT_3).isEmpty();
    }

    private boolean hasPotentialInputRecipe() {
        if (level == null) {
            return false;
        }

        return level.getRecipeManager()
                .getAllRecipesFor(
                        SulfuricResonanceChamberRecipeRegistry.TYPE.get()
                )
                .stream()
                .anyMatch(holder -> holder.value().matchesPresentInputs(
                        inventory.get(INPUT_1),
                        inventory.get(INPUT_2),
                        inventory.get(INPUT_3)
                ));
    }

    private boolean canInsertIntoInput(int slot, ItemStack stack) {
        if (level == null) {
            return slot >= INPUT_1 && slot <= INPUT_3;
        }

        List<RecipeHolder<SulfuricResonanceChamberRecipe>> recipes =
                level.getRecipeManager()
                        .getAllRecipesFor(
                                SulfuricResonanceChamberRecipeRegistry.TYPE.get()
                        );

        if (recipes.isEmpty()) {
            return true;
        }

        return recipes.stream()
                .anyMatch(holder -> holder.value().acceptsInput(slot, stack));
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

    public ContainerData getMenuData() {
        return menuData;
    }

    public @Nullable IFluidHandler getFluidCapability(
            @Nullable net.minecraft.core.Direction side
    ) {
        if (side == null) {
            return fluidCapability;
        }
        return side == SulfuricResonanceChamberBlock.fluidSide(getBlockState())
                ? fluidCapability
                : null;
    }

    @SuppressWarnings("unused")
    public FluidStack getRenderedAcid() {
        return sulfuricAcid.getFluid().copy();
    }


    public float getClientRingAngle(float partialTick) {
        return clientPreviousRingAngle
                + (clientRingAngle - clientPreviousRingAngle) * partialTick;
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

        RecipeHolder<SulfuricResonanceChamberRecipe> recipe = findInputRecipe();
        if (recipe != null) {
            tooltip.add(Component.translatable(
                    "tooltip.sulfuricresonance.sulfuric_resonance_chamber.recipe",
                    recipe.value().result().getHoverName()
            ));
        }

        tooltip.add(Component.translatable(
                "tooltip.sulfuricresonance.sulfuric_resonance_chamber.status",
                Component.translatable(status.translationKey())
        ));
        tooltip.add(Component.translatable(
                "tooltip.sulfuricresonance.sulfuric_resonance_chamber.heat_detailed",
                Component.translatable(
                        "tooltip.sulfuricresonance.thermochemical.heat."
                                + heatTier.serializedId
                ),
                temperature
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

        if (processing && processingTime > 0) {
            int percent = Math.min(
                    100,
                    Math.round(processingTicks * 100.0F / processingTime)
            );
            tooltip.add(Component.translatable(
                    "tooltip.sulfuricresonance.sulfuric_resonance_chamber.progress",
                    percent
            ));
        }

        return true;
    }

    @Override
    protected void write(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.write(tag, provider, clientPacket);
        tag.putInt("InventoryVersion", 2);
        ContainerHelper.saveAllItems(tag, inventory, provider);
        tag.put(
                "SulfuricAcid",
                sulfuricAcid.writeToNBT(provider, new CompoundTag())
        );
        tag.putString("HeatTier", heatTier.serializedId);
        tag.putInt("Temperature", temperature);
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putInt("ProcessingTime", processingTime);
        tag.putBoolean("Ready", ready);
        tag.putBoolean("Processing", processing);
        tag.putInt("ChamberStatus", status.ordinal());

    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);

        for (int i = 0; i < SLOT_COUNT; i++) {
            inventory.set(i, ItemStack.EMPTY);
        }

        if (tag.getInt("InventoryVersion") >= 2) {
            ContainerHelper.loadAllItems(tag, inventory, provider);
        } else {

            NonNullList<ItemStack> legacy =
                    NonNullList.withSize(2, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag, legacy, provider);
            inventory.set(INPUT_1, legacy.get(0));
            inventory.set(OUTPUT, legacy.get(1));
        }

        sulfuricAcid.readFromNBT(provider, tag.getCompound("SulfuricAcid"));
        heatTier = MoltenRotorBlockEntity.RotorHeatLevel
                .fromSerializedId(tag.getString("HeatTier"));
        temperature = tag.getInt("Temperature");
        processingTicks = tag.getInt("ProcessingTicks");
        processingTime = tag.getInt("ProcessingTime");
        ready = tag.getBoolean("Ready");
        processing = tag.getBoolean("Processing");
        status = ChamberStatus.fromOrdinal(tag.getInt("ChamberStatus"));

    }


    public enum ChamberStatus {
        IDLE("gui.sulfuricresonance.chamber.status.idle"),
        MISSING_INGREDIENTS("gui.sulfuricresonance.chamber.status.missing_ingredients"),
        MISSING_ACID("gui.sulfuricresonance.chamber.status.missing_acid"),
        INSUFFICIENT_HEAT("gui.sulfuricresonance.chamber.status.insufficient_heat"),
        INSUFFICIENT_SPEED("gui.sulfuricresonance.chamber.status.insufficient_speed"),
        OUTPUT_BLOCKED("gui.sulfuricresonance.chamber.status.output_blocked"),
        READY("gui.sulfuricresonance.chamber.status.ready"),
        PROCESSING("gui.sulfuricresonance.chamber.status.processing"),
        NO_VALID_RECIPE("gui.sulfuricresonance.chamber.status.no_valid_recipe");

        private final String translationKey;

        ChamberStatus(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }

        public static ChamberStatus fromOrdinal(int ordinal) {
            ChamberStatus[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return IDLE;
            }
            return values[ordinal];
        }
    }
}
