package io.hxneyw.repo.content.fluids.spritzer;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModEffects;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import io.hxneyw.repo.content.recipes.precisionspraying.PrecisionSprayingRegistry;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public class PerforatedSpritzerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
   protected SmartFluidTank tankInventory;
   public IFluidHandler fluidCapability;
   protected LerpedFloat fluidLevel;
   protected int luminosity;
   protected boolean forceFluidLevelUpdate;
   private static final int SYNC_RATE = 8;
   protected int syncCooldown;
   protected boolean queuedSync;
   private static final int SPRAY_START_THRESHOLD = 3500;
   private static final int FLUID_PER_SPRAY = 25;
   private static final int SPRAY_INTERVAL = 10;
   private int sprayTimer = 0;
   private boolean isPerformingSpray = false;
   public SmartFluidTank tank;
   private boolean spraying = false;
   private int sprayingTicks = 0;
   public static final int MAX_FILTER_ENTRIES = 18;
   private PrecisionFilterMode precisionFilterMode = PrecisionFilterMode.ITEM;
   private final List<ResourceLocation> precisionItemFilters = new ArrayList<>();
   private final List<ResourceLocation> precisionEntityFilters = new ArrayList<>();
   private long precisionBlockContactPos = Long.MIN_VALUE;
   private ResourceLocation precisionBlockContactId;
   private int precisionBlockContactCount;
   private final List<PendingPrecisionBlockImpact> pendingPrecisionBlockImpacts = new ArrayList<>();
   private final ContainerData precisionMenuData = new ContainerData() {
      @Override
      public int get(int index) {
         if (index == 0) {
            return PerforatedSpritzerBlockEntity.this.precisionFilterMode.ordinal();
         }
         int itemIndex = index - 1;
         if (itemIndex >= 0 && itemIndex < MAX_FILTER_ENTRIES) {
            if (itemIndex >= PerforatedSpritzerBlockEntity.this.precisionItemFilters.size()) {
               return -1;
            }
            ResourceLocation id = PerforatedSpritzerBlockEntity.this.precisionItemFilters.get(itemIndex);
            return BuiltInRegistries.ITEM.getId(BuiltInRegistries.ITEM.get(id));
         }
         int entityIndex = index - 1 - MAX_FILTER_ENTRIES;
         if (entityIndex >= 0 && entityIndex < MAX_FILTER_ENTRIES) {
            if (entityIndex >= PerforatedSpritzerBlockEntity.this.precisionEntityFilters.size()) {
               return -1;
            }
            ResourceLocation id = PerforatedSpritzerBlockEntity.this.precisionEntityFilters.get(entityIndex);
            return BuiltInRegistries.ENTITY_TYPE.getId(BuiltInRegistries.ENTITY_TYPE.get(id));
         }
         return -1;
      }

      @Override
      public void set(int index, int value) {
      }

      @Override
      public int getCount() {
         return 1 + MAX_FILTER_ENTRIES * 2;
      }
   };

   public PerforatedSpritzerBlockEntity(BlockPos pos, BlockState state) {
      super(AllBlockEntities.PERFORATED_SPRITZER.get(), pos, state);
      this.tankInventory = this.createInventory();
      this.tank = this.tankInventory;
      this.forceFluidLevelUpdate = true;
      this.refreshCapability();
      this.fluidLevel = LerpedFloat.linear().startWithValue(0.0);
   }

   protected SmartFluidTank createInventory() {
      return new SmartFluidTank(3500, this::onFluidStackChanged);
   }

   protected void onFluidStackChanged(FluidStack newFluidStack) {
      if (this.hasLevel() && this.level != null) {
         FluidType attributes = newFluidStack.getFluid().getFluidType();
         int newLuminosity = (int)(attributes.getLightLevel(newFluidStack) / 1.2F);
         if (this.luminosity != newLuminosity) {
            this.luminosity = newLuminosity;
            if (!this.level.isClientSide) {
               this.level.getChunkSource().getLightEngine().checkBlock(this.worldPosition);
            }
         }

         if (this.fluidLevel != null && !this.level.isClientSide) {
            float newFillState = this.getFillState();
            this.fluidLevel.chase(newFillState, 0.2F, Chaser.LINEAR);
         }

         if (!this.level.isClientSide && !this.isPerformingSpray) {
            this.setChanged();
            this.sendDataImmediately();
         }
      }
   }

   public void refreshCapability() {
      this.fluidCapability = new IFluidHandler() {
         public int getTanks() {
            return PerforatedSpritzerBlockEntity.this.tankInventory.getTanks();
         }

         @NotNull
         public FluidStack getFluidInTank(int tank) {
            return PerforatedSpritzerBlockEntity.this.tankInventory.getFluidInTank(tank);
         }

         public int getTankCapacity(int tank) {
            return PerforatedSpritzerBlockEntity.this.tankInventory.getTankCapacity(tank);
         }

         public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return PerforatedSpritzerBlockEntity.this.tankInventory.isFluidValid(tank, stack);
         }

         public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            int filled = PerforatedSpritzerBlockEntity.this.tankInventory.fill(resource, action);
            if (filled > 0 && action.execute() && PerforatedSpritzerBlockEntity.this.level != null && !PerforatedSpritzerBlockEntity.this.level.isClientSide) {
               PerforatedSpritzerBlockEntity.this.onFluidStackChanged(PerforatedSpritzerBlockEntity.this.tankInventory.getFluid());
            }

            return filled;
         }

         @NotNull
         @Override
         public FluidStack drain(
                 @NotNull FluidStack resource,
                 @NotNull FluidAction action
         ) {
            FluidStack drained =
                    PerforatedSpritzerBlockEntity.this
                            .tankInventory
                            .drain(resource, action);

            PerforatedSpritzerBlockEntity.this
                    .handleExecutedDrain(drained, action);

            return drained;
         }

         @NotNull
         @Override
         public FluidStack drain(
                 int maxDrain,
                 @NotNull FluidAction action
         ) {
            FluidStack drained =
                    PerforatedSpritzerBlockEntity.this
                            .tankInventory
                            .drain(maxDrain, action);

            PerforatedSpritzerBlockEntity.this
                    .handleExecutedDrain(drained, action);

            return drained;
         }
      };
      this.invalidateCapabilities();
      if (this.hasLevel() && this.level != null && !this.level.isClientSide) {
         this.level.invalidateCapabilities(this.worldPosition);
      }
   }

   public void onLoad() {
      super.onLoad();
      this.refreshCapability();
      if (this.level != null && !this.level.isClientSide) {
         this.level.invalidateCapabilities(this.worldPosition);
      }
   }

   private void handleExecutedDrain(
           FluidStack drained,
           FluidAction action
   ) {
      if (drained.isEmpty()
              || !action.execute()
              || this.level == null
              || this.level.isClientSide) {
         return;
      }

      this.onFluidStackChanged(
              this.tankInventory.getFluid()
      );
   }

   public void initialize() {
      super.initialize();
      if (this.level != null && !this.level.isClientSide) {
         this.forceFluidLevelUpdate = true;
         this.sendDataImmediately();
      }
   }

   public void tick() {
      super.tick();
      if (this.syncCooldown > 0) {
         this.syncCooldown--;
         if (this.syncCooldown == 0 && this.queuedSync) {
            this.sendData();
         }
      }

      if (this.fluidLevel != null) {
         this.fluidLevel.tickChaser();
      }

      if (this.forceFluidLevelUpdate && this.level != null && !this.level.isClientSide) {
         this.forceFluidLevelUpdate = false;
         float newFillState = this.getFillState();
         if (this.fluidLevel != null) {
            this.fluidLevel.chase(newFillState, 0.2F, Chaser.LINEAR);
         }

         this.sendDataImmediately();
      }

      if (this.level != null && !this.level.isClientSide) {
         if (this.isPrecision() && this.level instanceof ServerLevel serverLevel) {
            this.tickPrecisionBlockImpacts(serverLevel);
         } else if (!this.pendingPrecisionBlockImpacts.isEmpty()) {
            this.pendingPrecisionBlockImpacts.clear();
         }

         boolean wasSpraying = this.spraying;
         int currentAmount = this.tankInventory.getFluidAmount();
         boolean shouldSpray =
                 (this.spraying || currentAmount >= SPRAY_START_THRESHOLD)
                         && currentAmount >= FLUID_PER_SPRAY
                         && this.canSprayFluid()
                         && this.canSprayCurrentMode();
         if (shouldSpray) {
            this.spraying = true;
            this.sprayTimer++;
            if (this.sprayTimer >= SPRAY_INTERVAL) {
               this.sprayTimer = 0;
               if (!this.isPerformingSpray
                       && this.tankInventory.getFluidAmount() >= FLUID_PER_SPRAY) {
                  this.performSpray();
               }
            }
         } else {
            this.spraying = false;
            this.sprayTimer = 0;
         }

         if (wasSpraying != this.spraying) {
            this.sendData();
         }
      }

      if (this.spraying) {
         this.sprayingTicks++;
      } else {
         this.sprayingTicks = 0;
      }
   }

   private boolean canSprayFluid() {
      FluidStack fluid =
              this.tankInventory.getFluid();

      return !fluid.isEmpty()
              && (
              fluid.getFluid() == Fluids.WATER
                      || fluid.getFluid() == Fluids.LAVA
                      || fluid.getFluid()
                      == Fluids.FLOWING_LAVA
                      || fluid.getFluid()
                      == AllModFluids.SULFURIC_ACID.get()
      );
   }

   private void performSpray() {
      if (!this.isPerformingSpray) {
         this.isPerformingSpray = true;

         try {
            FluidStack fluid = this.tankInventory.getFluid();
            if (fluid.isEmpty() || !this.canSprayFluid()) {
               return;
            }

            int beforeAmount = this.tankInventory.getFluidAmount();
            if (beforeAmount < FLUID_PER_SPRAY) {
               return;
            }

            FluidStack drained =
                    this.tankInventory.drain(
                            FLUID_PER_SPRAY,
                            FluidAction.EXECUTE
                    );

            if (drained.isEmpty()
                    || drained.getAmount() != FLUID_PER_SPRAY) {
               return;
            }

            if (this.fluidLevel != null) {
               float newFillState = this.getFillState();
               this.fluidLevel.chase(newFillState, 0.2F, Chaser.LINEAR);
            }

            this.setChanged();
            if (this.level instanceof ServerLevel serverLevel) {
               this.spawnSprayParticles(serverLevel, drained);
               if (this.isPrecision()) {
                  this.applyPrecisionEffects(serverLevel, drained);
               } else {
                  this.applyFluidEffects(serverLevel, drained);
               }
            }
         } finally {
            this.isPerformingSpray = false;
         }

         if (this.level != null && !this.level.isClientSide) {
            this.sendDataImmediately();
         }
      }
   }

   private boolean isPrecision() {
      return this.getBlockState().hasProperty(PerforatedSpritzerBlock.PRECISION)
              && this.getBlockState().getValue(PerforatedSpritzerBlock.PRECISION);
   }

   private boolean canSprayCurrentMode() {
      if (!this.isPrecision()) {
         return true;
      }

      if (!(this.level instanceof ServerLevel serverLevel)) {
         return false;
      }

      return this.hasPrecisionItemTarget(serverLevel)
              || this.hasPrecisionEntityTarget(serverLevel);
   }

   private AABB getPrecisionSprayArea() {
      return new AABB(
              this.worldPosition.getX(),
              this.worldPosition.getY() - 3.0D,
              this.worldPosition.getZ(),
              this.worldPosition.getX() + 1.0D,
              this.worldPosition.getY(),
              this.worldPosition.getZ() + 1.0D
      );
   }

   private boolean hasPrecisionItemTarget(ServerLevel serverLevel) {
      if (this.precisionItemFilters.isEmpty()) {
         return false;
      }

      FluidStack fluid = this.tankInventory.getFluid();
      if (!PrecisionSprayingRegistry.isSulfuricAcid(fluid)) {
         return false;
      }

      for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(
              ItemEntity.class,
              this.getPrecisionSprayArea(),
              entity -> this.matchesPrecisionItem(entity.getItem())
      )) {
         if (PrecisionSprayingRegistry.getResult(itemEntity.getItem(), fluid).isPresent()) {
            return true;
         }
      }

      BlockPos targetPos = this.getExposedPrecisionBlockTarget(serverLevel);
      if (targetPos == null) {
         return false;
      }

      BlockState targetState = serverLevel.getBlockState(targetPos);
      if (!this.matchesPrecisionBlock(targetState)
              || PrecisionSprayingRegistry.getResult(targetState).isEmpty()) {
         return false;
      }

      return this.getPrecisionBlockContactProgress(targetPos, targetState)
              < PrecisionSprayingRegistry.SPRAY_CONTACTS_REQUIRED;
   }

   private boolean hasPrecisionEntityTarget(ServerLevel serverLevel) {
      if (this.precisionEntityFilters.isEmpty()) {
         return false;
      }

      FluidStack fluid = this.tankInventory.getFluid();
      return !serverLevel.getEntitiesOfClass(
              Entity.class,
              this.getPrecisionSprayArea(),
              entity -> this.matchesPrecisionEntity(entity.getType())
                      && this.canApplyPrecisionEntityEffect(entity, fluid)
      ).isEmpty();
   }

   private void applyPrecisionEffects(
           ServerLevel serverLevel,
           FluidStack fluid
   ) {
      this.applyPrecisionItemEffect(serverLevel, fluid);
      this.applyPrecisionEntityEffect(serverLevel, fluid);
   }

   private void applyPrecisionItemEffect(
           ServerLevel serverLevel,
           FluidStack fluid
   ) {
      if (this.precisionItemFilters.isEmpty()
              || !PrecisionSprayingRegistry.isSulfuricAcid(fluid)) {
         return;
      }

      for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(
              ItemEntity.class,
              this.getPrecisionSprayArea(),
              entity -> this.matchesPrecisionItem(entity.getItem())
      )) {
         java.util.Optional<ItemStack> result =
                 PrecisionSprayingRegistry.getResult(itemEntity.getItem(), fluid);
         if (result.isEmpty()) {
            continue;
         }

         ItemStack current = itemEntity.getItem();
         ItemStack output = result.get();
         output.setCount(1);

         if (current.getCount() <= 1) {
            itemEntity.setItem(output);
         } else {
            current.shrink(1);
            itemEntity.setItem(current);
            ItemEntity outputEntity = new ItemEntity(
                    serverLevel,
                    itemEntity.getX(),
                    itemEntity.getY(),
                    itemEntity.getZ(),
                    output
            );
            outputEntity.setDeltaMovement(itemEntity.getDeltaMovement());
            serverLevel.addFreshEntity(outputEntity);
         }

         this.emitPrecisionReaction(serverLevel, itemEntity.blockPosition());
         return;
      }

      BlockPos targetPos = this.getExposedPrecisionBlockTarget(serverLevel);
      if (targetPos == null) {
         this.resetPrecisionBlockContact();
         return;
      }

      BlockState targetState = serverLevel.getBlockState(targetPos);
      if (!this.matchesPrecisionBlock(targetState)) {
         this.resetPrecisionBlockContact();
         return;
      }

      java.util.Optional<BlockState> result =
              PrecisionSprayingRegistry.getResult(targetState);
      if (result.isEmpty()) {
         this.resetPrecisionBlockContact();
         return;
      }

      if (this.getPrecisionBlockContactProgress(targetPos, targetState)
              >= PrecisionSprayingRegistry.SPRAY_CONTACTS_REQUIRED) {
         return;
      }

      this.pendingPrecisionBlockImpacts.add(
              new PendingPrecisionBlockImpact(
                      targetPos.asLong(),
                      BuiltInRegistries.BLOCK.getKey(targetState.getBlock()),
                      this.getPrecisionBlockImpactDelay(targetPos)
              )
      );
   }

   private BlockPos getExposedPrecisionBlockTarget(ServerLevel serverLevel) {
      for (int offset = 1; offset <= 3; offset++) {
         BlockPos targetPos = this.worldPosition.below(offset);
         if (!serverLevel.getBlockState(targetPos).isAir()) {
            return targetPos;
         }
      }
      return null;
   }

   private int getPrecisionBlockContactProgress(
           BlockPos targetPos,
           BlockState targetState
   ) {
      ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(targetState.getBlock());
      long targetPosLong = targetPos.asLong();
      int progress = 0;
      if (this.precisionBlockContactPos == targetPosLong
              && blockId.equals(this.precisionBlockContactId)) {
         progress += this.precisionBlockContactCount;
      }
      for (PendingPrecisionBlockImpact impact : this.pendingPrecisionBlockImpacts) {
         if (impact.targetPos == targetPosLong
                 && blockId.equals(impact.blockId)) {
            progress++;
         }
      }
      return progress;
   }

   private int getPrecisionBlockImpactDelay(BlockPos targetPos) {
      double startY = this.worldPosition.getY() + 1.5D / 16.0D;
      double targetY = targetPos.getY() + 1.0D;
      double distance = Math.max(0.0D, startY - targetY);
      double velocity = 0.0D;
      double fallen = 0.0D;
      for (int ticks = 1; ticks <= 60; ticks++) {
         velocity -= 0.016D;
         fallen -= velocity;
         if (fallen >= distance) {
            return ticks;
         }
      }
      return 60;
   }

   private void tickPrecisionBlockImpacts(ServerLevel serverLevel) {
      for (int i = this.pendingPrecisionBlockImpacts.size() - 1; i >= 0; i--) {
         PendingPrecisionBlockImpact impact = this.pendingPrecisionBlockImpacts.get(i);
         impact.ticksRemaining--;
         if (impact.ticksRemaining > 0) {
            continue;
         }

         this.pendingPrecisionBlockImpacts.remove(i);
         BlockPos targetPos = BlockPos.of(impact.targetPos);
         BlockState targetState = serverLevel.getBlockState(targetPos);
         ResourceLocation currentBlockId = BuiltInRegistries.BLOCK.getKey(targetState.getBlock());
         if (!impact.blockId.equals(currentBlockId)
                 || !this.matchesPrecisionBlock(targetState)) {
            continue;
         }

         java.util.Optional<BlockState> result =
                 PrecisionSprayingRegistry.getResult(targetState);
         if (result.isEmpty()
                 || !this.registerPrecisionBlockContact(targetPos, targetState)) {
            continue;
         }

         serverLevel.setBlock(targetPos, result.get(), Block.UPDATE_ALL);
         this.resetPrecisionBlockContact();
         this.emitPrecisionReaction(serverLevel, targetPos);
      }
   }

   private boolean registerPrecisionBlockContact(
           BlockPos targetPos,
           BlockState targetState
   ) {
      ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(targetState.getBlock());
      long targetPosLong = targetPos.asLong();
      if (this.precisionBlockContactPos != targetPosLong
              || !blockId.equals(this.precisionBlockContactId)) {
         this.precisionBlockContactPos = targetPosLong;
         this.precisionBlockContactId = blockId;
         this.precisionBlockContactCount = 0;
      }

      this.precisionBlockContactCount++;
      this.setChanged();
      return this.precisionBlockContactCount >= PrecisionSprayingRegistry.SPRAY_CONTACTS_REQUIRED;
   }

   private void resetPrecisionBlockContact() {
      if (this.precisionBlockContactCount == 0
              && this.precisionBlockContactPos == Long.MIN_VALUE
              && this.precisionBlockContactId == null) {
         return;
      }
      this.precisionBlockContactPos = Long.MIN_VALUE;
      this.precisionBlockContactId = null;
      this.precisionBlockContactCount = 0;
      this.setChanged();
   }

   private static final class PendingPrecisionBlockImpact {
      private final long targetPos;
      private final ResourceLocation blockId;
      private int ticksRemaining;

      private PendingPrecisionBlockImpact(
              long targetPos,
              ResourceLocation blockId,
              int ticksRemaining
      ) {
         this.targetPos = targetPos;
         this.blockId = blockId;
         this.ticksRemaining = ticksRemaining;
      }
   }

   private void emitPrecisionReaction(
           ServerLevel serverLevel,
           BlockPos targetPos
   ) {
      serverLevel.sendParticles(
              ModParticles.ACID_DRIP.get(),
              targetPos.getX() + 0.5D,
              targetPos.getY() + 0.7D,
              targetPos.getZ() + 0.5D,
              8,
              0.25D,
              0.2D,
              0.25D,
              0.03D
      );
      serverLevel.playSound(
              null,
              targetPos,
              SoundEvents.LAVA_EXTINGUISH,
              SoundSource.BLOCKS,
              0.35F,
              1.65F
      );
   }

   private boolean canApplyPrecisionEntityEffect(
           Entity entity,
           FluidStack fluid
   ) {
      if (fluid.getFluid() == Fluids.WATER) {
         return entity.isOnFire();
      }
      if (fluid.getFluid() == AllModFluids.SULFURIC_ACID.get()) {
         return entity instanceof LivingEntity;
      }
      if (fluid.getFluid() == Fluids.LAVA
              || fluid.getFluid() == Fluids.FLOWING_LAVA) {
         return !entity.fireImmune();
      }
      return false;
   }

   private void applyPrecisionEntityEffect(
           ServerLevel serverLevel,
           FluidStack fluid
   ) {
      if (this.precisionEntityFilters.isEmpty()) {
         return;
      }

      for (Entity entity : serverLevel.getEntitiesOfClass(
              Entity.class,
              this.getPrecisionSprayArea(),
              candidate -> this.matchesPrecisionEntity(candidate.getType())
                      && this.canApplyPrecisionEntityEffect(candidate, fluid)
      )) {
         if (fluid.getFluid() == Fluids.WATER) {
            entity.clearFire();
            entity.setRemainingFireTicks(0);
            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,
                    entity.getX(),
                    entity.getY() + 0.5D,
                    entity.getZ(),
                    5,
                    0.3D,
                    0.3D,
                    0.3D,
                    0.2D
            );
         } else if (fluid.getFluid() == AllModFluids.SULFURIC_ACID.get()
                 && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(AllModEffects.ACID_BURN, 100, 0));
         } else if (fluid.getFluid() == Fluids.LAVA
                 || fluid.getFluid() == Fluids.FLOWING_LAVA) {
            entity.igniteForSeconds(5.0F);
         }
      }
   }

   private boolean matchesPrecisionItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      return this.precisionItemFilters.contains(id);
   }

   private boolean matchesPrecisionBlock(BlockState state) {
      if (state.getBlock().asItem() == net.minecraft.world.item.Items.AIR) {
         return false;
      }
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(state.getBlock().asItem());
      return this.precisionItemFilters.contains(id);
   }

   private boolean matchesPrecisionEntity(EntityType<?> type) {
      ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
      return this.precisionEntityFilters.contains(id);
   }

   private void spawnSprayParticles(
           ServerLevel serverLevel,
           FluidStack fluid
   ) {
      if (!fluid.isEmpty()) {
         
         
         double[] holeCenters = new double[]{
            3.5D / 16.0D,
            5.5D / 16.0D,
            8.0D / 16.0D,
            10.5D / 16.0D,
            12.5D / 16.0D
         };

         
         
         double y = this.worldPosition.getY() + 1.5D / 16.0D;

         for (double xOffset : holeCenters) {
            for (double zOffset : holeCenters) {
               double x = this.worldPosition.getX() + xOffset;
               double z = this.worldPosition.getZ() + zOffset;
               if (fluid.getFluid() == Fluids.WATER) {
                  serverLevel.sendParticles(ParticleTypes.FALLING_WATER, x, y, z, 1, 0.0, -0.1, 0.0, 0.0);
               } else if (fluid.getFluid() == AllModFluids.SULFURIC_ACID.get()) {
                  serverLevel.sendParticles(ModParticles.ACID_DRIP.get(), x, y, z, 1, 0.0, -0.1, 0.0, 0.0);
               } else if (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA) {
                  serverLevel.sendParticles(ParticleTypes.FALLING_LAVA, x, y, z, 1, 0.0, -0.05, 0.0, 0.0);
                  if (serverLevel.random.nextFloat() < 0.3F) {
                     serverLevel.sendParticles(ParticleTypes.DRIPPING_LAVA, x, y, z, 1, 0.0, -0.05, 0.0, 0.0);
                  }
               }
            }
         }
      }
   }

   private void applyFluidEffects(
           ServerLevel serverLevel,
           FluidStack fluid
   ) {
      if (!fluid.isEmpty()) {
         BlockPos below = this.worldPosition.below();

         for (int y = 0; y >= -2; y--) {
            BlockPos checkPos = below.offset(0, y, 0);
            if (fluid.getFluid() == Fluids.WATER) {
               AABB extinguishArea = new AABB(checkPos);

               for (Entity entity : serverLevel.getEntitiesOfClass(Entity.class, extinguishArea)) {
                  if (entity.isOnFire()) {
                     entity.clearFire();
                     entity.setRemainingFireTicks(0);
                     serverLevel.sendParticles(ParticleTypes.SPLASH, entity.getX(), entity.getY() + 0.5, entity.getZ(), 5, 0.3, 0.3, 0.3, 0.2);
                  }
               }

               BlockState state = serverLevel.getBlockState(checkPos);
               if (state.getBlock() instanceof FarmBlock) {
                  int currentMoisture = state.getValue(FarmBlock.MOISTURE);
                  if (currentMoisture < 7 && serverLevel.random.nextFloat() < 0.08F) {serverLevel.setBlock(checkPos, state.setValue(FarmBlock.MOISTURE, currentMoisture + 1), 2
                     );
                  }
               }

               if (state.getBlock() instanceof CropBlock crop) {
                  BlockPos farmlandBelow = checkPos.below();
                  BlockState farmlandState = serverLevel.getBlockState(farmlandBelow);
                  if (farmlandState.getBlock() instanceof FarmBlock) {
                     int moisture = farmlandState.getValue(FarmBlock.MOISTURE);
                     if (moisture < 7 && serverLevel.random.nextFloat() < 0.08F) {
                        serverLevel.setBlock(
                                farmlandBelow,
                                farmlandState.setValue(
                                        FarmBlock.MOISTURE,
                                        moisture + 1
                                ),
                                2
                        );
                     }

                     int currentAge = crop.getAge(state);
                     if (moisture >= 4 && currentAge < 6 && serverLevel.random.nextFloat() < 0.015F) {
                        serverLevel.setBlock(checkPos, crop.getStateForAge(currentAge + 1), 2);
                     }
                  }
               }
            }

            if (fluid.getFluid() == AllModFluids.SULFURIC_ACID.get()) {
               serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(checkPos), entityx -> true)
                  .forEach(entityx -> entityx.addEffect(new MobEffectInstance(AllModEffects.ACID_BURN, 100, 0)));
            }

            if (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA) {
               serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(checkPos), entityx -> true).forEach(entityx -> entityx.igniteForSeconds(5.0F));
               BlockState statex = serverLevel.getBlockState(checkPos);
               if (statex.getFluidState().is(Fluids.WATER) || statex.getFluidState().is(Fluids.FLOWING_WATER)) {
                  if (statex.getFluidState().isSource()) {
                     serverLevel.setBlock(checkPos, Blocks.STONE.defaultBlockState(), 3);
                  } else {
                     serverLevel.setBlock(checkPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                  }

                  serverLevel.playSound(null, checkPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
               }
            }
         }
      }
   }

   public void setPrecisionFilterMode(PrecisionFilterMode mode) {
      this.precisionFilterMode = mode;
      this.setChanged();
      this.sendDataImmediately();
   }

   public void setItemFilter(ItemStack stack) {
      this.precisionItemFilters.clear();
      if (!stack.isEmpty()) {
         this.precisionItemFilters.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
      }
      this.precisionFilterMode = PrecisionFilterMode.ITEM;
      this.resetPrecisionBlockContact();
      this.setChanged();
      this.sendDataImmediately();
   }

   public void toggleItemFilterByRegistryId(int registryId) {
      net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.byId(registryId);
      if (item == null || item == net.minecraft.world.item.Items.AIR) {
         return;
      }
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
      if (!this.precisionItemFilters.remove(id)
              && this.precisionItemFilters.size() < MAX_FILTER_ENTRIES) {
         this.precisionItemFilters.add(id);
      }
      this.precisionFilterMode = PrecisionFilterMode.ITEM;
      this.resetPrecisionBlockContact();
      this.setChanged();
      this.sendDataImmediately();
   }

   public void toggleEntityFilterByRegistryId(int registryId) {
      EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.byId(registryId);
      if (type == null) {
         return;
      }
      ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
      if (!this.precisionEntityFilters.remove(id)
              && this.precisionEntityFilters.size() < MAX_FILTER_ENTRIES) {
         this.precisionEntityFilters.add(id);
      }
      this.precisionFilterMode = PrecisionFilterMode.ENTITY;
      this.setChanged();
      this.sendDataImmediately();
   }

   public void clearCurrentPrecisionFilter() {
      if (this.precisionFilterMode == PrecisionFilterMode.ITEM) {
         this.precisionItemFilters.clear();
         this.resetPrecisionBlockContact();
      } else {
         this.precisionEntityFilters.clear();
      }
      this.setChanged();
      this.sendDataImmediately();
   }

   public ContainerData getPrecisionMenuData() {
      return this.precisionMenuData;
   }

   public float getFillState() {
      return (float)this.tankInventory.getFluidAmount() / this.tankInventory.getCapacity();
   }

   public LerpedFloat getFluidLevel() {
      return this.fluidLevel;
   }

   public void sendDataImmediately() {
      this.syncCooldown = 0;
      this.queuedSync = false;
      this.sendData();
   }

   public void sendData() {
      if (this.syncCooldown > 0) {
         this.queuedSync = true;
      } else {
         super.sendData();
         this.queuedSync = false;
         this.syncCooldown = SYNC_RATE;
      }
   }

   protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
      super.read(compound, registries, clientPacket);
      this.tankInventory.readFromNBT(registries, compound.getCompound("TankContent"));
      this.luminosity = compound.getInt("Luminosity");
      if (!clientPacket) {
         this.spraying = false;
         this.sprayingTicks = 0;
         this.sprayTimer = 0;
         this.isPerformingSpray = compound.getBoolean("IsPerformingSpray");
         this.refreshCapability();
         if (this.hasLevel() && this.level != null) {
            this.level.invalidateCapabilities(this.worldPosition);
         }
      } else {
         this.spraying = compound.getBoolean("Spraying");
         this.sprayingTicks = compound.getInt("SprayingTicks");
         this.sprayTimer = compound.getInt("SprayTimer");
      }

      this.precisionFilterMode = PrecisionFilterMode.fromOrdinal(
              compound.getInt("PrecisionFilterMode")
      );
      this.precisionBlockContactPos = compound.contains("PrecisionBlockContactPos", Tag.TAG_LONG)
              ? compound.getLong("PrecisionBlockContactPos")
              : Long.MIN_VALUE;
      this.precisionBlockContactId = compound.contains("PrecisionBlockContactId", Tag.TAG_STRING)
              ? ResourceLocation.tryParse(compound.getString("PrecisionBlockContactId"))
              : null;
      this.precisionBlockContactCount = compound.getInt("PrecisionBlockContactCount");
      if (this.precisionBlockContactPos == Long.MIN_VALUE
              || this.precisionBlockContactId == null
              || this.precisionBlockContactCount <= 0) {
         this.precisionBlockContactPos = Long.MIN_VALUE;
         this.precisionBlockContactId = null;
         this.precisionBlockContactCount = 0;
      }
      this.precisionItemFilters.clear();
      this.precisionEntityFilters.clear();
      if (compound.contains("PrecisionItemFilters", Tag.TAG_LIST)) {
         ListTag itemFilters = compound.getList("PrecisionItemFilters", Tag.TAG_STRING);
         for (int i = 0; i < itemFilters.size() && this.precisionItemFilters.size() < MAX_FILTER_ENTRIES; i++) {
            ResourceLocation id = ResourceLocation.tryParse(itemFilters.getString(i));
            if (id != null && BuiltInRegistries.ITEM.get(id) != net.minecraft.world.item.Items.AIR) {
               this.precisionItemFilters.add(id);
            }
         }
      } else if (compound.contains("PrecisionItemFilter", Tag.TAG_COMPOUND)) {
         ItemStack legacy = ItemStack.parseOptional(registries, compound.getCompound("PrecisionItemFilter"));
         if (!legacy.isEmpty()) {
            this.precisionItemFilters.add(BuiltInRegistries.ITEM.getKey(legacy.getItem()));
         }
      }
      if (compound.contains("PrecisionEntityFilters", Tag.TAG_LIST)) {
         ListTag entityFilters = compound.getList("PrecisionEntityFilters", Tag.TAG_STRING);
         for (int i = 0; i < entityFilters.size() && this.precisionEntityFilters.size() < MAX_FILTER_ENTRIES; i++) {
            ResourceLocation id = ResourceLocation.tryParse(entityFilters.getString(i));
            if (id != null && BuiltInRegistries.ENTITY_TYPE.get(id) != null) {
               this.precisionEntityFilters.add(id);
            }
         }
      } else if (compound.contains("PrecisionEntityFilter", Tag.TAG_STRING)) {
         ResourceLocation legacy = ResourceLocation.tryParse(compound.getString("PrecisionEntityFilter"));
         if (legacy != null && BuiltInRegistries.ENTITY_TYPE.get(legacy) != null) {
            this.precisionEntityFilters.add(legacy);
         }
      }

      if (clientPacket) {
         float fillState = this.getFillState();
         this.fluidLevel = LerpedFloat.linear().startWithValue(fillState);
         this.fluidLevel.chase(fillState, 0.5, Chaser.EXP);
         if (this.luminosity > 0 && this.hasLevel() && this.level != null) {
            this.level.getChunkSource().getLightEngine().checkBlock(this.worldPosition);
         }

         if (compound.contains("LazySync")) {
            this.fluidLevel.chase(this.fluidLevel.getChaseTarget(), 0.125, Chaser.EXP);
         }
      } else {
         this.forceFluidLevelUpdate = true;
      }
   }

   public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
      super.write(compound, registries, clientPacket);
      compound.put("TankContent", this.tankInventory.writeToNBT(registries, new CompoundTag()));
      compound.putInt("Luminosity", this.luminosity);
      compound.putBoolean("Spraying", this.spraying);
      compound.putInt("SprayingTicks", this.sprayingTicks);
      compound.putInt("SprayTimer", this.sprayTimer);
      compound.putBoolean("IsPerformingSpray", this.isPerformingSpray);
      compound.putInt("PrecisionFilterMode", this.precisionFilterMode.ordinal());
      if (this.precisionBlockContactId != null && this.precisionBlockContactCount > 0) {
         compound.putLong("PrecisionBlockContactPos", this.precisionBlockContactPos);
         compound.putString("PrecisionBlockContactId", this.precisionBlockContactId.toString());
         compound.putInt("PrecisionBlockContactCount", this.precisionBlockContactCount);
      }
      ListTag itemFilters = new ListTag();
      for (ResourceLocation id : this.precisionItemFilters) {
         itemFilters.add(StringTag.valueOf(id.toString()));
      }
      compound.put("PrecisionItemFilters", itemFilters);
      ListTag entityFilters = new ListTag();
      for (ResourceLocation id : this.precisionEntityFilters) {
         entityFilters.add(StringTag.valueOf(id.toString()));
      }
      compound.put("PrecisionEntityFilters", entityFilters);
      if (clientPacket) {
         if (this.forceFluidLevelUpdate) {
            compound.putBoolean("ForceFluidLevel", true);
         }

         if (this.queuedSync) {
            compound.putBoolean("LazySync", true);
         }

         this.forceFluidLevelUpdate = false;
      }
   }

   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
   }

   @Override
   public boolean addToGoggleTooltip(
           List<Component> tooltip,
           boolean isPlayerSneaking
   ) {
      boolean added = this.level != null
              && this.containedFluidTooltip(
              tooltip,
              isPlayerSneaking,
              this.tankInventory
      );

      if (!this.isPrecision()) {
         return added;
      }

      tooltip.add(Component.translatable(
              "goggle.sulfuricresonance.precision_spritzer.item_filters",
              this.precisionItemFilters.size()
      ).withStyle(ChatFormatting.AQUA));
      tooltip.add(Component.translatable(
              "goggle.sulfuricresonance.precision_spritzer.entity_filters",
              this.precisionEntityFilters.size()
      ).withStyle(ChatFormatting.AQUA));
      return true;
   }

   public SmartFluidTank getTankInventory() {
      return this.tankInventory;
   }

   public enum PrecisionFilterMode {
      ITEM,
      ENTITY;

      public static PrecisionFilterMode fromOrdinal(int ordinal) {
         return ordinal == ENTITY.ordinal() ? ENTITY : ITEM;
      }
   }
}
