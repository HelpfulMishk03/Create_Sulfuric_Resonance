package io.hxneyw.repo.content.entities;

import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("resource")
public class CinderFlareEntity extends ThrowableItemProjectile {
    public static final int BURN_DURATION_TICKS = 1200;
    private static final EntityDataAccessor<Boolean> STUCK = SynchedEntityData.defineId(
            CinderFlareEntity.class,
            EntityDataSerializers.BOOLEAN
    );
    private static final EntityDataAccessor<Integer> ATTACHED_FACE = SynchedEntityData.defineId(
            CinderFlareEntity.class,
            EntityDataSerializers.INT
    );
    private static final EntityDataAccessor<Float> SURFACE_ROLL = SynchedEntityData.defineId(
            CinderFlareEntity.class,
            EntityDataSerializers.FLOAT
    );

    private int burnTicks = BURN_DURATION_TICKS;
    @Nullable
    private BlockPos lightPos;
    private boolean ownsLight;
    @Nullable
    private BlockPos attachedBlockPos;

    public CinderFlareEntity(EntityType<? extends CinderFlareEntity> type, Level level) {
        super(type, level);
    }

    public CinderFlareEntity(Level level, LivingEntity shooter) {
        super(ModEntities.CINDER_FLARE.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STUCK, false);
        builder.define(ATTACHED_FACE, Direction.UP.get3DDataValue());
        builder.define(SURFACE_ROLL, 0.0F);
    }

    @Override
    @NotNull
    protected Item getDefaultItem() {
        return Items.LIT_CINDER_FLARE.get();
    }

    @Override
    public void tick() {
        if (!level().isClientSide && isStuck() && !hasAttachedSupport()) {
            detachFromSurface();
        }

        if (isStuck()) {
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
        } else {
            setNoGravity(false);
        }

        super.tick();

        if (isStuck()) {
            setDeltaMovement(Vec3.ZERO);
        }

        if (level().isClientSide) {
            spawnClientParticles();
            return;
        }

        burnTicks--;

        if (isStuck()) {
            ensureTemporaryLight();
        }

        if (burnTicks > 0 && burnTicks % 48 == 0) {
            level().playSound(
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    0.32F,
                    1.3F + random.nextFloat() * 0.25F
            );
        }

        if (burnTicks <= 0) {
            burnOut();
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);

        if (isStuck()) {
            return;
        }

        Direction direction = result.getDirection();
        Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal()).scale(0.04D);
        float surfaceRoll = Mth.wrapDegrees(getYRot());
        setPos(result.getLocation().add(normal));
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(true);
        entityData.set(ATTACHED_FACE, direction.get3DDataValue());
        entityData.set(SURFACE_ROLL, surfaceRoll);
        entityData.set(STUCK, true);
        attachedBlockPos = result.getBlockPos().immutable();

        if (!level().isClientSide) {
            igniteDirectContact(result);
            ensureTemporaryLight();
            level().playSound(
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    SoundEvents.COPPER_HIT,
                    SoundSource.BLOCKS,
                    0.4F,
                    1.35F + random.nextFloat() * 0.15F
            );
            if (level() instanceof ServerLevel serverLevel) {
                Vec3 flameTip = getFlameTipPosition();
                serverLevel.sendParticles(
                        ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                        flameTip.x,
                        flameTip.y,
                        flameTip.z,
                        2,
                        0.035D,
                        0.03D,
                        0.035D,
                        0.008D
                );
            }
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);

        if (level().isClientSide) {
            return;
        }

        Entity target = result.getEntity();
        if (target == getOwner()) {
            return;
        }

        target.hurt(damageSources().thrown(this, getOwner()), 1.5F);
        target.igniteForSeconds(4.0F);
        setDeltaMovement(getDeltaMovement().scale(0.35D).add(0.0D, -0.08D, 0.0D));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BurnTicks", burnTicks);
        tag.putBoolean("Stuck", isStuck());
        tag.putBoolean("OwnsLight", ownsLight);
        tag.putInt("AttachedFace", entityData.get(ATTACHED_FACE));
        tag.putFloat("SurfaceRoll", entityData.get(SURFACE_ROLL));
        if (lightPos != null) {
            tag.putLong("LightPos", lightPos.asLong());
        }
        if (attachedBlockPos != null) {
            tag.putLong("AttachedBlockPos", attachedBlockPos.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        burnTicks = tag.contains("BurnTicks")
                ? Math.max(1, tag.getInt("BurnTicks"))
                : BURN_DURATION_TICKS;
        entityData.set(STUCK, tag.getBoolean("Stuck"));
        ownsLight = tag.getBoolean("OwnsLight");
        entityData.set(
                ATTACHED_FACE,
                tag.contains("AttachedFace")
                        ? tag.getInt("AttachedFace")
                        : Direction.UP.get3DDataValue()
        );
        entityData.set(
                SURFACE_ROLL,
                tag.contains("SurfaceRoll")
                        ? tag.getFloat("SurfaceRoll")
                        : 0.0F
        );
        lightPos = tag.contains("LightPos")
                ? BlockPos.of(tag.getLong("LightPos"))
                : null;
        attachedBlockPos = tag.contains("AttachedBlockPos")
                ? BlockPos.of(tag.getLong("AttachedBlockPos"))
                : null;
        setNoGravity(isStuck());
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (!level().isClientSide && reason != RemovalReason.UNLOADED_TO_CHUNK) {
            removeTemporaryLight();
        }
        super.remove(reason);
    }

    private boolean isStuck() {
        return entityData.get(STUCK);
    }

    public boolean isStuckToSurface() {
        return isStuck();
    }

    @NotNull
    public Direction getAttachedFace() {
        return Direction.from3DDataValue(entityData.get(ATTACHED_FACE));
    }

    public float getSurfaceRoll() {
        return entityData.get(SURFACE_ROLL);
    }

    @NotNull
    public Vec3 getFlameTipPosition() {
        if (!isStuck()) {
            Vec3 motion = getDeltaMovement();
            if (motion.lengthSqr() > 1.0E-6D) {
                return position().add(motion.normalize().scale(0.28D));
            }
            return position().add(0.0D, 0.22D, 0.0D);
        }

        Direction face = getAttachedFace();
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        Vec3 localUp = switch (face) {
            case UP -> new Vec3(0.0D, 0.0D, -1.0D);
            case DOWN -> new Vec3(0.0D, 0.0D, 1.0D);
            default -> new Vec3(0.0D, 1.0D, 0.0D);
        };
        Vec3 localRight = localUp.cross(normal).normalize();
        double radians = Math.toRadians(getSurfaceRoll());
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        Vec3 rotatedRight = localRight.scale(cosine).add(localUp.scale(sine));
        Vec3 rotatedUp = localUp.scale(cosine).subtract(localRight.scale(sine));

        return position()
                .add(rotatedRight.scale(-0.22D))
                .add(rotatedUp.scale(0.22D))
                .add(normal.scale(0.025D));
    }

    private boolean hasAttachedSupport() {
        if (attachedBlockPos == null || !level().isLoaded(attachedBlockPos)) {
            return true;
        }

        BlockState state = level().getBlockState(attachedBlockPos);
        return !state.isAir() && !state.getCollisionShape(level(), attachedBlockPos).isEmpty();
    }

    private void detachFromSurface() {
        entityData.set(STUCK, false);
        attachedBlockPos = null;
        setNoGravity(false);
        setDeltaMovement(0.0D, -0.08D, 0.0D);
        removeTemporaryLight();
    }

    private void igniteDirectContact(BlockHitResult result) {
        BlockPos hitPos = result.getBlockPos();
        Direction face = result.getDirection();
        BlockState hitState = level().getBlockState(hitPos);

        if ((hitState.getBlock() instanceof CampfireBlock
                || hitState.getBlock() instanceof CandleBlock
                || hitState.getBlock() instanceof CandleCakeBlock)
                && hitState.hasProperty(BlockStateProperties.LIT)
                && !hitState.getValue(BlockStateProperties.LIT)) {
            level().setBlock(
                    hitPos,
                    hitState.setValue(BlockStateProperties.LIT, true),
                    Block.UPDATE_ALL
            );
            return;
        }

        if (!hitState.isFlammable(level(), hitPos, face)) {
            return;
        }

        hitState.onCaughtFire(
                level(),
                hitPos,
                face,
                getOwner() instanceof LivingEntity livingOwner ? livingOwner : null
        );

        BlockPos firePos = hitPos.relative(face);
        if (!level().getBlockState(firePos).isAir()) {
            return;
        }

        if (!BaseFireBlock.canBePlacedAt(level(), firePos, face)) {
            return;
        }

        level().setBlock(
                firePos,
                BaseFireBlock.getState(level(), firePos),
                Block.UPDATE_ALL
        );
    }

    private void ensureTemporaryLight() {
        if (!(level() instanceof ServerLevel)) {
            return;
        }

        if (lightPos != null) {
            BlockState current = level().getBlockState(lightPos);
            if (ownsLight && current.is(Blocks.LIGHT)) {
                if (current.getValue(LightBlock.LEVEL) != 15) {
                    level().setBlock(
                            lightPos,
                            current.setValue(LightBlock.LEVEL, 15),
                            Block.UPDATE_CLIENTS
                    );
                }
                return;
            }

            ownsLight = false;
            lightPos = null;
        }

        BlockPos origin = blockPosition();
        BlockPos[] candidates = new BlockPos[] {
                origin,
                origin.above(),
                origin.north(),
                origin.south(),
                origin.east(),
                origin.west(),
                origin.below()
        };

        for (BlockPos candidate : candidates) {
            BlockState state = level().getBlockState(candidate);
            if (!state.isAir()) {
                continue;
            }

            level().setBlock(
                    candidate,
                    Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15),
                    Block.UPDATE_CLIENTS
            );
            lightPos = candidate.immutable();
            ownsLight = true;
            return;
        }
    }

    private void removeTemporaryLight() {
        if (!ownsLight || lightPos == null || !level().isLoaded(lightPos)) {
            return;
        }

        if (level().getBlockState(lightPos).is(Blocks.LIGHT)) {
            level().removeBlock(lightPos, false);
        }

        ownsLight = false;
        lightPos = null;
    }

    private void burnOut() {
        if (level() instanceof ServerLevel serverLevel) {
            Vec3 flameTip = getFlameTipPosition();
            serverLevel.sendParticles(
                    ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                    flameTip.x,
                    flameTip.y,
                    flameTip.z,
                    4,
                    0.08D,
                    0.08D,
                    0.08D,
                    0.008D
            );
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    flameTip.x,
                    flameTip.y,
                    flameTip.z,
                    3,
                    0.07D,
                    0.05D,
                    0.07D,
                    0.012D
            );
            level().playSound(
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.35F,
                    1.25F
            );
        }

        removeTemporaryLight();
        discard();
    }

    private void spawnClientParticles() {
        Vec3 flameTip = getFlameTipPosition();

        if (tickCount % 7 == 0 && random.nextFloat() < 0.85F) {
            level().addParticle(
                    ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                    flameTip.x,
                    flameTip.y,
                    flameTip.z,
                    (random.nextDouble() - 0.5D) * 0.009D,
                    0.009D + random.nextDouble() * 0.007D,
                    (random.nextDouble() - 0.5D) * 0.009D
            );
        }

        if (tickCount % 28 == 0 && random.nextFloat() < 0.35F) {
            level().addParticle(
                    ParticleTypes.SMOKE,
                    flameTip.x,
                    flameTip.y,
                    flameTip.z,
                    0.0D,
                    0.006D,
                    0.0D
            );
        }
    }
}
