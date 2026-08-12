package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import java.util.EnumMap;
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

    private static final Map<ThermalGaugeBlockEntity, EnumMap<PanelSlot, Float>>
            PREVIOUS_ANGLES = new WeakHashMap<>();

    private static final double PIVOT_X = 6.6D / 16.0D;
    private static final double PIVOT_Y = 1.56D / 16.0D;
    private static final double PIVOT_Z = 6.65D / 16.0D;

    public ThermalGaugeRenderer(
            @SuppressWarnings("unused") BlockEntityRendererProvider.Context context
    ) {
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
        BakedModel baseModel = ClientModEvents.THERMAL_GAUGE_BASE.get();
        BakedModel needleModel = ClientModEvents.THERMAL_GAUGE_NEEDLE.get();
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel missing = minecraft.getModelManager().getMissingModel();

        if (baseModel == null
                || needleModel == null
                || baseModel == missing
                || needleModel == missing) {
            return;
        }

        poseStack.pushPose();
        applyMountTransform(poseStack, gauge.getBlockState());

        for (PanelSlot slot : gauge.getActiveSlots()) {
            poseStack.pushPose();
            applySlotTranslation(poseStack, slot);
            renderModel(
                    poseStack,
                    buffer,
                    baseModel,
                    RenderType.translucent(),
                    light,
                    overlay
            );

            poseStack.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            calculateNeedleAngle(gauge, slot)
                    )
            );
            poseStack.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);
            renderModel(
                    poseStack,
                    buffer,
                    needleModel,
                    RenderType.solid(),
                    light,
                    overlay
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static float calculateNeedleAngle(
            ThermalGaugeBlockEntity gauge,
            PanelSlot slot
    ) {
        float range = ThermalGaugeBlockEntity.MAX_TEMPERATURE
                - ThermalGaugeBlockEntity.MIN_TEMPERATURE;
        float normalized = Mth.clamp(
                (gauge.getDisplayTemperature(slot)
                        - ThermalGaugeBlockEntity.MIN_TEMPERATURE) / range,
                0.0F,
                1.0F
        );
        float target = Mth.lerp(normalized, 0.0F, -90.0F);
        EnumMap<PanelSlot, Float> angles = PREVIOUS_ANGLES.computeIfAbsent(
                gauge,
                ignored -> new EnumMap<>(PanelSlot.class)
        );
        float previous = angles.getOrDefault(slot, target);
        float current = Mth.lerp(0.18F, previous, target);
        angles.put(slot, current);
        return current;
    }

    private static void applySlotTranslation(
            PoseStack poseStack,
            PanelSlot slot
    ) {
        double x = slot.xOffset * 0.5D - 0.25D;
        double z = slot.yOffset * 0.5D - 0.25D;
        poseStack.translate(x, 0.0D, z);
    }

    private static void applyMountTransform(
            PoseStack poseStack,
            BlockState state
    ) {
        float xRotation = FactoryPanelBlock.getXRot(state) + Mth.HALF_PI;
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
            RenderType renderType,
            int light,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(renderType);
        RandomSource random = RandomSource.create(42L);

        for (BakedQuad quad : model.getQuads(
                null,
                null,
                random,
                ModelData.EMPTY,
                renderType
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
                    renderType
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
