package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import io.hxneyw.repo.content.blocks.thermalwarningalarm.ThermalWarningAlarmBlock;
import io.hxneyw.repo.content.blocks.thermalwarningalarm.ThermalWarningAlarmBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ThermalWarningAlarmRenderer
        extends SafeBlockEntityRenderer<ThermalWarningAlarmBlockEntity> {

    private static final double STRIKER_PIVOT_X = 3.6D / 16.0D;
    private static final double STRIKER_PIVOT_Y = 2.275D / 16.0D;
    private static final double STRIKER_PIVOT_Z = 1.2D / 16.0D;
    private static final float STRIKER_MAX_ANGLE = 14.0F;
    private static final float TWO_PI = (float) (Math.PI * 2.0D);

    public ThermalWarningAlarmRenderer(
            @SuppressWarnings("unused") BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    protected void renderSafe(
            ThermalWarningAlarmBlockEntity alarm,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state = alarm.getBlockState();
        boolean connected = state.getValue(ThermalWarningAlarmBlock.CONNECTED);
        boolean alarming = state.getValue(ThermalWarningAlarmBlock.ALARMING);
        float time = alarm.getLevel() == null
                ? partialTicks
                : alarm.getLevel().getGameTime() + partialTicks;

        poseStack.pushPose();
        applyMountTransform(poseStack, state);

        renderPartial(
                ClientModEvents.THERMAL_WARNING_ALARM_BELL,
                state,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );

        renderStriker(
                state,
                alarming,
                time,
                poseStack,
                buffer,
                light,
                overlay
        );

        int mainFilamentLight = alarming
                ? alarmingFilamentLight(light, time)
                : light;
        int mainBulbLight = alarming
                ? alarmingBulbLight(light, time)
                : light;

        renderPartial(
                ClientModEvents.THERMAL_WARNING_ALARM_MAIN_FILAMENT,
                state,
                poseStack,
                buffer,
                mainFilamentLight,
                overlay,
                RenderType.translucent()
        );


        renderPartial(
                ClientModEvents.THERMAL_WARNING_ALARM_MAIN_BULB,
                state,
                poseStack,
                buffer,
                mainBulbLight,
                overlay,
                RenderType.translucent()
        );

        renderPartial(
                ClientModEvents.THERMAL_WARNING_ALARM_STATUS_GREEN,
                state,
                poseStack,
                buffer,
                connected ? LightTexture.FULL_BRIGHT : light,
                overlay,
                RenderType.translucent()
        );


        renderPartial(
                ClientModEvents.THERMAL_WARNING_ALARM_STATUS_RED,
                state,
                poseStack,
                buffer,
                connected ? light : disconnectedPulseLight(light, time),
                overlay,
                RenderType.translucent()
        );


        if (alarming) {
            renderVibrationLines(
                    state,
                    time,
                    poseStack,
                    buffer,
                    overlay
            );
        }

        poseStack.popPose();
    }

    private static void renderStriker(
            BlockState state,
            boolean alarming,
            float time,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        float swing = alarming ? strikerSwing(time) : 0.0F;
        float angle = swing * STRIKER_MAX_ANGLE;

        poseStack.pushPose();
        rotateAroundY(
                poseStack,
                angle
        );
        renderPartial(
                ClientModEvents.THERMAL_WARNING_ALARM_STRIKER,
                state,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        poseStack.popPose();
    }

    private static void renderVibrationLines(
            BlockState state,
            float time,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay
    ) {
        float swing = strikerSwing(time);

        if (swing >= 0.55F) {
            renderPartial(
                    ClientModEvents.THERMAL_WARNING_ALARM_VIBRATION_LEFT,
                    state,
                    poseStack,
                    buffer,
                    LightTexture.FULL_BRIGHT,
                    overlay,
                    RenderType.cutout()
            );
        } else if (swing <= -0.55F) {
            renderPartial(
                    ClientModEvents.THERMAL_WARNING_ALARM_VIBRATION_RIGHT,
                    state,
                    poseStack,
                    buffer,
                    LightTexture.FULL_BRIGHT,
                    overlay,
                    RenderType.cutout()
            );
        }
    }

    private static float strikerSwing(float time) {
        float phase = TWO_PI * time
                / ThermalWarningAlarmBlockEntity.STRIKER_PERIOD_TICKS;
        return (float) Math.sin(phase);
    }

    private static void renderPartial(
            PartialModel model,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            RenderType renderType
    ) {
        CachedBuffers.partial(model, state)
                .light(light)
                .overlay(overlay)
                .renderInto(
                        poseStack,
                        buffer.getBuffer(renderType)
                );
    }

    private static void rotateAroundY(
            PoseStack poseStack,
            float angle
    ) {
        poseStack.translate(ThermalWarningAlarmRenderer.STRIKER_PIVOT_X, ThermalWarningAlarmRenderer.STRIKER_PIVOT_Y, ThermalWarningAlarmRenderer.STRIKER_PIVOT_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-ThermalWarningAlarmRenderer.STRIKER_PIVOT_X, -ThermalWarningAlarmRenderer.STRIKER_PIVOT_Y, -ThermalWarningAlarmRenderer.STRIKER_PIVOT_Z);
    }

    private static final float DISCONNECTED_RED_CYCLE_TICKS = 30.0F;
    private static final float DISCONNECTED_RED_RISE_END_TICKS = 9.0F;
    private static final float DISCONNECTED_RED_PEAK_END_TICKS = 13.0F;
    private static final float DISCONNECTED_RED_FALL_END_TICKS = 27.0F;

    private static int disconnectedPulseLight(int packedLight, float time) {
        float brightness = disconnectedRedBrightness(time);
        return emissiveSurfaceLight(packedLight, brightness, 5, 3);
    }

    private static float disconnectedRedBrightness(float time) {
        float cycle = time % DISCONNECTED_RED_CYCLE_TICKS;
        if (cycle < 0.0F) {
            cycle += DISCONNECTED_RED_CYCLE_TICKS;
        }
        final float minimum = 0.28F;

        if (cycle < DISCONNECTED_RED_RISE_END_TICKS) {
            float progress = cycle / DISCONNECTED_RED_RISE_END_TICKS;
            return Mth.lerp(smootherStep(progress), minimum, 1.0F);
        }

        if (cycle < DISCONNECTED_RED_PEAK_END_TICKS) {
            return 1.0F;
        }

        if (cycle < DISCONNECTED_RED_FALL_END_TICKS) {
            float progress = (cycle - DISCONNECTED_RED_PEAK_END_TICKS)
                    / (DISCONNECTED_RED_FALL_END_TICKS - DISCONNECTED_RED_PEAK_END_TICKS);
            return Mth.lerp(smootherStep(progress), 1.0F, minimum);
        }

        return minimum;
    }

    private static final float ALARM_LIGHT_CYCLE_TICKS = 40.0F;
    private static final float ALARM_LIGHT_RISE_END_TICKS = 4.0F;
    private static final float ALARM_LIGHT_PEAK_END_TICKS = 10.0F;
    private static final float ALARM_LIGHT_FADE_END_TICKS = 36.0F;

    private static int alarmingFilamentLight(int packedLight, float time) {
        float brightness = warningBeaconBrightness(time, 0.48F);
        return emissiveSurfaceLight(packedLight, brightness, 8, 5);
    }

    private static int alarmingBulbLight(int packedLight, float time) {
        float brightness = warningBeaconBrightness(time, 0.34F);
        return emissiveSurfaceLight(packedLight, brightness, 6, 3);
    }

    private static float warningBeaconBrightness(float time, float minimum) {
        float cycle = positiveMod(time);

        if (cycle < ALARM_LIGHT_RISE_END_TICKS) {
            float progress = cycle / ALARM_LIGHT_RISE_END_TICKS;
            float eased = smootherStep(progress);
            return Mth.lerp(eased, minimum, 1.0F);
        }

        if (cycle < ALARM_LIGHT_PEAK_END_TICKS) {
            return 1.0F;
        }

        if (cycle < ALARM_LIGHT_FADE_END_TICKS) {
            float progress = (cycle - ALARM_LIGHT_PEAK_END_TICKS)
                    / (ALARM_LIGHT_FADE_END_TICKS - ALARM_LIGHT_PEAK_END_TICKS);
            float eased = smootherStep(progress);
            return Mth.lerp(eased, 1.0F, minimum);
        }

        return minimum;
    }

    private static int emissiveSurfaceLight(
            int packedLight,
            float brightness,
            int minimumBlockLight,
            int minimumSkyLight
    ) {
        if (brightness >= 0.985F) {
            return LightTexture.FULL_BRIGHT;
        }

        int baseBlock = LightTexture.block(packedLight);
        int baseSky = LightTexture.sky(packedLight);
        int targetBlock = Mth.floor(Mth.lerp(brightness, minimumBlockLight, 15.0F));
        int targetSky = Mth.floor(Mth.lerp(brightness, minimumSkyLight, 15.0F));

        return LightTexture.pack(
                Math.max(baseBlock, targetBlock),
                Math.max(baseSky, targetSky)
        );
    }

    private static float smootherStep(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * clamped
                * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
    }

    private static float positiveMod(float value) {
        float result = value % ThermalWarningAlarmRenderer.ALARM_LIGHT_CYCLE_TICKS;
        return result < 0.0F ? result + ThermalWarningAlarmRenderer.ALARM_LIGHT_CYCLE_TICKS : result;
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
}
