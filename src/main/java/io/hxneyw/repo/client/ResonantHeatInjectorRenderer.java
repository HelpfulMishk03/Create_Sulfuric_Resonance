package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.resonantheatinjector.ResonantHeatInjectorBlock;
import io.hxneyw.repo.content.blocks.resonantheatinjector.ResonantHeatInjectorBlockEntity;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class ResonantHeatInjectorRenderer
        extends SafeBlockEntityRenderer<ResonantHeatInjectorBlockEntity> {

    public ResonantHeatInjectorRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        Objects.requireNonNull(context, "context");
    }

    @Override
    protected void renderSafe(
            ResonantHeatInjectorBlockEntity injector,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BakedModel model = ClientModEvents.RESONANT_HEAT_INJECTOR_SHAFT.get();
        Minecraft minecraft = Minecraft.getInstance();

        if (model == null || model == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        BlockState state = injector.getBlockState();
        Direction facing = state.getValue(ResonantHeatInjectorBlock.FACING);
        Direction inputSide = ResonantHeatInjectorBlock.inputSide(state);
        float angle = KineticBlockEntityRenderer.getAngleForBe(
                injector,
                injector.getBlockPos(),
                inputSide.getAxis()
        );

        if (facing == Direction.SOUTH || facing == Direction.WEST) {
            angle = -angle;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        rotateToFacing(poseStack, facing);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        double centerX = 15.0D / 16.0D;
        poseStack.translate(centerX, 0.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotation(angle));
        poseStack.translate(-centerX, -0.5D, -0.5D);

        renderModel(poseStack, buffer, model, light, overlay);
        poseStack.popPose();
    }

    private static void rotateToFacing(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
            }
        }
    }

    private static void renderModel(
            PoseStack poseStack,
            MultiBufferSource buffer,
            BakedModel model,
            int light,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create(42L);

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

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(
                    null,
                    direction,
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
}
