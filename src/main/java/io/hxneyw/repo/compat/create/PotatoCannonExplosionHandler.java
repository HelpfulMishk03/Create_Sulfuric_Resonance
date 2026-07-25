package io.hxneyw.repo.compat.create;

import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import io.hxneyw.repo.content.Items;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber
public class PotatoCannonExplosionHandler {
   @SubscribeEvent
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      if (event.getProjectile() instanceof PotatoProjectileEntity potatoProjectile) {
         ItemStack item = potatoProjectile.getItem();
         if (item.is(Items.PYROCLAST_BOMB.get())) {
            Level level = potatoProjectile.level();
            if (!level.isClientSide) {
               Vec3 hitPos = event.getRayTraceResult().getLocation();
               Entity directHitEntity = event.getRayTraceResult() instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
               level.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
               breakBlocksInRadius(level, hitPos);
               level.getEntitiesOfClass(LivingEntity.class, potatoProjectile.getBoundingBox().inflate(2.5)).forEach(entity -> {
                  if (entity != potatoProjectile.getOwner() && entity != directHitEntity) {
                     entity.hurt(level.damageSources().explosion(potatoProjectile, potatoProjectile.getOwner()), 1.0F);
                  }
               });
               if (level instanceof ServerLevel serverLevel) {
                  for (int i = 0; i < 25; i++) {
                     serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        1,
                        (level.getRandom().nextDouble() - 0.5) * 0.6,
                        level.getRandom().nextDouble() * 0.5,
                        (level.getRandom().nextDouble() - 0.5) * 0.6,
                        0.12
                     );
                  }

                  for (int i = 0; i < 10; i++) {
                     serverLevel.sendParticles(
                        ParticleTypes.EXPLOSION,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        1,
                        (level.getRandom().nextDouble() - 0.5) * 0.5,
                        level.getRandom().nextDouble() * 0.3,
                        (level.getRandom().nextDouble() - 0.5) * 0.5,
                        0.1
                     );
                  }
               }
            }
         }
      }
   }

   private static void breakBlocksInRadius(Level level, Vec3 center) {
      List<BlockPos> blocksToBreak = new ArrayList<>();
      int minX = (int)Math.floor(center.x - 1.5);
      int minY = (int)Math.floor(center.y - 1.5);
      int minZ = (int)Math.floor(center.z - 1.5);
      int maxX = (int)Math.ceil(center.x + 1.5);
      int maxY = (int)Math.ceil(center.y + 1.5);
      int maxZ = (int)Math.ceil(center.z + 1.5);

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos pos = new BlockPos(x, y, z);
               double distance = Math.sqrt(Math.pow(x + 0.5 - center.x, 2.0) + Math.pow(y + 0.5 - center.y, 2.0) + Math.pow(z + 0.5 - center.z, 2.0));
               if (distance <= 1.5) {
                  BlockState state = level.getBlockState(pos);
                  Block block = state.getBlock();
                  if (block != Blocks.OBSIDIAN && block != Blocks.CRYING_OBSIDIAN && state.getDestroySpeed(level, pos) >= 0.0F) {
                     blocksToBreak.add(pos);
                  }
               }
            }
         }
      }

      blocksToBreak.sort((a, b) -> {
         double distA = a.getCenter().distanceTo(center);
         double distB = b.getCenter().distanceTo(center);
         return Double.compare(distA, distB);
      });

      for (int i = 0; i < Math.min(5, blocksToBreak.size()); i++) {
         level.destroyBlock(blocksToBreak.get(i), true);
      }
   }
}
