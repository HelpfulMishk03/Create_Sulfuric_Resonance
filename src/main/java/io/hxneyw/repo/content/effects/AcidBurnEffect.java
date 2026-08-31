package io.hxneyw.repo.content.effects;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("resource")
public class AcidBurnEffect extends MobEffect {
   public AcidBurnEffect() {
      super(MobEffectCategory.HARMFUL, 14352128);
   }

   public boolean applyEffectTick(LivingEntity entity, int amplifier) {
      if (!entity.level().isClientSide()) {
         entity.hurt(entity.damageSources().magic(), 3.0F);
      }

      return true;
   }

   @NotNull
   public Component getDisplayName() {
      return Component.translatable("effect.sulfuricresonance.acid_burn");
   }

   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration % 20 == 0;
   }
}
