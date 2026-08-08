package io.hxneyw.repo.content.blocks.thermochemicalcogwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.hxneyw.repo.client.ClientModEvents;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;

public class ThermochemicalCogwheelRenderer
        extends KineticBlockEntityRenderer<ThermochemicalCogwheelBlockEntity> {

    public ThermochemicalCogwheelRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    protected void renderSafe(
            ThermochemicalCogwheelBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (VisualizationManager.supportsVisualization(
                blockEntity.getLevel()
        )) {
            return;
        }

        if (!ICogWheel.isLargeCog(
                blockEntity.getBlockState()
        )) {
            super.renderSafe(
                    blockEntity,
                    partialTicks,
                    poseStack,
                    buffer,
                    light,
                    overlay
            );
            return;
        }

        VertexConsumer vertexConsumer =
                buffer.getBuffer(RenderType.solid());

        Axis axis = getRotationAxisOf(blockEntity);

        Direction facing =
                Direction.fromAxisAndDirection(
                        axis,
                        AxisDirection.POSITIVE
                );

        renderRotatingBuffer(
                blockEntity,
                CachedBuffers.partialFacingVertical(
                        ClientModEvents.LARGE_THERMOCHEMICAL_COGWHEEL_SHAFTLESS,
                        blockEntity.getBlockState(),
                        facing
                ),
                poseStack,
                vertexConsumer,
                light
        );

        float shaftAngle =
                BracketedKineticBlockEntityRenderer
                        .getAngleForLargeCogShaft(
                                blockEntity,
                                axis
                        );

        SuperByteBuffer shaft =
                CachedBuffers.partialFacingVertical(
                        ClientModEvents.THERMOCHEMICAL_COGWHEEL_SHAFT,
                        blockEntity.getBlockState(),
                        facing
                );

        kineticRotationTransform(
                shaft,
                blockEntity,
                axis,
                shaftAngle,
                light
        );

        shaft.renderInto(
                poseStack,
                vertexConsumer
        );
    }
}