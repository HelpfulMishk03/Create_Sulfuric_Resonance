package io.hxneyw.repo.content.entities;

import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.recipes.precisionspraying.PrecisionSprayingRegistry;
import io.hxneyw.repo.content.registry.AllModEffects;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class SulfuricAcidFlaskEntity extends ThrowableItemProjectile {
    private static final double SPLASH_RADIUS = 2.75D;
    private static final int ACID_BURN_DURATION_TICKS = 100;
    private static final int ACID_BURN_AMPLIFIER = 0;
    private static final int COPPER_HORIZONTAL_RADIUS = 2;
    private static final int COPPER_VERTICAL_RADIUS = 1;
    private static final int MAX_COPPER_REACTIONS = 3;

    public SulfuricAcidFlaskEntity(
            EntityType<? extends SulfuricAcidFlaskEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public SulfuricAcidFlaskEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SULFURIC_ACID_FLASK.get(), shooter, level);
    }

    @Override
    @NotNull
    protected Item getDefaultItem() {
        return Items.SULFURIC_ACID_FLASK.get();
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        applyAcidBurn(serverLevel);
        applyCopperReactions(serverLevel);
        emitImpact(serverLevel);
        this.discard();
    }

    private void applyAcidBurn(ServerLevel serverLevel) {
        serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(SPLASH_RADIUS),
                LivingEntity::isAlive
        ).forEach(entity -> entity.addEffect(new MobEffectInstance(
                AllModEffects.ACID_BURN,
                ACID_BURN_DURATION_TICKS,
                ACID_BURN_AMPLIFIER,
                false,
                true,
                true
        )));
    }

    private void applyCopperReactions(ServerLevel serverLevel) {
        BlockPos impactPos = this.blockPosition();
        List<BlockPos> reactivePositions = new ArrayList<>();

        for (BlockPos candidate : BlockPos.withinManhattan(
                impactPos,
                COPPER_HORIZONTAL_RADIUS,
                COPPER_VERTICAL_RADIUS,
                COPPER_HORIZONTAL_RADIUS
        )) {
            if (PrecisionSprayingRegistry.getResult(
                    serverLevel.getBlockState(candidate)
            ).isPresent()) {
                reactivePositions.add(candidate.immutable());
            }
        }

        reactivePositions.sort(Comparator.comparingDouble(impactPos::distSqr));
        int reactions = Math.min(MAX_COPPER_REACTIONS, reactivePositions.size());
        for (int index = 0; index < reactions; index++) {
            BlockPos position = reactivePositions.get(index);
            BlockState currentState = serverLevel.getBlockState(position);
            PrecisionSprayingRegistry.getResult(currentState).ifPresent(result -> {
                serverLevel.setBlock(position, result, Block.UPDATE_ALL);
                serverLevel.sendParticles(
                        ModParticles.ACID_DRIP.get(),
                        position.getX() + 0.5D,
                        position.getY() + 0.7D,
                        position.getZ() + 0.5D,
                        6,
                        0.3D,
                        0.2D,
                        0.3D,
                        0.04D
                );
            });
        }
    }

    private void emitImpact(ServerLevel serverLevel) {
        serverLevel.sendParticles(
                ModParticles.ACID_DRIP.get(),
                this.getX(),
                this.getY(),
                this.getZ(),
                28,
                0.9D,
                0.45D,
                0.9D,
                0.12D
        );
        serverLevel.playSound(
                null,
                this.blockPosition(),
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                0.9F,
                1.15F + this.random.nextFloat() * 0.2F
        );
        serverLevel.playSound(
                null,
                this.blockPosition(),
                SoundEvents.LAVA_EXTINGUISH,
                SoundSource.PLAYERS,
                0.45F,
                1.75F + this.random.nextFloat() * 0.25F
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && this.random.nextFloat() < 0.45F) {
            this.level().addParticle(
                    ModParticles.ACID_DRIP.get(),
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0.0D,
                    -0.015D,
                    0.0D
            );
        }
    }
}
