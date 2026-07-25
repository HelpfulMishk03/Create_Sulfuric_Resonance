package io.hxneyw.repo.content.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllModSounds {
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "sulfuricresonance");
   public static final DeferredHolder<SoundEvent, SoundEvent> LOG_INSERT = SOUNDS.register(
      "log_insert", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "log_insert"))
   );
   public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_PADDING_HIT = SOUNDS.register(
      "rubber_padding_hit", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "rubber_padding_hit"))
   );
}
