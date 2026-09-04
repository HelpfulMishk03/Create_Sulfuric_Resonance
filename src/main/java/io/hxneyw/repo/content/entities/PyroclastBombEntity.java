package io.hxneyw.repo.content.entities;

import io.hxneyw.repo.content.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
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
      if (this.level() instanceof ServerLevel serverLevel) {
         PyroclastBombDetonation.resolve(serverLevel, this, this.getOwner(), result.getLocation(), this.getItem());
         this.discard();
      }
   }

   public void tick() {
      super.tick();
      if (!this.isAlive()) {
         return;
      }

      if (!this.level().isClientSide && this.isInWaterOrBubble() && this.level() instanceof ServerLevel serverLevel) {
         PyroclastBombDetonation.extinguish(serverLevel, this.position(), this.getItem());
         this.discard();
         return;
      }

      if (this.level().isClientSide) {
         PyroclastBombDetonation.addTrailParticle(this);
      }
   }
}
