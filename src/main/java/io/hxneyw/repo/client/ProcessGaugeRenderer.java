package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.processgauge.ProcessGaugeBlock;
import io.hxneyw.repo.content.blocks.processgauge.ProcessGaugeBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.joml.Vector3f;

@SuppressWarnings("SpellCheckingInspection")
public class ProcessGaugeRenderer
        extends SafeBlockEntityRenderer<ProcessGaugeBlockEntity> {

    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "sulfuricresonance",
                    "textures/misc/process_status_overlay.png"
            );

    private static final double POINTER_X = 5.5D / 16.0D;
    private static final double POINTER_Y = 7.615D / 16.0D;
    private static final double POINTER_Z = 10.1675D / 16.0D;

    private static final double DRUM_X = 9.85D / 16.0D;
    private static final double DRUM_Y = 7.825D / 16.0D;
    private static final double DRUM_Z = 12.0D / 16.0D;

    private static final double FLOOR_POINTER_X = 5.5D / 16.0D;
    private static final double FLOOR_POINTER_Y = 5.8325D / 16.0D;
    private static final double FLOOR_POINTER_Z = 9.015D / 16.0D;

    private static final double FLOOR_DRUM_X = 9.85D / 16.0D;
    private static final double FLOOR_DRUM_Y = 4.0D / 16.0D;
    private static final double FLOOR_DRUM_Z = 9.225D / 16.0D;

    private static final float CONTACT_X1 = 7.05F;
    private static final float CONTACT_X2 = 8.95F;

    public ProcessGaugeRenderer(
            @SuppressWarnings("unused") BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    protected void renderSafe(
            ProcessGaugeBlockEntity gauge,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state = gauge.getBlockState();
        Direction facing = state.getValue(ProcessGaugeBlock.FACING);
        AttachFace face = state.getValue(ProcessGaugeBlock.FACE);

        poseStack.pushPose();
        applyFacing(poseStack, facing);

        if (face == AttachFace.FLOOR) {
            renderFloorGauge(gauge, partialTicks, state, poseStack, buffer, light, overlay);
        } else {
            renderWallGauge(gauge, partialTicks, state, poseStack, buffer, light, overlay);
        }

        poseStack.popPose();
    }

    private static void renderWallGauge(
            ProcessGaugeBlockEntity gauge,
            float partialTicks,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        renderPointer(gauge, partialTicks, state, poseStack, buffer, light, overlay);
        renderDrum(gauge, partialTicks, state, poseStack, buffer, light, overlay);
        renderStatusLamp(gauge, poseStack, buffer, light, overlay);
        if (gauge.isActive()) {
            renderOutputContacts(poseStack, buffer, overlay);
        }
    }

    private static void renderFloorGauge(
            ProcessGaugeBlockEntity gauge,
            float partialTicks,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        renderFloorPointer(gauge, partialTicks, state, poseStack, buffer, light, overlay);
        renderFloorDrum(gauge, partialTicks, state, poseStack, buffer, light, overlay);
        renderFloorStatusLamp(gauge, poseStack, buffer, light, overlay);
        if (gauge.isActive()) {
            renderFloorOutputContacts(poseStack, buffer, overlay);
        }
    }

    private static void renderPointer(
            ProcessGaugeBlockEntity gauge,
            float partialTicks,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        poseStack.pushPose();
        poseStack.translate(POINTER_X, POINTER_Y, POINTER_Z);
        poseStack.mulPose(Axis.ZP.rotationDegrees(gauge.getPointerAngle(partialTicks)));
        poseStack.translate(-POINTER_X, -POINTER_Y, -POINTER_Z);
        CachedBuffers.partial(ClientModEvents.PROCESS_GAUGE_POINTER, state)
                .light(light)
                .overlay(overlay)
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
        poseStack.popPose();
    }

    private static void renderFloorPointer(
            ProcessGaugeBlockEntity gauge,
            float partialTicks,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        poseStack.pushPose();
        poseStack.translate(FLOOR_POINTER_X, FLOOR_POINTER_Y, FLOOR_POINTER_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-gauge.getPointerAngle(partialTicks)));
        poseStack.translate(-FLOOR_POINTER_X, -FLOOR_POINTER_Y, -FLOOR_POINTER_Z);
        CachedBuffers.partial(ClientModEvents.PROCESS_GAUGE_POINTER_FLOOR, state)
                .light(light)
                .overlay(overlay)
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
        poseStack.popPose();
    }

    private static void renderDrum(
            ProcessGaugeBlockEntity gauge,
            float partialTicks,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        poseStack.pushPose();
        poseStack.translate(DRUM_X, DRUM_Y, DRUM_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(gauge.getDrumAngle(partialTicks)));
        poseStack.translate(-DRUM_X, -DRUM_Y, -DRUM_Z);
        CachedBuffers.partial(ClientModEvents.PROCESS_GAUGE_DRUM, state)
                .light(light)
                .overlay(overlay)
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
        poseStack.popPose();
    }

    private static void renderFloorDrum(
            ProcessGaugeBlockEntity gauge,
            float partialTicks,
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        poseStack.pushPose();
        poseStack.translate(FLOOR_DRUM_X, FLOOR_DRUM_Y, FLOOR_DRUM_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(gauge.getDrumAngle(partialTicks)));
        poseStack.translate(-FLOOR_DRUM_X, -FLOOR_DRUM_Y, -FLOOR_DRUM_Z);
        CachedBuffers.partial(ClientModEvents.PROCESS_GAUGE_DRUM_FLOOR, state)
                .light(light)
                .overlay(overlay)
                .renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
        poseStack.popPose();
    }

    private static void renderStatusLamp(
            ProcessGaugeBlockEntity gauge,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        int color = statusColor(gauge);
        int statusLight = statusLight(gauge, light);
        renderVerticalQuad(
                poseStack, buffer,
                7.42F / 16.0F, 4.20F / 16.0F,
                8.58F / 16.0F, 4.90F / 16.0F,
                10.015F / 16.0F,
                color, statusLight, overlay
        );
    }

    private static void renderFloorStatusLamp(
            ProcessGaugeBlockEntity gauge,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        int color = statusColor(gauge);
        int statusLight = statusLight(gauge, light);
        renderHorizontalQuad(
                poseStack, buffer,
                7.42F / 16.0F, 5.60F / 16.0F,
                8.58F / 16.0F, 6.30F / 16.0F,
                5.985F / 16.0F,
                color, statusLight, overlay
        );
    }

    private static int statusColor(ProcessGaugeBlockEntity gauge) {
        if (gauge.isActive()) {
            return argb(150, 63, 214, 96);
        }
        return switch (gauge.getLinkStatus()) {
            case VALID -> argb(80, 57, 83, 88);
            case UNAVAILABLE -> argb(128, 210, 139, 40);
            case INVALID, UNBOUND, UNLINKED -> argb(130, 187, 48, 52);
        };
    }

    private static int statusLight(ProcessGaugeBlockEntity gauge, int light) {
        if (gauge.isActive()) {
            return LightTexture.FULL_BRIGHT;
        }
        return gauge.getLinkStatus() == ProcessGaugeBlockEntity.LinkStatus.VALID
                ? light
                : boostBlockLight(light);
    }

    private static void renderOutputContacts(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay
    ) {
        int coreColor = argb(235, 235, 48, 64);
        int haloColor = argb(82, 235, 48, 64);

        renderVerticalContact(
                poseStack, buffer,
                0.45F, 0.75F, 11.295F,
                coreColor, haloColor, overlay
        );
        renderVerticalContact(
                poseStack, buffer,
                0.00F, 0.30F, 15.395F,
                coreColor, haloColor, overlay
        );
    }

    private static void renderFloorOutputContacts(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay
    ) {
        int coreColor = argb(235, 235, 48, 64);
        int haloColor = argb(82, 235, 48, 64);

        renderHorizontalContact(
                poseStack, buffer,
                1.85F, 2.15F, 4.705F,
                coreColor, haloColor, overlay
        );
        renderHorizontalContact(
                poseStack, buffer,
                1.40F, 1.70F, 0.605F,
                coreColor, haloColor, overlay
        );
    }

    private static void renderVerticalContact(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float y1,
            float y2,
            float z,
            int coreColor,
            int haloColor,
            int overlay
    ) {
        renderVerticalQuad(
                poseStack, buffer,
                CONTACT_X1 / 16.0F, y1 / 16.0F,
                CONTACT_X2 / 16.0F, y2 / 16.0F,
                z / 16.0F,
                coreColor, LightTexture.FULL_BRIGHT, overlay
        );

        float haloPadding = 0.12F;
        renderVerticalQuad(
                poseStack, buffer,
                (CONTACT_X1 - haloPadding) / 16.0F,
                (y1 - haloPadding) / 16.0F,
                (CONTACT_X2 + haloPadding) / 16.0F,
                (y2 + haloPadding) / 16.0F,
                (z - 0.02F) / 16.0F,
                haloColor, LightTexture.FULL_BRIGHT, overlay
        );
    }

    private static void renderHorizontalContact(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float z1,
            float z2,
            float y,
            int coreColor,
            int haloColor,
            int overlay
    ) {
        renderHorizontalQuad(
                poseStack, buffer,
                CONTACT_X1 / 16.0F, z1 / 16.0F,
                CONTACT_X2 / 16.0F, z2 / 16.0F,
                (y + 0.01F) / 16.0F,
                coreColor, LightTexture.FULL_BRIGHT, overlay
        );

        float haloPadding = 0.12F;
        renderHorizontalQuad(
                poseStack, buffer,
                (CONTACT_X1 - haloPadding) / 16.0F,
                (z1 - haloPadding) / 16.0F,
                (CONTACT_X2 + haloPadding) / 16.0F,
                (z2 + haloPadding) / 16.0F,
                (y + 0.03F) / 16.0F,
                haloColor, LightTexture.FULL_BRIGHT, overlay
        );
    }

    private static void renderVerticalQuad(
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
        vertex(consumer, pose, x1, y1, z, 0, 1, color, light, overlay, 0, -1);
        vertex(consumer, pose, x2, y1, z, 1, 1, color, light, overlay, 0, -1);
        vertex(consumer, pose, x2, y2, z, 1, 0, color, light, overlay, 0, -1);
        vertex(consumer, pose, x1, y2, z, 0, 0, color, light, overlay, 0, -1);
    }

    private static void renderHorizontalQuad(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float x1,
            float z1,
            float x2,
            float z2,
            float y,
            int color,
            int light,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityTranslucent(WHITE_TEXTURE)
        );
        PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, x1, y, z2, 0, 1, color, light, overlay, 1, 0);
        vertex(consumer, pose, x2, y, z2, 1, 1, color, light, overlay, 1, 0);
        vertex(consumer, pose, x2, y, z1, 1, 0, color, light, overlay, 1, 0);
        vertex(consumer, pose, x1, y, z1, 0, 0, color, light, overlay, 1, 0);
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
            int overlay,
            float normalY,
            float normalZ
    ) {
        consumer.addVertex(pose, new Vector3f(x, y, z))
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, normalY, normalZ);
    }

    private static int boostBlockLight(int packedLight) {
        return LightTexture.pack(
                Math.max(LightTexture.block(packedLight), 10),
                LightTexture.sky(packedLight)
        );
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (alpha & 255) << 24
                | (red & 255) << 16
                | (green & 255) << 8
                | (blue & 255);
    }

    private static void applyFacing(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
            }
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }
}
