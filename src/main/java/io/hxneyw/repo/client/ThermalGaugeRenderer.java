package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelRenderer;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ThermalGaugeRenderer extends SafeBlockEntityRenderer<ThermalGaugeBlockEntity> {

    private final FactoryPanelRenderer factoryPanelRenderer;

    private static final Map<ThermalGaugeBlockEntity, EnumMap<PanelSlot, Float>> PREVIOUS_ANGLES =
            new WeakHashMap<>();

    private static final double PIVOT_X = 6.6D / 16.0D;
    private static final double PIVOT_Y = 1.56D / 16.0D;
    private static final double PIVOT_Z = 6.65D / 16.0D;

    private static final double RENDER_DROP = 0.0D / 16.0D;
    private static final double NEEDLE_LIFT = 0.05D / 16.0D;

    private static final float COLD_ANGLE = 0.0F;
    private static final float MAX_ANGLE = -90.0F;

    public ThermalGaugeRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        factoryPanelRenderer = new FactoryPanelRenderer(context);
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
        BlockState state = gauge.getBlockState();

        if (gauge.activeFactoryPanelCount() > 0) {
            factoryPanelRenderer.render(
                    gauge,
                    partialTicks,
                    poseStack,
                    buffer,
                    light,
                    overlay
            );
        }

        poseStack.pushPose();
        applyMountTransform(poseStack, state);

        for (PanelSlot slot : gauge.getActiveSlots()) {
            poseStack.pushPose();

            applySlotTranslation(poseStack, slot);
            poseStack.translate(0.0D, RENDER_DROP, 0.0D);

            CachedBuffers.partial(ClientModEvents.THERMAL_GAUGE_BASE, state)
                    .light(light)
                    .overlay(overlay)
                    .renderInto(
                            poseStack,
                            buffer.getBuffer(RenderType.translucent())
                    );

            poseStack.pushPose();
            poseStack.translate(0.0D, NEEDLE_LIFT, 0.0D);
            poseStack.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            calculateNeedleAngle(gauge, slot)
                    )
            );
            poseStack.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);

            CachedBuffers.partial(ClientModEvents.THERMAL_GAUGE_NEEDLE, state)
                    .light(light)
                    .overlay(overlay)
                    .renderInto(
                            poseStack,
                            buffer.getBuffer(RenderType.translucent())
                    );

            poseStack.popPose();
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static float calculateNeedleAngle(
            ThermalGaugeBlockEntity gauge,
            PanelSlot slot
    ) {
        float range =
                ThermalGaugeBlockEntity.MAX_TEMPERATURE
                        - ThermalGaugeBlockEntity.MIN_TEMPERATURE;

        float normalized = Mth.clamp(
                (gauge.getDisplayTemperature(slot)
                        - ThermalGaugeBlockEntity.MIN_TEMPERATURE)
                        / range,
                0.0F,
                1.0F
        );

        float target = Mth.lerp(
                normalized,
                COLD_ANGLE,
                MAX_ANGLE
        );

        EnumMap<PanelSlot, Float> angles =
                PREVIOUS_ANGLES.computeIfAbsent(
                        gauge,
                        ignored -> new EnumMap<>(PanelSlot.class)
                );

        float previous = angles.getOrDefault(slot, target);
        float current = Math.abs(target - previous) < 0.01F
                ? target
                : Mth.lerp(0.12F, previous, target);
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
        float xRotation =
                FactoryPanelBlock.getXRot(state)
                        + Mth.HALF_PI;
        float yRotation =
                FactoryPanelBlock.getYRot(state);

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotation(yRotation));
        poseStack.mulPose(Axis.XP.rotation(xRotation));
        poseStack.mulPose(Axis.YP.rotation(Mth.PI));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }
}
