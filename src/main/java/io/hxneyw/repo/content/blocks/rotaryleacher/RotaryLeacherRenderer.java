package io.hxneyw.repo.content.blocks.rotaryleacher;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.FluidRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import javax.annotation.ParametersAreNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;
import io.hxneyw.repo.client.ClientModEvents;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

@ParametersAreNonnullByDefault
public final class RotaryLeacherRenderer extends KineticBlockEntityRenderer<RotaryLeacherBlockEntity> {
    public RotaryLeacherRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override public @NotNull AABB getRenderBoundingBox(RotaryLeacherBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(1.0 / 16.0).expandTowards(0, 1, 0);
    }

    @Override protected void renderSafe(RotaryLeacherBlockEntity be, float partialTicks, PoseStack pose,
                                        MultiBufferSource buffer, int light, int overlay) {
        if (be.getBlockState().getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) return;
        VertexConsumer solid = buffer.getBuffer(RenderType.solid());
        float operatingSpeed = be.getOperatingSpeed();
        float inputAngle = AnimationTickHolder.getRenderTime(be.getLevel()) * operatingSpeed * 0.3F;
        float facingAngle = switch (be.getBlockState().getValue(RotaryLeacherBlock.HORIZONTAL_FACING)) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };

        renderRotatingModel(ClientModEvents.ROTARY_LEACHER_GEAR, be, pose, solid, light, overlay, 0.0F, facingAngle + inputAngle);

        float whiskAngle = operatingSpeed == 0 ? 0 : AnimationTickHolder.getRenderTime(be.getLevel()) * Math.copySign(32.0F, operatingSpeed) * 0.3F;
        pose.pushPose();
        pose.translate(0, 1, 0);
        CachedBuffers.partial(ClientModEvents.ROTARY_LEACHER_SLEEVES, be.getBlockState())
                .light(light).overlay(overlay).renderInto(pose, solid);
        pose.popPose();
        renderRotatingModel(ClientModEvents.ROTARY_LEACHER_WHISK, be, pose, solid, light, overlay, 1.0F, facingAngle + whiskAngle);
        renderBatchItems(be, pose, buffer, light, overlay, whiskAngle);
        renderAcid(be, pose, buffer, light);
        pose.pushPose();
        pose.translate(0, 1, 0);
        CachedBuffers.partial(ClientModEvents.ROTARY_LEACHER_GLASS, be.getBlockState())
                .light(light).overlay(overlay)
                .renderInto(pose, buffer.getBuffer(RotaryLeacherGlassRenderType.GLASS));
        pose.popPose();
    }

    private void renderRotatingModel(PartialModel model, RotaryLeacherBlockEntity be, PoseStack pose,
                                    VertexConsumer consumer, int light, int overlay,
                                    float blockYOffset, float angle) {
        pose.pushPose();
        pose.translate(0, blockYOffset, 0);
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-0.5F, -0.5F, -0.5F);
        CachedBuffers.partial(model, be.getBlockState())
                .light(light).overlay(overlay).renderInto(pose, consumer);
        pose.popPose();
    }

    private void renderBatchItems(RotaryLeacherBlockEntity be, PoseStack pose, MultiBufferSource buffer,
                                 int light, int overlay, float angle) {
        if (be.getAcidAmount() <= 0) return;
        ItemStack input = be.getManualItemCapability().getStackInSlot(RotaryLeacherBlockEntity.INPUT);
        if (input.isEmpty()) return;
        int visibleItems = Math.min(4, input.getCount());
        float orbit = angle * ((float)Math.PI / 180.0F);
        for (int i = 0; i < visibleItems; i++) {
            float itemAngle = orbit + (float)(Math.PI * 2.0 * i / visibleItems);
            pose.pushPose();
            pose.translate(0.5F + (float)Math.cos(itemAngle) * 0.22F, 1.19F + (i % 2) * 0.035F,
                    0.5F + (float)Math.sin(itemAngle) * 0.22F);
            pose.mulPose(Axis.YP.rotationDegrees(-angle - i * (360.0F / visibleItems)));
            pose.scale(0.22F, 0.22F, 0.22F);
            Minecraft.getInstance().getItemRenderer().renderStatic(input.copyWithCount(1), ItemDisplayContext.FIXED,
                    light, overlay, pose, buffer, be.getLevel(), i);
            pose.popPose();
        }
    }

    private void renderAcid(RotaryLeacherBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light) {
        FluidStack fluidStack = be.getAcidFluid();
        if (fluidStack.isEmpty()) return;
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite still = atlas.apply(extensions.getStillTexture(fluidStack));
        int color = 0x99000000 | (extensions.getTintColor(fluidStack) & 0x00FFFFFF);
        int blockLight = Math.max(light >> 4 & 15, fluid.getFluidType().getLightLevel(fluidStack));
        int fluidLight = light & 15728640 | blockLight << 4;
        var consumer = buffer.getBuffer(RenderType.translucent());
        float xMin = 0.13F;
        float xMax = 0.87F;
        float zMin = 0.13F;
        float zMax = 0.87F;
        float yMin = 1.08F;
        float yMax = 1.37F;
        float fill = Math.min(1.0F, (float) be.getAcidAmount() / be.getAcidCapacity());
        float fluidHeight = yMin + (yMax - yMin) * fill;
        FluidRenderHelper.renderStillTiledFace(Direction.NORTH, xMin, yMin, xMax, fluidHeight, zMin, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.SOUTH, xMin, yMin, xMax, fluidHeight, zMax, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.WEST, zMin, yMin, zMax, fluidHeight, xMin, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.EAST, zMin, yMin, zMax, fluidHeight, xMax, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.UP, xMin, zMin, xMax, zMax, fluidHeight, consumer, pose, fluidLight, color, still);
    }
}
