package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalclutch.ThermochemicalClutchBlockEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;

public class ThermochemicalClutchRenderer
        extends KineticBlockEntityRenderer<ThermochemicalClutchBlockEntity> {

    private static final float LOCK_TRAVEL = 2.5F / 16.0F;

    public ThermochemicalClutchRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    protected void renderSafe(
            ThermochemicalClutchBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        Block block = blockEntity.getBlockState().getBlock();
        Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(
                blockEntity.getBlockState()
        );
        BlockPos pos = blockEntity.getBlockPos();
        float time = AnimationTickHolder.getRenderTime(
                blockEntity.getLevel()
        );

        for (Direction direction : Iterate.directions) {
            Direction.Axis axis = direction.getAxis();
            if (boxAxis != axis) {
                continue;
            }

            float offset = getRotationOffsetForPosition(
                    blockEntity,
                    pos,
                    axis
            );
            float angle = (
                    time
                            * blockEntity.getSpeed()
                            * 3.0F / 10.0F
            ) % 360.0F;
            angle *= blockEntity.getRotationSpeedModifier(direction);
            angle += offset;
            angle = angle / 180.0F * (float) Math.PI;

            SuperByteBuffer shaft = CachedBuffers.partialFacing(
                    ClientModEvents.THERMOCHEMICAL_CLUTCH_SHAFT_HALF,
                    blockEntity.getBlockState(),
                    direction
            );

            int shaftLight = blockEntity.getLevel() == null
                    ? light
                    : LevelRenderer.getLightColor(
                            blockEntity.getLevel(),
                            pos.relative(direction)
                    );

            kineticRotationTransform(
                    shaft,
                    blockEntity,
                    axis,
                    angle,
                    shaftLight
            );
            shaft.renderInto(
                    poseStack,
                    buffer.getBuffer(RenderType.solid())
            );
        }

        renderLock(
                blockEntity,
                partialTicks,
                boxAxis,
                poseStack,
                buffer,
                light,
                overlay
        );
    }

    private static void renderLock(
            ThermochemicalClutchBlockEntity blockEntity,
            float partialTicks,
            Direction.Axis axis,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        float progress = lockEase(blockEntity.getLockProgress(partialTicks));
        Direction lockDirection = getLockDirection(blockEntity, axis);

        int lockLight = light;
        if (blockEntity.getLevel() != null) {
            lockLight = LevelRenderer.getLightColor(
                    blockEntity.getLevel(),
                    blockEntity.getBlockPos().relative(lockDirection)
            );
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        orientLock(poseStack, lockDirection);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        CachedBuffers.partial(
                        ClientModEvents.THERMOCHEMICAL_CLUTCH_LOCK_HOUSING,
                        blockEntity.getBlockState()
                )
                .light(lockLight)
                .overlay(overlay)
                .renderInto(
                        poseStack,
                        buffer.getBuffer(RenderType.cutout())
                );

        poseStack.translate(0.0F, -LOCK_TRAVEL * progress, 0.0F);

        CachedBuffers.partial(
                        ClientModEvents.THERMOCHEMICAL_CLUTCH_LOCK_PIECE,
                        blockEntity.getBlockState()
                )
                .light(lockLight)
                .overlay(overlay)
                .renderInto(
                        poseStack,
                        buffer.getBuffer(RenderType.cutout())
                );

        poseStack.popPose();
    }

    private static Direction getLockDirection(
            ThermochemicalClutchBlockEntity blockEntity,
            Direction.Axis axis
    ) {
        BlockPos pos = blockEntity.getBlockPos();
        BlockPos source = blockEntity.source;

        if (source != null) {
            int dx = source.getX() - pos.getX();
            int dy = source.getY() - pos.getY();
            int dz = source.getZ() - pos.getZ();

            if (axis == Direction.Axis.X && dx != 0) {
                return dx > 0 ? Direction.WEST : Direction.EAST;
            }
            if (axis == Direction.Axis.Y && dy != 0) {
                return dy > 0 ? Direction.DOWN : Direction.UP;
            }
            if (axis == Direction.Axis.Z && dz != 0) {
                return dz > 0 ? Direction.NORTH : Direction.SOUTH;
            }
        }

        Direction negative = switch (axis) {
            case X -> Direction.WEST;
            case Y -> Direction.DOWN;
            case Z -> Direction.NORTH;
        };
        Direction positive = negative.getOpposite();

        float negativeModifier = Math.abs(
                blockEntity.getRotationSpeedModifier(negative)
        );
        float positiveModifier = Math.abs(
                blockEntity.getRotationSpeedModifier(positive)
        );

        if (negativeModifier < positiveModifier) {
            return negative;
        }
        if (positiveModifier < negativeModifier) {
            return positive;
        }

        return negative;
    }

    private static void orientLock(
            PoseStack poseStack,
            Direction direction
    ) {
        switch (direction) {
            case NORTH -> poseStack.mulPose(
                    Axis.YP.rotationDegrees(180.0F)
            );
            case EAST -> poseStack.mulPose(
                    Axis.YP.rotationDegrees(90.0F)
            );
            case WEST -> poseStack.mulPose(
                    Axis.YP.rotationDegrees(-90.0F)
            );
            case UP -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(-90.0F)
            );
            case DOWN -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(90.0F)
            );
            case SOUTH -> {
            }
        }
    }

    private static float lockEase(float progress) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);
        float inverse = 1.0F - clamped;
        float eased = 1.0F - inverse * inverse * inverse;

        if (clamped > 0.65F && clamped < 1.0F) {
            float settle = (clamped - 0.65F) / 0.35F;
            eased += (float) Math.sin(settle * Math.PI) * 0.035F;
        }

        return Mth.clamp(eased, 0.0F, 1.035F);
    }
}
