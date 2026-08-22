package io.hxneyw.repo.content.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllModSounds {
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "sulfuricresonance");

   public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_PADDING_HIT = SOUNDS.register(
           "rubber_padding_hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "rubber_padding_hit"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> MOLTEN_ROTOR_RUMBLE = SOUNDS.register(
           "molten_rotor_rumble", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "molten_rotor_rumble"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> THERMAL_WARNING_ALARM_STRIKE = SOUNDS.register(
           "thermal_warning_alarm_strike", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "thermal_warning_alarm_strike"))
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> THERMOCHEMICAL_CLUTCH_ENGAGE = SOUNDS.register(
           "thermochemical_clutch_engage", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "thermochemical_clutch_engage"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> THERMOCHEMICAL_CLUTCH_RELEASE = SOUNDS.register(
           "thermochemical_clutch_release", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "thermochemical_clutch_release"))
   );

}