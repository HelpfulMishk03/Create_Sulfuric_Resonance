package io.hxneyw.repo.content.transport;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import io.hxneyw.repo.content.recipes.caddyprocessing.CaddyProcessingRecipe;
import io.hxneyw.repo.content.recipes.caddyprocessing.CaddyProcessingRecipeRegistry;
import io.hxneyw.repo.mixin.SawBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Per-carrier block processing on moving Create belts. */
public final class CaddyBeltProcessing {
    private static final String ROOT_KEY = "CSRBlockProcessing";
    private static final String PENDING_KEY = "CSRPendingStationCapture";
    private static final String PENDING_SEGMENT_KEY = "SegmentIndex";
    private static final String PENDING_DIRECTION_KEY = "BeltPositive";
    private static final String RECIPE_KEY = "Recipe";
    private static final String SEGMENT_KEY = "Segment";
    private static final String SEGMENT_INDEX_KEY = "SegmentIndex";
    private static final String PROGRESS_KEY = "Progress";
    private static final String LAST_TICK_KEY = "LastTick";
    private static final String ANIMATION_KEY = "OwnsSpoutAnimation";
    private static final String PRESS_ANIMATION_KEY = "OwnsPressAnimation";
    private static final String QUEUED_KEY = "QueuedForStation";
    private static final String DIRECTION_KEY = "BeltPositive";
    private static final String TRANSFORMED_KEY = "CargoTransformed";
    private static final ResourceLocation PRECISION_SPRITZER_ID = ResourceLocation.fromNamespaceAndPath(
            "sulfuricresonance", "precision_spritzer");

    @SuppressWarnings("unused")
    private CaddyBeltProcessing() { }

    /**
     * Records a station reached by Create's upcoming-step scan. The carrier is
     * allowed to finish that normal belt step; it is captured on the next tick
     * at the position Create actually moved it to.
     */
    public static boolean deferArrival(Level level, BeltBlockEntity belt,
                                       TransportedItemStack transported,
                                       int segmentIndex, boolean beltMovementPositive) {
        if (!(transported.stack.getItem() instanceof BlockCaddyItem)) return false;
        ItemStack cargo = BlockCaddyItem.getCargoStack(transported.stack);
        if (isUnprocessableCargo(transported.stack, cargo)) return false;

        BlockPos segmentPos = BeltHelper.getPositionForOffset(belt, segmentIndex);
        if (findPrecisionSpritzer(level, segmentPos, cargo) != null) {
            writePendingCapture(transported.stack, segmentIndex, beltMovementPositive);
            belt.notifyUpdate();
            return true;
        }
        for (RecipeHolder<CaddyProcessingRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(CaddyProcessingRecipeRegistry.TYPE.get())) {
            CaddyProcessingRecipe recipe = holder.value();
            // Combustion Belt work follows the belt's ordinary moving
            // exposure path; it must never reserve or pull a carrier.
            if (recipe.processor() == CaddyProcessingRecipe.Processor.COMBUSTION_BELT) continue;
            Station station = findStation(level, segmentPos, recipe.processor());
            if (station != null && recipe.matches(new SingleRecipeInput(cargo), level)
                    && meetsHeat(recipe, station)) {
                writePendingCapture(transported.stack, segmentIndex, beltMovementPositive);
            belt.notifyUpdate();
                return true;
            }
        }
        return false;
    }

    public static boolean hasPendingCapture(ItemStack stack) {
        return pendingCaptureData(stack) != null;
    }

    /** Captures a deferred arrival only after Create has moved it past the station center. */
    public static void capturePendingArrival(Level level, BeltBlockEntity belt,
                                                BeltInventory inventory,
                                                TransportedItemStack transported) {
        CompoundTag pending = pendingCaptureData(transported.stack);
        if (pending == null) return;

        int segmentIndex = pending.getInt(PENDING_SEGMENT_KEY);
        boolean positive = pending.getBoolean(PENDING_DIRECTION_KEY);
        float center = segmentIndex + .5f;
        float position = transported.beltPosition;
        io.hxneyw.repo.CreateSulfuricResonance.LOGGER.info(
                "CSR_ARRIVAL controller={} stationIndex={} current={} previous={} center={}",
                belt.getBlockPos(), segmentIndex, position, transported.prevBeltPosition, center);
        boolean crossedCenter = positive ? position >= center : position <= center;
        if (!crossedCenter) return;
        if (Math.abs(position - center) > .5f) {
            clearPendingCapture(transported.stack);
            return;
        }

        clearPendingCapture(transported.stack);
        startAt(level, belt, inventory, transported, segmentIndex, positive);
    }

    /** Called when a carrier is already at the station center. */
    public static boolean startAt(Level level, BeltBlockEntity belt, BeltInventory inventory,
                                  TransportedItemStack transported,
                                  int segmentIndex, boolean beltMovementPositive) {
        if (!(transported.stack.getItem() instanceof BlockCaddyItem)) return false;
        ItemStack cargo = BlockCaddyItem.getCargoStack(transported.stack);
        if (isUnprocessableCargo(transported.stack, cargo)) return false;

        BlockPos segmentPos = BeltHelper.getPositionForOffset(belt, segmentIndex);
        Station spritzer = findPrecisionSpritzer(level, segmentPos, cargo);
        if (spritzer != null) {
            boolean queued = stationOccupied(inventory, transported, segmentPos, segmentIndex)
                    || queueHasPriority(inventory, transported, segmentPos);
            if (transported.stack.getCount() > 1) {
                TransportedItemStack remainder = transported.getSimilar();
                remainder.stack = transported.stack.copyWithCount(transported.stack.getCount() - 1);
                remainder.locked = false;
                remainder.lockedExternally = false;
                transported.stack = transported.stack.copyWithCount(1);
                inventory.addItem(remainder);
            }
            writeProcessing(transported.stack, PRECISION_SPRITZER_ID, segmentPos, 0,
                    level.getGameTime(), false, false, queued, beltMovementPositive, segmentIndex);
            transported.locked = true;
            belt.notifyUpdate();
            return true;
        }
        for (RecipeHolder<CaddyProcessingRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(CaddyProcessingRecipeRegistry.TYPE.get())) {
            CaddyProcessingRecipe recipe = holder.value();
            if (recipe.processor() == CaddyProcessingRecipe.Processor.COMBUSTION_BELT) continue;
            Station station = findStation(level, segmentPos, recipe.processor());
            if (station == null || !recipe.matches(new SingleRecipeInput(cargo), level)
                    || !meetsHeat(recipe, station)) continue;
            // A belt carrier can hold a stack of identical Caddy items. Split
            // one carrier off for this operation so a single recipe cycle can
            // never transform every Caddy in that stack at once.
            if (transported.stack.getCount() > 1) {
                TransportedItemStack queued = transported.getSimilar();
                int remainderCount = transported.stack.getCount() - 1;
                queued.stack = transported.stack.copyWithCount(remainderCount);
                queued.locked = false;
                queued.lockedExternally = false;
                transported.stack = transported.stack.copyWithCount(1);
                inventory.addItem(queued);
            }

            // Keep arrivals FIFO even for processors with no Create animation
            // signal (fans, washing, heating). Otherwise a later Caddy could
            // start beside an older queued carrier as soon as the original
            // occupant leaves the station.
            boolean queued = stationOccupied(inventory, transported, segmentPos, segmentIndex)
                    || queueHasPriority(inventory, transported, segmentPos)
                    || machineAnimationBusy(station);
            boolean hasResources = hasFluid(level, station.position(), recipe);
            boolean animationStarted = !queued && hasResources && startMachineAnimation(station, cargo);
            boolean pressStarted = !queued && pressAnimationActive(station);
            writeProcessing(transported.stack, holder.id(), segmentPos, 0, level.getGameTime(),
                    animationStarted, pressStarted, queued, beltMovementPositive, segmentIndex);
            transported.locked = true;
            belt.notifyUpdate();
            return true;
        }
        return false;
    }

    private static boolean stationOccupied(BeltInventory inventory, TransportedItemStack current,
                                            BlockPos segmentPos, int segmentIndex) {
        // A station is claimed by an active carrier at its center or by a
        // non-Caddy belt item physically occupying the trigger area. Waiting
        // Caddies remain upstream and are ignored here so the queue can drain.
        for (TransportedItemStack other : inventory.getTransportedItems()) {
            if (other == current || other.stack.isEmpty()) continue;
            CompoundTag data = processingData(other.stack);
            if (data != null) {
                if (!data.getBoolean(QUEUED_KEY)
                        && BlockPos.of(data.getLong(SEGMENT_KEY)).equals(segmentPos)) return true;
                continue;
            }
            if (Math.abs(other.beltPosition - (segmentIndex + .5f)) <= .35f) return true;
        }
        return false;
    }

    /** Advances or completes a Caddy already held at a processor. */
    public static boolean tickHeld(Level level, BeltBlockEntity belt, BeltInventory inventory,
                                   TransportedItemStack transported) {
        if (!(transported.stack.getItem() instanceof BlockCaddyItem)) return false;
        CompoundTag data = processingData(transported.stack);
        if (data == null) return false;

        ResourceLocation recipeId = ResourceLocation.tryParse(data.getString(RECIPE_KEY));
        BlockPos segmentPos = BlockPos.of(data.getLong(SEGMENT_KEY));
        if (PRECISION_SPRITZER_ID.equals(recipeId)) {
            int segmentIndex = data.getInt(SEGMENT_INDEX_KEY);
            if (data.getBoolean(QUEUED_KEY)) {
                if (stationOccupied(inventory, transported, segmentPos, segmentIndex)
                        || queueHasPriority(inventory, transported, segmentPos)) return true;
                boolean beltPositive = data.getBoolean(DIRECTION_KEY);
                writeProcessing(transported.stack, PRECISION_SPRITZER_ID, segmentPos, 0,
                        level.getGameTime(), false, false, false, beltPositive, segmentIndex);
                belt.notifyUpdate();
                return true;
            }
            if (!(level.getBlockEntity(segmentPos.above(1)) instanceof PerforatedSpritzerBlockEntity)
                    && !(level.getBlockEntity(segmentPos.above(2)) instanceof PerforatedSpritzerBlockEntity)
                    && !(level.getBlockEntity(segmentPos.above(3)) instanceof PerforatedSpritzerBlockEntity)) {
                clearProcessing(transported.stack);
                transported.locked = false;
                belt.notifyUpdate();
                return true;
            }
            if (data.getBoolean(TRANSFORMED_KEY)) {
                if (precisionSpritzerBusy(level, segmentPos)) return true;
                clearProcessing(transported.stack);
                transported.locked = false;
                belt.setChanged();
                belt.notifyUpdate();
            }
            return true;
        }
        RecipeHolder<CaddyProcessingRecipe> holder = recipeId == null ? null
                : level.getRecipeManager().getAllRecipesFor(CaddyProcessingRecipeRegistry.TYPE.get())
                        .stream().filter(found -> found.id().equals(recipeId)).findFirst().orElse(null);
        if (holder == null || !belt.getBlockPos().equals(belt.getController())) {
            finishRecordedAnimation(level, segmentPos, data);
            clearProcessing(transported.stack);
            transported.locked = false;
            belt.notifyUpdate();
            return true;
        }

        CaddyProcessingRecipe recipe = holder.value();
        if (recipe.processor() == CaddyProcessingRecipe.Processor.COMBUSTION_BELT) {
            // Release carriers saved by older builds; moving exposure now owns
            // this recipe and will transform the cargo when its requirements
            // are met.
            clearProcessing(transported.stack);
            transported.locked = false;
            belt.notifyUpdate();
            return true;
        }
        ItemStack cargo = BlockCaddyItem.getCargoStack(transported.stack);
        Station station = findStation(level, segmentPos, recipe.processor());
        if (data.getBoolean(TRANSFORMED_KEY)) {
            if (station != null && machineAnimationBusy(station)) return true;
            clearProcessing(transported.stack);
            transported.locked = false;
            belt.setChanged();
            belt.notifyUpdate();
            return true;
        }
        if (isUnprocessableCargo(transported.stack, cargo) || station == null
                || !recipe.matches(new SingleRecipeInput(cargo), level)) {
            finishMachineAnimation(station, data.getBoolean(ANIMATION_KEY));
            clearProcessing(transported.stack);
            transported.locked = false;
            belt.notifyUpdate();
            return true;
        }
        // Keep an Ashesil carrier parked if the heat source temporarily drops
        // below the recipe tier. It resumes its single-block timer when heat returns.
        if (!meetsHeat(recipe, station)) return true;

        if (!hasFluid(level, station.position(), recipe)) return true;

        if (data.getBoolean(QUEUED_KEY)) {
            int segment = data.getInt(SEGMENT_INDEX_KEY);
            // Other waiting carriers do not block the active one. Once the
            // station is free, the first queued carrier to reach this tick
            // starts; the rest remain locked upstream and retry every tick.
            if (stationOccupied(inventory, transported, segmentPos, segment)
                    || queueHasPriority(inventory, transported, segmentPos)
                    || machineAnimationBusy(station)) return true;
            ItemStack queuedCargo = BlockCaddyItem.getCargoStack(transported.stack);
            boolean animationStarted = hasFluid(level, station.position(), recipe)
                    && startMachineAnimation(station, queuedCargo);
            boolean pressStarted = pressAnimationActive(station);
            boolean beltPositive = data.getBoolean(DIRECTION_KEY);
            writeProcessing(transported.stack, holder.id(), segmentPos, 0, level.getGameTime(),
                    animationStarted, pressStarted, false, beltPositive, segment);
            belt.notifyUpdate();
            return true;
        }

        long gameTime = level.getGameTime();
        int progress = Math.max(0, data.getInt(PROGRESS_KEY));
        long lastTick = data.getLong(LAST_TICK_KEY);
        boolean pressAnimation = data.getBoolean(PRESS_ANIMATION_KEY);
        if (recipe.processor() == CaddyProcessingRecipe.Processor.SPOUT
                && !data.getBoolean(ANIMATION_KEY)) {
            boolean started = startMachineAnimation(station, cargo);
            if (started) {
                writeProcessing(transported.stack, holder.id(), segmentPos, progress, gameTime,
                        true, pressAnimation, false, data.getBoolean(DIRECTION_KEY),
                        data.getInt(SEGMENT_INDEX_KEY));
                belt.notifyUpdate();
                return true;
            }
        }
        if (pressAnimation) {
            PressingBehaviour pressing = station.entity() instanceof MechanicalPressBlockEntity press
                    ? press.getPressingBehaviour() : null;
            // The Caddy operation completes on the exact server tick Create's
            // press cycle ends; the recipe timer must not move cargo early.
            // Create applies a belt press recipe at the bottom of the stroke,
            // when runningTicks reaches CYCLE / 2. Transform the carried block
            // on that contact tick instead of waiting for the head to retract.
            if (pressing == null || !pressing.running
                    || pressing.runningTicks < PressingBehaviour.CYCLE / 2) return true;
        } else if (gameTime > lastTick) {
            progress = Math.min(recipe.processingTime(), progress + 1);
            if (progress % 5 == 0) {
                emitProcessingParticles(level, segmentPos, recipe.processor());
            }
            writeProcessing(transported.stack, holder.id(), segmentPos, progress, gameTime,
                    data.getBoolean(ANIMATION_KEY), false, false, data.getBoolean(DIRECTION_KEY),
                    data.getInt(SEGMENT_INDEX_KEY));
            belt.setChanged();
        }
        if (!pressAnimation && progress < recipe.processingTime()) return true;

        ItemStack result = recipe.result();
        if (result.isEmpty() || result.getCount() != 1 || !(result.getItem() instanceof BlockItem)) {
            finishMachineAnimation(station, data.getBoolean(ANIMATION_KEY));
            clearProcessing(transported.stack);
            transported.locked = false;
            belt.notifyUpdate();
            return true;
        }
        if (!drainFluid(level, station.position(), recipe)) {
            return true;
        }
        preserveProperties(cargo, result);
        BlockCaddyItem.setCargoStack(transported.stack, result);
        if (pressAnimation || (data.getBoolean(ANIMATION_KEY) && machineAnimationBusy(station))) {
            markCargoTransformed(transported.stack);
            belt.setChanged();
            belt.notifyUpdate();
            return true;
        }
        clearProcessing(transported.stack);
        transported.prevBeltPosition = transported.beltPosition;
        transported.locked = false;
        belt.setChanged();
        belt.notifyUpdate();
        return true;
    }

    private static boolean isUnprocessableCargo(ItemStack caddy, ItemStack cargo) {
        return cargo.isEmpty()
                || !(cargo.getItem() instanceof BlockItem)
                || BlockCaddyItem.isStorageCargo(caddy)
                // A payload means a block entity carries state that this recipe
                // has not explicitly promised to preserve.
                || cargo.get(DataComponents.BLOCK_ENTITY_DATA) != null;
    }

    private static Station findPrecisionSpritzer(Level level, BlockPos segmentPos, ItemStack cargo) {
        for (int height = 1; height <= 3; height++) {
            BlockEntity entity = level.getBlockEntity(segmentPos.above(height));
            if (entity instanceof PerforatedSpritzerBlockEntity spritzer
                    && spritzer.isPrecisionMode()
                    && spritzer.hasPrecisionResult(cargo)) {
                return new Station(entity.getBlockPos(), entity, 0);
            }
        }
        return null;
    }

    private static boolean precisionSpritzerBusy(Level level, BlockPos segmentPos) {
        for (int height = 1; height <= 3; height++) {
            BlockEntity entity = level.getBlockEntity(segmentPos.above(height));
            if (entity instanceof PerforatedSpritzerBlockEntity spritzer && spritzer.isSpraying()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPrecisionProcessing(ItemStack stack) {
        CompoundTag data = processingData(stack);
        return data != null && PRECISION_SPRITZER_ID.toString().equals(data.getString(RECIPE_KEY));
    }

    public static void markPrecisionTransformed(ItemStack stack) {
        markCargoTransformed(stack);
    }

    /** Records one successful precision spray against a held Caddy. */
    public static int recordPrecisionSpray(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0;
        CompoundTag root = customData.copyTag();
        if (!root.contains(ROOT_KEY, Tag.TAG_COMPOUND)) return 0;
        CompoundTag data = root.getCompound(ROOT_KEY);
        if (!PRECISION_SPRITZER_ID.toString().equals(data.getString(RECIPE_KEY))
                || data.getBoolean(TRANSFORMED_KEY)) return 0;
        int progress = Math.max(0, data.getInt(PROGRESS_KEY)) + 1;
        data.putInt(PROGRESS_KEY, progress);
        root.put(ROOT_KEY, data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return progress;
    }

    /** Starts Create's own machine feedback while the Caddy-specific recipe runs. */
    private static boolean startMachineAnimation(Station station, ItemStack cargo) {
        if (station.entity() instanceof MechanicalPressBlockEntity press) {
            PressingBehaviour pressing = press.getPressingBehaviour();
            if (pressing != null && !pressing.running) {
                pressing.start(PressingBehaviour.Mode.BELT);
                pressing.particleItems.add(cargo.copy());
                press.sendData();
            }
        } else if (station.entity() instanceof SawBlockEntity saw) {
            ((SawBlockEntityAccessor) saw).sulfuricresonance$setPlayEvent(cargo.copy());
            saw.sendData();
        } else if (station.entity() instanceof SpoutBlockEntity spout
                && spout.processingTicks < 0) {
            spout.processingTicks = SpoutBlockEntity.FILLING_TIME;
            spout.notifyUpdate();
            AllSoundEvents.SPOUTING.playOnServer(spout.getLevel(), spout.getBlockPos(),
                    0.75f, 0.9f);
            return true;
        }
        return false;
    }

    private static boolean machineAnimationBusy(Station station) {
        return pressAnimationActive(station)
                || station.entity() instanceof SpoutBlockEntity spout && spout.processingTicks >= 0;
    }

    private static boolean pressAnimationActive(Station station) {
        return station.entity() instanceof MechanicalPressBlockEntity press
                && press.getPressingBehaviour() != null && press.getPressingBehaviour().running;
    }

    private static boolean queueHasPriority(BeltInventory inventory, TransportedItemStack current,
                                            BlockPos segmentPos) {
        // BeltInventory keeps carriers in travel order (front to back), reversing
        // the list when the belt reverses. Position comparisons are not enough:
        // two queued carriers can be clamped to exactly the same float position,
        // which made both believe they had priority and let press cooldown timing
        // decide which one got the next operation. Use Create's ordering as the
        // FIFO tie-breaker, independent of the float coordinates.
        boolean reachedCurrent = false;
        for (TransportedItemStack other : inventory.getTransportedItems()) {
            if (other == current) {
                reachedCurrent = true;
                continue;
            }
            if (reachedCurrent || other.stack.isEmpty()) continue;
            CompoundTag queued = processingData(other.stack);
            if (queued == null || !queued.getBoolean(QUEUED_KEY)
                    || !BlockPos.of(queued.getLong(SEGMENT_KEY)).equals(segmentPos)) continue;
            return true;
        }
        return false;
    }

    private static void emitProcessingParticles(Level level, BlockPos segmentPos,
                                                CaddyProcessingRecipe.Processor processor) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        var particle = switch (processor) {
            case FAN_BLASTING, COMBUSTION_BELT -> ParticleTypes.FLAME;
            case FAN_HAUNTING -> ParticleTypes.SOUL_FIRE_FLAME;
            case FAN_SMOKING -> ParticleTypes.LARGE_SMOKE;
            case FAN_SPLASHING -> ParticleTypes.SPLASH;
            case FAN_COOLING -> ParticleTypes.SNOWFLAKE;
            default -> null;
        };
        if (particle == null) return;
        serverLevel.sendParticles(particle,
                segmentPos.getX() + .5, segmentPos.getY() + .8, segmentPos.getZ() + .5,
                3, .18, .08, .18, .01);
    }

    private static void finishMachineAnimation(Station station, boolean startedByCaddy) {
        if (startedByCaddy && station != null && station.entity() instanceof SpoutBlockEntity spout) {
            spout.processingTicks = -1;
            spout.notifyUpdate();
        }
    }

    private static void finishRecordedAnimation(Level level, BlockPos segmentPos, CompoundTag data) {
        if (!data.getBoolean(ANIMATION_KEY)) return;
        BlockEntity entity = level.getBlockEntity(segmentPos.above(2));
        if (entity instanceof SpoutBlockEntity spout) {
            spout.processingTicks = -1;
            spout.notifyUpdate();
        }
    }

    private static boolean meetsHeat(CaddyProcessingRecipe recipe, Station station) {
        return recipe.minimumHeatRank() <= 0 || station.heatRank() >= recipe.minimumHeatRank();
    }

    @Nullable
    private static Station findStation(Level level, BlockPos segmentPos,
                                       CaddyProcessingRecipe.Processor processor) {
        if (processor == CaddyProcessingRecipe.Processor.COMBUSTION_BELT) {
            BlockEntity entity = level.getBlockEntity(segmentPos);
            if (entity instanceof CombustionBeltAccessor accessor
                    && accessor.sulfuricresonance$isCombustionBelt()) {
                return new Station(segmentPos, entity,
                        accessor.sulfuricresonance$getReceivedHeatTier().rank);
            }
            return null;
        }

        BlockEntity machine = level.getBlockEntity(segmentPos.above(2));
        boolean correctMachine = switch (processor) {
            case PRESS -> machine instanceof MechanicalPressBlockEntity;
            case SAW -> machine instanceof SawBlockEntity;
            case SPOUT -> machine instanceof SpoutBlockEntity;
            default -> false;
        };
        if (correctMachine) return new Station(machine.getBlockPos(), machine, 0);

        return findFanStation(level, segmentPos, processor);
    }

    @Nullable
    private static Station findFanStation(Level level, BlockPos segmentPos,
                                          CaddyProcessingRecipe.Processor processor) {
        for (Direction airflow : Direction.values()) {
            for (int distance = 1; distance <= 8; distance++) {
                for (int verticalOffset = 0; verticalOffset <= 1; verticalOffset++) {
                    BlockPos fanPos = segmentPos.relative(airflow.getOpposite(), distance)
                            .above(verticalOffset);
                    BlockEntity blockEntity = level.getBlockEntity(fanPos);
                    if (!(blockEntity instanceof EncasedFanBlockEntity fan)
                            || fan.getSpeed() == 0 || fan.getAirFlowDirection() != airflow) continue;

                    if (processor == CaddyProcessingRecipe.Processor.FAN_COOLING) {
                        return new Station(fanPos, fan, 0);
                    }
                    FanProcessingType processingType = fan.airCurrent.getTypeAt(distance - 1);
                    if (matchesFanProcessor(processingType, processor)) {
                        return new Station(fanPos, fan, 0);
                    }
                }
            }
        }
        return null;
    }

    private static boolean matchesFanProcessor(@Nullable FanProcessingType type,
                                               CaddyProcessingRecipe.Processor processor) {
        return switch (processor) {
            case FAN_BLASTING -> type == AllFanProcessingTypes.BLASTING;
            case FAN_HAUNTING -> type == AllFanProcessingTypes.HAUNTING;
            case FAN_SMOKING -> type == AllFanProcessingTypes.SMOKING;
            case FAN_SPLASHING -> type == AllFanProcessingTypes.SPLASHING;
            default -> false;
        };
    }

    private static boolean hasFluid(Level level, BlockPos stationPos, CaddyProcessingRecipe recipe) {
        if (recipe.fluidAmount() <= 0) return true;
        Optional<ResourceLocation> fluidId = recipe.fluid();
        if (fluidId.isEmpty()) return false;
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId.get());
        if (fluid == net.minecraft.world.level.material.Fluids.EMPTY) return false;
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, stationPos, null);
        if (handler == null) return false;
        FluidStack drained = handler.drain(
                new FluidStack(fluid, recipe.fluidAmount()), IFluidHandler.FluidAction.SIMULATE);
        return drained.getAmount() >= recipe.fluidAmount() && drained.getFluid() == fluid;
    }

    private static boolean drainFluid(Level level, BlockPos stationPos, CaddyProcessingRecipe recipe) {
        if (recipe.fluidAmount() <= 0) return true;
        if (!hasFluid(level, stationPos, recipe)) return false;
        ResourceLocation fluidId = recipe.fluid().orElseThrow();
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, stationPos, null);
        if (handler == null) return false;
        FluidStack drained = handler.drain(new FluidStack(fluid, recipe.fluidAmount()), IFluidHandler.FluidAction.EXECUTE);
        if (drained.getFluid() == fluid && drained.getAmount() >= recipe.fluidAmount()) return true;
        if (!drained.isEmpty()) handler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        return false;
    }

    private static void preserveProperties(ItemStack input, ItemStack output) {
        BlockState inputState = BlockCaddyItem.getCargoBlockState(BlockCaddyItem.createStack(input));
        BlockState outputState = ((BlockItem) output.getItem()).getBlock().defaultBlockState();
        Map<String, String> properties = new HashMap<>();
        for (Property<?> targetProperty : outputState.getBlock().getStateDefinition().getProperties()) {
            Property<?> sourceProperty = inputState.getBlock().getStateDefinition()
                    .getProperty(targetProperty.getName());
            if (sourceProperty == null) continue;
            String value = inputState.getValue(sourceProperty).toString();
            if (targetProperty.getValue(value).isPresent()) properties.put(targetProperty.getName(), value);
        }
        if (!properties.isEmpty()) {
            output.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(properties));
        }
    }

    private static void writeProcessing(ItemStack stack, ResourceLocation recipeId, BlockPos segment,
                                        int progress, long gameTime, boolean animationStarted,
                                        boolean pressAnimation, boolean queued, boolean beltPositive,
                                        int segmentIndex) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = customData == null ? new CompoundTag() : customData.copyTag();
        CompoundTag data = new CompoundTag();
        data.putString(RECIPE_KEY, recipeId.toString());
        data.putLong(SEGMENT_KEY, segment.asLong());
        data.putInt(SEGMENT_INDEX_KEY, segmentIndex);
        data.putInt(PROGRESS_KEY, progress);
        data.putLong(LAST_TICK_KEY, gameTime);
        data.putBoolean(ANIMATION_KEY, animationStarted);
        data.putBoolean(PRESS_ANIMATION_KEY, pressAnimation);
        data.putBoolean(QUEUED_KEY, queued);
        data.putBoolean(DIRECTION_KEY, beltPositive);
        root.put(ROOT_KEY, data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static void writePendingCapture(ItemStack stack, int segmentIndex, boolean beltPositive) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = customData == null ? new CompoundTag() : customData.copyTag();
        CompoundTag pending = new CompoundTag();
        pending.putInt(PENDING_SEGMENT_KEY, segmentIndex);
        pending.putBoolean(PENDING_DIRECTION_KEY, beltPositive);
        root.put(PENDING_KEY, pending);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    @Nullable
    private static CompoundTag pendingCaptureData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag root = customData.copyTag();
        return root.contains(PENDING_KEY, Tag.TAG_COMPOUND) ? root.getCompound(PENDING_KEY) : null;
    }

    private static void clearPendingCapture(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        CompoundTag root = customData.copyTag();
        root.remove(PENDING_KEY);
        stack.set(DataComponents.CUSTOM_DATA, root.isEmpty() ? null : CustomData.of(root));
    }

    @Nullable
    private static CompoundTag processingData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag root = customData.copyTag();
        return root.contains(ROOT_KEY, Tag.TAG_COMPOUND) ? root.getCompound(ROOT_KEY) : null;
    }

    private static void clearProcessing(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        CompoundTag root = customData.copyTag();
        root.remove(ROOT_KEY);
        stack.set(DataComponents.CUSTOM_DATA, root.isEmpty() ? null : CustomData.of(root));
    }

    private static void markCargoTransformed(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        CompoundTag root = customData.copyTag();
        if (!root.contains(ROOT_KEY, Tag.TAG_COMPOUND)) return;
        CompoundTag data = root.getCompound(ROOT_KEY);
        data.putBoolean(TRANSFORMED_KEY, true);
        root.put(ROOT_KEY, data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    @SuppressWarnings("unused")
    private record Station(BlockPos position, BlockEntity entity, int heatRank) { }
}
