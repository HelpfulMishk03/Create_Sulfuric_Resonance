package io.hxneyw.repo.content.fluids.spritzer;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModEffects;
import io.hxneyw.repo.content.registry.AllModFluids;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.List;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
         boolean wasSpraying = this.spraying;
         int currentAmount = this.tankInventory.getFluidAmount();
         boolean shouldSpray =
                 (this.spraying || currentAmount >= SPRAY_START_THRESHOLD)
                         && currentAmount >= FLUID_PER_SPRAY
                         && this.canSprayFluid();
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
               this.spawnSprayParticles(serverLevel);
               this.applyFluidEffects(serverLevel);
            }
         } finally {
            this.isPerformingSpray = false;
         }

         if (this.level != null && !this.level.isClientSide) {
            this.sendDataImmediately();
         }
      }
   }

   private void spawnSprayParticles(ServerLevel serverLevel) {
      FluidStack fluid = this.tankInventory.getFluid();
      if (!fluid.isEmpty()) {
         double[][] holePositions = new double[][]{
            {0.25, 0.25},
            {0.4375, 0.25},
            {0.625, 0.25},
            {0.8125, 0.25},
            {0.25, 0.375},
            {0.4375, 0.375},
            {0.625, 0.375},
            {0.8125, 0.375},
            {0.25, 0.5},
            {0.4375, 0.5},
            {0.625, 0.5},
            {0.8125, 0.5},
            {0.25, 0.625},
            {0.4375, 0.625},
            {0.625, 0.625},
            {0.8125, 0.625},
            {0.25, 0.75},
            {0.4375, 0.75},
            {0.625, 0.75},
            {0.8125, 0.75}
         };

         for (double[] hole : holePositions) {
            double x = this.worldPosition.getX() + hole[0];
            double y = this.worldPosition.getY() + 0.05;
            double z = this.worldPosition.getZ() + hole[1];
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

   private void applyFluidEffects(ServerLevel serverLevel) {
      FluidStack fluid = this.tankInventory.getFluid();
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
      return this.level != null
              && this.containedFluidTooltip(
              tooltip,
              isPlayerSneaking,
              this.tankInventory
      );
   }

   public SmartFluidTank getTankInventory() {
      return this.tankInventory;
   }
}
