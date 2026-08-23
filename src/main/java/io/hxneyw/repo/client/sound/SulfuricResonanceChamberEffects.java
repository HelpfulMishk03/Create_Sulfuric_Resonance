package io.hxneyw.repo.client.sound;

import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity.ChamberStatus;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity.ReactionLevel;
import io.hxneyw.repo.content.registry.AllModSounds;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
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
public final class SulfuricResonanceChamberEffects {

    private static final Map<SulfuricResonanceChamberBlockEntity, VisualState>
            VISUAL_STATES = new WeakHashMap<>();
    private static final Map<SulfuricResonanceChamberBlockEntity, ChamberStartupSound>
            STARTUP_SOUNDS = new WeakHashMap<>();

    private SulfuricResonanceChamberEffects() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                SulfuricResonanceChamberBlockEntity.setClientEffectsTick(
                        SulfuricResonanceChamberEffects::tickClient
                )
        );
    }

    public static void tickClient(
            SulfuricResonanceChamberBlockEntity chamber
    ) {
        if (!(chamber.getLevel() instanceof ClientLevel level)) {
            return;
        }

        VisualState state = VISUAL_STATES.computeIfAbsent(
                chamber,
                ignored -> new VisualState()
        );
        ChamberStatus status = chamber.getStatus();
        boolean processing = chamber.isProcessingActive();
        boolean outputPresent = chamber.hasCompletedOutput();
        float platformLift = chamber.getClientPlatformLift(1.0F);

        if (!state.initialized) {
            state.initialized = true;
            state.wasProcessing = processing;
            state.previousStatus = status;
            state.hadOutput = outputPresent;
            state.platformLatched = platformLift >= 0.92F;
            tickParticles(level, chamber);
            return;
        }

        if (!state.wasProcessing && processing) {
            playLocal(
                    level,
                    chamber,
                    AllModSounds.SULFURIC_RESONANCE_CHAMBER_READY.get(),
                    0.46F,
                    1.02F
            );
            startStartupSound(chamber);
            spawnStartupPulse(level, chamber);
        } else if (state.previousStatus != ChamberStatus.READY
                && status == ChamberStatus.READY) {
            playLocal(
                    level,
                    chamber,
                    AllModSounds.SULFURIC_RESONANCE_CHAMBER_READY.get(),
                    0.46F,
                    1.02F
            );
        }

        if (state.previousStatus != ChamberStatus.MISSING_ACID
                && status == ChamberStatus.MISSING_ACID) {
            playLocal(
                    level,
                    chamber,
                    AllModSounds.SULFURIC_RESONANCE_CHAMBER_DRY_CLICK.get(),
                    0.58F,
                    0.96F
            );
        }

        if (status == ChamberStatus.INSUFFICIENT_SPEED) {
            if (state.previousStatus != ChamberStatus.INSUFFICIENT_SPEED) {
                playStrain(level, chamber);
                state.strainCooldown = 80;
            } else if (state.strainCooldown > 0) {
                state.strainCooldown--;
            } else {
                playStrain(level, chamber);
                state.strainCooldown = 80;
            }
        } else {
            state.strainCooldown = 0;
        }

        boolean latchEligible = processing
                || chamber.isReadyState()
                || status == ChamberStatus.OUTPUT_BLOCKED;
        if (latchEligible
                && platformLift >= 0.94F
                && !state.platformLatched) {
            playLocal(
                    level,
                    chamber,
                    AllModSounds.SULFURIC_RESONANCE_CHAMBER_LOCK_IN.get(),
                    0.82F,
                    0.94F
            );
            state.platformLatched = true;
        } else if (platformLift < 0.28F) {
            state.platformLatched = false;
        }

        if (state.hadOutput && !outputPresent) {
            playLocal(
                    level,
                    chamber,
                    AllModSounds.SULFURIC_RESONANCE_CHAMBER_RELEASE.get(),
                    0.78F,
                    1.00F
            );
            spawnReleasePulse(level, chamber);
        }

        tickParticles(level, chamber);

        state.wasProcessing = processing;
        state.previousStatus = status;
        state.hadOutput = outputPresent;
    }

    private static void startStartupSound(
            SulfuricResonanceChamberBlockEntity chamber
    ) {
        ChamberStartupSound existing = STARTUP_SOUNDS.get(chamber);
        if (existing != null && !existing.isStopped()) {
            return;
        }

        ChamberStartupSound sound = new ChamberStartupSound(chamber);
        STARTUP_SOUNDS.put(chamber, sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    private static void playStrain(
            ClientLevel level,
            SulfuricResonanceChamberBlockEntity chamber
    ) {
        playLocal(
                level,
                chamber,
                AllModSounds.SULFURIC_RESONANCE_CHAMBER_STRAIN.get(),
                0.62F,
                0.90F
        );
    }

    private static void playLocal(
            ClientLevel level,
            SulfuricResonanceChamberBlockEntity chamber,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        level.playLocalSound(
                chamber.getBlockPos().getX() + 0.5D,
                chamber.getBlockPos().getY() + 0.5D,
                chamber.getBlockPos().getZ() + 0.5D,
                sound,
                SoundSource.BLOCKS,
                volume,
                pitch,
                false
        );
    }

    private static void tickParticles(
            ClientLevel level,
            SulfuricResonanceChamberBlockEntity chamber
    ) {
        long phase = level.getGameTime()
                + Math.floorMod(chamber.getBlockPos().asLong(), 97L);

        if (chamber.isProcessingActive()) {
            float activation = chamber.getClientVisualActivation(1.0F);
            int interval = activation < 0.55F ? 8 : 18;
            if (phase % interval == 0L) {
                spawnResonanceParticle(level, chamber, activation);
            }
            return;
        }

        if (chamber.isReadyState() && phase % 48L == 0L) {
            double angle = phase * 0.17D;
            double x = chamber.getBlockPos().getX()
                    + 0.5D
                    + Math.cos(angle) * 0.14D;
            double z = chamber.getBlockPos().getZ()
                    + 0.5D
                    + Math.sin(angle) * 0.14D;
            level.addParticle(
                    ParticleTypes.END_ROD,
                    x,
                    chamber.getBlockPos().getY() + 0.47D,
                    z,
                    0.0D,
                    0.006D,
                    0.0D
            );
        }
    }

    private static void spawnResonanceParticle(
            ClientLevel level,
            SulfuricResonanceChamberBlockEntity chamber,
            float activation
    ) {
        RandomSource random = level.random;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = 0.12D + random.nextDouble() * 0.08D;
        double x = chamber.getBlockPos().getX()
                + 0.5D
                + Math.cos(angle) * radius;
        double y = chamber.getBlockPos().getY()
                + 0.41D
                + random.nextDouble() * 0.13D;
        double z = chamber.getBlockPos().getZ()
                + 0.5D
                + Math.sin(angle) * radius;

        level.addParticle(
                ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                x,
                y,
                z,
                0.0D,
                0.002D + activation * 0.003D,
                0.0D
        );

        if (activation > 0.72F && random.nextFloat() < 0.28F) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    x,
                    y + 0.02D,
                    z,
                    0.0D,
                    0.008D,
                    0.0D
            );
        }
    }

    private static void spawnStartupPulse(
            ClientLevel level,
            SulfuricResonanceChamberBlockEntity chamber
    ) {
        double x = chamber.getBlockPos().getX() + 0.5D;
        double y = chamber.getBlockPos().getY() + 0.43D;
        double z = chamber.getBlockPos().getZ() + 0.5D;
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI * 0.5D;
            level.addParticle(
                    ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                    x + Math.cos(angle) * 0.15D,
                    y,
                    z + Math.sin(angle) * 0.15D,
                    0.0D,
                    0.004D,
                    0.0D
            );
        }
    }

    private static void spawnReleasePulse(
            ClientLevel level,
            SulfuricResonanceChamberBlockEntity chamber
    ) {
        double x = chamber.getBlockPos().getX() + 0.5D;
        double y = chamber.getBlockPos().getY() + 0.38D;
        double z = chamber.getBlockPos().getZ() + 0.5D;
        for (int i = 0; i < 3; i++) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    x + (level.random.nextDouble() - 0.5D) * 0.18D,
                    y + level.random.nextDouble() * 0.08D,
                    z + (level.random.nextDouble() - 0.5D) * 0.18D,
                    0.0D,
                    0.010D,
                    0.0D
            );
        }
    }

    private static final class ChamberStartupSound
            extends AbstractTickableSoundInstance {

        private final SulfuricResonanceChamberBlockEntity chamber;
        private boolean ending;

        private ChamberStartupSound(
                SulfuricResonanceChamberBlockEntity chamber
        ) {
            super(
                    AllModSounds.SULFURIC_RESONANCE_CHAMBER_STARTUP.get(),
                    SoundSource.BLOCKS,
                    RandomSource.create()
            );
            this.chamber = chamber;
            this.looping = false;
            this.delay = 0;
            this.relative = false;
            this.attenuation = Attenuation.LINEAR;
            this.volume = 0.10F;
            this.pitch = initialPitch(chamber.getClientVisualReactionLevel());
            this.x = chamber.getBlockPos().getX() + 0.5D;
            this.y = chamber.getBlockPos().getY() + 0.5D;
            this.z = chamber.getBlockPos().getZ() + 0.5D;
        }

        @Override
        public void tick() {
            if (!isChamberStillPresent() || !chamber.isProcessingActive()) {
                ending = true;
            }

            float activation = chamber.getClientVisualActivation(1.0F);
            ReactionLevel reactionLevel = chamber.getClientVisualReactionLevel();

            if (activation >= 0.995F) {
                ending = true;
            }

            if (ending) {
                volume = Mth.lerp(0.22F, volume, 0.0F);
                if (volume <= 0.012F) {
                    stop();
                    STARTUP_SOUNDS.remove(chamber, this);
                }
                return;
            }

            float targetVolume = Mth.lerp(activation, 0.14F, 0.32F);
            float targetPitch = reactionLevel == ReactionLevel.RESONANCE
                    ? Mth.lerp(activation, 0.62F, 0.82F)
                    : Mth.lerp(activation, 0.78F, 1.00F);
            volume = Mth.lerp(0.14F, volume, targetVolume);
            pitch = Mth.lerp(0.10F, pitch, targetPitch);
        }

        private boolean isChamberStillPresent() {
            Minecraft minecraft = Minecraft.getInstance();
            return !chamber.isRemoved()
                    && chamber.getLevel() != null
                    && minecraft.level == chamber.getLevel();
        }

        private static float initialPitch(ReactionLevel reactionLevel) {
            return reactionLevel == ReactionLevel.RESONANCE
                    ? 0.62F
                    : 0.78F;
        }
    }

    private static final class VisualState {
        private boolean initialized;
        private boolean wasProcessing;
        private boolean hadOutput;
        private boolean platformLatched;
        private int strainCooldown;
        private ChamberStatus previousStatus = ChamberStatus.IDLE;
    }
}
