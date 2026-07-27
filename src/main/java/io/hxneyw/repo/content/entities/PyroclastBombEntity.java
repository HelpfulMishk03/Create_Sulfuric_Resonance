package io.hxneyw.repo.content.entities;

import io.hxneyw.repo.content.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("resource")
public class PyroclastBombEntity extends ThrowableItemProjectile {
   public PyroclastBombEntity(EntityType<? extends PyroclastBombEntity> type, Level level) {
      super(type, level);
   }

   public PyroclastBombEntity(Level level, LivingEntity shooter) {
      super(ModEntities.PYROCLAST_BOMB.get(), shooter, level);
   }

   @Override
   @NotNull
   protected Item getDefaultItem() {
      return Items.PYROCLAST_BOMB.get();
   }

   protected void onHit(@NotNull HitResult result) {
      super.onHit(result);
      if (!this.level().isClientSide) {
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.45F, false, ExplosionInteraction.BLOCK);
         this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5)).forEach(entity -> {
            if (entity != this.getOwner()) {
               entity.hurt(this.damageSources().thrown(this, this.getOwner()), 3.8F);
            }
         });
         if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 18; i++) {
               serverLevel.sendParticles(
                  ParticleTypes.FLAME,
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  1,
                  (this.random.nextDouble() - 0.5) * 0.5,
                  this.random.nextDouble() * 0.4,
                  (this.random.nextDouble() - 0.5) * 0.5,
                  0.1
               );
            }
         }

         this.discard();
      }
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide && this.random.nextFloat() < 0.5F) {
         this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
      }
   }
}
