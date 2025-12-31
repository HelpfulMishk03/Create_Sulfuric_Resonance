package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, CreateSulfuricResonance.MODID);

    public static final Supplier<SimpleParticleType> COMBUSTION_PURPLE_FLAME = PARTICLE_TYPES.register("combustion_purple_flame",
            () -> new SimpleParticleType(false));
}