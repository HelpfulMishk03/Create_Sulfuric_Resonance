package io.hxneyw.repo.content.blocks;

import io.hxneyw.repo.Config;
import io.hxneyw.repo.content.registry.AllModSounds;
import io.hxneyw.repo.content.registry.AllVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class RubberPaddingBlock extends Block {
   private static final String NBT_BOUNCED = "RubberPaddingBounced";
   private static final String NBT_BOUNCE_COUNT = "RubberPaddingBounceCount";
   private static final String NBT_LAST_BOUNCE_TIME = "RubberPaddingLastBounceTime";
   private static final String NBT_LAST_Y_POS = "RubberPaddingLastYPos";
   private static final String NBT_SETTLING = "RubberPaddingSettling";
   private static final double BASE_ENTITY_BOUNCE_MULTIPLIER = 0.5;
   private static final double BASE_ITEM_FIRST_BOUNCE = 0.28;
   private static final double ITEM_SUBSEQUENT_MULTIPLIER = 0.6;
   private static final double ITEM_STOP_THRESHOLD = 0.05;
   private static final int BOUNCE_COOLDOWN_TICKS = 4;
   private static final double MIN_FALL_VELOCITY = 0.2;
   private static final double HORIZONTAL_RETENTION = 0.9;
   private static final double HORIZONTAL_MIN_SPEED = 0.18;
   private static final int SETTLING_TIME = 20;
   private static final double BOBBING_THRESHOLD = 0.05;

   public RubberPaddingBlock(Properties properties) {
      super(properties);
   }

   @NotNull
   public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return AllVoxelShapes.RubberPadding.getShape();
   }

   @NotNull
   public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      return AllVoxelShapes.RubberPadding.getShape();
   }

   @NotNull
   public VoxelShape getBlockSupportShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
      return AllVoxelShapes.RubberPadding.getShape();
   }

   public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
      return false;
   }

   public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentState, @NotNull Direction direction) {
      return false;
   }

   @NotNull
   public RenderShape getRenderShape(@NotNull BlockState state) {
      return RenderShape.MODEL;
   }

   public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, Entity entity, float fallDistance) {
      if (!entity.isSuppressingBounce() && (Boolean)Config.RUBBER_PADDING_ENABLED.get()) {
         entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
      } else {
         super.fallOn(level, state, pos, entity, fallDistance);
      }
   }

   public void updateEntityAfterFallOn(@NotNull BlockGetter blockGetter, @NotNull Entity entity) {
      if ((Boolean)Config.RUBBER_PADDING_ENABLED.get() && !entity.isSuppressingBounce()) {
         Vec3 velocity = entity.getDeltaMovement();
         if (entity instanceof ItemEntity item) {
            CompoundTag itemData = item.getPersistentData();
            long currentTime = item.level().getGameTime();
            long lastBounceTime = itemData.getLong("RubberPaddingLastBounceTime");
            if (currentTime - lastBounceTime > 20L && Math.abs(velocity.y) < 0.05) {
               itemData.putBoolean("RubberPaddingSettling", true);
               super.updateEntityAfterFallOn(blockGetter, entity);
               return;
            }

            boolean isSettling = itemData.getBoolean("RubberPaddingSettling");
            if (isSettling && Math.abs(velocity.y) < 0.2) {
               super.updateEntityAfterFallOn(blockGetter, entity);
               return;
            }
         }

         if (velocity.y >= -0.2) {
            if (entity instanceof ItemEntity item) {
               this.clearBounceData(item);
            }

            super.updateEntityAfterFallOn(blockGetter, entity);
         } else {
            if (entity instanceof ItemEntity itemEntity) {
               if ((Double)Config.ITEM_BOUNCE_MULTIPLIER.get() <= 0.0) {
                  super.updateEntityAfterFallOn(blockGetter, entity);
                  return;
               }

               this.bounceItem(itemEntity, velocity);
            } else {
               if ((Double)Config.ENTITY_BOUNCE_MULTIPLIER.get() <= 0.0) {
                  super.updateEntityAfterFallOn(blockGetter, entity);
                  return;
               }

               this.bounceEntity(entity, velocity);
            }
         }
      } else {
         super.updateEntityAfterFallOn(blockGetter, entity);
      }
   }

   private void bounceItem(ItemEntity item, Vec3 velocity) {
      Level level = item.level();
      double verticalSpeed = Math.abs(velocity.y);
      long currentTime = level.getGameTime();
      CompoundTag itemData = item.getPersistentData();
      boolean hasBouncedBefore = itemData.getBoolean("RubberPaddingBounced");
      int bounceCount = itemData.getInt("RubberPaddingBounceCount");
      long lastBounceTime = itemData.getLong("RubberPaddingLastBounceTime");
      double lastYPos = itemData.getDouble("RubberPaddingLastYPos");
      itemData.remove("RubberPaddingSettling");
      double currentYPos = item.getY();
      if (!hasBouncedBefore || !(Math.abs(currentYPos - lastYPos) < 0.1)) {
         if (currentTime - lastBounceTime < 4L) {
            item.setDeltaMovement(velocity.x * 0.9, velocity.y, velocity.z * 0.9);
         } else if (hasBouncedBefore && verticalSpeed < 0.05) {
            this.clearBounceData(item);
            item.setDeltaMovement(velocity.x * 0.7, 0.0, velocity.z * 0.7);
         } else {
            double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            double newX;
            double newZ;
            if (horizontalSpeed > 0.01) {
               double dirX = velocity.x / horizontalSpeed;
               double dirZ = velocity.z / horizontalSpeed;
               double retainedSpeed = Math.max(horizontalSpeed * 0.9, 0.18);
               newX = dirX * retainedSpeed;
               newZ = dirZ * retainedSpeed;
            } else {
               newX = 0.0;
               newZ = 0.0;
            }

            double newY;
            if (!hasBouncedBefore) {
               double configMultiplier = (Double)Config.ITEM_BOUNCE_MULTIPLIER.get();
               newY = 0.28 * configMultiplier;
               itemData.putBoolean("RubberPaddingBounced", true);
               itemData.putInt("RubberPaddingBounceCount", 1);
               itemData.putLong("RubberPaddingLastBounceTime", currentTime);
               itemData.putDouble("RubberPaddingLastYPos", currentYPos);
               level.playSound(
                  null,
                  item.blockPosition(),
                  (SoundEvent)AllModSounds.RUBBER_PADDING_HIT.get(),
                  SoundSource.BLOCKS,
                  0.3F,
                  1.0F + level.random.nextFloat() * 0.2F
               );
               this.spawnItemParticles(level, item);
            } else {
               newY = 0.28 * Math.pow(0.6, bounceCount);
               itemData.putInt("RubberPaddingBounceCount", bounceCount + 1);
               itemData.putLong("RubberPaddingLastBounceTime", currentTime);
               itemData.putDouble("RubberPaddingLastYPos", currentYPos);
               if (newY > 0.1) {
                  level.playSound(
                     null,
                     item.blockPosition(),
                     (SoundEvent)AllModSounds.RUBBER_PADDING_HIT.get(),
                     SoundSource.BLOCKS,
                     0.55F * (float)Math.pow(0.6, bounceCount),
                     1.0F + level.random.nextFloat() * 0.2F
                  );
                  this.spawnItemParticles(level, item);
               }
            }

            item.setDeltaMovement(newX, newY, newZ);
         }
      }
   }

   private void bounceEntity(Entity entity, Vec3 velocity) {
      Level level = entity.level();
      double configMultiplier = (Double)Config.ENTITY_BOUNCE_MULTIPLIER.get();
      double bounceY = -velocity.y * 0.5 * configMultiplier;
      entity.setDeltaMovement(velocity.x, bounceY, velocity.z);
      level.playSound(
         null, entity.blockPosition(), (SoundEvent)AllModSounds.RUBBER_PADDING_HIT.get(), SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.2F
      );
      this.spawnEntityParticles(level, entity);
   }

   private void clearBounceData(ItemEntity item) {
      CompoundTag data = item.getPersistentData();
      data.remove("RubberPaddingBounced");
      data.remove("RubberPaddingBounceCount");
      data.remove("RubberPaddingLastBounceTime");
      data.remove("RubberPaddingLastYPos");
      data.remove("RubberPaddingSettling");
   }

   public float getFriction(@NotNull BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos, Entity entity) {
      return super.getFriction(state, world, pos, entity);
   }

   private void spawnItemParticles(Level level, ItemEntity item) {
      if (level instanceof ServerLevel serverLevel) {
         BlockState state = serverLevel.getBlockState(item.blockPosition());
         serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), item.getX(), item.getY(), item.getZ(), 2, 0.15, 0.0, 0.15, 0.05);
      }
   }

   private void spawnEntityParticles(Level level, Entity entity) {
      if (level instanceof ServerLevel serverLevel) {
         BlockState state = serverLevel.getBlockState(entity.blockPosition());
         serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, state),
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            6,
            entity.getBbWidth() / 2.0F,
            0.0,
            entity.getBbWidth() / 2.0F,
            0.1
         );
      }
   }
}
