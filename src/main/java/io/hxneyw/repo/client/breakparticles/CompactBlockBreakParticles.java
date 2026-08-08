package io.hxneyw.repo.client.breakparticles;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class CompactBlockBreakParticles {

    private static final int DESTROY_PARTICLE_COUNT = 12;

    private CompactBlockBreakParticles() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(
            RegisterClientExtensionsEvent event
    ) {
        event.registerBlock(
                new IClientBlockExtensions() {
                    @Override
                    public boolean addDestroyEffects(
                            @NotNull BlockState state,
                            @NotNull Level level,
                            @NotNull BlockPos pos,
                            @NotNull ParticleEngine manager
                    ) {
                        spawnControlledDestroyParticles(
                                state,
                                level,
                                pos
                        );

                        return true;
                    }
                },
                AllModBlocks.THERMAL_RELAY_SWITCH.get(),
                AllModBlocks.PERFORATED_SPRITZER.get(),
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get(),
                AllModBlocks.THERMOCHEMICAL_GEARBOX.get(),
                AllModBlocks.THERMOCHEMICAL_SHAFT.get(),
                AllModBlocks.THERMOCHEMICAL_CONDUIT.get(),
                AllModBlocks.THERMOCHEMICAL_COGWHEEL.get(),
                AllModBlocks.LARGE_THERMOCHEMICAL_COGWHEEL.get(),
                AllModBlocks.SULFUR_BURNER.get(),
                AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()
        );
    }

    private static void spawnControlledDestroyParticles(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        VoxelShape shape = state.getShape(level, pos);

        if (shape.isEmpty()) {
            return;
        }

        AABB bounds = shape.bounds();
        RandomSource random = level.getRandom();

        for (int i = 0; i < DESTROY_PARTICLE_COUNT; i++) {
            double x = pos.getX()
                    + bounds.minX
                    + random.nextDouble()
                    * (bounds.maxX - bounds.minX);

            double y = pos.getY()
                    + bounds.minY
                    + random.nextDouble()
                    * (bounds.maxY - bounds.minY);

            double z = pos.getZ()
                    + bounds.minZ
                    + random.nextDouble()
                    * (bounds.maxZ - bounds.minZ);

            double velocityX =
                    (random.nextDouble() - 0.5D) * 0.12D;

            double velocityY =
                    0.04D + random.nextDouble() * 0.08D;

            double velocityZ =
                    (random.nextDouble() - 0.5D) * 0.12D;

            level.addParticle(
                    new BlockParticleOption(
                            ParticleTypes.BLOCK,
                            state
                    ),
                    x,
                    y,
                    z,
                    velocityX,
                    velocityY,
                    velocityZ
            );
        }
    }
}