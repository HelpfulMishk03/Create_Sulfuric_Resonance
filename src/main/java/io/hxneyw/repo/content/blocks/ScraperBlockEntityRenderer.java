package io.hxneyw.repo.content.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.logistics.FlapStuffs;
import io.hxneyw.repo.client.ClientModEvents;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.ChestBlock;

/** Renders the scraper's named rubber_flaps model group with Create's funnel flap curve. */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ScraperBlockEntityRenderer implements BlockEntityRenderer<ScraperBlockEntity> {
    private static final float HINGE_Y = 11.2f / 16f;
    private static final float HINGE_Z = 12.7f / 16f;

    public ScraperBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ScraperBlockEntity scraper, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        if (scraper.getLevel() == null) {
            return;
        }

        Direction facing = scraper.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
        if (scraper.getLevel().getBlockState(scraper.getBlockPos().relative(facing)).getBlock() instanceof ChestBlock) {
            SuperByteBuffer connector = CachedBuffers.partial(
                    ClientModEvents.SCRAPER_CHEST_CONNECTOR, scraper.getBlockState());
            poseStack.pushPose();
            poseStack.translate(.5f, .5f, .5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
            poseStack.translate(-.5f, -.5f, -.5f);
            connector.light(light).overlay(overlay).renderInto(poseStack, vertexConsumer);
            poseStack.popPose();
        }
        float flapValue = scraper.getFlapValue(partialTick);

        // Create's funnel animation uses four flap sections with different
        // sinusoidal intensities. The scraper's named strips are split into the
        // same four width-wise sections and use Create's own angle function.
        for (int segment = 0; segment < ClientModEvents.SCRAPER_FLAPS.length; segment++) {
            SuperByteBuffer flap = CachedBuffers.partial(
                    ClientModEvents.SCRAPER_FLAPS[segment],
                    scraper.getBlockState());
            poseStack.pushPose();
            poseStack.translate(.5f, .5f, .5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
            poseStack.translate(-.5f, -.5f, -.5f);
            poseStack.translate(0, HINGE_Y, HINGE_Z);
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    FlapStuffs.flapAngle(flapValue, segment) * 0.8f
            ));
            poseStack.translate(0, -HINGE_Y, -HINGE_Z);
            flap.light(light).overlay(overlay).renderInto(poseStack, vertexConsumer);
            poseStack.popPose();
        }
    }
}
