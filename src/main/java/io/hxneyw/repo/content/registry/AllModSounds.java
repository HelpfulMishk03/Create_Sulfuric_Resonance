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

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFUR_BURNER_IGNITE = SOUNDS.register(
           "sulfur_burner_ignite", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfur_burner_ignite"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFUR_BURNER_CRACKING = SOUNDS.register(
           "sulfur_burner_cracking", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfur_burner_cracking"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFUR_BURNER_LOOP = SOUNDS.register(
           "sulfur_burner_loop", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfur_burner_loop"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFUR_BURNER_EXTINGUISH = SOUNDS.register(
           "sulfur_burner_extinguish", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfur_burner_extinguish"))
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> SULFURIC_RESONANCE_CHAMBER_STARTUP = SOUNDS.register(
           "sulfuric_resonance_chamber_startup", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfuric_resonance_chamber_startup"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFURIC_RESONANCE_CHAMBER_LOCK_IN = SOUNDS.register(
           "sulfuric_resonance_chamber_lock_in", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfuric_resonance_chamber_lock_in"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFURIC_RESONANCE_CHAMBER_READY = SOUNDS.register(
           "sulfuric_resonance_chamber_ready", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfuric_resonance_chamber_ready"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFURIC_RESONANCE_CHAMBER_RELEASE = SOUNDS.register(
           "sulfuric_resonance_chamber_release", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfuric_resonance_chamber_release"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFURIC_RESONANCE_CHAMBER_STRAIN = SOUNDS.register(
           "sulfuric_resonance_chamber_strain", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfuric_resonance_chamber_strain"))
   );

   public static final DeferredHolder<SoundEvent, SoundEvent> SULFURIC_RESONANCE_CHAMBER_DRY_CLICK = SOUNDS.register(
           "sulfuric_resonance_chamber_dry_click", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "sulfuric_resonance_chamber_dry_click"))
   );

}
