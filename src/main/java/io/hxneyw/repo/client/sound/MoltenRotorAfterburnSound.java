package io.hxneyw.repo.client.sound;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.registry.AllModSounds;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class MoltenRotorAfterburnSound extends AbstractTickableSoundInstance {
    private static final Map<MoltenRotorBlockEntity, MoltenRotorAfterburnSound> ACTIVE_SOUNDS = new WeakHashMap<>();

    private final MoltenRotorBlockEntity blockEntity;

    private MoltenRotorAfterburnSound(MoltenRotorBlockEntity blockEntity) {
        super(AllModSounds.MOLTEN_ROTOR_RUMBLE.get(), SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = blockEntity;
        this.looping = true;
        this.delay = 0;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.volume = 0.01F;
        this.pitch = 0.76F;
        this.x = blockEntity.getBlockPos().getX() + 0.5D;
        this.y = blockEntity.getBlockPos().getY() + 0.5D;
        this.z = blockEntity.getBlockPos().getZ() + 0.5D;
    }

    public static void ensurePlaying(MoltenRotorBlockEntity blockEntity) {
        if (!blockEntity.isAfterburning()) {
            return;
        }

        MoltenRotorAfterburnSound existing = ACTIVE_SOUNDS.get(blockEntity);
        if (existing != null && !existing.isStopped()) {
            return;
        }

        MoltenRotorAfterburnSound sound = new MoltenRotorAfterburnSound(blockEntity);
        ACTIVE_SOUNDS.put(blockEntity, sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    @Override
    public void tick() {
        if (!isFurnaceStillPresent() || !blockEntity.isAfterburning()) {
            this.volume = Mth.lerp(0.18F, this.volume, 0.0F);
            if (this.volume <= 0.01F) {
                stop();
                ACTIVE_SOUNDS.remove(blockEntity, this);
            }
            return;
        }

        float intensity = Mth.clamp((blockEntity.getExactTemperature() - 1599.0F) / 401.0F, 0.0F, 1.0F);
        float targetVolume = Mth.lerp(intensity, 0.11F, 0.32F);
        float targetPitch = Mth.lerp(intensity, 0.78F, 0.68F);

        this.volume = Mth.lerp(0.055F, this.volume, targetVolume);
        this.pitch = Mth.lerp(0.045F, this.pitch, targetPitch);
    }

    private boolean isFurnaceStillPresent() {
        Minecraft minecraft = Minecraft.getInstance();
        return !blockEntity.isRemoved()
            && blockEntity.getLevel() != null
            && minecraft.level == blockEntity.getLevel();
    }
}
