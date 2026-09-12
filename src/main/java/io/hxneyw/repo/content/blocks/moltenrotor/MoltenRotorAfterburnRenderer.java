package io.hxneyw.repo.content.blocks.moltenrotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.hxneyw.repo.Config;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

final class MoltenRotorAfterburnRenderer {
    private static final float FLOOR_Y = 3.15F / 16.0F;
    private static final float ROOF_UNDERSIDE_Y = 12.82F / 16.0F;
    private static final float REAR_INNER_Z = 13.78F / 16.0F;
    private static final float LEFT_INNER_X = 3.10F / 16.0F;
    private static final float RIGHT_INNER_X = 12.90F / 16.0F;

    private MoltenRotorAfterburnRenderer() {
    }

    static void render(
        MoltenRotorBlockEntity blockEntity,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Direction facing
    ) {
        if (blockEntity.getLevel() == null || !blockEntity.isCombustionActive()) {
            return;
        }

        float temperature = blockEntity.getExactTemperature();

        float baseFire = smoothstep(280.0F, 1400.0F, temperature);
        if (baseFire <= 0.002F) {
            return;
        }

        float sideFire = smoothstep(560.0F, 1500.0F, temperature);
        float afterburnExpansion = smoothstep(1400.0F, 2000.0F, temperature);
if (!Config.AFTERBURN_FLAMES_ENABLED.get()) {
            return;
        }

        FireSprite fire0 = FireSprite.of(ModelBakery.FIRE_0, bufferSource);
        FireSprite fire1 = FireSprite.of(ModelBakery.FIRE_1, bufferSource);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        MoltenRotorRenderer.rotateToFacing(poseStack, facing);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        renderRearCombustion(poseStack, fire0, fire1, baseFire, afterburnExpansion);

        if (sideFire > 0.002F) {
            renderSideCombustion(poseStack, fire0, fire1, sideFire, afterburnExpansion);
        }



        poseStack.popPose();
    }

    private static void renderRearCombustion(
        PoseStack poseStack,
        FireSprite fire0,
        FireSprite fire1,
        float baseFire,
        float expansion
    ) {
        float alpha = Mth.lerp(baseFire, 0.38F, 0.92F);
        float height = Mth.lerp(baseFire, 0.335F, 0.610F)
            + expansion * 0.095F;

        float y1 = Math.min(FLOOR_Y + height, ROOF_UNDERSIDE_Y - 0.022F);

        float widthA = Mth.lerp(baseFire, 0.18F, 0.28F);
        float widthB = Mth.lerp(baseFire, 0.19F, 0.31F);
        float widthC = Mth.lerp(baseFire, 0.17F, 0.27F);
        float centerHalf = widthB * 0.5F;
        float centerGap = Mth.lerp(baseFire, 0.030F, 0.050F);

        renderVerticalZ(
            poseStack,
            fire0,
            0.26F - widthA * 0.5F,
            FLOOR_Y,
            0.26F + widthA * 0.5F,
            y1 * 0.94F,
            REAR_INNER_Z,
            alpha * 0.82F,
            false
        );

        renderVerticalZ(
            poseStack,
            fire1,
            0.50F - centerHalf,
            FLOOR_Y,
            0.50F - centerGap,
            y1,
            REAR_INNER_Z - 0.004F,
            alpha,
            true
        );

        renderVerticalZ(
            poseStack,
            fire1,
            0.50F + centerGap,
            FLOOR_Y,
            0.50F + centerHalf,
            y1,
            REAR_INNER_Z - 0.004F,
            alpha,
            false
        );

        renderVerticalZ(
            poseStack,
            fire0,
            0.74F - widthC * 0.5F,
            FLOOR_Y,
            0.74F + widthC * 0.5F,
            y1 * 0.91F,
            REAR_INNER_Z - 0.008F,
            alpha * 0.78F,
            true
        );

        if (baseFire > 0.48F) {
            float secondary = smoothstep(0.48F, 1.0F, baseFire);
            float secondaryTop = Math.min(FLOOR_Y + 0.30F + secondary * 0.22F, y1 - 0.015F);

            renderVerticalZ(
                poseStack,
                fire1,
                0.355F,
                FLOOR_Y + 0.025F,
                0.458F,
                secondaryTop,
                REAR_INNER_Z - 0.012F,
                alpha * 0.38F,
                false
            );

            renderVerticalZ(
                poseStack,
                fire1,
                0.542F,
                FLOOR_Y + 0.025F,
                0.645F,
                secondaryTop,
                REAR_INNER_Z - 0.012F,
                alpha * 0.38F,
                true
            );
        }
    }

    private static void renderSideCombustion(
        PoseStack poseStack,
        FireSprite fire0,
        FireSprite fire1,
        float sideFire,
        float expansion
    ) {
        float alpha = Mth.lerp(sideFire, 0.26F, 0.80F);
        float lowHeight = FLOOR_Y + Mth.lerp(sideFire, 0.22F, 0.43F) + expansion * 0.055F;
        float highHeight = FLOOR_Y + Mth.lerp(sideFire, 0.25F, 0.48F) + expansion * 0.070F;

        lowHeight = Math.min(lowHeight, ROOF_UNDERSIDE_Y - 0.060F);
        highHeight = Math.min(highHeight, ROOF_UNDERSIDE_Y - 0.038F);

        renderVerticalSegment(
            poseStack,
            fire1,
            LEFT_INNER_X + 0.006F,
            0.345F,
            LEFT_INNER_X + 0.104F,
            0.625F,
            FLOOR_Y,
            highHeight,
            alpha,
            false
        );

        renderVerticalSegment(
            poseStack,
            fire0,
            LEFT_INNER_X + 0.004F,
            0.565F,
            LEFT_INNER_X + 0.074F,
            0.825F,
            FLOOR_Y,
            lowHeight,
            alpha * 0.72F,
            true
        );

        renderVerticalSegment(
            poseStack,
            fire1,
            RIGHT_INNER_X - 0.006F,
            0.345F,
            RIGHT_INNER_X - 0.104F,
            0.625F,
            FLOOR_Y,
            highHeight * 0.985F,
            alpha,
            true
        );

        renderVerticalSegment(
            poseStack,
            fire0,
            RIGHT_INNER_X - 0.004F,
            0.565F,
            RIGHT_INNER_X - 0.074F,
            0.825F,
            FLOOR_Y,
            lowHeight * 1.015F,
            alpha * 0.72F,
            false
        );

        if (sideFire > 0.62F) {
            float wallAlpha = smoothstep(0.62F, 1.0F, sideFire) * 0.42F;

            renderVerticalSegment(
                poseStack,
                fire0,
                LEFT_INNER_X + 0.003F,
                0.320F,
                LEFT_INNER_X + 0.003F,
                0.755F,
                FLOOR_Y + 0.010F,
                Math.min(FLOOR_Y + 0.39F + expansion * 0.10F, ROOF_UNDERSIDE_Y - 0.055F),
                wallAlpha,
                true
            );

            renderVerticalSegment(
                poseStack,
                fire0,
                RIGHT_INNER_X - 0.003F,
                0.320F,
                RIGHT_INNER_X - 0.003F,
                0.755F,
                FLOOR_Y + 0.010F,
                Math.min(FLOOR_Y + 0.39F + expansion * 0.10F, ROOF_UNDERSIDE_Y - 0.055F),
                wallAlpha,
                false
            );
        }

        if (sideFire > 0.42F) {
            float cross = smoothstep(0.42F, 1.0F, sideFire);
            float crossAlpha = Mth.lerp(cross, 0.12F, 0.36F);
            float crossTop = Math.min(
                FLOOR_Y + Mth.lerp(cross, 0.24F, 0.38F) + expansion * 0.045F,
                ROOF_UNDERSIDE_Y - 0.105F
            );

            renderVerticalSegment(
                poseStack,
                fire1,
                LEFT_INNER_X + 0.070F,
                0.390F,
                0.455F,
                0.548F,
                FLOOR_Y + 0.018F,
                crossTop,
                crossAlpha,
                false
            );

            renderVerticalSegment(
                poseStack,
                fire1,
                0.545F,
                0.572F,
                RIGHT_INNER_X - 0.070F,
                0.735F,
                FLOOR_Y + 0.018F,
                crossTop,
                crossAlpha,
                false
            );

            renderVerticalSegment(
                poseStack,
                fire1,
                LEFT_INNER_X + 0.070F,
                0.730F,
                0.455F,
                0.592F,
                FLOOR_Y + 0.018F,
                crossTop,
                crossAlpha,
                true
            );

            renderVerticalSegment(
                poseStack,
                fire1,
                0.545F,
                0.528F,
                RIGHT_INNER_X - 0.070F,
                0.390F,
                FLOOR_Y + 0.018F,
                crossTop,
                crossAlpha,
                true
            );
        }
    }


    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private static void renderVerticalZ(
        PoseStack poseStack,
        FireSprite sprite,
        float x0,
        float y0,
        float x1,
        float y1,
        float z,
        float alpha,
        boolean mirror
    ) {
        PoseStack.Pose pose = poseStack.last();
        float u0 = mirror ? sprite.u1 : sprite.u0;
        float u1 = mirror ? sprite.u0 : sprite.u1;
        int a = alpha(alpha);

        vertex(sprite.consumer, pose, x0, y0, z, u0, sprite.v1, a, 0.0F, -1.0F);
        vertex(sprite.consumer, pose, x1, y0, z, u1, sprite.v1, a, 0.0F, -1.0F);
        vertex(sprite.consumer, pose, x1, y1, z, u1, sprite.v0, a, 0.0F, -1.0F);
        vertex(sprite.consumer, pose, x0, y1, z, u0, sprite.v0, a, 0.0F, -1.0F);
    }

    private static void renderVerticalSegment(
        PoseStack poseStack,
        FireSprite sprite,
        float x0,
        float z0,
        float x1,
        float z1,
        float y0,
        float y1,
        float alpha,
        boolean mirror
    ) {
        PoseStack.Pose pose = poseStack.last();
        float u0 = mirror ? sprite.u1 : sprite.u0;
        float u1 = mirror ? sprite.u0 : sprite.u1;
        int a = alpha(alpha);

        float dx = x1 - x0;
        float dz = z1 - z0;
        float length = Mth.sqrt(dx * dx + dz * dz);
        float nx = length > 0.0F ? -dz / length : 0.0F;
        float nz = length > 0.0F ? dx / length : -1.0F;

        vertex(sprite.consumer, pose, x0, y0, z0, u0, sprite.v1, a, nx, nz);
        vertex(sprite.consumer, pose, x1, y0, z1, u1, sprite.v1, a, nx, nz);
        vertex(sprite.consumer, pose, x1, y1, z1, u1, sprite.v0, a, nx, nz);
        vertex(sprite.consumer, pose, x0, y1, z0, u0, sprite.v0, a, nx, nz);

        vertex(sprite.consumer, pose, x0, y1, z0, u0, sprite.v0, a, -nx, -nz);
        vertex(sprite.consumer, pose, x1, y1, z1, u1, sprite.v0, a, -nx, -nz);
        vertex(sprite.consumer, pose, x1, y0, z1, u1, sprite.v1, a, -nx, -nz);
        vertex(sprite.consumer, pose, x0, y0, z0, u0, sprite.v1, a, -nx, -nz);
    }

    private static int alpha(float alpha) {
        return Mth.clamp((int) (alpha * 255.0F), 0, 255);
    }

    private static void vertex(
        VertexConsumer consumer,
        PoseStack.Pose pose,
        float x,
        float y,
        float z,
        float u,
        float v,
        int alpha,
        float nx,
        float nz
    ) {
        consumer.addVertex(pose, new Vector3f(x, y, z))
            .setColor(255, 255, 255, alpha)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, nx, 0.0F, nz);
    }

    private record FireSprite(
        VertexConsumer consumer,
        float u0,
        float u1,
        float v0,
        float v1
    ) {
        private static FireSprite of(Material material, MultiBufferSource bufferSource) {
            TextureAtlasSprite sprite = material.sprite();
            VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityTranslucentEmissive(material.atlasLocation())
            );

            return new FireSprite(
                consumer,
                sprite.getU0(),
                sprite.getU1(),
                sprite.getV0(),
                sprite.getV1()
            );
        }
    }
}
