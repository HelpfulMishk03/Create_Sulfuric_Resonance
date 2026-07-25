package io.hxneyw.repo.content.blocks;

import io.hxneyw.repo.content.registry.AllModEffects;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class SulfuricAcidBlock extends LiquidBlock {
   private static final Map<BlockPos, Long> LAST_REACTION_TIME = new HashMap<>();
   private static final int REACTION_COOLDOWN_TICKS = 10;

   public SulfuricAcidBlock(FlowingFluid fluid, Properties properties) {
      super(fluid, properties);
   }

   public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
      if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
         long currentTime = level.getGameTime();
         long lastReaction = LAST_REACTION_TIME.getOrDefault(pos, 0L);
         if (currentTime - lastReaction < 10L) {
            super.neighborChanged(state, level, pos, block, fromPos, isMoving);
            return;
         }

         LAST_REACTION_TIME.put(pos, currentTime);
         if (currentTime % 200L == 0L) {
            LAST_REACTION_TIME.entrySet().removeIf(entry -> currentTime - entry.getValue() > 200L);
         }

         for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getFluidState().is(Fluids.WATER) || neighborState.getFluidState().is(Fluids.FLOWING_WATER)) {
               level.playSound(
                  null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F
               );
               serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.5, 0.5, 0.5, 0.0);
            }

            if (neighborState.getFluidState().is(Fluids.LAVA) || neighborState.getFluidState().is(Fluids.FLOWING_LAVA)) {
               if (neighborState.getFluidState().isSource()) {
                  level.setBlockAndUpdate(neighborPos, Blocks.OBSIDIAN.defaultBlockState());
               } else {
                  level.setBlockAndUpdate(neighborPos, Blocks.STONE.defaultBlockState());
               }

               level.playSound(
                  null, neighborPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
               );
               serverLevel.sendParticles(
                  ParticleTypes.LARGE_SMOKE, neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5, 12, 0.5, 0.5, 0.5, 0.05
               );
               serverLevel.sendParticles(
                  ParticleTypes.LAVA, neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0
               );
               break;
            }
         }
      }

      super.neighborChanged(state, level, pos, block, fromPos, isMoving);
   }

   public void entityInside(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
      if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
         livingEntity.addEffect(new MobEffectInstance(AllModEffects.ACID_BURN, 100, 0, false, true, true));
      }

      super.entityInside(state, level, pos, entity);
   }
}
