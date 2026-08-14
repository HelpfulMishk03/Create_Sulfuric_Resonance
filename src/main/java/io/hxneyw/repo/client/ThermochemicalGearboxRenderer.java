package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalgearbox.ThermochemicalGearboxBlockEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class ThermochemicalGearboxRenderer
        extends KineticBlockEntityRenderer<ThermochemicalGearboxBlockEntity> {

    public ThermochemicalGearboxRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    protected void renderSafe(
            ThermochemicalGearboxBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        float renderTime = AnimationTickHolder.getRenderTime(
                blockEntity.getLevel()
        );
        BlockPos pos = blockEntity.getBlockPos();

        for (Direction direction : Direction.values()) {
            Axis axis = direction.getAxis();
            SuperByteBuffer shaft = CachedBuffers.partialFacingVertical(
                    ClientModEvents.THERMOCHEMICAL_GEARBOX_SHAFT,
                    blockEntity.getBlockState(),
                    direction
            );

            float offset = getRotationOffsetForPosition(
                    blockEntity,
                    pos,
                    axis
            );
            float angle = (
                    renderTime
                            * blockEntity.getSpeed()
                            * 3.0F / 10.0F
            ) % 360.0F;

            if (blockEntity.getSpeed() != 0.0F
                    && blockEntity.hasSource()) {
                angle *= blockEntity.getRotationSpeedModifier(direction);
            }

            angle += offset;
            angle = angle / 180.0F * (float) Math.PI;

            kineticRotationTransform(
                    shaft,
                    blockEntity,
                    axis,
                    angle,
                    light
            );
            shaft.renderInto(
                    poseStack,
                    buffer.getBuffer(RenderType.solid())
            );
        }
    }
}
