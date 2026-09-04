package io.hxneyw.repo.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.hxneyw.repo.content.entities.CinderFlareEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CinderFlareRenderer extends ThrownItemRenderer<CinderFlareEntity> {
    private final ItemRenderer itemRenderer;

    public CinderFlareRenderer(EntityRendererProvider.Context context) {
        super(context, 0.7F, true);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            CinderFlareEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.scale(0.7F, 0.7F, 0.7F);
        if (entity.isStuckToSurface()) {
            orientToSurface(poseStack, entity.getAttachedFace());
            poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getSurfaceRoll()));
        } else {
            orientToFlight(poseStack, entity.getDeltaMovement());
        }
        itemRenderer.renderStatic(
                entity.getItem(),
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();
    }

    @Override
    protected int getBlockLightLevel(CinderFlareEntity entity, BlockPos pos) {
        return 15;
    }

    private static void orientToSurface(PoseStack poseStack, Direction face) {
        switch (face) {
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case SOUTH -> {
            }
        }
    }

    private static void orientToFlight(PoseStack poseStack, Vec3 motion) {
        if (motion.lengthSqr() <= 1.0E-6D) {
            return;
        }

        Vector3f modelTip = new Vector3f(-1.0F, 1.0F, 0.0F).normalize();
        Vector3f flightDirection = new Vector3f(
                (float) motion.x,
                (float) motion.y,
                (float) motion.z
        ).normalize();
        poseStack.mulPose(new Quaternionf().rotationTo(modelTip, flightDirection));
    }
}
