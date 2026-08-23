package io.hxneyw.repo.content.blocks.sulfuricresonancechamber;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipe;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipeRegistry;
import io.hxneyw.repo.content.process.IProcessStateProvider;
import io.hxneyw.repo.content.process.ProcessState;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

@SuppressWarnings("SpellCheckingInspection")
public class SulfuricResonanceChamberBlockEntity extends KineticBlockEntity implements Container, IProcessStateProvider {

    public static final int ACID_CAPACITY = 1500;
    public static final int INPUT_1 = 0;
    public static final int INPUT_2 = 1;
    public static final int INPUT_3 = 2;
    public static final int OUTPUT = 3;
    public static final int SLOT_COUNT = 4;

    public static final int MENU_DATA_COUNT = 11;

    private static final Map<ResourceLocation, ReactionLevel> REACTION_LEVELS =
            new ConcurrentHashMap<>();
    private static Consumer<SulfuricResonanceChamberBlockEntity> clientEffectsTick =
            blockEntity -> {};

    static {
        setReactionLevel(
                ResourceLocation.fromNamespaceAndPath(
                        "sulfuricresonance",
                        "sulfuric_resonance_chamber/thermal_matrix"
                ),
                ReactionLevel.RESONANCE
        );
    }

    public static void setReactionLevel(
            ResourceLocation recipeId,
            ReactionLevel level
    ) {
        if (recipeId == null || level == null) {
            return;
        }
        REACTION_LEVELS.put(recipeId, level);
    }

    public static ReactionLevel getReactionLevel(
            @Nullable ResourceLocation recipeId
    ) {
        if (recipeId == null) {
            return ReactionLevel.NORMAL;
        }
        return REACTION_LEVELS.getOrDefault(
                recipeId,
                ReactionLevel.NORMAL
        );
    }

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
            if (isInputLocked()
                    || slot < INPUT_1
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
            if (isInputLocked()
                    || slot < INPUT_1
                    || slot > INPUT_3
                    || stack.isEmpty()) {
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

        if (isInputSlot(slot) && isInputLocked()) {
            abortProcessingForInputChange();
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

        if (isInputSlot(slot) && isInputLocked()) {
            abortProcessingForInputChange();
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

        if (isInputSlot(slot) && isInputLocked()) {
            ItemStack current = inventory.get(slot);

            
            
            boolean sameItem = !current.isEmpty()
                    && !stack.isEmpty()
                    && ItemStack.isSameItemSameComponents(current, stack);
            boolean removal = stack.isEmpty()
                    || sameItem && stack.getCount() < current.getCount();
            boolean unchanged = sameItem
                    && stack.getCount() == current.getCount();

            if (!removal && !unchanged) {
                return;
            }

            if (removal) {
                abortProcessingForInputChange();
            }
        }

        ItemStack stored = stack.copy();
        if (stored.getCount() > 64) {
            stored.setCount(64);
        }
        inventory.set(slot, stored);
        onContentsChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return isInputSlot(slot)
                && !isInputLocked()
                && canInsertIntoInput(slot, stack);
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
    private OperatingMode operatingMode = OperatingMode.AUTOMATIC;
    private boolean manualStartRequested;
    private @Nullable ResourceLocation activeRecipeId;
    private ChamberStatus status = ChamberStatus.IDLE;
    private UUID processIdentity = UUID.randomUUID();

    private static final float NORMAL_RING_PEAK_DEGREES_PER_TICK = 5.5F;
    private static final float RESONANCE_RING_PEAK_DEGREES_PER_TICK = 8.0F;
    private static final float NORMAL_RING_STARTUP_MULTIPLIER = 0.20F;
    private static final float RESONANCE_RING_STARTUP_MULTIPLIER = 0.30F;
    private static final float NORMAL_RING_PEAK_SPEED_BOOST = 0.10F;
    private static final float RESONANCE_RING_PEAK_SPEED_BOOST = 0.18F;
    private static final float RING_ENGAGEMENT_SPAN = 0.14F;
    private static final int NORMAL_VISUAL_STARTUP_TICKS = 100;
    private static final int RESONANCE_VISUAL_STARTUP_TICKS = 140;
    private static final float READY_RING_INNER_SPEED = 0.16F;
    private static final float READY_RING_OUTER_SPEED = 0.10F;
    private static final float HEAT_WAIT_RING_INNER_SPEED = 0.065F;
    private static final float HEAT_WAIT_RING_OUTER_SPEED = 0.040F;
    private static final float PLATFORM_RISE_PER_TICK = 0.085F;
    private static final float PLATFORM_FALL_PER_TICK = 0.040F;
    private static final float READY_GLOW_RISE_PER_TICK = 0.10F;
    private static final float READY_GLOW_FALL_PER_TICK = 0.08F;
    private static final float NORMAL_VISUAL_COOLDOWN_PER_TICK = 1.0F / 42.0F;
    private static final float RESONANCE_VISUAL_COOLDOWN_PER_TICK = 1.0F / 56.0F;
    private static final float NORMAL_REACTION_COOLDOWN_PER_TICK = 0.025F;
    private static final float RESONANCE_REACTION_COOLDOWN_PER_TICK = 0.018F;
    private static final float INNER_RING_START = 0.02F;
    private static final float OUTER_RING_START = 0.30F;
    private static final int COMPLETION_PEAK_TICKS = 7;
    private float clientPreviousInnerRingAngle;
    private float clientInnerRingAngle;
    private float clientPreviousOuterRingAngle;
    private float clientOuterRingAngle;
    private float clientPreviousReactionProgress;
    private float clientReactionProgress;
    private float clientPreviousVisualActivation;
    private float clientVisualActivation;
    private float clientPreviousCompletionPulse;
    private float clientCompletionPulse;
    private float clientPreviousPlatformLift;
    private float clientPlatformLift;
    private float clientPreviousReadyGlow;
    private float clientReadyGlow;
    private int clientCompletionPeakTicks;
    private boolean clientVisualStateInitialized;
    private ReactionLevel clientVisualReactionLevel = ReactionLevel.NORMAL;

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
                case 10 -> operatingMode.ordinal();
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
            clientPreviousInnerRingAngle = clientInnerRingAngle;
            clientPreviousOuterRingAngle = clientOuterRingAngle;
            clientPreviousReactionProgress = clientReactionProgress;
            clientPreviousVisualActivation = clientVisualActivation;
            clientPreviousCompletionPulse = clientCompletionPulse;
            clientPreviousPlatformLift = clientPlatformLift;
            clientPreviousReadyGlow = clientReadyGlow;

            if (!clientVisualStateInitialized) {
                clientVisualStateInitialized = true;
                clientPlatformLift = getClientPlatformTarget();
                clientPreviousPlatformLift = clientPlatformLift;
                clientReadyGlow = ready ? 1.0F : 0.0F;
                clientPreviousReadyGlow = clientReadyGlow;
            }

            if (processing) {
                clientVisualReactionLevel = getReactionLevel(activeRecipeId);
                clientVisualActivation = Math.min(
                        1.0F,
                        clientVisualActivation
                                + getVisualWakePerTick(clientVisualReactionLevel)
                );

                if (processingTime > 0) {
                    float targetProgress = Math.clamp(
                            (processingTicks + 1.0F) / processingTime,
                            0.0F,
                            1.0F
                    );
                    float predictedProgress = Math.min(
                            1.0F,
                            clientReactionProgress + 1.0F / processingTime
                    );
                    clientReactionProgress = Math.max(
                            predictedProgress,
                            targetProgress
                    );
                }

                if (processingTicks <= 1) {
                    clientCompletionPeakTicks = 0;
                    clientCompletionPulse = 0.0F;
                } else if (clientCompletionPeakTicks <= 0
                        && clientCompletionPulse > 0.0F) {
                    clientCompletionPulse = Math.max(
                            0.0F,
                            clientCompletionPulse - 0.18F
                    );
                }
            } else {
                RecipeHolder<SulfuricResonanceChamberRecipe> visualRecipe =
                        findInputRecipe();
                if (visualRecipe != null) {
                    clientVisualReactionLevel = getReactionLevel(
                            visualRecipe.id()
                    );
                }

                if (clientCompletionPeakTicks > 0) {
                    clientVisualActivation = Math.max(
                            clientVisualActivation,
                            1.0F
                    );
                    clientCompletionPulse =
                            clientCompletionPeakTicks
                                    / (float) COMPLETION_PEAK_TICKS;
                    clientCompletionPeakTicks--;
                } else {
                    float visualCooldown =
                            clientVisualReactionLevel == ReactionLevel.RESONANCE
                                    ? RESONANCE_VISUAL_COOLDOWN_PER_TICK
                                    : NORMAL_VISUAL_COOLDOWN_PER_TICK;
                    clientVisualActivation = Math.max(
                            0.0F,
                            clientVisualActivation - visualCooldown
                    );
                    clientCompletionPulse = Math.max(
                            0.0F,
                            clientCompletionPulse - 0.18F
                    );
                }

                if (clientReactionProgress > 0.0F) {
                    float reactionCooldown =
                            clientVisualReactionLevel == ReactionLevel.RESONANCE
                                    ? RESONANCE_REACTION_COOLDOWN_PER_TICK
                                    : NORMAL_REACTION_COOLDOWN_PER_TICK;
                    clientReactionProgress = Math.max(
                            0.0F,
                            clientReactionProgress - reactionCooldown
                    );
                }
            }

            float targetPlatformLift = getClientPlatformTarget();
            float platformStep = targetPlatformLift > clientPlatformLift
                    ? PLATFORM_RISE_PER_TICK
                    : PLATFORM_FALL_PER_TICK;
            clientPlatformLift = moveToward(
                    clientPlatformLift,
                    targetPlatformLift,
                    platformStep
            );

            float targetReadyGlow = ready ? 1.0F : 0.0F;
            float readyGlowStep = targetReadyGlow > clientReadyGlow
                    ? READY_GLOW_RISE_PER_TICK
                    : READY_GLOW_FALL_PER_TICK;
            clientReadyGlow = moveToward(
                    clientReadyGlow,
                    targetReadyGlow,
                    readyGlowStep
            );

            float innerSpeed;
            float outerSpeed;
            if (processing) {
                innerSpeed = getRingSpeed(
                        clientVisualActivation,
                        INNER_RING_START,
                        clientReactionProgress,
                        clientCompletionPulse,
                        clientVisualReactionLevel
                );
                outerSpeed = getRingSpeed(
                        clientVisualActivation,
                        OUTER_RING_START,
                        clientReactionProgress,
                        clientCompletionPulse,
                        clientVisualReactionLevel
                );
            } else if (ready) {
                innerSpeed = READY_RING_INNER_SPEED;
                outerSpeed = READY_RING_OUTER_SPEED;
            } else if (status == ChamberStatus.INSUFFICIENT_HEAT) {
                innerSpeed = HEAT_WAIT_RING_INNER_SPEED;
                outerSpeed = HEAT_WAIT_RING_OUTER_SPEED;
            } else {
                innerSpeed = 0.0F;
                outerSpeed = 0.0F;
            }

            clientInnerRingAngle += innerSpeed;
            if (clientInnerRingAngle >= 360.0F) {
                clientInnerRingAngle -= 360.0F;
                clientPreviousInnerRingAngle -= 360.0F;
            }

            clientOuterRingAngle += outerSpeed;
            if (clientOuterRingAngle >= 360.0F) {
                clientOuterRingAngle -= 360.0F;
                clientPreviousOuterRingAngle -= 360.0F;
            }

            if (clientVisualActivation <= 0.0F
                    && clientReactionProgress <= 0.0F
                    && clientCompletionPulse <= 0.0F
                    && !ready) {
                clientVisualReactionLevel = ReactionLevel.NORMAL;
            }

            clientEffectsTick.accept(this);
            return;
        }

        MoltenRotorBlockEntity.RotorHeatLevel previousHeatTier = heatTier;
        int previousTemperature = temperature;
        updateHeat();

        boolean previousReady = ready;
        boolean previousProcessing = processing;
        ChamberStatus previousStatus = status;
        int previousProgressPercent = getProgressPercent();

        RecipeHolder<SulfuricResonanceChamberRecipe> inputHolder =
                getLockedOrInputRecipe();

        if (processing && inputHolder == null) {
            
            
            clearProcessingState();
        }

        if (inputHolder == null) {
            processingTime = 0;
            ready = false;
            processing = false;
            manualStartRequested = false;
            activeRecipeId = null;
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
            boolean requirementsReady = status == ChamberStatus.READY;
            boolean startAllowed = processing
                    || operatingMode == OperatingMode.AUTOMATIC
                    || manualStartRequested;

            if (requirementsReady && startAllowed) {
                if (!processing) {
                    processing = true;
                    activeRecipeId = inputHolder.id();
                }

                ready = false;
                manualStartRequested = false;
                status = ChamberStatus.PROCESSING;
                processingTicks++;

                if (processingTicks >= recipe.processingTime()) {
                    if (completeRecipe(recipe)) {
                        clearProcessingState();
                        status = ChamberStatus.IDLE;
                    } else {
                        clearProcessingState();
                    }
                }
            } else if (requirementsReady) {
                ready = true;
                activeRecipeId = null;
                resetInterruptedProgress();
            } else {
                ready = false;
                manualStartRequested = false;
                if (processing) {
                    clearProcessingState();
                } else {
                    resetInterruptedProgress();
                    activeRecipeId = null;
                }
            }
        }

        int currentProgressPercent = getProgressPercent();
        if (previousReady != ready
                || previousProcessing != processing
                || previousStatus != status
                || previousHeatTier != heatTier
                || previousTemperature != temperature
                || previousProgressPercent != currentProgressPercent) {
            setChanged();
            sendData();
        }
    }

    private static float getVisualWakePerTick(
            ReactionLevel reactionLevel
    ) {
        int ticks = reactionLevel == ReactionLevel.RESONANCE
                ? RESONANCE_VISUAL_STARTUP_TICKS
                : NORMAL_VISUAL_STARTUP_TICKS;
        return 1.0F / ticks;
    }

    private float getClientPlatformTarget() {
        if (processing
                || ready
                || status == ChamberStatus.OUTPUT_BLOCKED
                || !inventory.get(OUTPUT).isEmpty()) {
            return 1.0F;
        }
        if (status == ChamberStatus.INSUFFICIENT_HEAT) {
            return 0.62F;
        }
        if (status == ChamberStatus.INSUFFICIENT_SPEED) {
            return 0.38F;
        }
        return 0.0F;
    }

    private static float moveToward(
            float current,
            float target,
            float amount
    ) {
        if (current < target) {
            return Math.min(target, current + amount);
        }
        return Math.max(target, current - amount);
    }

    private static float getRingSpeed(
            float activation,
            float start,
            float reactionProgress,
            float completionPulse,
            ReactionLevel reactionLevel
    ) {
        if (activation <= start) {
            return 0.0F;
        }

        float engagement = Math.clamp(
                (activation - start) / RING_ENGAGEMENT_SPAN,
                0.0F,
                1.0F
        );
        engagement = engagement * engagement * (3.0F - 2.0F * engagement);

        float progress = Math.clamp(reactionProgress, 0.0F, 1.0F);
        float progressCurve;
        float startupMultiplier;
        float peakDegreesPerTick;
        float peakBoost;

        if (reactionLevel == ReactionLevel.RESONANCE) {
            progressCurve = (float) Math.pow(progress, 0.70D);
            startupMultiplier = RESONANCE_RING_STARTUP_MULTIPLIER;
            peakDegreesPerTick = RESONANCE_RING_PEAK_DEGREES_PER_TICK;
            peakBoost = RESONANCE_RING_PEAK_SPEED_BOOST;
        } else {
            progressCurve = progress * progress * (3.0F - 2.0F * progress);
            startupMultiplier = NORMAL_RING_STARTUP_MULTIPLIER;
            peakDegreesPerTick = NORMAL_RING_PEAK_DEGREES_PER_TICK;
            peakBoost = NORMAL_RING_PEAK_SPEED_BOOST;
        }

        float processingSpeed = startupMultiplier
                + (1.0F - startupMultiplier) * progressCurve;
        float completionBoost = 1.0F
                + peakBoost * Math.clamp(completionPulse, 0.0F, 1.0F);

        return engagement
                * processingSpeed
                * peakDegreesPerTick
                * completionBoost;
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

    private @Nullable RecipeHolder<SulfuricResonanceChamberRecipe>
    getLockedOrInputRecipe() {
        if (!processing) {
            return findInputRecipe();
        }

        RecipeHolder<SulfuricResonanceChamberRecipe> locked =
                findActiveRecipe();
        if (locked != null
                && locked.value().matchesInputs(
                inventory.get(INPUT_1),
                inventory.get(INPUT_2),
                inventory.get(INPUT_3)
        )) {
            return locked;
        }

        
        
        if (activeRecipeId == null) {
            RecipeHolder<SulfuricResonanceChamberRecipe> recovered =
                    findInputRecipe();
            if (recovered != null) {
                activeRecipeId = recovered.id();
                return recovered;
            }
        }

        return null;
    }

    private @Nullable RecipeHolder<SulfuricResonanceChamberRecipe>
    findActiveRecipe() {
        if (level == null || activeRecipeId == null) {
            return null;
        }

        return level.getRecipeManager()
                .getAllRecipesFor(
                        SulfuricResonanceChamberRecipeRegistry.TYPE.get()
                )
                .stream()
                .filter(holder -> holder.id().equals(activeRecipeId))
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

    public boolean canInsertIntoInput(int slot, ItemStack stack) {
        if (isInputLocked()
                || !isInputSlot(slot)
                || stack.isEmpty()) {
            return false;
        }

        if (level == null) {
            return true;
        }

        ItemStack current = inventory.get(slot);
        if (!current.isEmpty()
                && !ItemStack.isSameItemSameComponents(current, stack)) {
            return false;
        }

        ItemStack test1 = inventory.get(INPUT_1).copy();
        ItemStack test2 = inventory.get(INPUT_2).copy();
        ItemStack test3 = inventory.get(INPUT_3).copy();
        ItemStack candidate = current.isEmpty()
                ? stack.copyWithCount(1)
                : current.copy();

        switch (slot) {
            case INPUT_1 -> test1 = candidate;
            case INPUT_2 -> test2 = candidate;
            case INPUT_3 -> test3 = candidate;
            default -> {
                return false;
            }
        }

        List<RecipeHolder<SulfuricResonanceChamberRecipe>> recipes =
                level.getRecipeManager()
                        .getAllRecipesFor(
                                SulfuricResonanceChamberRecipeRegistry.TYPE.get()
                        );

        if (recipes.isEmpty()) {
            return false;
        }

        ItemStack finalTest1 = test1;
        ItemStack finalTest2 = test2;
        ItemStack finalTest3 = test3;
        return recipes.stream()
                .anyMatch(holder -> holder.value().matchesPresentInputs(
                        finalTest1,
                        finalTest2,
                        finalTest3
                ));
    }

    private static boolean isInputSlot(int slot) {
        return slot >= INPUT_1 && slot <= INPUT_3;
    }

    public boolean isInputLocked() {
        return processing || processingTicks > 0;
    }

    private void abortProcessingForInputChange() {
        if (!isInputLocked()) {
            return;
        }

        clearProcessingState();
        status = ChamberStatus.MISSING_INGREDIENTS;
        onContentsChanged();
    }

    private void clearProcessingState() {
        processingTicks = 0;
        processingTime = 0;
        processing = false;
        ready = false;
        manualStartRequested = false;
        activeRecipeId = null;
        setChanged();
    }


    public boolean toggleOperatingMode() {
        if (processing) {
            return false;
        }

        operatingMode = operatingMode == OperatingMode.AUTOMATIC
                ? OperatingMode.MANUAL
                : OperatingMode.AUTOMATIC;
        manualStartRequested = false;
        setChanged();
        sendData();
        return true;
    }

    public boolean requestManualStart() {
        if (operatingMode != OperatingMode.MANUAL
                || processing
                || !ready
                || status != ChamberStatus.READY) {
            return false;
        }

        manualStartRequested = true;
        setChanged();
        sendData();
        return true;
    }

    private void onContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            sendData();
        }
    }

    public @Nullable IItemHandler getItemCapability(
            @Nullable Direction side
    ) {
        if (side == null) {
            return itemCapability;
        }

        return SulfuricResonanceChamberBlock.isItemAutomationSide(
                getBlockState(),
                side
        ) ? itemCapability : null;
    }

    public ContainerData getMenuData() {
        return menuData;
    }

    public @Nullable IFluidHandler getFluidCapability(
            @Nullable Direction side
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


    public float getClientInnerRingAngle(float partialTick) {
        return clientPreviousInnerRingAngle
                + (clientInnerRingAngle - clientPreviousInnerRingAngle)
                * partialTick;
    }

    public float getClientOuterRingAngle(float partialTick) {
        return clientPreviousOuterRingAngle
                + (clientOuterRingAngle - clientPreviousOuterRingAngle)
                * partialTick;
    }

    public float getClientReactionProgress(float partialTick) {
        if (level != null && level.isClientSide) {
            return Math.clamp(
                    clientPreviousReactionProgress
                            + (clientReactionProgress - clientPreviousReactionProgress)
                            * partialTick,
                    0.0F,
                    1.0F
            );
        }

        if (!processing || processingTime <= 0) {
            return 0.0F;
        }
        return Math.clamp(
                processingTicks / (float) processingTime,
                0.0F,
                1.0F
        );
    }

    public float getClientVisualActivation(float partialTick) {
        if (level != null && level.isClientSide) {
            return Math.clamp(
                    clientPreviousVisualActivation
                            + (clientVisualActivation - clientPreviousVisualActivation)
                            * partialTick,
                    0.0F,
                    1.0F
            );
        }
        return processing ? 1.0F : 0.0F;
    }

    public float getClientPlatformLift(float partialTick) {
        if (level != null && level.isClientSide) {
            return Math.clamp(
                    clientPreviousPlatformLift
                            + (clientPlatformLift - clientPreviousPlatformLift)
                            * partialTick,
                    0.0F,
                    1.0F
            );
        }
        return getClientPlatformTarget();
    }

    public float getClientReadyGlow(float partialTick) {
        if (level != null && level.isClientSide) {
            return Math.clamp(
                    clientPreviousReadyGlow
                            + (clientReadyGlow - clientPreviousReadyGlow)
                            * partialTick,
                    0.0F,
                    1.0F
            );
        }
        return ready ? 1.0F : 0.0F;
    }

    public float getClientFailureIndexOffset(float partialTick) {
        if (level == null || status != ChamberStatus.INSUFFICIENT_SPEED) {
            return 0.0F;
        }

        long offset = Math.floorMod(worldPosition.asLong(), 64L);
        float cycle = Math.floorMod(level.getGameTime() + offset, 64L)
                + partialTick;
        if (cycle >= 24.0F) {
            return 0.0F;
        }

        float local = cycle < 12.0F
                ? cycle / 12.0F
                : (cycle - 12.0F) / 12.0F;
        local = Math.clamp(local, 0.0F, 1.0F);
        float eased = local * local * (3.0F - 2.0F * local);
        return cycle < 12.0F
                ? eased * 7.0F
                : (1.0F - eased) * 7.0F;
    }

    public float getClientCompletionPulse(float partialTick) {
        if (level != null && level.isClientSide) {
            return Math.clamp(
                    clientPreviousCompletionPulse
                            + (clientCompletionPulse - clientPreviousCompletionPulse)
                            * partialTick,
                    0.0F,
                    1.0F
            );
        }
        return 0.0F;
    }

    public float getClientHeatVisualStrength() {
        if (heatTier == null) {
            return 0.0F;
        }

        return switch (heatTier) {
            case NONE -> 0.0F;
            case SMOULDERING, FADING -> 0.28F;
            case KINDLED -> 0.38F;
            case SEETHING -> 0.52F;
            case RADIANT -> 0.68F;
        };
    }

    public ChamberStatus getStatus() {
        return status;
    }

    public boolean isProcessingActive() {
        return processing;
    }

    public boolean isReadyState() {
        return ready || status == ChamberStatus.READY;
    }

    public boolean hasCompletedOutput() {
        return !inventory.get(OUTPUT).isEmpty();
    }

    public static void setClientEffectsTick(
            Consumer<SulfuricResonanceChamberBlockEntity> effectsTick
    ) {
        clientEffectsTick = Objects.requireNonNull(effectsTick);
    }

    public ReactionLevel getActiveReactionLevel() {
        return getReactionLevel(activeRecipeId);
    }

    public ReactionLevel getClientVisualReactionLevel() {
        if (level != null
                && level.isClientSide
                && (clientVisualActivation > 0.0F
                || clientReactionProgress > 0.0F
                || clientCompletionPulse > 0.0F)) {
            return clientVisualReactionLevel;
        }
        return getActiveReactionLevel();
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

        RecipeHolder<SulfuricResonanceChamberRecipe> recipe =
                getLockedOrInputRecipe();
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
            tooltip.add(Component.translatable(
                    "tooltip.sulfuricresonance.sulfuric_resonance_chamber.progress",
                    getProgressPercent()
            ));
        }

        return true;
    }

    private int getProgressPercent() {
        if (!processing || processingTime <= 0) {
            return 0;
        }
        return Math.min(
                100,
                Math.round(processingTicks * 100.0F / processingTime)
        );
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
        tag.putString("OperatingMode", operatingMode.serializedName());
        tag.putInt("ChamberStatus", status.ordinal());
        tag.putUUID("ProcessIdentity", processIdentity);
        if (activeRecipeId != null) {
            tag.putString("ActiveRecipe", activeRecipeId.toString());
        } else {
            tag.remove("ActiveRecipe");
        }

    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);

        boolean wasClientProcessing = clientPacket && processing;
        ItemStack previousClientOutput = clientPacket
                ? inventory.get(OUTPUT).copy()
                : ItemStack.EMPTY;

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
        operatingMode = OperatingMode.fromSerializedName(
                tag.getString("OperatingMode")
        );
        manualStartRequested = false;
        status = ChamberStatus.fromOrdinal(tag.getInt("ChamberStatus"));
        if (tag.hasUUID("ProcessIdentity")) {
            processIdentity = tag.getUUID("ProcessIdentity");
        }
        String activeRecipe = tag.getString("ActiveRecipe");
        activeRecipeId = activeRecipe.isBlank()
                ? null
                : ResourceLocation.tryParse(activeRecipe);

        if (clientPacket) {
            ItemStack syncedOutput = inventory.get(OUTPUT);
            boolean outputIncreased = wasClientProcessing
                    && !syncedOutput.isEmpty()
                    && (previousClientOutput.isEmpty()
                    || ItemStack.isSameItemSameComponents(
                    previousClientOutput,
                    syncedOutput
            ) && syncedOutput.getCount() > previousClientOutput.getCount());

            if (outputIncreased) {
                clientCompletionPeakTicks = COMPLETION_PEAK_TICKS;
                clientCompletionPulse = 1.0F;
                clientPreviousCompletionPulse = 1.0F;
                clientVisualActivation = 1.0F;
                clientPreviousVisualActivation = 1.0F;
            }

            if (processing && processingTime > 0) {
                float syncedProgress = Math.clamp(
                        processingTicks / (float) processingTime,
                        0.0F,
                        1.0F
                );
                float syncedActivation = Math.clamp(
                        processingTicks
                                * getVisualWakePerTick(
                                getReactionLevel(activeRecipeId)
                        ),
                        0.0F,
                        1.0F
                );
                clientReactionProgress = Math.max(
                        clientReactionProgress,
                        syncedProgress
                );
                
                clientPreviousReactionProgress = Math.max(
                        clientPreviousReactionProgress,
                        syncedProgress
                );
                clientVisualActivation = Math.max(
                        clientVisualActivation,
                        syncedActivation
                );
                
                clientPreviousVisualActivation = Math.max(
                        clientPreviousVisualActivation,
                        syncedActivation
                );
                clientVisualReactionLevel = getReactionLevel(activeRecipeId);
            }
        }
    }


    @Override
    public @NotNull ProcessState getProcessState() {
        if (processing || status == ChamberStatus.PROCESSING) {
            return ProcessState.PROCESSING;
        }

        if (ready || status == ChamberStatus.READY) {
            return ProcessState.READY;
        }

        return switch (status) {
            case MISSING_ACID,
                    INSUFFICIENT_HEAT,
                    INSUFFICIENT_SPEED,
                    OUTPUT_BLOCKED -> ProcessState.BLOCKED;
            default -> ProcessState.IDLE;
        };
    }

    @Override
    public @NotNull UUID getProcessIdentity() {
        return processIdentity;
    }

    public enum ReactionLevel {
        NORMAL,
        RESONANCE
    }

    public enum OperatingMode {
        AUTOMATIC("automatic"),
        MANUAL("manual");

        private final String serializedName;

        OperatingMode(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        public String translationKey() {
            return "gui.sulfuricresonance.chamber.mode." + serializedName;
        }

        public static OperatingMode fromOrdinal(int ordinal) {
            OperatingMode[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return AUTOMATIC;
            }
            return values[ordinal];
        }

        public static OperatingMode fromSerializedName(String name) {
            if (MANUAL.serializedName.equals(name)) {
                return MANUAL;
            }
            return AUTOMATIC;
        }
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
