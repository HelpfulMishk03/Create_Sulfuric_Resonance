package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
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