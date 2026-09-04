package io.hxneyw.repo.compat.create;

import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.entities.PyroclastBombDetonation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
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
            event.setCanceled(true);
            if (potatoProjectile.level() instanceof ServerLevel serverLevel) {
               Vec3 hitPos = event.getRayTraceResult().getLocation();
               PyroclastBombDetonation.resolve(serverLevel, potatoProjectile, potatoProjectile.getOwner(), hitPos, item);
               potatoProjectile.discard();
            }
         }
      }
   }
}
