package io.hxneyw.repo.client.sound;

import io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerBlockEntity;
import io.hxneyw.repo.content.registry.AllModSounds;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = "sulfuricresonance",
        value = Dist.CLIENT
)
public final class SulfurBurnerEffects {

    private static final int AFTERGLOW_TICKS = 70;
    private static final Map<SulfurBurnerBlockEntity, VisualState>
            VISUAL_STATES = new WeakHashMap<>();
    private static final Map<SulfurBurnerBlockEntity, BurnerLoopSound>
            LOOP_SOUNDS = new WeakHashMap<>();
    private static final Map<SulfurBurnerBlockEntity, BurnerCrackingSound>
            CRACKING_SOUNDS = new WeakHashMap<>();

    private SulfurBurnerEffects() {
    }

    @SubscribeEvent
    public static void onClientSetup(
            FMLClientSetupEvent event
    ) {
        event.enqueueWork(() ->
                SulfurBurnerBlockEntity.setClientEffectsTick(
                        SulfurBurnerEffects::tickClient
                )
        );
    }

    public static void tickClient(
            SulfurBurnerBlockEntity blockEntity
    ) {
        if (!(blockEntity.getLevel()
                instanceof ClientLevel level)) {
            return;
        }

        VisualState state =
                VISUAL_STATES.computeIfAbsent(
                        blockEntity,
                        ignored -> new VisualState()
                );

        boolean burning =
                blockEntity.isBurning();

        boolean seething =
                burning
                        && blockEntity.getHeatLevel()
                        == com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SEETHING;

        if (!state.initialized) {
            state.initialized = true;
            state.wasBurning = burning;
            state.wasSeething = seething;
        } else {
            if (!state.wasBurning && burning) {
                state.ignitionFlashTicks = 8;
                spawnIgnitionBurst(
                        level,
                        blockEntity
                );
            }

            if (state.wasBurning && !burning) {
                state.afterglowTicks =
                        AFTERGLOW_TICKS;

                spawnShutdownBurst(
                        level,
                        blockEntity
                );
            }

            if (!state.wasSeething && seething) {
                spawnStabilizationPulse(
                        level,
                        blockEntity
                );
            }

            state.wasBurning = burning;
            state.wasSeething = seething;
        }

        if (state.ignitionFlashTicks > 0) {
            state.ignitionFlashTicks--;
        }

        if (!burning
                && state.afterglowTicks > 0) {
            state.afterglowTicks--;
        }

        if (burning) {
            tickCombustionParticles(
                    level,
                    blockEntity
            );
        }

        ensureLoopSound(blockEntity);
        ensureCrackingSound(blockEntity);
    }

    public static int getCoreLight(
            SulfurBurnerBlockEntity blockEntity,
            int packedLight
    ) {
        int baseBlock =
                net.minecraft.client.renderer.LightTexture
                        .block(packedLight);

        int sky =
                net.minecraft.client.renderer.LightTexture
                        .sky(packedLight);

        float intensity =
                getCoreIntensity(blockEntity);

        int targetBlock =
                Mth.clamp(
                        Math.round(intensity * 15.0F),
                        0,
                        15
                );

        return net.minecraft.client.renderer.LightTexture.pack(
                Math.max(baseBlock, targetBlock),
                sky
        );
    }

    private static float getCoreIntensity(
            SulfurBurnerBlockEntity blockEntity
    ) {
        VisualState state =
                VISUAL_STATES.get(blockEntity);

        boolean burning =
                blockEntity.isBurning();

        if (burning) {
            float warmup =
                    blockEntity.getWarmupProgress();

            float intensity;

            if (warmup < 0.4F) {
                intensity =
                        Mth.lerp(
                                warmup / 0.4F,
                                0.10F,
                                0.35F
                        );

                ClientLevel level =
                        (ClientLevel)
                                blockEntity.getLevel();

                if (level != null) {
                    long phase =
                            level.getGameTime()
                                    + blockEntity
                                    .getBlockPos()
                                    .asLong();

                    float instability =
                            (float) Math.sin(
                                    phase * 0.68D
                            ) * 0.055F;

                    intensity =
                            Mth.clamp(
                                    intensity
                                            + instability,
                                    0.08F,
                                    0.42F
                            );
                }
            } else if (warmup < 0.8F) {
                intensity =
                        Mth.lerp(
                                (warmup - 0.4F)
                                        / 0.4F,
                                0.35F,
                                0.75F
                        );
            } else {
                intensity =
                        Mth.lerp(
                                (warmup - 0.8F)
                                        / 0.2F,
                                0.75F,
                                1.0F
                        );
            }

            if (state != null
                    && state.ignitionFlashTicks > 0) {
                float flash =
                        state.ignitionFlashTicks
                                / 8.0F;

                intensity =
                        Math.max(
                                intensity,
                                0.55F
                                        + flash * 0.35F
                        );
            }

            return Mth.clamp(
                    intensity,
                    0.0F,
                    1.0F
            );
        }

        if (state != null
                && state.afterglowTicks > 0) {
            float afterglow =
                    state.afterglowTicks
                            / (float) AFTERGLOW_TICKS;

            return afterglow
                    * afterglow
                    * 0.78F;
        }

        return 0.0F;
    }

    private static void tickCombustionParticles(
            ClientLevel level,
            SulfurBurnerBlockEntity blockEntity
    ) {
        int burnTicks =
                blockEntity.getActiveBurnTicks();

        float warmup =
                blockEntity.getWarmupProgress();

        long phase =
                level.getGameTime()
                        + blockEntity
                        .getBlockPos()
                        .asLong();

        boolean flameTick;

        if (warmup < 0.4F) {
            flameTick =
                    phase % 11L == 0L
                            || phase % 17L == 4L;
        } else if (warmup < 0.8F) {
            flameTick =
                    phase % 7L == 0L;
        } else if (warmup < 1.0F) {
            flameTick =
                    phase % 5L == 0L;
        } else {
            flameTick =
                    phase % 8L == 0L;
        }

        if (flameTick) {
            spawnSulfurFlame(
                    level,
                    blockEntity,
                    warmup < 0.4F
            );
        }

        int cinderInterval =
                warmup < 0.4F
                        ? 27
                        : warmup < 0.8F
                        ? 18
                        : warmup < 1.0F
                        ? 15
                        : 36;

        if (phase % cinderInterval == 0L) {
            spawnCinder(
                    level,
                    blockEntity
            );
        }

        int smokeInterval =
                warmup < 0.4F
                        ? 42
                        : warmup < 1.0F
                        ? 34
                        : 52;

        if (phase % smokeInterval == 0L
                && burnTicks > 8) {
            spawnSmoke(
                    level,
                    blockEntity
            );
        }
    }

    private static void spawnSulfurFlame(
            ClientLevel level,
            SulfurBurnerBlockEntity blockEntity,
            boolean unstable
    ) {
        RandomSource random =
                level.random;

        double lateral =
                (
                        random.nextDouble()
                                - 0.5D
                ) * (
                        unstable
                                ? 0.20D
                                : 0.13D
                );

        double[] point =
                burnerPoint(
                        blockEntity,
                        lateral,
                        0.0D
                );

        double lateralVelocity =
                (
                        random.nextDouble()
                                - 0.5D
                ) * (
                        unstable
                                ? 0.020D
                                : 0.008D
                );

        Direction right =
                blockEntity
                        .getBlockState()
                        .getValue(
                                io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerBlock.FACING
                        )
                        .getClockWise();

        level.addParticle(
                ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                point[0],
                point[1],
                point[2],
                right.getStepX()
                        * lateralVelocity,
                unstable
                        ? 0.010D
                        + random.nextDouble()
                        * 0.018D
                        : 0.012D
                        + random.nextDouble()
                        * 0.010D,
                right.getStepZ()
                        * lateralVelocity
        );
    }

    private static void spawnCinder(
            ClientLevel level,
            SulfurBurnerBlockEntity blockEntity
    ) {
        RandomSource random =
                level.random;

        double[] point =
                burnerPoint(
                        blockEntity,
                        (
                                random.nextDouble()
                                        - 0.5D
                        ) * 0.14D,
                        0.015D
                );

        level.addParticle(
                ParticleTypes.LAVA,
                point[0],
                point[1],
                point[2],
                (
                        random.nextDouble()
                                - 0.5D
                ) * 0.012D,
                0.010D
                        + random.nextDouble()
                        * 0.018D,
                (
                        random.nextDouble()
                                - 0.5D
                ) * 0.012D
        );
    }

    private static void spawnSmoke(
            ClientLevel level,
            SulfurBurnerBlockEntity blockEntity
    ) {
        RandomSource random =
                level.random;

        double[] point =
                burnerPoint(
                        blockEntity,
                        (
                                random.nextDouble()
                                        - 0.5D
                        ) * 0.12D,
                        0.025D
                );

        level.addParticle(
                ParticleTypes.SMOKE,
                point[0],
                point[1],
                point[2],
                0.0D,
                0.010D
                        + random.nextDouble()
                        * 0.006D,
                0.0D
        );
    }

    private static void spawnIgnitionBurst(
            ClientLevel level,
            SulfurBurnerBlockEntity blockEntity
    ) {
        for (int i = 0; i < 4; i++) {
            spawnSulfurFlame(
                    level,
                    blockEntity,
                    true
            );
        }

        spawnCinder(
                level,
                blockEntity
        );

        spawnCinder(
                level,
                blockEntity
        );

        spawnSmoke(
                level,
                blockEntity
        );
    }

    private static void spawnStabilizationPulse(
            ClientLevel level,
            SulfurBurnerBlockEntity blockEntity
    ) {
        for (int i = 0; i < 3; i++) {
            spawnSulfurFlame(
                    level,
                    blockEntity,
                    false
            );
        }

        spawnCinder(
                level,
                blockEntity
        );

        spawnSmoke(
                level,
                blockEntity
        );
    }

    private static void spawnShutdownBurst(
            ClientLevel level,
            SulfurBurnerBlockEntity blockEntity
    ) {
        spawnSulfurFlame(
                level,
                blockEntity,
                true
        );

        spawnSmoke(
                level,
                blockEntity
        );

        spawnSmoke(
                level,
                blockEntity
        );
    }

    private static double[] burnerPoint(
            SulfurBurnerBlockEntity blockEntity,
            double lateral,
            double vertical
    ) {
        BlockState state =
                blockEntity.getBlockState();

        Direction facing =
                state.getValue(
                        io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerBlock.FACING
                );

        Direction right =
                facing.getClockWise();

        BlockPos pos =
                blockEntity.getBlockPos();

        double inward =
                2.5D / 16.0D;

        double x =
                pos.getX()
                        + 0.5D
                        - facing.getStepX()
                        * inward
                        + right.getStepX()
                        * lateral;

        double y =
                pos.getY()
                        + 3.3D / 16.0D
                        - 0.6D / 16.0D
                        + 0.025D
                        + vertical;

        double z =
                pos.getZ()
                        + 0.5D
                        - facing.getStepZ()
                        * inward
                        + right.getStepZ()
                        * lateral;

        return new double[] {
                x,
                y,
                z
        };
    }

    private static void ensureLoopSound(
            SulfurBurnerBlockEntity blockEntity
    ) {
        if (!blockEntity.isBurning()) {
            return;
        }

        BurnerLoopSound existing =
                LOOP_SOUNDS.get(blockEntity);

        if (existing != null
                && !existing.isStopped()) {
            return;
        }

        BurnerLoopSound sound =
                new BurnerLoopSound(
                        blockEntity
                );

        LOOP_SOUNDS.put(
                blockEntity,
                sound
        );

        Minecraft.getInstance()
                .getSoundManager()
                .play(sound);
    }

    private static void ensureCrackingSound(
            SulfurBurnerBlockEntity blockEntity
    ) {
        if (!blockEntity.isBurning()
                || blockEntity.getWarmupProgress()
                >= 1.0F) {
            return;
        }

        BurnerCrackingSound existing =
                CRACKING_SOUNDS.get(
                        blockEntity
                );

        if (existing != null
                && !existing.isStopped()) {
            return;
        }

        BurnerCrackingSound sound =
                new BurnerCrackingSound(
                        blockEntity
                );

        CRACKING_SOUNDS.put(
                blockEntity,
                sound
        );

        Minecraft.getInstance()
                .getSoundManager()
                .play(sound);
    }

    private static boolean isBurnerStillPresent(
            SulfurBurnerBlockEntity blockEntity
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        return !blockEntity.isRemoved()
                && blockEntity.getLevel()
                != null
                && minecraft.level
                == blockEntity.getLevel();
    }

    private static float loopVolume(
            SulfurBurnerBlockEntity blockEntity
    ) {
        if (!blockEntity.isBurning()) {
            return 0.0F;
        }

        float progress =
                blockEntity.getWarmupProgress();

        if (progress < 0.2F) {
            return Mth.lerp(
                    progress / 0.2F,
                    0.025F,
                    0.050F
            );
        }

        if (progress < 0.4F) {
            return Mth.lerp(
                    (progress - 0.2F)
                            / 0.2F,
                    0.050F,
                    0.090F
            );
        }

        if (progress < 0.8F) {
            return Mth.lerp(
                    (progress - 0.4F)
                            / 0.4F,
                    0.090F,
                    0.190F
            );
        }

        return Mth.lerp(
                (progress - 0.8F)
                        / 0.2F,
                0.190F,
                0.210F
        );
    }

    private static float loopPitch(
            SulfurBurnerBlockEntity blockEntity
    ) {
        return Mth.lerp(
                blockEntity.getWarmupProgress(),
                0.88F,
                1.00F
        );
    }

    private static float crackingVolume(
            SulfurBurnerBlockEntity blockEntity
    ) {
        if (!blockEntity.isBurning()) {
            return 0.0F;
        }

        float progress =
                blockEntity.getWarmupProgress();

        if (progress >= 1.0F) {
            return 0.0F;
        }

        if (progress < 0.4F) {
            return 0.34F;
        }

        if (progress < 0.8F) {
            return Mth.lerp(
                    (progress - 0.4F)
                            / 0.4F,
                    0.34F,
                    0.17F
            );
        }

        return Mth.lerp(
                (progress - 0.8F)
                        / 0.2F,
                0.17F,
                0.02F
        );
    }

    private static final class VisualState {
        private boolean initialized;
        private boolean wasBurning;
        private boolean wasSeething;
        private int ignitionFlashTicks;
        private int afterglowTicks;
    }

    private static final class BurnerLoopSound
            extends AbstractTickableSoundInstance {

        private final SulfurBurnerBlockEntity blockEntity;

        private BurnerLoopSound(
                SulfurBurnerBlockEntity blockEntity
        ) {
            super(
                    AllModSounds.SULFUR_BURNER_LOOP.get(),
                    SoundSource.BLOCKS,
                    RandomSource.create()
            );

            this.blockEntity =
                    blockEntity;

            this.looping = true;
            this.delay = 0;
            this.relative = false;
            this.attenuation =
                    Attenuation.LINEAR;

            this.volume = 0.01F;
            this.pitch =
                    loopPitch(blockEntity);

            this.x =
                    blockEntity
                            .getBlockPos()
                            .getX()
                            + 0.5D;

            this.y =
                    blockEntity
                            .getBlockPos()
                            .getY()
                            + 0.35D;

            this.z =
                    blockEntity
                            .getBlockPos()
                            .getZ()
                            + 0.5D;
        }

        @Override
        public void tick() {
            boolean valid =
                    isBurnerStillPresent(
                            blockEntity
                    );

            float targetVolume =
                    valid
                            ? loopVolume(
                            blockEntity
                    ) * 1.75F
                            : 0.0F;

            float targetPitch =
                    valid
                            ? loopPitch(
                            blockEntity
                    )
                            : 0.88F;

            this.volume =
                    Mth.lerp(
                            targetVolume
                                    > this.volume
                                    ? 0.055F
                                    : 0.14F,
                            this.volume,
                            targetVolume
                    );

            this.pitch =
                    Mth.lerp(
                            0.05F,
                            this.pitch,
                            targetPitch
                    );

            if (targetVolume <= 0.0F
                    && this.volume <= 0.01F) {
                this.stop();

                LOOP_SOUNDS.remove(
                        blockEntity,
                        this
                );
            }
        }
    }

    private static final class BurnerCrackingSound
            extends AbstractTickableSoundInstance {

        private final SulfurBurnerBlockEntity blockEntity;

        private BurnerCrackingSound(
                SulfurBurnerBlockEntity blockEntity
        ) {
            super(
                    AllModSounds.SULFUR_BURNER_CRACKING.get(),
                    SoundSource.BLOCKS,
                    RandomSource.create()
            );

            this.blockEntity =
                    blockEntity;

            this.looping = true;
            this.delay = 0;
            this.relative = false;
            this.attenuation =
                    Attenuation.LINEAR;

            this.volume = 0.01F;
            this.pitch = 0.96F;

            this.x =
                    blockEntity
                            .getBlockPos()
                            .getX()
                            + 0.5D;

            this.y =
                    blockEntity
                            .getBlockPos()
                            .getY()
                            + 0.30D;

            this.z =
                    blockEntity
                            .getBlockPos()
                            .getZ()
                            + 0.5D;
        }

        @Override
        public void tick() {
            boolean valid =
                    isBurnerStillPresent(
                            blockEntity
                    );

            float targetVolume =
                    valid
                            ? crackingVolume(
                            blockEntity
                    )
                            : 0.0F;

            this.volume =
                    Mth.lerp(
                            targetVolume
                                    > this.volume
                                    ? 0.16F
                                    : 0.22F,
                            this.volume,
                            targetVolume
                    );

            this.pitch =
                    Mth.lerp(
                            0.06F,
                            this.pitch,
                            0.96F
                                    + blockEntity
                                    .getWarmupProgress()
                                    * 0.05F
                    );

            if (targetVolume <= 0.0F
                    && this.volume <= 0.01F) {
                this.stop();

                CRACKING_SOUNDS.remove(
                        blockEntity,
                        this
                );
            }
        }
    }
}
