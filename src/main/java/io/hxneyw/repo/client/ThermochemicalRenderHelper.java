package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;

final class ThermochemicalRenderHelper {

    private ThermochemicalRenderHelper() {
    }

    static void renderVerticalPartialOnAxis(
            KineticBlockEntity blockEntity,
            PartialModel partialModel,
            Direction.Axis targetAxis,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BakedModel model = partialModel.get();
        Minecraft minecraft = Minecraft.getInstance();

        if (model == null
                || model
                == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        float angleRadians =
                KineticBlockEntityRenderer.getAngleForBe(
                        blockEntity,
                        blockEntity.getBlockPos(),
                        targetAxis
                );

        poseStack.pushPose();
        poseStack.translate(
                0.5D,
                0.5D,
                0.5D
        );

        switch (targetAxis) {
            case X -> {
                poseStack.mulPose(
                        Axis.XP.rotation(
                                angleRadians
                        )
                );
                poseStack.mulPose(
                        Axis.ZP.rotationDegrees(
                                -90.0F
                        )
                );
            }
            case Y -> poseStack.mulPose(
                    Axis.YP.rotation(
                            angleRadians
                    )
            );
            case Z -> {
                poseStack.mulPose(
                        Axis.ZP.rotation(
                                angleRadians
                        )
                );
                poseStack.mulPose(
                        Axis.XP.rotationDegrees(
                                90.0F
                        )
                );
            }
        }

        poseStack.translate(
                -0.5D,
                -0.5D,
                -0.5D
        );

        renderModel(
                poseStack,
                buffer,
                model,
                light,
                overlay
        );

        poseStack.popPose();
    }

    static void renderVerticalHalfTowardFace(
            KineticBlockEntity blockEntity,
            Direction face,
            float faceSpeedMultiplier,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BakedModel model = ClientModEvents.THERMOCHEMICAL_GEARBOX_SHAFT.get();
        Minecraft minecraft = Minecraft.getInstance();

        if (model == null
                || model
                == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        float renderTime =
                AnimationTickHolder.getRenderTime(
                        blockEntity.getLevel()
                );

        float offset =
                KineticBlockEntityRenderer
                        .getRotationOffsetForPosition(
                                blockEntity,
                                blockEntity.getBlockPos(),
                                face.getAxis()
                        );

        float positiveAxisAngle = (
                renderTime
                        * blockEntity.getSpeed()
                        * faceSpeedMultiplier
                        * 3.0F / 10.0F
                        + offset
        ) % 360.0F;

        float localAngle =
                face.getAxisDirection()
                        == Direction.AxisDirection.NEGATIVE
                        ? -positiveAxisAngle
                        : positiveAxisAngle;

        poseStack.pushPose();
        poseStack.translate(
                0.5D,
                0.5D,
                0.5D
        );

        switch (face) {
            case UP -> {
            }
            case DOWN -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(
                            180.0F
                    )
            );
            case NORTH -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(
                            -90.0F
                    )
            );
            case SOUTH -> poseStack.mulPose(
                    Axis.XP.rotationDegrees(
                            90.0F
                    )
            );
            case EAST -> poseStack.mulPose(
                    Axis.ZP.rotationDegrees(
                            -90.0F
                    )
            );
            case WEST -> poseStack.mulPose(
                    Axis.ZP.rotationDegrees(
                            90.0F
                    )
            );
        }

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        localAngle
                )
        );

        poseStack.translate(
                -0.5D,
                -0.5D,
                -0.5D
        );

        renderModel(
                poseStack,
                buffer,
                model,
                light,
                overlay
        );

        poseStack.popPose();
    }

    private static void renderModel(
            PoseStack poseStack,
            MultiBufferSource buffer,
            BakedModel model,
            int light,
            int overlay
    ) {
        VertexConsumer consumer =
                buffer.getBuffer(
                        RenderType.solid()
                );

        RandomSource random =
                RandomSource.create(42L);

        for (BakedQuad quad : model.getQuads(
                null,
                null,
                random,
                ModelData.EMPTY,
                RenderType.solid()
        )) {
            consumer.putBulkData(
                    poseStack.last(),
                    quad,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F,
                    light,
                    overlay
            );
        }
    }
}