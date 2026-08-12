package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ThermalGaugeRenderer
        extends SafeBlockEntityRenderer<ThermalGaugeBlockEntity> {

    private static final Map<ThermalGaugeBlockEntity, Float>
            PREVIOUS_ANGLES = new WeakHashMap<>();

    private static final double PIVOT_X = 6.6D / 16.0D;
    private static final double PIVOT_Y = 1.56D / 16.0D;
    private static final double PIVOT_Z = 6.65D / 16.0D;

    public ThermalGaugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(
            ThermalGaugeBlockEntity gauge,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BakedModel needleModel = ClientModEvents.THERMAL_GAUGE_NEEDLE.get();
        Minecraft minecraft = Minecraft.getInstance();

        if (needleModel == null
                || needleModel == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        poseStack.pushPose();
        applyMountTransform(poseStack, gauge.getBlockState());

        poseStack.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        calculateNeedleAngle(gauge)
                )
        );
        poseStack.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);
        renderModel(poseStack, buffer, needleModel, light, overlay);
        poseStack.popPose();
    }

    private static float calculateNeedleAngle(
            ThermalGaugeBlockEntity gauge
    ) {
        float range = ThermalGaugeBlockEntity.MAX_TEMPERATURE
                - ThermalGaugeBlockEntity.MIN_TEMPERATURE;
        float normalized = Mth.clamp(
                (gauge.getDisplayTemperature()
                        - ThermalGaugeBlockEntity.MIN_TEMPERATURE) / range,
                0.0F,
                1.0F
        );
        float target = Mth.lerp(normalized, -45.0F, 45.0F);
        float previous = PREVIOUS_ANGLES.getOrDefault(gauge, target);
        float current = Mth.lerp(0.18F, previous, target);
        PREVIOUS_ANGLES.put(gauge, current);
        return current;
    }

    private static void applyMountTransform(
            PoseStack poseStack,
            BlockState state
    ) {
        float xRotation =
                FactoryPanelBlock.getXRot(state) + Mth.HALF_PI;
        float yRotation = FactoryPanelBlock.getYRot(state);

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotation(yRotation));
        poseStack.mulPose(Axis.XP.rotation(xRotation));
        poseStack.mulPose(Axis.YP.rotation(Mth.PI));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
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
