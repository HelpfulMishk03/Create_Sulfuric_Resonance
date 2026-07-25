package io.hxneyw.repo.compat.create;

import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType.Builder;
import com.simibubi.create.api.registry.CreateRegistries;
import io.hxneyw.repo.content.Items;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModdedPotatoProjectileTypes {
   public static final ResourceKey<PotatoCannonProjectileType> PYROCLAST_BOMB = ResourceKey.create(
      CreateRegistries.POTATO_PROJECTILE_TYPE, ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "pyroclast_bomb")
   );

   public static void bootstrap(BootstrapContext<PotatoCannonProjectileType> ctx) {
      ctx.register(
         PYROCLAST_BOMB,
         new Builder()
            .damage(3)
            .reloadTicks(25)
            .velocity(1.2F)
            .knockback(1.5F)
            .renderTumbling()
            .soundPitch(0.7F)
                 .addItems(Items.PYROCLAST_BOMB.get())
            .build()
      );
   }
}
