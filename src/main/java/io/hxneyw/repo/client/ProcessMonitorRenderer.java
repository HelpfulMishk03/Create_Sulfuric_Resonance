package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlock;
import io.hxneyw.repo.content.blocks.processmonitor.ProcessMonitorBlockEntity;
import io.hxneyw.repo.content.process.ProcessState;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.joml.Vector3f;

@SuppressWarnings("SpellCheckingInspection")
public class ProcessMonitorRenderer
        extends SafeBlockEntityRenderer<ProcessMonitorBlockEntity> {

    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "textures/misc/process_status_overlay.png"
            );

    private static final float[] ROW_CENTERS = {
            11.12F, 9.74F, 8.36F, 6.98F, 5.60F
    };

    private static final double SELECTOR_PIVOT_X = 10.80D / 16.0D;
    private static final double SELECTOR_PIVOT_Y = 10.08D / 16.0D;
    private static final double SELECTOR_PIVOT_Z = 10.375D / 16.0D;

    private static final double ANTENNA_FLOOR_OFFSET_Y = -10.90D / 16.0D;
    private static final double ANTENNA_FLOOR_OFFSET_Z = 0.40D / 16.0D;

    public ProcessMonitorRenderer(
            @SuppressWarnings("unused") BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    protected void renderSafe(
            ProcessMonitorBlockEntity monitor,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state = monitor.getBlockState();
        Direction facing = state.getValue(ProcessMonitorBlock.FACING);
        AttachFace face = state.getValue(ProcessMonitorBlock.FACE);

        poseStack.pushPose();
        applyPanelTransform(poseStack, facing, face);

        renderSelector(
                monitor,
                partialTicks,
                state,
                poseStack,
                buffer,
                light,
                overlay
        );

        for (int channel = 0; channel < ProcessMonitorBlockEntity.CHANNEL_COUNT; channel++) {
            ProcessMonitorBlockEntity.ChannelSnapshot snapshot =
                    monitor.getChannelSnapshot(channel);
            renderStatusWindow(
                    channel,
                    snapshot,
                    face,
                    poseStack,
                    buffer,
                    light,
                    overlay
            );
        }

        if (face == AttachFace.WALL) {
            renderAntennaTelemetry(
                    monitor,
                    partialTicks,
                    poseStack,
                    buffer,
                    overlay
            );
        }

        float pulse = monitor.getBindingPulse(partialTicks);
        if (pulse > 0.0F) {
            int alpha = 70 + Math.round(150.0F * pulse);
            renderQuad(
                    poseStack,
                    buffer,
                    10.39F / 16.0F,
                    5.47F / 16.0F,
                    11.21F / 16.0F,
                    6.80F / 16.0F,
                    10.195F / 16.0F,
                    argb(alpha, 205, 78, 126),
                    LightTexture.FULL_BRIGHT,
                    overlay
            );
        }

        poseStack.popPose();

        if (face == AttachFace.FLOOR) {
            poseStack.pushPose();
            applyFacing(poseStack, facing);
            poseStack.translate(0.0D, ANTENNA_FLOOR_OFFSET_Y, ANTENNA_FLOOR_OFFSET_Z);
            CachedBuffers.partial(ClientModEvents.PROCESS_MONITOR_ANTENNA, state)
                    .light(light)
                    .overlay(overlay)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
            renderAntennaTelemetry(
                    monitor,
                    partialTicks,
                    poseStack,
                    buffer,
                    overlay
            );
            poseStack.popPose();
        }
    }

    private static void renderAntennaTelemetry(
            ProcessMonitorBlockEntity monitor,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay
    ) {
        if (!monitor.hasTelemetrySignal() || monitor.getLevel() == null) {
            return;
        }

        float period = monitor.hasProcessingChannel() ? 14.0F : 34.0F;
        float phase = ((monitor.getLevel().getGameTime() % (long) period)
                + partialTicks) / period;

        
        
        
        final float travelPortion = 0.88F;
        float baseY = 14.98F;
        float topY = 18.02F;
        float halfHeight = monitor.hasProcessingChannel() ? 0.15F : 0.11F;

        int pulseColor = monitor.hasProcessingChannel()
                ? argb(220, 221, 104, 61)
                : argb(175, 181, 82, 64);

        if (phase <= travelPortion) {
            float travelPhase = Mth.clamp(phase / travelPortion, 0.0F, 1.0F);
            float travelY = Mth.lerp(travelPhase, baseY, topY);
            renderQuad(
                    poseStack,
                    buffer,
                    12.17F / 16.0F,
                    (travelY - halfHeight) / 16.0F,
                    12.43F / 16.0F,
                    (travelY + halfHeight) / 16.0F,
                    12.865F / 16.0F,
                    pulseColor,
                    LightTexture.FULL_BRIGHT,
                    overlay
            );
        } else {
            float terminalPhase = Mth.clamp(
                    (phase - travelPortion) / (1.0F - travelPortion),
                    0.0F,
                    1.0F
            );
            int alpha = Math.round(190.0F * (1.0F - terminalPhase));
            renderQuad(
                    poseStack,
                    buffer,
                    12.00F / 16.0F,
                    18.08F / 16.0F,
                    12.60F / 16.0F,
                    18.54F / 16.0F,
                    12.685F / 16.0F,
                    argb(alpha, 233, 151, 72),
                    LightTexture.FULL_BRIGHT,
                    overlay
            );
        }
    }

    private static void renderSelector(
            ProcessMonitorBlockEntity monitor,
            float partialTicks,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        poseStack.pushPose();
        poseStack.translate(
                SELECTOR_PIVOT_X,
                SELECTOR_PIVOT_Y,
                SELECTOR_PIVOT_Z
        );
        poseStack.mulPose(
                Axis.ZP.rotationDegrees(monitor.getSelectorAngle(partialTicks))
        );
        poseStack.translate(
                -SELECTOR_PIVOT_X,
                -SELECTOR_PIVOT_Y,
                -SELECTOR_PIVOT_Z
        );

        CachedBuffers.partial(ClientModEvents.PROCESS_MONITOR_SELECTOR, state)
                .light(light)
                .overlay(overlay)
                .renderInto(
                        poseStack,
                        buffer.getBuffer(RenderType.cutout())
                );
        poseStack.popPose();
    }

    private static void renderStatusWindow(
            int channel,
            ProcessMonitorBlockEntity.ChannelSnapshot snapshot,
            AttachFace face,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        WindowAppearance appearance = appearance(snapshot);
        float centerY = ROW_CENTERS[channel] / 16.0F;
        int boostedLight = boostBlockLight(
                light,
                snapshot.isAvailable() ? 10 : 6
        );

        renderQuad(
                poseStack,
                buffer,
                4.76F / 16.0F,
                (ROW_CENTERS[channel] - 0.23F) / 16.0F,
                8.41F / 16.0F,
                (ROW_CENTERS[channel] + 0.23F) / 16.0F,
                10.425F / 16.0F,
                appearance.color(),
                boostedLight,
                overlay
        );

        renderWindowText(
                appearance.label(),
                centerY,
                appearance.textColor(),
                face,
                poseStack,
                buffer
        );
    }

    private static WindowAppearance appearance(
            ProcessMonitorBlockEntity.ChannelSnapshot snapshot
    ) {
        return switch (snapshot.availability()) {
            case UNLINKED -> new WindowAppearance(
                    "--",
                    argb(92, 25, 31, 33),
                    0xFF718084
            );
            case UNAVAILABLE -> new WindowAppearance(
                    "OFF",
                    argb(104, 111, 76, 28),
                    0xFFE5B36A
            );
            case INVALID -> new WindowAppearance(
                    "ERR",
                    argb(112, 122, 39, 42),
                    0xFFFF9A9A
            );
            case AVAILABLE -> stateAppearance(snapshot.state());
        };
    }

    private static WindowAppearance stateAppearance(ProcessState actualState) {
        int background = switch (actualState) {
            case IDLE -> argb(92, 49, 71, 77);
            case READY -> argb(110, 42, 135, 67);
            case PROCESSING -> argb(116, 196, 108, 27);
            case BLOCKED -> argb(120, 163, 45, 46);
        };

        return new WindowAppearance(
                actualState.rendererLabel(),
                background,
                0xFFFFFFFF
        );
    }

    private static void renderWindowText(
            String text,
            float y,
            int color,
            AttachFace face,
            PoseStack poseStack,
            MultiBufferSource buffer
    ) {
        Font font = Minecraft.getInstance().font;
        float scale = 0.00305F;
        float x = 6.585F / 16.0F;
        float z = 10.405F / 16.0F;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        if (face == AttachFace.FLOOR) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(scale, -scale, scale);

        float textX = -font.width(text) / 2.0F;
        float textY = -font.lineHeight / 2.0F;
        font.drawInBatch(
                text,
                textX,
                textY,
                color,
                false,
                poseStack.last().pose(),
                buffer,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                LightTexture.FULL_BRIGHT
        );
        poseStack.popPose();
    }

    private static void renderQuad(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float x1,
            float y1,
            float x2,
            float y2,
            float z,
            int color,
            int light,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityTranslucent(WHITE_TEXTURE)
        );
        PoseStack.Pose pose = poseStack.last();

        vertex(consumer, pose, x1, y1, z, 0.0F, 1.0F, color, light, overlay);
        vertex(consumer, pose, x2, y1, z, 1.0F, 1.0F, color, light, overlay);
        vertex(consumer, pose, x2, y2, z, 1.0F, 0.0F, color, light, overlay);
        vertex(consumer, pose, x1, y2, z, 0.0F, 0.0F, color, light, overlay);
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            int light,
            int overlay
    ) {
        consumer.addVertex(pose, new Vector3f(x, y, z))
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }

    private static int boostBlockLight(int packedLight, int minimumBlockLight) {
        return LightTexture.pack(
                Math.max(LightTexture.block(packedLight), minimumBlockLight),
                LightTexture.sky(packedLight)
        );
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (alpha & 255) << 24
                | (red & 255) << 16
                | (green & 255) << 8
                | (blue & 255);
    }

    private static void applyPanelTransform(
            PoseStack poseStack,
            Direction facing,
            AttachFace face
    ) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyYaw(poseStack, facing);
        if (face == AttachFace.FLOOR) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }

    private static void applyFacing(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyYaw(poseStack, facing);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }

    private static void applyYaw(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
            }
        }
    }

    private record WindowAppearance(
            String label,
            int color,
            int textColor
    ) {
    }
}
