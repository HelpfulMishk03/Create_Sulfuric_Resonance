package io.hxneyw.repo.content.blocks;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.Config;
import io.hxneyw.repo.content.blocks.behaviour.CombustionHeatingBehaviour;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MoltenRotorBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {
   private float currentTemperature = 20.0F;
   private float baseHeatingRate = 0.0F;
   private float currentMaxTemp = 0.0F;
   private int remainingBurnTime = 0;
   private int activeFuelCount = 0;
   private int activeLogStickCount = 0;
   private int clientUpdateCounter = 0;
   private int lastNotifiedFuelCount = 0;
   private MoltenRotorBlockEntity.RotorHeatLevel currentHeatTier = MoltenRotorBlockEntity.RotorHeatLevel.NONE;
   private MoltenRotorBlockEntity.RotorHeatLevel lastSentHeatTier = MoltenRotorBlockEntity.RotorHeatLevel.NONE;
   private MoltenRotorBlockEntity.FuelType activeFuelType = MoltenRotorBlockEntity.FuelType.NONE;
   private ItemStack activeFuelStack = ItemStack.EMPTY;
   private boolean creativeMode = false;
   private boolean kineticsInitialized = false;
   private boolean hasLavaInStack = false;
   private boolean hasSulfurInStack = false;
   private boolean hasCoalInStack = false;
   private boolean hasCharcoalInStack = false;
   private boolean hasStickInStack = false;
   private final List<ItemStack> pendingFuel = new ArrayList<>();
   public int tntCooldown = 0;
   public final IItemHandler fuelHandler = new IItemHandler() {
      public int getSlots() {
         return 1;
      }

      @NotNull
      public ItemStack getStackInSlot(int slot) {
         return ItemStack.EMPTY;
      }

      @NotNull
      public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
         MoltenRotorBlockEntity.FuelType fuelType = MoltenRotorBlockEntity.this.getFuelTypeFromItem(stack);
         if (fuelType == null || fuelType == MoltenRotorBlockEntity.FuelType.NONE) {
            return stack;
         } else if (!MoltenRotorBlockEntity.this.insertFuel(stack, simulate)) {
            return stack;
         } else {
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
         }
      }

      @NotNull
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         return ItemStack.EMPTY;
      }

      public int getSlotLimit(int slot) {
         return 64;
      }

      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
         return MoltenRotorBlockEntity.this.insertFuel(stack, true);
      }
   };

   public MoltenRotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public MoltenRotorBlockEntity(BlockPos pos, BlockState state) {
      this(AllBlockEntities.MOLTEN_ROTOR.get(), pos, state);
   }

   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      super.addBehaviours(behaviours);
      behaviours.add(new CombustionHeatingBehaviour(this));
   }

   public float getGeneratedSpeed() {
      float baseSpeed = this.currentHeatTier.rpmCap;
      if (baseSpeed == 0.0F) {
         return 0.0F;
      } else {
         Direction facing =
                 this.getBlockState().getValue(MoltenRotorBlock.FACING);
         return facing != Direction.NORTH && facing != Direction.EAST ? baseSpeed : -baseSpeed;
      }
   }

   public float calculateAddedStressCapacity() {
      float speed = Math.abs(this.getGeneratedSpeed());
      return speed == 0.0F ? 0.0F : (this.lastCapacityProvided = this.currentHeatTier.baseStressCapacity / speed);
   }

   public float calculateStressApplied() {
      return 0.0F;
   }

   public boolean isCreativeMode() {
      return this.creativeMode;
   }

   public float getTotalStressOutput() {
      return this.currentHeatTier.baseStressCapacity;
   }

   public MoltenRotorBlockEntity.RotorHeatLevel getCurrentHeatTier() {
      return this.currentHeatTier;
   }

   public int getDisplayTemperature() {
      return (int)this.currentTemperature;
   }

   public int getDisplayFuelTime() {
      return Math.max(this.remainingBurnTime, 0);
   }

   public String getHeatTierName() {
      return this.currentHeatTier == MoltenRotorBlockEntity.RotorHeatLevel.FADING ? "Heated" : this.currentHeatTier.displayName;
   }

   public boolean isCombustionActive() {
      return this.currentHeatTier.isAtLeast(RotorHeatLevel.SMOULDERING);
   }

   public boolean shouldShowStatus() {
      return this.remainingBurnTime > 0 || this.currentTemperature > 20.0F;
   }

   public void setCreativeMode(boolean creative) {
      if (this.creativeMode == creative)
         return;

      this.creativeMode = creative;
      this.activeFuelStack = ItemStack.EMPTY;
      this.remainingBurnTime = 0;
      this.activeFuelType = FuelType.NONE;
      this.activeFuelCount = 0;
      this.activeLogStickCount = 0;
      this.pendingFuel.clear();
      this.baseHeatingRate = 0.0F;
      this.currentMaxTemp = 0.0F;
      this.clearFuelStackFlags();

      if (creative) {
         this.currentTemperature = 400.0F;
         this.currentHeatTier = RotorHeatLevel.SMOULDERING;
      } else {
         this.currentTemperature = 20.0F;
         this.currentHeatTier = RotorHeatLevel.NONE;
      }

      this.updateBlockVisuals();
      this.notifyKineticNetworkOfChange();
      this.setChanged();
      this.sendData();
   }

   public void cycleCreativeTier() {
      if (this.creativeMode) {
         MoltenRotorBlockEntity.RotorHeatLevel previousTier = this.currentHeatTier;

         this.currentHeatTier = switch (this.currentHeatTier) {
            case NONE, SMOULDERING, FADING -> MoltenRotorBlockEntity.RotorHeatLevel.SEETHING;
            case KINDLED, SEETHING -> MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;
            case RADIANT -> MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING;
         };

         this.currentTemperature = switch (this.currentHeatTier) {
            case SMOULDERING -> 400.0F;
            case SEETHING -> 1000.0F;
            case RADIANT -> 1450.0F;
            default -> 20.0F;
         };
         this.updateBlockVisuals();
         this.notifyKineticNetworkOfChange();
         this.setChanged();
         this.sendData();
         if (this.level instanceof ServerLevel sl
            && this.currentHeatTier == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT
            && previousTier != MoltenRotorBlockEntity.RotorHeatLevel.RADIANT) {
            this.spawnRadiantParticles(sl);
         }
      }
   }

   public void initializeKinetics() {
      if (!this.kineticsInitialized && this.level != null && !this.level.isClientSide) {
         this.kineticsInitialized = true;
         this.attachKinetics();
         this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         this.setChanged();
      }
   }

   public MoltenRotorBlockEntity.FuelType getFuelTypeFromItem(ItemStack stack) {
      if (stack.is(Items.STICK)) {
         return MoltenRotorBlockEntity.FuelType.STICK;
      } else if (stack.is(ItemTags.LOGS)) {
         return MoltenRotorBlockEntity.FuelType.LOG;
      } else if (stack.is(Items.COAL)) {
         return MoltenRotorBlockEntity.FuelType.COAL;
      } else if (stack.is(Items.CHARCOAL)) {
         return MoltenRotorBlockEntity.FuelType.CHARCOAL;
      } else if (stack.is(Items.COAL_BLOCK)) {
         return MoltenRotorBlockEntity.FuelType.COAL_BLOCK;
      } else if (stack.is(Items.DRIED_KELP_BLOCK)) {
         return MoltenRotorBlockEntity.FuelType.KELP_BLOCK;
      } else if (stack.is(Items.TNT)) {
         return MoltenRotorBlockEntity.FuelType.TNT;
      } else if (stack.is(AllItems.BLAZE_CAKE.get())) {
         return MoltenRotorBlockEntity.FuelType.BLAZE_CAKE;
      } else {
         return stack.is(io.hxneyw.repo.content.Items.SOUL_FIRED_BLAZE_CAKE.get()) ? MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE : null;
      }
   }

   public void tick() {
      super.tick();
      if (this.level != null && !this.level.isClientSide) {
         if (this.tntCooldown > 0) {
            this.tntCooldown--;
         }

         if (this.creativeMode) {
            this.updateBlockVisuals();
            this.setChanged();
            if (++this.clientUpdateCounter >= 1) {
               this.clientUpdateCounter = 0;
               this.sendData();
            }
         } else {
            boolean needsUpdate = false;
            MoltenRotorBlockEntity.RotorHeatLevel previousTier = this.currentHeatTier;
            if (this.remainingBurnTime > 0) {
               this.remainingBurnTime--;
               float heatingPerTick = this.baseHeatingRate / 20.0F;
               if (Config.RAIN_AFFECTS_MOLTEN_ROTOR.get() && this.level.isRainingAt(this.worldPosition.above())) {
                  heatingPerTick *= 0.5F;
               }

               float targetTemp = this.calculateMaxStackedTemp();
               if (this.currentTemperature < targetTemp) {
                  this.currentTemperature = this.clampTemperature(Math.min(this.currentTemperature + heatingPerTick, targetTemp));
                  needsUpdate = true;
               }

               if (this.remainingBurnTime == 0) {
                  this.clearActiveFuel();
                  this.startNextPendingFuel();
                  needsUpdate = true;
               }
            } else if (this.currentTemperature > 20.0F) {
               float coolingPerTick = 0.1F;
               if (Config.RAIN_AFFECTS_MOLTEN_ROTOR.get() && this.level.isRainingAt(this.worldPosition.above())) {
                  coolingPerTick *= 2.0F;
               }

               this.currentTemperature = this.clampTemperature(this.currentTemperature - coolingPerTick);
               needsUpdate = true;
            }

            if (this.level.getGameTime() % 100L == 0L) {
               float oldTemp = this.currentTemperature;
               this.currentTemperature = this.clampTemperature(this.currentTemperature);
               if (oldTemp != this.currentTemperature) {
                  needsUpdate = true;
               }
            }

            MoltenRotorBlockEntity.RotorHeatLevel newTier = this.calculateHeatTierFromTemp();
            if (newTier != previousTier) {
               this.currentHeatTier = newTier;
               this.updateBlockVisuals();
               if (this.tierAffectsRotation(previousTier, newTier)) {
                  this.notifyKineticNetworkOfChange();
               }

               needsUpdate = true;
            }

            if (needsUpdate) {
               this.setChanged();
            }

            if (this.currentHeatTier != this.lastSentHeatTier) {
               this.lastSentHeatTier = this.currentHeatTier;
               this.clientUpdateCounter = 0;
               this.sendData();
               if (newTier == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT
                  && previousTier != MoltenRotorBlockEntity.RotorHeatLevel.RADIANT
                  && this.level instanceof ServerLevel sl) {
                  this.spawnRadiantParticles(sl);
               }
            } else if (++this.clientUpdateCounter >= 1) {
               this.clientUpdateCounter = 0;
               this.sendData();
            }
         }
      }
   }

   public int getDisplayCooldownTime() {
      if (!this.creativeMode && !(this.currentTemperature <= 20.0F) && this.remainingBurnTime <= 0) {
         float coolingPerTick = 0.1F;
         if (Config.RAIN_AFFECTS_MOLTEN_ROTOR.get() && this.level != null && this.level.isRainingAt(this.worldPosition.above())) {
            coolingPerTick *= 2.0F;
         }

         return (int)Math.ceil((this.currentTemperature - 300.0F) / coolingPerTick);
      } else {
         return 0;
      }
   }

   private boolean tierAffectsRotation(MoltenRotorBlockEntity.RotorHeatLevel from, MoltenRotorBlockEntity.RotorHeatLevel to) {
      return from == MoltenRotorBlockEntity.RotorHeatLevel.NONE != (to == MoltenRotorBlockEntity.RotorHeatLevel.NONE) || from.rpmCap != to.rpmCap;
   }

   private void notifyKineticNetworkOfChange() {
      if (this.level != null && !this.level.isClientSide && this.kineticsInitialized) {
         super.updateGeneratedRotation();
         this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         this.setChanged();
      }
   }

   private MoltenRotorBlockEntity.RotorHeatLevel calculateHeatTierFromTemp() {
      if (this.currentTemperature < 300.0F) {
         return MoltenRotorBlockEntity.RotorHeatLevel.NONE;
      } else if (this.currentTemperature >= 1300.0F) {
         return MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;
      } else if (this.currentTemperature >= 800.0F) {
         return MoltenRotorBlockEntity.RotorHeatLevel.SEETHING;
      } else if (this.currentTemperature >= 500.0F) {
         return MoltenRotorBlockEntity.RotorHeatLevel.KINDLED;
      } else {
         return this.currentTemperature >= 300.0F && this.currentTemperature < 350.0F && this.remainingBurnTime == 0
            ? MoltenRotorBlockEntity.RotorHeatLevel.FADING
            : MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING;
      }
   }

   private void updateBlockVisuals() {
      if (this.level != null) {
         HeatLevel visualHeat = switch (this.currentHeatTier) {
            case NONE -> HeatLevel.NONE;
            case SMOULDERING, FADING, KINDLED -> HeatLevel.KINDLED;
            case SEETHING, RADIANT -> HeatLevel.SEETHING;
         };
         BlockState state = this.getBlockState();
         if (state.getValue(MoltenRotorBlock.HEAT_LEVEL) != visualHeat) {
            this.level.setBlock(
                    this.worldPosition,
                    state.setValue(MoltenRotorBlock.HEAT_LEVEL, visualHeat),
                    3
            );
            this.level.updateNeighborsAt(this.worldPosition.above(), this.getBlockState().getBlock());
         }
      }
   }

   public boolean insertFuel(ItemStack stack, boolean simulate) {
      if (this.level == null || stack.isEmpty()) {
         return false;
      }

      MoltenRotorBlockEntity.FuelType fuelType = this.getFuelTypeFromItem(stack);
      if (fuelType == null || fuelType == MoltenRotorBlockEntity.FuelType.NONE) {
         return false;
      }

      if (fuelType == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE
         && !this.currentHeatTier.isAtLeast(RotorHeatLevel.SEETHING)) {
         return false;
      }

      if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
         return this.insertLogBoostStick(simulate);
      }

      if (!this.hasActiveOrPendingFuel()) {
         if (!simulate) {
            this.startFuel(stack);
            this.setChanged();
            this.sendData();
         }

         return true;
      }

      if (this.isSpecialFuel(fuelType)) {
         if ((this.activeFuelType != MoltenRotorBlockEntity.FuelType.NONE && this.isSpecialFuel(this.activeFuelType)) || this.hasPendingSpecialFuel()) {
            return false;
         }

         if (!simulate) {
            this.enqueueFuel(stack);
            this.setChanged();
            this.sendData();
         }

         return true;
      }

      MoltenRotorBlockEntity.FuelType normalFuelInUse = this.getNormalFuelInUse();
      if (normalFuelInUse == null || normalFuelInUse != fuelType || this.hasPendingSpecialFuel()) {
         return false;
      }

      if (this.getFuelUnitCount(fuelType) >= fuelType.maxStackSize) {
         return false;
      }

      if (!simulate) {
         this.enqueueFuel(stack);
         this.setChanged();
         this.sendData();
      }

      return true;
   }

   private boolean insertLogBoostStick(boolean simulate) {
      if (this.activeFuelType != MoltenRotorBlockEntity.FuelType.LOG || this.remainingBurnTime <= 0) {
         return false;
      }

      int logUnits = this.getFuelUnitCount(MoltenRotorBlockEntity.FuelType.LOG);
      if (logUnits + this.activeLogStickCount >= 32) {
         return false;
      }

      if (!simulate) {
         this.remainingBurnTime += (int)MoltenRotorBlockEntity.FuelType.STICK.baseBurnTimeTicks;
         this.activeLogStickCount++;
         this.activeFuelCount = logUnits + this.activeLogStickCount;
         this.updateFuelStackFlags(MoltenRotorBlockEntity.FuelType.STICK);
         this.setChanged();
         this.sendData();
      }

      return true;
   }

   private boolean hasActiveOrPendingFuel() {
      return this.activeFuelType != MoltenRotorBlockEntity.FuelType.NONE && this.remainingBurnTime > 0 || !this.pendingFuel.isEmpty();
   }

   private boolean isSpecialFuel(MoltenRotorBlockEntity.FuelType fuelType) {
      return fuelType == MoltenRotorBlockEntity.FuelType.BLAZE_CAKE || fuelType == MoltenRotorBlockEntity.FuelType.SOUL_FIRED_BLAZE_CAKE;
   }

   private MoltenRotorBlockEntity.FuelType getNormalFuelInUse() {
      if (this.activeFuelType != MoltenRotorBlockEntity.FuelType.NONE && !this.isSpecialFuel(this.activeFuelType)) {
         return this.activeFuelType;
      }

      for (ItemStack queuedStack : this.pendingFuel) {
         MoltenRotorBlockEntity.FuelType queuedType = this.getFuelTypeFromItem(queuedStack);
         if (queuedType != null && !this.isSpecialFuel(queuedType)) {
            return queuedType;
         }
      }

      return null;
   }

   private boolean hasPendingSpecialFuel() {
      for (ItemStack queuedStack : this.pendingFuel) {
         MoltenRotorBlockEntity.FuelType queuedType = this.getFuelTypeFromItem(queuedStack);
         if (queuedType != null && this.isSpecialFuel(queuedType)) {
            return true;
         }
      }

      return false;
   }

   private int getFuelUnitCount(MoltenRotorBlockEntity.FuelType fuelType) {
      int count = this.activeFuelType == fuelType && this.remainingBurnTime > 0 ? 1 : 0;

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
         if (ItemStack.isSameItemSameComponents(queuedStack, queuedUnit) && queuedStack.getCount() < queuedStack.getMaxStackSize()) {
            queuedStack.grow(1);
            return;
         }
      }

      this.pendingFuel.add(queuedUnit);
   }

   private void startFuel(ItemStack fuelStack) {
      FuelType fuelType = this.getFuelTypeFromItem(fuelStack);

      if (fuelType == null || fuelType == FuelType.NONE) {
         return;
      }

      this.activeFuelStack = fuelStack.copyWithCount(1);
      this.activeFuelType = fuelType;
      this.activeFuelCount = 1;
      this.activeLogStickCount = 0;
      this.remainingBurnTime = (int) fuelType.baseBurnTimeTicks;
      this.baseHeatingRate = fuelType.celsiusPerSecond;
      this.currentMaxTemp = fuelType.maxTempReachable;

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
      this.baseHeatingRate = this.currentMaxTemp = 0.0F;
      this.clearFuelStackFlags();
   }

   public List<ItemStack> drainPendingFuelForDrop() {
      List<ItemStack> drops = new ArrayList<>();

      for (ItemStack queuedStack : this.pendingFuel) {
         drops.add(queuedStack.copy());
      }

      this.pendingFuel.clear();
      this.setChanged();
      return drops;
   }
   public void addUltimateFuel(int ticks) {
      if (this.level != null) {
         this.remainingBurnTime += ticks;
         this.currentTemperature = this.clampTemperature(Math.max(this.currentTemperature, 1300.0F));
         this.setChanged();
         this.sendData();
      }
   }

   private float clampTemperature(float temp) {
      return Math.clamp(temp, 20.0F, 1599.0F);
   }

   private void updateFuelStackFlags(MoltenRotorBlockEntity.FuelType fuelType) {
      if (fuelType == MoltenRotorBlockEntity.FuelType.COAL) {
         this.hasCoalInStack = true;
      } else if (fuelType == MoltenRotorBlockEntity.FuelType.CHARCOAL) {
         this.hasCharcoalInStack = true;
      } else if (fuelType == MoltenRotorBlockEntity.FuelType.STICK) {
         this.hasStickInStack = true;
      }
   }

   private void clearFuelStackFlags() {
      this.hasLavaInStack = this.hasSulfurInStack = this.hasCoalInStack = this.hasCharcoalInStack = this.hasStickInStack = false;
   }

   private float calculateMaxStackedTemp() {
      return this.hasStickInStack && this.activeFuelType == MoltenRotorBlockEntity.FuelType.LOG ? 550.0F : this.currentMaxTemp;
   }

   private void spawnRadiantParticles(ServerLevel sl) {
      sl.sendParticles(ModParticles.COMBUSTION_PURPLE_FLAME.get(),
         this.worldPosition.getX() + 0.5,
         this.worldPosition.getY() + 0.5,
         this.worldPosition.getZ() + 0.5,
         50,
         0.6,
         0.6,
         0.6,
         0.008
      );
      sl.sendParticles(
         ParticleTypes.SOUL_FIRE_FLAME,
         this.worldPosition.getX() + 0.5,
         this.worldPosition.getY() + 0.5,
         this.worldPosition.getZ() + 0.5,
         20,
         0.4,
         0.4,
         0.4,
         0.005
      );
      sl.sendParticles(
         ParticleTypes.END_ROD, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.008
      );
   }

   private Component getActiveFuelDisplayName() {
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

   private void addQueuedFuelTooltip(List<Component> tooltip) {
      if (this.pendingFuel.isEmpty()) {
         return;
      }

      tooltip.add(
              Component.literal("Queued Fuel: " + this.getQueuedFuelCount())
                      .withStyle(ChatFormatting.GRAY)
      );

      for (ItemStack queuedStack : this.pendingFuel) {
         tooltip.add(
                 Component.literal("  ")
                         .append(queuedStack.getHoverName())
                         .append(Component.literal(" ×" + queuedStack.getCount()))
                         .withStyle(ChatFormatting.DARK_GRAY)
         );
      }
   }

   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      tooltip.add(Component.literal(""));
      if (this.creativeMode) {
         tooltip.add(Component.literal("★ CREATIVE MODE").withStyle(
                 ChatFormatting.LIGHT_PURPLE,
                 ChatFormatting.BOLD
         ));
         tooltip.add(Component.literal("  (Infinite heat - right-click to cycle)").withStyle(ChatFormatting.DARK_GRAY));
      }
      ChatFormatting color = switch (this.currentHeatTier) {
         case NONE -> ChatFormatting.GRAY;
         case SMOULDERING, FADING -> ChatFormatting.YELLOW;
         case KINDLED -> ChatFormatting.RED;
         case SEETHING -> ChatFormatting.DARK_RED;
         case RADIANT -> ChatFormatting.DARK_PURPLE;
      };
      tooltip.add(Component.literal("Heat: " + this.getHeatTierName() + " (" + this.getDisplayTemperature() + "°C)").withStyle(color));
      tooltip.add(
         Component.literal(
               "Stress Capacity: " + (this.currentHeatTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE ? (int)this.getTotalStressOutput() : 0) + " su"
            )
            .withStyle(this.currentHeatTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE ? ChatFormatting.GOLD : ChatFormatting.GRAY)
      );

      if (this.remainingBurnTime > 0
              && this.activeFuelType != FuelType.NONE) {
         tooltip.add(
                 Component.literal("Burning: ")
                         .withStyle(ChatFormatting.GRAY)
                         .append(this.getActiveFuelDisplayName().copy()
                                 .withStyle(ChatFormatting.WHITE))
         );
      }

      this.addQueuedFuelTooltip(tooltip);

      if (this.remainingBurnTime > 0) {
         int sec = this.remainingBurnTime / 20;
         int min = sec / 60;
         int remSec = sec % 60;
         tooltip.add(Component.literal("Fuel Remaining: " + (min > 0 ? min + "m " : "") + remSec + "s").withStyle(ChatFormatting.GREEN));
      } else if (this.getDisplayCooldownTime() > 0) {
         int sec = this.getDisplayCooldownTime() / 20;
         int min = sec / 60;
         int remSec = sec % 60;
         tooltip.add(Component.literal("Cooling Down: " + (min > 0 ? min + "m " : "") + remSec + "s").withStyle(ChatFormatting.YELLOW));
      }

      if (this.currentHeatTier == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT) {
         tooltip.add(Component.literal("✦ RADIANT TIER ACTIVE").withStyle(ChatFormatting.DARK_PURPLE));
         tooltip.add(Component.literal("  (Combustion recipes enabled)").withStyle(ChatFormatting.DARK_GRAY));
      }

      return true;
   }

   protected void write(@NotNull CompoundTag tag, @NotNull Provider provider, boolean clientPacket) {
      super.write(tag, provider, clientPacket);
      tag.putFloat("Temperature", this.currentTemperature);
      tag.putInt("FuelTime", this.remainingBurnTime);
      tag.putString("HeatTier", this.currentHeatTier.serializedId);
      tag.putString("ActiveFuelType", this.activeFuelType.serializedId);
      if (!this.activeFuelStack.isEmpty()) {
         tag.put("ActiveFuelStack", this.activeFuelStack.save(provider));
      }
      tag.putInt("FuelCount", this.activeFuelCount);
      tag.putInt("ActiveLogStickCount", this.activeLogStickCount);
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
      tag.putBoolean("KineticsInit", this.kineticsInitialized);
      tag.putBoolean("CreativeMode", this.creativeMode);
      tag.putInt("LastNotifiedFuel", this.lastNotifiedFuelCount);
   }

   protected void read(@NotNull CompoundTag tag, @NotNull Provider provider, boolean clientPacket) {
      super.read(tag, provider, clientPacket);
      this.currentTemperature = tag.getFloat("Temperature");
      this.remainingBurnTime = tag.getInt("FuelTime");
      this.activeFuelCount = tag.getInt("FuelCount");
      this.activeLogStickCount = tag.getInt("ActiveLogStickCount");
      this.pendingFuel.clear();
      if (tag.contains("PendingFuel", Tag.TAG_LIST)) {
         ListTag pendingFuelTag = tag.getList("PendingFuel", Tag.TAG_COMPOUND);
         for (int i = 0; i < pendingFuelTag.size(); i++) {
            ItemStack queuedStack = ItemStack.parseOptional(provider, pendingFuelTag.getCompound(i));
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
      this.kineticsInitialized = tag.getBoolean("KineticsInit");
      this.creativeMode = tag.getBoolean("CreativeMode");
      this.lastNotifiedFuelCount = tag.getInt("LastNotifiedFuel");
      if (tag.contains("HeatTier", Tag.TAG_STRING)) {
         this.currentHeatTier = RotorHeatLevel.fromSerializedId(tag.getString("HeatTier"));
      } else {
         int tierIndex = tag.getInt("HeatTier");
         this.currentHeatTier = tierIndex >= 0 && tierIndex < RotorHeatLevel.values().length
                 ? RotorHeatLevel.values()[tierIndex]
                 : RotorHeatLevel.NONE;
      }
      if (tag.contains("ActiveFuelType", Tag.TAG_STRING)) {
         this.activeFuelType = MoltenRotorBlockEntity.FuelType.fromSerializedId(tag.getString("ActiveFuelType"));
      } else {
         int fuelIndex = tag.getInt("FuelType");
         this.activeFuelType = fuelIndex >= 0 && fuelIndex < MoltenRotorBlockEntity.FuelType.values().length
            ? MoltenRotorBlockEntity.FuelType.values()[fuelIndex]
            : MoltenRotorBlockEntity.FuelType.NONE;
         this.pendingFuel.clear();
      }
      if (clientPacket) {
         this.lastSentHeatTier = this.currentHeatTier;
         if (this.level != null && this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         }
      }
   }

   public enum FuelType {
      NONE("none", 0.0F, 0.0F, 0, 0.0F, 1.0F),
      STICK("stick", 2.5F, 150.0F, 32, 100.0F, 1.0F),
      LOG("log", 8.5F, 475.0F, 8, 700.0F, 1.0F),
      COAL("coal", 13.0F, 550.0F, 32, 350.0F, 1.0F),
      CHARCOAL("charcoal", 14.0F, 650.0F, 32, 350.0F, 0.8F),
      COAL_BLOCK("coal_block", 25.0F, 900.0F, 1, 2400.0F, 1.0F),
      KELP_BLOCK("kelp_block", 15.5F, 750.0F, 3, 400.0F, 1.0F),
      TNT("tnt", 25.0F, 600.0F, 1, 750.0F, 1.2F),
      BLAZE_CAKE("blaze_cake", 40.0F, 1200.0F, 1, 3000.0F, 1.0F),
      SOUL_FIRED_BLAZE_CAKE(
              "soul_fired_blaze_cake",
              45.5F,
              1599.0F,
              1,
              3500.0F,
              1.0F
      );

      public final String serializedId;
      public final float celsiusPerSecond;
      public final float maxTempReachable;
      public final int maxStackSize;
      public final float baseBurnTimeTicks;
      public final float burnRateMultiplier;

       FuelType(
              String serializedId,
              float cps,
              float maxTemp,
              int maxStack,
              float ticks,
              float burnMult
      ) {
         this.serializedId = serializedId;
         this.celsiusPerSecond = cps;
         this.maxTempReachable = maxTemp;
         this.maxStackSize = maxStack;
         this.baseBurnTimeTicks = ticks;
         this.burnRateMultiplier = burnMult;
      }

      public String getDisplayName() {
         String[] words = this.serializedId.split("_");
         StringBuilder result = new StringBuilder();

         for (String word : words) {
            if (!result.isEmpty()) {
               result.append(' ');
            }

            if (!word.isEmpty()) {
               result.append(Character.toUpperCase(word.charAt(0)));

               if (word.length() > 1) {
                  result.append(word.substring(1));
               }
            }
         }

         return result.toString();
      }

      public static FuelType fromSerializedId(String serializedId) {
         for (FuelType fuelType : values()) {
            if (fuelType.serializedId.equals(serializedId)) {
               return fuelType;
            }
         }

         return NONE;
      }
   }

   public enum RotorHeatLevel {
      NONE("none", 0, 0, 0.0F, 0.0F, "Unheated"),
      SMOULDERING("smouldering", 1, 300, 32.0F, 128.0F, "Heated"),
      FADING("fading", 1, 325, 24.0F, 128.0F, "Heated"),
      KINDLED("kindled", 2, 650, 64.0F, 1024.0F, "Heated"),
      SEETHING("seething", 3, 950, 128.0F, 2048.0F, "Superheated"),
      RADIANT("radiant", 4, 1400, 256.0F, 8192.0F, "Combustion");

      public final String serializedId;
      public final int rank;
      public final int displayTemp;
      public final float rpmCap;
      public final float baseStressCapacity;
      public final String displayName;

       RotorHeatLevel(
              String serializedId,
              int rank,
              int displayTemp,
              float rpmCap,
              float baseStressCapacity,
              String displayName
      ) {
         this.serializedId = serializedId;
         this.rank = rank;
         this.displayTemp = displayTemp;
         this.rpmCap = rpmCap;
         this.baseStressCapacity = baseStressCapacity;
         this.displayName = displayName;
      }

      public boolean isAtLeast(RotorHeatLevel other) {
         return this.rank >= other.rank;
      }

      public static RotorHeatLevel fromSerializedId(String serializedId) {
         for (RotorHeatLevel heatLevel : values()) {
            if (heatLevel.serializedId.equals(serializedId)) {
               return heatLevel;
            }
         }

         return NONE;
      }
   }
}
