package io.hxneyw.repo.content.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("resource")
public class SulfurItem extends Item {
   public SulfurItem(Properties properties) {
      super(properties);
   }

   public boolean onEntityItemUpdate(@NotNull ItemStack stack, ItemEntity entity) {
      Level level = entity.level();
      if (entity.isOnFire() && !level.isClientSide) {
         entity.clearFire();
         entity.setRemainingFireTicks(0);
      }

      boolean inDanger = entity.isInLava()
         || level.getBlockState(entity.blockPosition()).getBlock() == Blocks.FIRE
         || level.getBlockState(entity.blockPosition()).getBlock() == Blocks.SOUL_FIRE;
      if (inDanger && level.isClientSide && level.random.nextFloat() < 0.3F) {
         level.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            entity.getX() + (level.random.nextDouble() - 0.5) * 0.2,
            entity.getY() + level.random.nextDouble() * 0.3,
            entity.getZ() + (level.random.nextDouble() - 0.5) * 0.2,
            0.0,
            0.02,
            0.0
         );
      }

      if (inDanger && !level.isClientSide && level.getGameTime() % 20L == 0L) {
         level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.0))
            .forEach(livingEntity -> livingEntity.hurt(level.damageSources().magic(), 1.0F));
      }

      return false;
   }
}
