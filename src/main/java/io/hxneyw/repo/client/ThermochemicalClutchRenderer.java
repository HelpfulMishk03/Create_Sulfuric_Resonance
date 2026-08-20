package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;

public class ThermochemicalClutchRenderer
        extends KineticBlockEntityRenderer<ThermochemicalClutchBlockEntity> {

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
        Axis boxAxis = ((IRotate) block).getRotationAxis(
                blockEntity.getBlockState()
        );
        BlockPos pos = blockEntity.getBlockPos();
        float time = AnimationTickHolder.getRenderTime(
                blockEntity.getLevel()
        );

        for (Direction direction : Iterate.directions) {
            Axis axis = direction.getAxis();
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
    }
}
