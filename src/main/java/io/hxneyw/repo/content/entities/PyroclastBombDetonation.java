package io.hxneyw.repo.content.entities;

import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModSounds;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PyroclastBombDetonation {
   private static final double ENTITY_RADIUS = 3.25D;
   private static final double BLOCK_RADIUS = 2.4D;
   private static final float CENTER_DAMAGE = 8.0F;
   private static final double KNOCKBACK_STRENGTH = 0.55D;
   private static final int MAX_DESTROYED_BLOCKS = 5;
   private static final float MAX_BREAKABLE_RESISTANCE = 1199.0F;
   private static final float DROP_SURVIVAL_CHANCE = 0.7F;
   private static final float IGNITION_CHANCE = 0.1F;

   private PyroclastBombDetonation() {
   }

   public static void resolve(ServerLevel level, Entity projectile, @Nullable Entity owner, Vec3 center, ItemStack stack) {
      if (projectile.isInWaterOrBubble() || isUnderwater(level, center)) {
         extinguish(level, center, stack);
      } else {
         detonate(level, projectile, owner, center);
      }
   }

   public static void detonate(ServerLevel level, Entity projectile, @Nullable Entity owner, Vec3 center) {
      level.playSound(
         null,
         center.x,
         center.y,
         center.z,
         AllModSounds.PYROCLAST_BOMB_DETONATE.get(),
         SoundSource.BLOCKS,
         2.4F,
         0.96F + level.random.nextFloat() * 0.08F
      );
      damageEntities(level, projectile, owner, center);
      destroyBlocks(level, owner, center);
      createParticles(level, center);
      igniteOneBlock(level, center);
   }

   public static void extinguish(ServerLevel level, Vec3 center, ItemStack sourceStack) {
      ItemStack recovered = sourceStack.isEmpty() ? new ItemStack(Items.PYROCLAST_BOMB.get()) : sourceStack.copy();
      recovered.setCount(1);
      ItemEntity pickup = new ItemEntity(level, center.x, center.y, center.z, recovered);
      pickup.setDeltaMovement(0.0D, 0.08D, 0.0D);
      pickup.setDefaultPickUpDelay();
      level.addFreshEntity(pickup);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.72F);
      level.sendParticles(ParticleTypes.BUBBLE, center.x, center.y, center.z, 14, 0.3D, 0.3D, 0.3D, 0.08D);
      level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 8, 0.2D, 0.2D, 0.2D, 0.025D);
   }

   public static void addTrailParticle(PyroclastBombEntity bomb) {
      if (bomb.tickCount % 2 == 0) {
         bomb.level().addParticle(
            ParticleTypes.SMOKE,
            bomb.getX(),
            bomb.getY() + 0.04D,
            bomb.getZ(),
            -bomb.getDeltaMovement().x * 0.025D,
            0.005D,
            -bomb.getDeltaMovement().z * 0.025D
         );
      }

      if (bomb.level().getRandom().nextInt(7) == 0) {
         bomb.level().addParticle(
            ModParticles.PYROCLASTIC_FRAGMENT.get(),
            bomb.getX(),
            bomb.getY(),
            bomb.getZ(),
            (bomb.level().getRandom().nextDouble() - 0.5D) * 0.025D,
            -0.015D,
            (bomb.level().getRandom().nextDouble() - 0.5D) * 0.025D
         );
      }
   }

   private static boolean isUnderwater(ServerLevel level, Vec3 center) {
      return level.getFluidState(BlockPos.containing(center)).is(FluidTags.WATER);
   }

   private static void damageEntities(ServerLevel level, Entity projectile, @Nullable Entity owner, Vec3 center) {
      AABB area = new AABB(center, center).inflate(ENTITY_RADIUS);
      for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, area)) {
         double distance = distanceToBounds(center, livingEntity.getBoundingBox());
         if (distance > ENTITY_RADIUS) {
            continue;
         }

         double falloff = 1.0D - distance / ENTITY_RADIUS;
         double exposure = Explosion.getSeenPercent(center, livingEntity);
         if (exposure <= 0.0D) {
            continue;
         }

         float damage = (float)(CENTER_DAMAGE * falloff * exposure);
         if (livingEntity == owner) {
            damage *= 0.5F;
         }

         if (damage > 0.0F) {
            livingEntity.hurt(level.damageSources().explosion(projectile, owner), damage);
         }

         applyKnockback(livingEntity, center, falloff * exposure);
      }
   }

   private static double distanceToBounds(Vec3 center, AABB bounds) {
      double x = Mth.clamp(center.x, bounds.minX, bounds.maxX);
      double y = Mth.clamp(center.y, bounds.minY, bounds.maxY);
      double z = Mth.clamp(center.z, bounds.minZ, bounds.maxZ);
      return center.distanceTo(new Vec3(x, y, z));
   }

   private static void applyKnockback(LivingEntity livingEntity, Vec3 center, double strengthScale) {
      Vec3 origin = livingEntity.position().add(0.0D, livingEntity.getBbHeight() * 0.5D, 0.0D);
      Vec3 direction = origin.subtract(center);
      double horizontalLength = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
      if (horizontalLength < 1.0E-4D) {
         direction = new Vec3(0.0D, 1.0D, 0.0D);
         horizontalLength = 1.0D;
      }

      double strength = KNOCKBACK_STRENGTH * strengthScale;
      livingEntity.push(
         direction.x / horizontalLength * strength,
         0.1D + strength * 0.32D,
         direction.z / horizontalLength * strength
      );
      livingEntity.hurtMarked = true;
   }

   private static void destroyBlocks(ServerLevel level, @Nullable Entity owner, Vec3 center) {
      BlockPos origin = BlockPos.containing(center);
      int range = Mth.ceil(BLOCK_RADIUS);
      List<BlockCandidate> candidates = new ArrayList<>();

      for (BlockPos cursor : BlockPos.betweenClosed(origin.offset(-range, -range, -range), origin.offset(range, range, range))) {
         BlockPos pos = cursor.immutable();
         double distance = pos.getCenter().distanceTo(center);
         if (distance > BLOCK_RADIUS) {
            continue;
         }

         BlockState state = level.getBlockState(pos);
         if (!canDestroy(level, pos, state)) {
            continue;
         }

         double score = distance + level.random.nextDouble() * 1.35D;
         candidates.add(new BlockCandidate(pos, score));
      }

      candidates.sort(Comparator.comparingDouble(BlockCandidate::score));
      int count = Math.min(MAX_DESTROYED_BLOCKS, candidates.size());
      for (int i = 0; i < count; i++) {
         boolean drop = level.random.nextFloat() < DROP_SURVIVAL_CHANCE;
         level.destroyBlock(candidates.get(i).pos(), drop, owner);
      }
   }

   private static boolean canDestroy(ServerLevel level, BlockPos pos, BlockState state) {
      if (state.isAir() || !state.getFluidState().isEmpty()) {
         return false;
      }

      if (state.getDestroySpeed(level, pos) < 0.0F) {
         return false;
      }

      return state.getBlock().getExplosionResistance() <= MAX_BREAKABLE_RESISTANCE;
   }

   private static void createParticles(ServerLevel level, Vec3 center) {
      level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 2, 0.16D, 0.12D, 0.16D, 0.02D);
      level.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z, 22, 0.72D, 0.52D, 0.72D, 0.16D);
      level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 12, 0.68D, 0.46D, 0.68D, 0.08D);
      level.sendParticles(ParticleTypes.ASH, center.x, center.y, center.z, 30, 0.9D, 0.65D, 0.9D, 0.12D);
      level.sendParticles(ModParticles.PYROCLASTIC_FRAGMENT.get(), center.x, center.y, center.z, 34, 0.78D, 0.58D, 0.78D, 0.22D);
   }

   private static void igniteOneBlock(ServerLevel level, Vec3 center) {
      if (level.random.nextFloat() >= IGNITION_CHANCE) {
         return;
      }

      BlockPos origin = BlockPos.containing(center);
      List<BlockPos> eligible = new ArrayList<>();
      BlockState fire = Blocks.FIRE.defaultBlockState();
      for (BlockPos cursor : BlockPos.betweenClosed(origin.offset(-2, -1, -2), origin.offset(2, 2, 2))) {
         BlockPos pos = cursor.immutable();
         if (level.isEmptyBlock(pos) && fire.canSurvive(level, pos)) {
            eligible.add(pos);
         }
      }

      if (!eligible.isEmpty()) {
         BlockPos chosen = eligible.get(level.random.nextInt(eligible.size()));
         level.setBlockAndUpdate(chosen, fire);
      }
   }

   private record BlockCandidate(BlockPos pos, double score) {
   }
}
