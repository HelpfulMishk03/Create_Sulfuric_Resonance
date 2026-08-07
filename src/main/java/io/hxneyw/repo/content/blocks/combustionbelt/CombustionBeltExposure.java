package io.hxneyw.repo.content.blocks.combustionbelt;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipe;
import io.hxneyw.repo.content.recipes.combustionbelt.CombustionBeltRecipeRegistry;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class CombustionBeltExposure {

    private static final ResourceLocation ROLLING_FIRE_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "rolling_fire"
            );

    private static final double ROLLING_FIRE_RADIUS_SQUARED = 64.0D;

    private static final String MOD_ROOT_KEY =
            "sulfuricresonance";

    private static final String EXPOSURE_ROOT_KEY =
            "CombustionBeltExposure";

    private static final String SEGMENTS_KEY =
            "Segments";

    private static final String LAST_QUALIFIED_TICK_KEY =
            "LastQualifiedTick";

    private static final String LAST_DECAY_TICK_KEY =
            "LastDecayTick";

    private static final String VISITED_SEGMENTS_KEY =
            "VisitedSegments";

    private static final String DIMENSION_KEY =
            "Dimension";

    private static final String POSITION_KEY =
            "Position";

    public static final long PROGRESS_RESET_SECONDS = 5L;
    private static final long PROGRESS_RESET_GRACE_TICKS =
            PROGRESS_RESET_SECONDS * 20L;

    private static final float MOVEMENT_EPSILON = 0.0001F;

    private static final String PROCESSING_RECIPE_KEY =
            "ProcessingRecipe";

    private static final String PROCESSING_TICKS_KEY =
            "ProcessingTicks";

    private static final String
            PROCESSING_LAST_QUALIFIED_TICK_KEY =
            "ProcessingLastQualifiedTick";

    private static final String
            PROCESSING_LAST_DECAY_TICK_KEY =
            "ProcessingLastDecayTick";

    private CombustionBeltExposure() {
    }

    public static void tickControllerInventory(
            BeltBlockEntity controller,
            MoltenRotorBlockEntity.RotorHeatLevel
                    liveChainHeatTier
    ) {
        Level level = controller.getLevel();

        if (level == null
                || !controller.isController()) {
            return;
        }

        MoltenRotorBlockEntity.RotorHeatLevel
                authoritativeHeatTier =
                liveChainHeatTier == null
                        ? MoltenRotorBlockEntity
                        .RotorHeatLevel.NONE
                        : liveChainHeatTier;

        BeltInventory inventory = controller.getInventory();

        if (inventory == null) {
            return;
        }

        List<TransportedItemStack> transportedItems =
                inventory.getTransportedItems();


        if (level.isClientSide()) {
            for (TransportedItemStack transported : transportedItems) {
                centerTransportedItem(transported);
            }
            return;
        }

        boolean persistentDataChanged = false;
        boolean inventoryChanged = false;

        for (TransportedItemStack transported : transportedItems) {
            UpdateResult result = tickTransportedStack(
                    level,
                    controller,
                    transported,
                    authoritativeHeatTier
            );

            persistentDataChanged |=
                    result.persistentDataChanged();

            inventoryChanged |= result.inventoryChanged();
        }

        if (persistentDataChanged || inventoryChanged) {
            controller.setChanged();
        }

        if (inventoryChanged) {
            controller.sendData();
        }
    }

    private static UpdateResult tickTransportedStack(
            Level level,
            BeltBlockEntity controller,
            TransportedItemStack transported,
            MoltenRotorBlockEntity.RotorHeatLevel
                    liveChainHeatTier
    ) {
        ItemStack stack = transported.stack;

        if (stack.isEmpty()) {
            return UpdateResult.NONE;
        }

        centerTransportedItem(transported);

        long gameTime = level.getGameTime();
        float previousPosition = transported.prevBeltPosition;
        float currentPosition = transported.beltPosition;

        boolean moving =
                Math.abs(currentPosition - previousPosition)
                        > MOVEMENT_EPSILON;

        ExposureDocument document =
                ExposureDocument.read(stack);

        boolean persistentDataChanged = false;

        RecipeHolder<CombustionBeltRecipe> holder =
                findMatchingRecipe(level, stack);

        CombustionBeltRecipe recipe =
                holder == null
                        ? null
                        : holder.value();

        MoltenRotorBlockEntity.RotorHeatLevel currentHeat =
                getHeatAtCurrentPosition(
                        level,
                        controller,
                        currentPosition,
                        liveChainHeatTier
                );

        boolean heatMeetsRecipe =
                recipe != null
                        && recipe.minimumHeat()
                        .accepts(currentHeat);

        boolean validMovingHeat =
                heatMeetsRecipe
                        && moving;

        if (recipe != null) {
            persistentDataChanged |= document.retainOnly(
                    recipe.minimumHeat().exposureBand()
            );
        }

        if (validMovingHeat) {
            persistentDataChanged |= addTraversedSegments(
                    level,
                    controller,
                    document,
                    previousPosition,
                    currentPosition,
                    gameTime,
                    liveChainHeatTier,
                    recipe.minimumHeat().exposureBand()
            );
        }

        for (ExposureBand band : ExposureBand.values()) {
            BandState state = document.getBand(band);

            if (state.segments() <= 0) {
                continue;
            }

            if (band.accepts(currentHeat)) {
                persistentDataChanged |=
                        state.touch(gameTime);
            } else if (state.applyDecay(gameTime)) {
                persistentDataChanged = true;
            }
        }

        if (holder == null) {
            persistentDataChanged |=
                    document.clearAllExposure();
        } else {
            int requiredTicks =
                    recipe.requiredProcessingTicks(
                            stack.getCount()
                    );

            int processingTicks =
                    document.processingTicksFor(
                            holder.id()
                    );

            if (validMovingHeat) {
                int nextProcessingTicks =
                        requiredTicks <= 0
                                ? 0
                                : Math.min(
                                requiredTicks,
                                processingTicks + 1
                        );

                persistentDataChanged |=
                        document.touchProcessing(
                                holder.id(),
                                nextProcessingTicks,
                                gameTime
                        );

                processingTicks =
                        nextProcessingTicks;

                spawnHeatingParticle(
                        level,
                        controller,
                        transported,
                        gameTime,
                        stack
                );
            } else if (heatMeetsRecipe) {
                persistentDataChanged |=
                        document.touchProcessing(
                                holder.id(),
                                processingTicks,
                                gameTime
                        );
            } else {
                persistentDataChanged |=
                        document.applyProcessingDecay(
                                holder.id(),
                                gameTime
                        );

                processingTicks =
                        document.processingTicksFor(
                                holder.id()
                        );
            }

            int completedSegments =
                    document
                            .getBand(
                                    recipe.minimumHeat()
                                            .exposureBand()
                            )
                            .segments();

            boolean segmentRequirementMet =
                    completedSegments
                            >= recipe.requiredSegments();

            boolean timeRequirementMet =
                    requiredTicks <= 0
                            || processingTicks >= requiredTicks;

            if (validMovingHeat
                    && segmentRequirementMet
                    && timeRequirementMet) {

                spawnCompletionBurst(
                        level,
                        controller,
                        transported,
                        stack
                );

                boolean completed = completeRecipe(
                        level,
                        transported,
                        recipe,
                        stack
                );

                if (completed) {
                    awardRollingFire(
                            level,
                            controller,
                            transported
                    );

                    return new UpdateResult(
                            persistentDataChanged,
                            true
                    );
                }
            }
        }

        if (isExpectedToLeaveBelt(
                controller,
                previousPosition,
                currentPosition
        )) {
            if (persistentDataChanged) {
                document.write(stack);
            }

            return new UpdateResult(
                    persistentDataChanged,
                    false
            );
        }

        if (persistentDataChanged) {
            document.write(stack);
        }

        return new UpdateResult(
                persistentDataChanged,
                false
        );
    }

    private static void centerTransportedItem(
            TransportedItemStack transported
    ) {

        transported.angle = 180;
    }

    private static boolean isExpectedToLeaveBelt(
            BeltBlockEntity controller,
            float previousPosition,
            float currentPosition
    ) {
        float movement =
                currentPosition - previousPosition;

        if (movement > MOVEMENT_EPSILON) {
            return currentPosition + movement
                    >= controller.beltLength - 0.5F;
        }

        if (movement < -MOVEMENT_EPSILON) {
            return currentPosition + movement <= 0.5F;
        }

        return false;
    }

    private static boolean addTraversedSegments(
            Level level,
            BeltBlockEntity controller,
            ExposureDocument document,
            float previousPosition,
            float currentPosition,
            long gameTime,
            MoltenRotorBlockEntity.RotorHeatLevel
                    liveChainHeatTier,
            ExposureBand requiredBand
    ) {
        if (controller.beltLength <= 0) {
            return false;
        }

        float minimumPosition = Math.min(
                previousPosition,
                currentPosition
        );

        float maximumPosition = Math.max(
                previousPosition,
                currentPosition
        );

        int firstSegment = Math.max(
                0,
                (int) Math.floor(minimumPosition)
        );

        int lastSegment = Math.min(
                controller.beltLength - 1,
                (int) Math.floor(maximumPosition)
        );

        boolean changed = false;

        for (int segment = firstSegment;
             segment <= lastSegment;
             segment++) {

            changed |= addSegmentExposure(
                    level,
                    controller,
                    document,
                    segment,
                    gameTime,
                    liveChainHeatTier,
                    requiredBand
            );
        }

        return changed;
    }

    private static boolean addSegmentExposure(
            Level level,
            BeltBlockEntity controller,
            ExposureDocument document,
            int segment,
            long gameTime,
            MoltenRotorBlockEntity.RotorHeatLevel
                    liveChainHeatTier,
            ExposureBand requiredBand
    ) {
        SegmentHeat segmentHeat = getSegmentHeat(
                level,
                controller,
                segment,
                liveChainHeatTier
        );

        if (!requiredBand.accepts(segmentHeat.heatTier())) {
            return false;
        }

        return document
                .getBand(requiredBand)
                .addUniqueSegment(
                        level.dimension().location(),
                        segmentHeat.position(),
                        gameTime
                );
    }

    private static RecipeHolder<CombustionBeltRecipe>
    findMatchingRecipe(
            Level level,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return null;
        }

        return level.getRecipeManager()
                .getRecipeFor(
                        CombustionBeltRecipeRegistry.TYPE.get(),
                        new SingleRecipeInput(stack),
                        level
                )
                .orElse(null);
    }

    private static boolean completeRecipe(
            Level level,
            TransportedItemStack transported,
            CombustionBeltRecipe recipe,
            ItemStack inputStack
    ) {
        ItemStack output = recipe.assemble(
                new SingleRecipeInput(inputStack),
                level.registryAccess()
        );

        if (output.isEmpty()) {
            return false;
        }

        long totalOutputCount =
                (long) inputStack.getCount() * output.getCount();

        if (totalOutputCount <= 0L
                || totalOutputCount > output.getMaxStackSize()) {
            return false;
        }

        output.setCount((int) totalOutputCount);
        clearCombustionBeltExposure(output);

        transported.stack = output;
        transported.prevBeltPosition = transported.beltPosition;


        transported.angle = 180;

        transported.lockedExternally = false;
        transported.clearFanProcessingData();

        return true;
    }

    private static void awardRollingFire(
            Level level,
            BeltBlockEntity controller,
            TransportedItemStack transported
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        MinecraftServer server = serverLevel.getServer();

        AdvancementHolder advancement =
                server.getAdvancements().get(
                        ROLLING_FIRE_ADVANCEMENT
                );

        if (advancement == null) {
            return;
        }

        Vec3 completionPosition =
                BeltHelper.getVectorForOffset(
                        controller,
                        transported.beltPosition
                );

        for (ServerPlayer player : serverLevel.getPlayers(candidate ->
                !candidate.isSpectator()
                        && candidate.position().distanceToSqr(
                        completionPosition
                ) <= ROLLING_FIRE_RADIUS_SQUARED
        )) {
            if (player.getAdvancements()
                    .getOrStartProgress(advancement)
                    .isDone()) {
                continue;
            }

            player.getAdvancements().award(
                    advancement,
                    "processed_item"
            );
        }
    }

    private static void clearCombustionBeltExposure(
            ItemStack stack
    ) {
        CustomData customData = stack.get(
                DataComponents.CUSTOM_DATA
        );

        if (customData == null) {
            return;
        }

        CompoundTag root = customData.copyTag();

        if (!root.contains(
                MOD_ROOT_KEY,
                Tag.TAG_COMPOUND
        )) {
            return;
        }

        CompoundTag modRoot =
                root.getCompound(MOD_ROOT_KEY);

        if (!modRoot.contains(
                EXPOSURE_ROOT_KEY,
                Tag.TAG_COMPOUND
        )) {
            return;
        }

        modRoot.remove(EXPOSURE_ROOT_KEY);

        if (modRoot.isEmpty()) {
            root.remove(MOD_ROOT_KEY);
        } else {
            root.put(MOD_ROOT_KEY, modRoot);
        }

        if (root.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(
                    DataComponents.CUSTOM_DATA,
                    CustomData.of(root)
            );
        }
    }

    private static void spawnHeatingParticle(
            Level level,
            BeltBlockEntity controller,
            TransportedItemStack transported,
            long gameTime,
            ItemStack particleStack
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || particleStack.isEmpty()) {
            return;
        }

        int interval = 8;

        int particlePhase = Math.floorMod(
                System.identityHashCode(transported),
                interval
        );

        if ((gameTime + particlePhase)
                % interval != 0L) {
            return;
        }

        Vec3 itemPosition =
                BeltHelper.getVectorForOffset(
                        controller,
                        transported.beltPosition
                );

        double velocityX =
                (serverLevel.random.nextDouble() - 0.5D)
                        * 0.025D;

        double velocityZ =
                (serverLevel.random.nextDouble() - 0.5D)
                        * 0.025D;

        serverLevel.sendParticles(
                itemParticle(particleStack),
                itemPosition.x,
                itemPosition.y + 0.43D,
                itemPosition.z,
                0,
                velocityX,
                0.045D,
                velocityZ,
                1.0D
        );
    }

    private static void spawnCompletionBurst(
            Level level,
            BeltBlockEntity controller,
            TransportedItemStack transported,
            ItemStack particleStack
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || particleStack.isEmpty()) {
            return;
        }

        Vec3 itemPosition =
                BeltHelper.getVectorForOffset(
                        controller,
                        transported.beltPosition
                );

        for (int particle = 0; particle < 4; particle++) {
            double velocityX =
                    (serverLevel.random.nextDouble() - 0.5D)
                            * 0.065D;

            double velocityY =
                    0.055D
                            + serverLevel.random.nextDouble()
                            * 0.035D;

            double velocityZ =
                    (serverLevel.random.nextDouble() - 0.5D)
                            * 0.065D;

            serverLevel.sendParticles(
                    itemParticle(particleStack),
                    itemPosition.x,
                    itemPosition.y + 0.45D,
                    itemPosition.z,
                    0,
                    velocityX,
                    velocityY,
                    velocityZ,
                    1.0D
            );
        }
    }

    private static ItemParticleOption itemParticle(
            ItemStack stack
    ) {
        ItemStack visualStack = stack.copy();
        visualStack.setCount(1);

        return new ItemParticleOption(
                ParticleTypes.ITEM,
                visualStack
        );
    }

    private static MoltenRotorBlockEntity.RotorHeatLevel
    getHeatAtCurrentPosition(
            Level level,
            BeltBlockEntity controller,
            float beltPosition,
            MoltenRotorBlockEntity.RotorHeatLevel
                    liveChainHeatTier
    ) {
        if (controller.beltLength <= 0
                || liveChainHeatTier
                == MoltenRotorBlockEntity
                .RotorHeatLevel.NONE) {
            return MoltenRotorBlockEntity
                    .RotorHeatLevel.NONE;
        }

        int segment = Math.clamp(
                (int) Math.floor(beltPosition),
                0,
                controller.beltLength - 1
        );

        return getSegmentHeat(
                level,
                controller,
                segment,
                liveChainHeatTier
        ).heatTier();
    }

    private static SegmentHeat getSegmentHeat(
            Level level,
            BeltBlockEntity controller,
            int segment,
            MoltenRotorBlockEntity.RotorHeatLevel
                    liveChainHeatTier
    ) {
        if (liveChainHeatTier == null
                || liveChainHeatTier
                == MoltenRotorBlockEntity
                .RotorHeatLevel.NONE
                || segment < 0
                || segment >= controller.beltLength) {
            return SegmentHeat.NONE;
        }

        BlockPos segmentPosition =
                BeltHelper.getPositionForOffset(
                        controller,
                        segment
                );

        if (!level.isLoaded(segmentPosition)) {
            return SegmentHeat.NONE;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(segmentPosition);

        if (!(blockEntity
                instanceof CombustionBeltAccessor accessor)
                || !accessor
                .sulfuricresonance$isCombustionBelt()) {
            return SegmentHeat.NONE;
        }

        return new SegmentHeat(
                segmentPosition.immutable(),
                liveChainHeatTier
        );
    }

    public enum ExposureBand {
        HEATED("Heated", 1),
        SUPERHEATED("Superheated", 3),
        COMBUSTION("Combustion", 4);

        private final String nbtKey;
        private final int minimumRank;

        ExposureBand(
                String nbtKey,
                int minimumRank
        ) {
            this.nbtKey = nbtKey;
            this.minimumRank = minimumRank;
        }

        public String nbtKey() {
            return this.nbtKey;
        }

        public boolean accepts(
                MoltenRotorBlockEntity.RotorHeatLevel heatTier
        ) {
            return heatTier != null
                    && heatTier.rank
                    >= this.minimumRank;
        }
    }

    private record SegmentHeat(
            BlockPos position,
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    ) {
        private static final SegmentHeat NONE =
                new SegmentHeat(
                        BlockPos.ZERO,
                        MoltenRotorBlockEntity.RotorHeatLevel.NONE
                );
    }

    private record UpdateResult(
            boolean persistentDataChanged,
            boolean inventoryChanged
    ) {
        private static final UpdateResult NONE =
                new UpdateResult(false, false);
    }

    private static final class ExposureDocument {
        private final CompoundTag root;
        private final CompoundTag modRoot;
        private final CompoundTag exposureRoot;

        private ExposureDocument(
                CompoundTag root,
                CompoundTag modRoot,
                CompoundTag exposureRoot
        ) {
            this.root = root;
            this.modRoot = modRoot;
            this.exposureRoot = exposureRoot;
        }

        private static ExposureDocument read(
                ItemStack stack
        ) {
            CustomData customData = stack.getOrDefault(
                    DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY
            );

            CompoundTag root = customData.copyTag();

            CompoundTag modRoot = root.contains(
                    MOD_ROOT_KEY,
                    Tag.TAG_COMPOUND
            )
                    ? root.getCompound(MOD_ROOT_KEY)
                    : new CompoundTag();

            CompoundTag exposureRoot =
                    modRoot.contains(
                            EXPOSURE_ROOT_KEY,
                            Tag.TAG_COMPOUND
                    )
                            ? modRoot.getCompound(
                            EXPOSURE_ROOT_KEY
                    )
                            : new CompoundTag();

            return new ExposureDocument(
                    root,
                    modRoot,
                    exposureRoot
            );
        }

        private BandState getBand(
                ExposureBand band
        ) {
            CompoundTag bandTag =
                    this.exposureRoot.contains(
                            band.nbtKey(),
                            Tag.TAG_COMPOUND
                    )
                            ? this.exposureRoot.getCompound(
                            band.nbtKey()
                    )
                            : new CompoundTag();

            return new BandState(
                    band,
                    this.exposureRoot,
                    bandTag
            );
        }

        private int processingTicksFor(
                ResourceLocation recipeId
        ) {
            if (!this.exposureRoot
                    .getString(PROCESSING_RECIPE_KEY)
                    .equals(recipeId.toString())) {
                return 0;
            }

            return Math.max(
                    0,
                    this.exposureRoot.getInt(
                            PROCESSING_TICKS_KEY
                    )
            );
        }

        private boolean touchProcessing(
                ResourceLocation recipeId,
                int processingTicks,
                long gameTime
        ) {
            String recipeIdString =
                    recipeId.toString();

            int clampedTicks =
                    Math.max(0, processingTicks);

            boolean changed =
                    !recipeIdString.equals(
                            this.exposureRoot.getString(
                                    PROCESSING_RECIPE_KEY
                            )
                    )
                            || clampedTicks
                            != this.exposureRoot.getInt(
                            PROCESSING_TICKS_KEY
                    )
                            || gameTime
                            != this.exposureRoot.getLong(
                            PROCESSING_LAST_QUALIFIED_TICK_KEY
                    )
                            || gameTime
                            != this.exposureRoot.getLong(
                            PROCESSING_LAST_DECAY_TICK_KEY
                    );

            if (!changed) {
                return false;
            }

            this.exposureRoot.putString(
                    PROCESSING_RECIPE_KEY,
                    recipeIdString
            );

            this.exposureRoot.putInt(
                    PROCESSING_TICKS_KEY,
                    clampedTicks
            );

            this.exposureRoot.putLong(
                    PROCESSING_LAST_QUALIFIED_TICK_KEY,
                    gameTime
            );

            this.exposureRoot.putLong(
                    PROCESSING_LAST_DECAY_TICK_KEY,
                    gameTime
            );

            return true;
        }

        private boolean applyProcessingDecay(
                ResourceLocation recipeId,
                long gameTime
        ) {
            if (!this.exposureRoot
                    .getString(PROCESSING_RECIPE_KEY)
                    .equals(recipeId.toString())) {
                return false;
            }

            int currentTicks =
                    Math.max(
                            0,
                            this.exposureRoot.getInt(
                                    PROCESSING_TICKS_KEY
                            )
                    );

            if (currentTicks == 0) {
                return this.clearProcessing();
            }

            long lastQualifiedTick =
                    this.exposureRoot.getLong(
                            PROCESSING_LAST_QUALIFIED_TICK_KEY
                    );

            if (gameTime < lastQualifiedTick) {
                this.exposureRoot.putLong(
                        PROCESSING_LAST_QUALIFIED_TICK_KEY,
                        gameTime
                );

                this.exposureRoot.putLong(
                        PROCESSING_LAST_DECAY_TICK_KEY,
                        gameTime
                );

                return true;
            }

            if (gameTime - lastQualifiedTick
                    < PROGRESS_RESET_GRACE_TICKS) {
                return false;
            }

            return this.clearAllExposure();
        }

        private boolean retainOnly(
                ExposureBand requiredBand
        ) {
            boolean changed = false;

            for (ExposureBand band : ExposureBand.values()) {
                if (band == requiredBand
                        || !this.exposureRoot.contains(
                        band.nbtKey()
                )) {
                    continue;
                }

                this.exposureRoot.remove(
                        band.nbtKey()
                );

                changed = true;
            }

            return changed;
        }

        private boolean clearAllExposure() {
            if (this.exposureRoot.isEmpty()) {
                return false;
            }

            for (String key
                    : this.exposureRoot.getAllKeys().toArray(
                    String[]::new
            )) {
                this.exposureRoot.remove(key);
            }

            return true;
        }

        private boolean clearProcessing() {
            boolean changed =
                    this.exposureRoot.contains(
                            PROCESSING_RECIPE_KEY
                    )
                            || this.exposureRoot.contains(
                            PROCESSING_TICKS_KEY
                    )
                            || this.exposureRoot.contains(
                            PROCESSING_LAST_QUALIFIED_TICK_KEY
                    )
                            || this.exposureRoot.contains(
                            PROCESSING_LAST_DECAY_TICK_KEY
                    );

            this.exposureRoot.remove(
                    PROCESSING_RECIPE_KEY
            );

            this.exposureRoot.remove(
                    PROCESSING_TICKS_KEY
            );

            this.exposureRoot.remove(
                    PROCESSING_LAST_QUALIFIED_TICK_KEY
            );

            this.exposureRoot.remove(
                    PROCESSING_LAST_DECAY_TICK_KEY
            );

            return changed;
        }

        private void write(ItemStack stack) {
            for (ExposureBand band
                    : ExposureBand.values()) {

                if (!this.exposureRoot.contains(
                        band.nbtKey(),
                        Tag.TAG_COMPOUND
                )) {
                    continue;
                }

                CompoundTag bandTag =
                        this.exposureRoot.getCompound(
                                band.nbtKey()
                        );

                if (bandTag.getInt(SEGMENTS_KEY) <= 0) {
                    this.exposureRoot.remove(
                            band.nbtKey()
                    );
                }
            }

            if (this.exposureRoot.isEmpty()) {
                this.modRoot.remove(EXPOSURE_ROOT_KEY);
            } else {
                this.modRoot.put(
                        EXPOSURE_ROOT_KEY,
                        this.exposureRoot
                );
            }

            if (this.modRoot.isEmpty()) {
                this.root.remove(MOD_ROOT_KEY);
            } else {
                this.root.put(
                        MOD_ROOT_KEY,
                        this.modRoot
                );
            }

            if (this.root.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
            } else {
                stack.set(
                        DataComponents.CUSTOM_DATA,
                        CustomData.of(this.root)
                );
            }
        }
    }

    private static final class BandState {
        private final ExposureBand band;
        private final CompoundTag exposureRoot;
        private final CompoundTag bandTag;

        private BandState(
                ExposureBand band,
                CompoundTag exposureRoot,
                CompoundTag bandTag
        ) {
            this.band = band;
            this.exposureRoot = exposureRoot;
            this.bandTag = bandTag;
        }

        private int segments() {
            return Math.max(
                    0,
                    this.bandTag.getInt(SEGMENTS_KEY)
            );
        }

        private boolean touch(long gameTime) {
            long previousQualifiedTick =
                    this.bandTag.getLong(
                            LAST_QUALIFIED_TICK_KEY
                    );

            long previousDecayTick =
                    this.bandTag.getLong(
                            LAST_DECAY_TICK_KEY
                    );

            if (previousQualifiedTick == gameTime
                    && previousDecayTick == gameTime) {
                return false;
            }

            this.bandTag.putLong(
                    LAST_QUALIFIED_TICK_KEY,
                    gameTime
            );

            this.bandTag.putLong(
                    LAST_DECAY_TICK_KEY,
                    gameTime
            );

            this.save();
            return true;
        }

        private boolean addUniqueSegment(
                ResourceLocation dimension,
                BlockPos segmentPosition,
                long gameTime
        ) {
            ListTag visitedSegments =
                    this.bandTag.contains(
                            VISITED_SEGMENTS_KEY,
                            Tag.TAG_LIST
                    )
                            ? this.bandTag.getList(
                            VISITED_SEGMENTS_KEY,
                            Tag.TAG_COMPOUND
                    )
                            : new ListTag();

            if (containsSegment(
                    visitedSegments,
                    dimension,
                    segmentPosition
            )) {

                this.touch(gameTime);
                return false;
            }

            CompoundTag visitedSegment =
                    new CompoundTag();

            visitedSegment.putString(
                    DIMENSION_KEY,
                    dimension.toString()
            );

            visitedSegment.putLong(
                    POSITION_KEY,
                    segmentPosition.asLong()
            );

            visitedSegments.add(visitedSegment);

            this.bandTag.put(
                    VISITED_SEGMENTS_KEY,
                    visitedSegments
            );

            this.bandTag.putInt(
                    SEGMENTS_KEY,
                    this.segments() + 1
            );

            this.bandTag.putLong(
                    LAST_QUALIFIED_TICK_KEY,
                    gameTime
            );

            this.bandTag.putLong(
                    LAST_DECAY_TICK_KEY,
                    gameTime
            );

            this.save();
            return true;
        }

        private boolean applyDecay(long gameTime) {
            int currentSegments = this.segments();

            if (currentSegments <= 0) {
                return false;
            }

            long lastQualifiedTick =
                    this.bandTag.getLong(
                            LAST_QUALIFIED_TICK_KEY
                    );

            if (gameTime < lastQualifiedTick) {
                this.bandTag.putLong(
                        LAST_QUALIFIED_TICK_KEY,
                        gameTime
                );

                this.bandTag.putLong(
                        LAST_DECAY_TICK_KEY,
                        gameTime
                );

                this.save();
                return true;
            }

            if (gameTime - lastQualifiedTick
                    < PROGRESS_RESET_GRACE_TICKS) {
                return false;
            }

            this.exposureRoot.remove(
                    this.band.nbtKey()
            );

            return true;
        }

        private void save() {
            this.exposureRoot.put(
                    this.band.nbtKey(),
                    this.bandTag
            );
        }

        private static boolean containsSegment(
                ListTag visitedSegments,
                ResourceLocation dimension,
                BlockPos segmentPosition
        ) {
            String dimensionId =
                    dimension.toString();

            long position =
                    segmentPosition.asLong();

            for (int index = 0;
                 index < visitedSegments.size();
                 index++) {

                CompoundTag visited =
                        visitedSegments.getCompound(index);

                if (position
                        == visited.getLong(POSITION_KEY)
                        && dimensionId.equals(
                        visited.getString(
                                DIMENSION_KEY
                        )
                )) {
                    return true;
                }
            }

            return false;
        }
    }
}