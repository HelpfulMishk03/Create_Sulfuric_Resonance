package io.hxneyw.repo.content.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {
   public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, "sulfuricresonance");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> COMBUSTION_PURPLE_FLAME = PARTICLE_TYPES.register(
      "combustion_purple_flame", () -> new SimpleParticleType(false)
   );
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ACID_DRIP = PARTICLE_TYPES.register("acid_drip", () -> new SimpleParticleType(false));
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PYROCLASTIC_FRAGMENT = PARTICLE_TYPES.register(
      "pyroclastic_fragment", () -> new SimpleParticleType(false)
   );

   public static void register(IEventBus eventBus) {
      PARTICLE_TYPES.register(eventBus);
   }
}
