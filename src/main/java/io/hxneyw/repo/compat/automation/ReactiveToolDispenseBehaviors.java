package io.hxneyw.repo.compat.automation;

import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.entities.CinderFlareEntity;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.entities.PyroclastBombEntity;
import io.hxneyw.repo.content.entities.SulfuricAcidFlaskEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("resource")
public final class ReactiveToolDispenseBehaviors {
   private ReactiveToolDispenseBehaviors() {
   }

   public static void register() {
      DispenserBlock.registerBehavior(
         Items.SULFURIC_ACID_FLASK.get(),
         new ReactiveProjectileBehavior(
            level -> new SulfuricAcidFlaskEntity(ModEntities.SULFURIC_ACID_FLASK.get(), level),
            0.8F,
            0.6F
         )
      );
      DispenserBlock.registerBehavior(
         Items.PYROCLAST_BOMB.get(),
         new ReactiveProjectileBehavior(
            level -> new PyroclastBombEntity(ModEntities.PYROCLAST_BOMB.get(), level),
            0.78F,
            1.4F
         )
      );
      DispenserBlock.registerBehavior(
         Items.LIT_CINDER_FLARE.get(),
         new ReactiveProjectileBehavior(
            level -> new CinderFlareEntity(ModEntities.CINDER_FLARE.get(), level),
            0.46F,
            0.25F
         )
      );
   }

   private static final class ReactiveProjectileBehavior extends DefaultDispenseItemBehavior {
      private final ProjectileFactory factory;
      private final float velocity;
      private final float inaccuracy;

      private ReactiveProjectileBehavior(
         ProjectileFactory factory,
         float velocity,
         float inaccuracy
      ) {
         this.factory = factory;
         this.velocity = velocity;
         this.inaccuracy = inaccuracy;
      }

      @Override
      protected @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
         Direction facing = source.state().getValue(DispenserBlock.FACING);
         Position position = DispenserBlock.getDispensePosition(source);
         ThrowableItemProjectile projectile = factory.create(source.level());
         projectile.setItem(stack.copyWithCount(1));
         projectile.setPos(position.x(), position.y(), position.z());
         projectile.shoot(
            facing.getStepX(),
            facing.getStepY(),
            facing.getStepZ(),
            velocity,
            inaccuracy
         );
         source.level().addFreshEntity(projectile);
         stack.shrink(1);
         return stack;
      }
   }

   @FunctionalInterface
   private interface ProjectileFactory {
      ThrowableItemProjectile create(net.minecraft.server.level.ServerLevel level);
   }
}
