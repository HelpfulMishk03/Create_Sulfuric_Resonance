package io.hxneyw.repo.content.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "sulfuricresonance");
   public static final DeferredHolder<EntityType<?>, EntityType<PyroclastBombEntity>> PYROCLAST_BOMB = ENTITY_TYPES.register(
      "pyroclastic_powder",
      () -> Builder.<PyroclastBombEntity>of(PyroclastBombEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("pyroclastic_powder")
   );

   public static void register(IEventBus eventBus) {
      ENTITY_TYPES.register(eventBus);
   }
}
