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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

@ParametersAreNonnullByDefault
public final class RotaryLeacherRenderer extends KineticBlockEntityRenderer<RotaryLeacherBlockEntity> {
    public RotaryLeacherRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override public @NotNull AABB getRenderBoundingBox(RotaryLeacherBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(1.0 / 16.0).expandTowards(0, 1, 0);
    }

    @Override protected void renderSafe(RotaryLeacherBlockEntity be, float partialTicks, PoseStack pose,
                                        MultiBufferSource buffer, int light, int overlay) {
        if (be.getBlockState().getValue(RotaryLeacherBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) return;
        if (be.getLevel() == null) return;
        VertexConsumer solid = buffer.getBuffer(RenderType.solid());
        float operatingSpeed = be.getOperatingSpeed();
        float inputAngle = AnimationTickHolder.getRenderTime(be.getLevel()) * operatingSpeed * 0.3F;
        float facingAngle = switch (be.getBlockState().getValue(RotaryLeacherBlock.HORIZONTAL_FACING)) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };

        CachedBuffers.partial(ClientModEvents.ROTARY_LEACHER_WHISK_GASKET, be.getBlockState())
                .light(light).overlay(overlay).renderInto(pose, solid);

        renderRotatingModel(ClientModEvents.ROTARY_LEACHER_GEAR, be, pose, solid, light, overlay, 0.0F, facingAngle + inputAngle);

        float whiskRotation = operatingSpeed == 0 ? 0 : AnimationTickHolder.getRenderTime(be.getLevel()) * Math.copySign(32.0F, operatingSpeed) * 0.3F;
        float whiskAngle = facingAngle + whiskRotation;
        renderRotatingModel(ClientModEvents.ROTARY_LEACHER_WHISK, be, pose, solid, light, overlay, 1.0F, whiskAngle);
        renderRotatingModel(ClientModEvents.ROTARY_LEACHER_SLEEVES, be, pose, solid, light, overlay, 1.0F, whiskAngle);

        MultiBufferSource.BufferSource immediate = buffer instanceof MultiBufferSource.BufferSource source ? source : null;
        if (immediate != null) immediate.endBatch();
        renderAcid(be, pose, buffer, light);
        if (immediate != null) immediate.endBatch(RotaryLeacherFluidRenderType.ACID);
        renderBatchItems(be, pose, buffer, light, overlay, whiskAngle);
        renderMixingParticles(be, whiskAngle);
        if (immediate != null) immediate.endBatch();
        pose.pushPose();
        pose.translate(0, 1, 0);
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-facingAngle));
        pose.translate(-0.5F, -0.5F, -0.5F);
        CachedBuffers.partial(ClientModEvents.ROTARY_LEACHER_GLASS, be.getBlockState())
                .light(light).overlay(overlay)
                .renderInto(pose, buffer.getBuffer(RotaryLeacherGlassRenderType.GLASS));
        if (immediate != null) immediate.endBatch(RotaryLeacherGlassRenderType.GLASS);
        CachedBuffers.partial(ClientModEvents.ROTARY_LEACHER_GLASS_LEFT, be.getBlockState())
                .light(light).overlay(overlay)
                .renderInto(pose, buffer.getBuffer(RotaryLeacherGlassRenderType.GLASS_LEFT_IN_FRONT));
        if (immediate != null) immediate.endBatch(RotaryLeacherGlassRenderType.GLASS_LEFT_IN_FRONT);
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
                                 int light, int overlay, float angleDegrees) {
        var inventory = be.getManualItemCapability();
        ItemStack input = inventory.getStackInSlot(RotaryLeacherBlockEntity.INPUT);
        ItemStack output = be.getVisibleProcessOutput();
        ItemStack byproduct = inventory.getStackInSlot(RotaryLeacherBlockEntity.BYPRODUCT);
        float orbit = angleDegrees * ((float)Math.PI / 180.0F);
        float mixHeight = 1.25F;
        float grooveStart = orbit + (float)Math.PI / 4.0F;
        float grooveStep = (float)Math.PI / 2.0F;
        int groove = 0;
        int inputTarget = input.isEmpty() ? 0 : !output.isEmpty() && !byproduct.isEmpty() ? 2
                : !output.isEmpty() || !byproduct.isEmpty() ? 3 : 4;
        int inputCount = Math.min(inputTarget, input.getCount());
        for (int i = 0; i < inputCount; i++, groove++) {
            float itemAngle = grooveStart + grooveStep * groove;
            float bob = (float)Math.sin(itemAngle) * 0.006F;
            drawItem(be, pose, buffer, light, overlay, input, itemAngle,
                    mixHeight + bob, groove);
        }
        int remainingGrooves = 4 - groove;
        int outputTarget = output.isEmpty() ? 0 : byproduct.isEmpty() ? remainingGrooves
                : Math.max(0, remainingGrooves - 1);
        int outputCount = Math.min(outputTarget, output.getCount());
        for (int i = 0; i < outputCount; i++, groove++) {
            float itemAngle = grooveStart + grooveStep * groove;
            float bob = (float)Math.sin(itemAngle) * 0.006F;
            drawItem(be, pose, buffer, light, overlay, output, itemAngle,
                    mixHeight + bob, 8 + groove);
        }
        if (!byproduct.isEmpty() && groove < 4) {
            float itemAngle = grooveStart + grooveStep * groove;
            float bob = (float)Math.sin(itemAngle) * 0.006F;
            drawItem(be, pose, buffer, light, overlay, byproduct, itemAngle,
                    mixHeight + bob, 15 + groove);
        }
    }

    private void renderMixingParticles(RotaryLeacherBlockEntity be, float angleDegrees) {
        float processingProgress = be.getProcessingProgress();
        if (be.getLevel() == null || be.getOperatingSpeed() == 0 || be.getAcidAmount() <= 0
                || processingProgress <= 0.0F || processingProgress >= 1.0F) return;

        var inventory = be.getManualItemCapability();
        ItemStack input = inventory.getStackInSlot(RotaryLeacherBlockEntity.INPUT);
        ItemStack output = be.getVisibleProcessOutput();
        ItemStack byproduct = inventory.getStackInSlot(RotaryLeacherBlockEntity.BYPRODUCT);
        int inputTarget = input.isEmpty() ? 0 : !output.isEmpty() && !byproduct.isEmpty() ? 2
                : !output.isEmpty() || !byproduct.isEmpty() ? 3 : 4;
        int groove = Math.min(inputTarget, input.getCount());
        int remainingGrooves = 4 - groove;
        int outputTarget = output.isEmpty() ? 0 : byproduct.isEmpty() ? remainingGrooves
                : Math.max(0, remainingGrooves - 1);
        groove += Math.min(outputTarget, output.getCount());
        if (!byproduct.isEmpty() && groove < 4) groove++;
        if (groove == 0) return;

        long gameTime = be.getLevel().getGameTime();
        long phase = gameTime + be.getBlockPos().asLong();
        if (Math.floorMod(phase, 3L) != 0) return;

        int gap = Math.floorMod((int) (gameTime / 3L + be.getBlockPos().asLong()), groove);
        float angle = angleDegrees * ((float) Math.PI / 180.0F)
                + (float) Math.PI / 4.0F + gap * ((float) Math.PI / 2.0F);
        double x = be.getBlockPos().getX() + 0.5D + Math.cos(angle) * 0.27D;
        double y = be.getBlockPos().getY() + 1.27D + Math.sin(angle) * 0.006D;
        double z = be.getBlockPos().getZ() + 0.5D - Math.sin(angle) * 0.27D;
        double direction = Math.signum(be.getOperatingSpeed());
        double tangentialSpeed = Math.min(0.012D, 0.003D + Math.abs(be.getOperatingSpeed()) * 0.000025D);
        double radialSpeed = 0.0015D;
        double xSpeed = -Math.sin(angle) * direction * tangentialSpeed + Math.cos(angle) * radialSpeed;
        double zSpeed = -Math.cos(angle) * direction * tangentialSpeed - Math.sin(angle) * radialSpeed;
        be.getLevel().addParticle(
                new DustParticleOptions(new Vector3f(0.80F, 0.70F, 0.26F), 0.55F),
                x, y, z, xSpeed, 0.0025D, zSpeed
        );
    }

    private void drawItem(RotaryLeacherBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light,
                          int overlay, ItemStack stack, float angle, float y, int seed) {
        pose.pushPose();
        pose.translate(0.5F + (float)Math.cos(angle) * 0.27F, y,
                0.5F - (float)Math.sin(angle) * 0.27F);
        float itemYaw = (float)Math.toDegrees(angle) + 90.0F;
        pose.mulPose(Axis.YP.rotationDegrees(itemYaw));
        pose.mulPose(Axis.XP.rotationDegrees(65.0F));
        pose.scale(0.36F, 0.36F, 0.36F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack.copyWithCount(1), ItemDisplayContext.GROUND,
                light, overlay, pose, buffer, be.getLevel(), seed);
        pose.popPose();
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
        var consumer = buffer.getBuffer(RotaryLeacherFluidRenderType.ACID);
        float xMin = 0.125F;
        float xMax = 0.875F;
        float zMin = 0.125F;
        float zMax = 0.875F;
        float yMin = 1.08F;
        float yMax = 1.92F;
        float fill = Math.min(1.0F, (float) be.getAcidAmount() / be.getAcidCapacity());
        float fluidHeight = yMin + (yMax - yMin) * fill * RotaryLeacherBlockEntity.MAX_ACID_SURFACE_FRACTION;
        FluidRenderHelper.renderStillTiledFace(Direction.NORTH, xMin, yMin, xMax, fluidHeight, zMin, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.SOUTH, xMin, yMin, xMax, fluidHeight, zMax, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.WEST, zMin, yMin, zMax, fluidHeight, xMin, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.EAST, zMin, yMin, zMax, fluidHeight, xMax, consumer, pose, fluidLight, color, still);
        FluidRenderHelper.renderStillTiledFace(Direction.UP, xMin, zMin, xMax, zMax, fluidHeight, consumer, pose, fluidLight, color, still);
    }
}
