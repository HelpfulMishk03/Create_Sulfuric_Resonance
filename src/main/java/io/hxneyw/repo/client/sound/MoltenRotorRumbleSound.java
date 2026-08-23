package io.hxneyw.repo.client.sound;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity.RotorHeatLevel;
import io.hxneyw.repo.content.registry.AllModSounds;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = "sulfuricresonance",
        value = Dist.CLIENT
)
public final class MoltenRotorRumbleSound
        extends AbstractTickableSoundInstance {

    private static final Map<MoltenRotorBlockEntity, MoltenRotorRumbleSound>
            ACTIVE_SOUNDS = new WeakHashMap<>();

    private final MoltenRotorBlockEntity blockEntity;

    private MoltenRotorRumbleSound(MoltenRotorBlockEntity blockEntity) {
        super(
                AllModSounds.MOLTEN_ROTOR_RUMBLE.get(),
                SoundSource.BLOCKS,
                RandomSource.create()
        );

        this.blockEntity = blockEntity;
        this.looping = true;
        this.delay = 0;
        this.relative = false;
        this.attenuation = Attenuation.LINEAR;
        this.volume = 0.01F;
        this.pitch = targetPitch(blockEntity.getCurrentHeatTier());

        this.x = blockEntity.getBlockPos().getX() + 0.5D;
        this.y = blockEntity.getBlockPos().getY() + 0.5D;
        this.z = blockEntity.getBlockPos().getZ() + 0.5D;
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MoltenRotorBlockEntity.setClientSoundTick(
                        MoltenRotorRumbleSound::ensurePlaying
                )
        );
    }

    public static void ensurePlaying(MoltenRotorBlockEntity blockEntity) {
        RotorHeatLevel tier = blockEntity.getCurrentHeatTier();
        if (tier == RotorHeatLevel.NONE) {
            return;
        }

        MoltenRotorRumbleSound existing = ACTIVE_SOUNDS.get(blockEntity);
        if (existing != null && !existing.isStopped()) {
            return;
        }

        MoltenRotorRumbleSound sound =
                new MoltenRotorRumbleSound(blockEntity);
        ACTIVE_SOUNDS.put(blockEntity, sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    @Override
    public void tick() {
        boolean valid = isFurnaceStillPresent();
        RotorHeatLevel tier = valid
                ? this.blockEntity.getCurrentHeatTier()
                : RotorHeatLevel.NONE;

        float targetVolume = targetVolume(tier);
        float targetPitch = targetPitch(tier);

        
        float volumeSmoothing = targetVolume > this.volume ? 0.06F : 0.12F;
        this.volume = Mth.lerp(volumeSmoothing, this.volume, targetVolume);
        this.pitch = Mth.lerp(0.05F, this.pitch, targetPitch);

        if (tier == RotorHeatLevel.NONE && this.volume <= 0.01F) {
            this.stop();
            ACTIVE_SOUNDS.remove(this.blockEntity, this);
        }
    }

    private boolean isFurnaceStillPresent() {
        Minecraft minecraft = Minecraft.getInstance();
        return !this.blockEntity.isRemoved()
                && this.blockEntity.getLevel() != null
                && minecraft.level == this.blockEntity.getLevel();
    }

    private static float targetVolume(RotorHeatLevel tier) {
        return switch (tier) {
            case NONE -> 0.0F;
            case FADING -> 0.30F;
            case SMOULDERING -> 0.42F;
            case KINDLED -> 0.58F;
            case SEETHING -> 0.74F;
            case RADIANT -> 0.9F;
        };
    }

    private static float targetPitch(RotorHeatLevel tier) {
        return switch (tier) {
            case NONE -> 0.72F;
            case FADING -> 0.76F;
            case SMOULDERING -> 0.80F;
            case KINDLED -> 0.88F;
            case SEETHING -> 0.98F;
            case RADIANT -> 1.08F;
        };
    }
}