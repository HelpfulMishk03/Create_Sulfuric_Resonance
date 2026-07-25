package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.effects.AcidBurnEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllModEffects {
   public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "sulfuricresonance");
   public static final DeferredHolder<MobEffect, AcidBurnEffect> ACID_BURN = EFFECTS.register("acid_burn", AcidBurnEffect::new);

   public static void register(IEventBus eventBus) {
      EFFECTS.register(eventBus);
      CreateSulfuricResonance.LOGGER.info("Effects registered for Sulfuric Resonance");
   }
}
