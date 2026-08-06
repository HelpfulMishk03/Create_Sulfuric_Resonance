package io.hxneyw.repo.content.blocks.moltenrotor;

import io.hxneyw.repo.content.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class MoltenRotorParticles {
    public static final int KINDLING_LOW_INTERVAL_TICKS = 6;
    public static final int KINDLING_HIGH_INTERVAL_TICKS = 4;
    public static final int HEATED_FLAME_INTERVAL_TICKS = 4;
    public static final int SUPERHEATED_FLAME_INTERVAL_TICKS = 3;
    public static final int COMBUSTION_FLAME_INTERVAL_TICKS = 3;
    public static final int INTERIOR_SMOKE_INTERVAL_TICKS = 40;
    public static final int EXHAUST_SMOKE_INTERVAL_TICKS = 25;
    public static final int SLIT_SMOKE_INTERVAL_TICKS = 15;
    public static final int LAVA_SPARK_INTERVAL_TICKS = 60;
    public static final int CRACKLE_INTERVAL_TICKS = 60;

    private static final double[][] CHAMBER_ANCHORS = {
            {-0.17, 0.00},
            {0.17, 0.00},
            {-0.18, 0.10},
            {0.18, 0.10},
            {-0.10, 0.18},
            {0.10, 0.18},
            {0.00, 0.06}
    };

    private static final double[][] BLAZE_ANCHORS = {
            {-0.14, 0.015},
            {0.14, 0.015},
            {-0.16, 0.075},
            {0.16, 0.075},
            {-0.10, 0.135},
            {0.10, 0.135}
    };

    private static final double[] EXHAUST_CHANNELS = {
            -0.18,
            0.0,
            0.18
    };

    private static final double[] SLIT_OFFSETS = {
            -0.25,
            0.0,
            0.25
    };

    private MoltenRotorParticles() {
    }

    public static void tickClient(
            MoltenRotorBlockEntity furnace
    ) {
        Level level = furnace.getLevel();

        if (level == null
                || !level.isClientSide
                || furnace.getDisplayFuelTime() <= 0) {
            return;
        }

        BlockPos pos = furnace.getBlockPos();
        Direction facing = furnace.getBlockState()
                .getValue(MoltenRotorBlock.FACING);
        float temperature = furnace.getDisplayTemperature();
        long gameTime = level.getGameTime();
        long phase = Math.floorMod(pos.asLong(), 4096L);

        if (temperature < 300.0F) {
            tickKindling(
                    level,
                    pos,
                    facing,
                    temperature,
                    gameTime,
                    phase
            );
            return;
        }

        float intensity = Math.clamp(
                (temperature - 200.0F) / 1000.0F,
                0.12F,
                1.0F
        );

        MoltenRotorBlockEntity.RotorHeatLevel heatTier =
                furnace.getCurrentHeatTier();

        MoltenRotorBlockEntity.FuelType fuelType =
                furnace.getRenderedFuelType();

        boolean radiant =
                heatTier
                        == MoltenRotorBlockEntity
                        .RotorHeatLevel.RADIANT;

        boolean blazeFuel =
                fuelType
                        == MoltenRotorBlockEntity
                        .FuelType.BLAZE_CAKE
                        || fuelType
                        == MoltenRotorBlockEntity
                        .FuelType.SOUL_FIRED_BLAZE_CAKE;

        boolean soulBlazeFuel =
                fuelType
                        == MoltenRotorBlockEntity
                        .FuelType.SOUL_FIRED_BLAZE_CAKE;

        int flameInterval = switch (heatTier) {
            case NONE, SMOULDERING, FADING, KINDLED ->
                    HEATED_FLAME_INTERVAL_TICKS;
            case SEETHING ->
                    SUPERHEATED_FLAME_INTERVAL_TICKS;
            case RADIANT ->
                    COMBUSTION_FLAME_INTERVAL_TICKS;
        };

        if (isDue(
                gameTime,
                phase,
                flameInterval,
                0L
        )) {
            long emissionIndex =
                    Math.floorDiv(
                            gameTime + phase,
                            flameInterval
                    );

            if (blazeFuel) {
                spawnBlazeFlame(
                        level,
                        pos,
                        facing,
                        intensity,
                        radiant,
                        soulBlazeFuel,
                        emissionIndex
                );
            } else {
                spawnChamberFlame(
                        level,
                        pos,
                        facing,
                        intensity,
                        radiant,
                        emissionIndex
                );
            }
        }

        if (isDue(
                gameTime,
                phase,
                INTERIOR_SMOKE_INTERVAL_TICKS,
                7L
        )) {
            spawnInteriorSmoke(
                    level,
                    pos,
                    facing,
                    gameTime,
                    phase
            );
        }

        if (isDue(
                gameTime,
                phase,
                EXHAUST_SMOKE_INTERVAL_TICKS,
                13L
        )) {
            spawnInteriorExhaust(
                    level,
                    pos,
                    facing,
                    gameTime,
                    phase
            );
        }

        if (isDue(
                gameTime,
                phase,
                SLIT_SMOKE_INTERVAL_TICKS,
                19L
        )) {
            spawnSlitSmoke(
                    level,
                    pos,
                    facing,
                    gameTime,
                    phase
            );
        }

        if (intensity > 0.70F
                && isDue(
                gameTime,
                phase,
                LAVA_SPARK_INTERVAL_TICKS,
                29L
        )) {
            spawnLavaSpark(
                    level,
                    pos,
                    facing,
                    gameTime,
                    phase
            );
        }

        if (isDue(
                gameTime,
                phase,
                CRACKLE_INTERVAL_TICKS,
                31L
        )) {
            RandomSource random =
                    randomFor(
                            pos,
                            gameTime,
                            101L
                    );

            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.35,
                    pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    0.35F + intensity * 0.18F,
                    0.82F + random.nextFloat() * 0.22F,
                    false
            );
        }
    }

    private static void tickKindling(
            Level level,
            BlockPos pos,
            Direction facing,
            float temperature,
            long gameTime,
            long phase
    ) {
        float progress = Math.clamp(
                (temperature - 20.0F) / 280.0F,
                0.0F,
                1.0F
        );

        int interval =
                progress < 0.55F
                        ? KINDLING_LOW_INTERVAL_TICKS
                        : KINDLING_HIGH_INTERVAL_TICKS;

        if (!isDue(
                gameTime,
                phase,
                interval,
                0L
        )) {
            return;
        }

        long emissionIndex =
                Math.floorDiv(
                        gameTime + phase,
                        interval
                );

        RandomSource random =
                randomFor(
                        pos,
                        emissionIndex,
                        11L
                );

        double side =
                (random.nextDouble() - 0.5)
                        * (0.08 + progress * 0.06);

        double depth =
                0.06
                        + (random.nextDouble() - 0.5)
                        * (0.05 + progress * 0.03);

        double localY =
                0.15
                        + random.nextDouble()
                        * (0.018 + progress * 0.035);

        double[] position =
                toWorldChamberPoint(
                        pos,
                        facing,
                        side,
                        localY,
                        depth
                );

        level.addParticle(
                ParticleTypes.SMALL_FLAME,
                position[0],
                position[1],
                position[2],
                0.0,
                0.002 + progress * 0.003,
                0.0
        );
    }

    private static void spawnChamberFlame(
            Level level,
            BlockPos pos,
            Direction facing,
            float intensity,
            boolean radiant,
            long emissionIndex
    ) {
        int anchorIndex =
                Math.floorMod(
                        emissionIndex,
                        CHAMBER_ANCHORS.length
                );

        double[] anchor =
                CHAMBER_ANCHORS[anchorIndex];

        RandomSource random =
                randomFor(
                        pos,
                        emissionIndex,
                        23L
                );

        double side =
                anchor[0]
                        + (random.nextDouble() - 0.5) * 0.025;

        double depth =
                anchor[1]
                        + (random.nextDouble() - 0.5) * 0.025;

        double localY =
                0.17
                        + random.nextDouble()
                        * (0.025 + intensity * 0.025);

        double[] position =
                toWorldChamberPoint(
                        pos,
                        facing,
                        side,
                        localY,
                        depth
                );

        ParticleOptions particle =
                selectNormalFlame(
                        radiant,
                        emissionIndex
                );

        level.addParticle(
                particle,
                position[0],
                position[1],
                position[2],
                0.0,
                0.002 + intensity * 0.003,
                0.0
        );
    }

    private static void spawnBlazeFlame(
            Level level,
            BlockPos pos,
            Direction facing,
            float intensity,
            boolean radiant,
            boolean soulBlazeFuel,
            long emissionIndex
    ) {
        int anchorIndex =
                Math.floorMod(
                        emissionIndex,
                        BLAZE_ANCHORS.length
                );

        double[] anchor =
                BLAZE_ANCHORS[anchorIndex];

        RandomSource random =
                randomFor(
                        pos,
                        emissionIndex,
                        37L
                );

        double side =
                anchor[0]
                        + (random.nextDouble() - 0.5) * 0.018;

        double depth =
                anchor[1]
                        + (random.nextDouble() - 0.5) * 0.018;

        double localY =
                0.16
                        + random.nextDouble()
                        * (0.028 + intensity * 0.022);

        double[] position =
                toWorldChamberPoint(
                        pos,
                        facing,
                        side,
                        localY,
                        depth
                );

        ParticleOptions particle =
                selectBlazeFlame(
                        radiant,
                        soulBlazeFuel,
                        emissionIndex
                );

        level.addParticle(
                particle,
                position[0],
                position[1],
                position[2],
                0.0,
                0.0025 + intensity * 0.0025,
                0.0
        );
    }

    private static ParticleOptions selectNormalFlame(
            boolean radiant,
            long emissionIndex
    ) {
        if (!radiant) {
            return ParticleTypes.SMALL_FLAME;
        }

        int cycle =
                Math.floorMod(
                        emissionIndex,
                        6
                );

        if (cycle == 1 || cycle == 4) {
            return ModParticles
                    .COMBUSTION_PURPLE_FLAME
                    .get();
        }

        if (cycle == 3) {
            return ParticleTypes.SOUL_FIRE_FLAME;
        }

        return ParticleTypes.SMALL_FLAME;
    }

    private static ParticleOptions selectBlazeFlame(
            boolean radiant,
            boolean soulBlazeFuel,
            long emissionIndex
    ) {
        int cycle =
                Math.floorMod(
                        emissionIndex,
                        6
                );

        if (soulBlazeFuel) {
            if (cycle == 1 || cycle == 4) {
                return ModParticles
                        .COMBUSTION_PURPLE_FLAME
                        .get();
            }

            return ParticleTypes.SOUL_FIRE_FLAME;
        }

        if (radiant && (cycle == 2 || cycle == 5)) {
            return ModParticles
                    .COMBUSTION_PURPLE_FLAME
                    .get();
        }

        return ParticleTypes.SMALL_FLAME;
    }

    private static void spawnInteriorSmoke(
            Level level,
            BlockPos pos,
            Direction facing,
            long gameTime,
            long phase
    ) {
        RandomSource random =
                randomFor(
                        pos,
                        gameTime + phase,
                        47L
                );

        double[] position =
                toWorldChamberPoint(
                        pos,
                        facing,
                        (random.nextDouble() - 0.5) * 0.10,
                        0.24 + random.nextDouble() * 0.04,
                        0.08 + random.nextDouble() * 0.04
                );

        level.addParticle(
                ParticleTypes.SMOKE,
                position[0],
                position[1],
                position[2],
                0.0,
                0.004,
                0.0
        );
    }

    private static void spawnInteriorExhaust(
            Level level,
            BlockPos pos,
            Direction facing,
            long gameTime,
            long phase
    ) {
        RandomSource random =
                randomFor(
                        pos,
                        gameTime + phase,
                        59L
                );

        int channelIndex =
                Math.floorMod(
                        Math.floorDiv(
                                gameTime + phase,
                                EXHAUST_SMOKE_INTERVAL_TICKS
                        ),
                        EXHAUST_CHANNELS.length
                );

        double side =
                EXHAUST_CHANNELS[channelIndex]
                        + (random.nextDouble() - 0.5) * 0.02;

        double[] position =
                toWorldChamberPoint(
                        pos,
                        facing,
                        side,
                        0.44 + random.nextDouble() * 0.045,
                        0.16 + random.nextDouble() * 0.05
                );

        Direction rear = facing.getOpposite();

        level.addParticle(
                ParticleTypes.SMOKE,
                position[0],
                position[1],
                position[2],
                rear.getStepX() * 0.010,
                0.010,
                rear.getStepZ() * 0.010
        );
    }

    private static void spawnSlitSmoke(
            Level level,
            BlockPos pos,
            Direction facing,
            long gameTime,
            long phase
    ) {
        int slitIndex =
                Math.floorMod(
                        Math.floorDiv(
                                gameTime + phase,
                                SLIT_SMOKE_INTERVAL_TICKS
                        ),
                        SLIT_OFFSETS.length
                );

        double slitOffset =
                SLIT_OFFSETS[slitIndex];

        RandomSource random =
                randomFor(
                        pos,
                        gameTime + phase,
                        71L
                );

        double x;
        double z;
        double velocityX;
        double velocityZ;

        switch (facing) {
            case NORTH -> {
                x = pos.getX() + 0.5 + slitOffset;
                z = pos.getZ() + 0.8;
                velocityX =
                        (random.nextDouble() - 0.5) * 0.012;
                velocityZ = 0.016;
            }
            case SOUTH -> {
                x = pos.getX() + 0.5 - slitOffset;
                z = pos.getZ() + 0.2;
                velocityX =
                        (random.nextDouble() - 0.5) * 0.012;
                velocityZ = -0.016;
            }
            case EAST -> {
                x = pos.getX() + 0.2;
                z = pos.getZ() + 0.5 + slitOffset;
                velocityX = -0.016;
                velocityZ =
                        (random.nextDouble() - 0.5) * 0.012;
            }
            case WEST -> {
                x = pos.getX() + 0.8;
                z = pos.getZ() + 0.5 - slitOffset;
                velocityX = 0.016;
                velocityZ =
                        (random.nextDouble() - 0.5) * 0.012;
            }
            default -> {
                return;
            }
        }

        level.addParticle(
                ParticleTypes.SMOKE,
                x,
                pos.getY() + 0.85,
                z,
                velocityX,
                0.024,
                velocityZ
        );
    }

    private static void spawnLavaSpark(
            Level level,
            BlockPos pos,
            Direction facing,
            long gameTime,
            long phase
    ) {
        RandomSource random =
                randomFor(
                        pos,
                        gameTime + phase,
                        83L
                );

        double[] anchor =
                CHAMBER_ANCHORS[
                        random.nextInt(
                                CHAMBER_ANCHORS.length
                        )
                        ];

        double[] position =
                toWorldChamberPoint(
                        pos,
                        facing,
                        anchor[0],
                        0.22,
                        anchor[1]
                );

        level.addParticle(
                ParticleTypes.SMALL_FLAME,
                position[0],
                position[1],
                position[2],
                0.0,
                0.008,
                0.0
        );
    }

    private static boolean isDue(
            long gameTime,
            long phase,
            int interval,
            long offset
    ) {
        return Math.floorMod(
                gameTime + phase + offset,
                interval
        ) == 0L;
    }

    private static RandomSource randomFor(
            BlockPos pos,
            long time,
            long salt
    ) {
        long seed =
                pos.asLong()
                        ^ time * 341873128712L
                        ^ salt * 132897987541L;

        return RandomSource.create(seed);
    }

    private static double[] toWorldChamberPoint(
            BlockPos pos,
            Direction facing,
            double side,
            double localY,
            double rearDepth
    ) {
        Direction rear = facing.getOpposite();
        Direction sideways = facing.getClockWise();

        return new double[]{
                pos.getX() + 0.5
                        + sideways.getStepX() * side
                        + rear.getStepX() * rearDepth,
                pos.getY() + localY,
                pos.getZ() + 0.5
                        + sideways.getStepZ() * side
                        + rear.getStepZ() * rearDepth
        };
    }
}
