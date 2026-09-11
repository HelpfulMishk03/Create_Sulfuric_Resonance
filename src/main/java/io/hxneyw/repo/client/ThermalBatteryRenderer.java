package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlock;
import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlockEntity;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class ThermalBatteryRenderer
        extends SafeBlockEntityRenderer<ThermalBatteryBlockEntity> {

    public ThermalBatteryRenderer(BlockEntityRendererProvider.Context context) {
        Objects.requireNonNull(context, "context");
    }

    @Override
    protected void renderSafe(
            ThermalBatteryBlockEntity battery,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel shaftModel = ClientModEvents.THERMAL_BATTERY_SHAFT.get();
        BlockState state = battery.getBlockState();
        Direction facing = ThermalBatteryBlock.interfaceSide(state);

        if (shaftModel != null
                && shaftModel != minecraft.getModelManager().getMissingModel()) {
            float angle = KineticBlockEntityRenderer.getAngleForBe(
                    battery,
                    battery.getBlockPos(),
                    facing.getAxis()
            );

            if (facing == Direction.SOUTH || facing == Direction.EAST) {
                angle = -angle;
            }

            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            rotateToFacing(poseStack, facing);
            poseStack.mulPose(Axis.ZP.rotation(angle));
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            renderModel(
                    poseStack,
                    buffer,
                    shaftModel,
                    light,
                    overlay,
                    1.0F
            );
            poseStack.popPose();
        }

        renderCoreGlow(
                battery,
                poseStack,
                buffer,
                light,
                overlay,
                facing,
                minecraft
        );
    }

    private static void rotateToFacing(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
            }
        }
    }

    private static void renderCoreGlow(
            ThermalBatteryBlockEntity battery,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            Direction facing,
            Minecraft minecraft
    ) {
        float charge = Math.clamp(
                battery.getStoredHeat() / ThermalBatteryBlockEntity.MAX_STORED_HEAT,
                0.0F,
                1.0F
        );
        if (charge <= 0.0001F) {
            return;
        }

        BakedModel glowModel = ClientModEvents.THERMAL_BATTERY_CORE_GLOW.get();
        if (glowModel == null
                || glowModel == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        int existingBlockLight = LightTexture.block(light);
        int skyLight = LightTexture.sky(light);
        int coreBlockLight = Math.max(
                existingBlockLight,
                Math.max(1, Math.round(10.0F * charge))
        );
        int coreLight = LightTexture.pack(coreBlockLight, skyLight);
        float brightness = 0.25F + 0.75F * charge;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        rotateToFacing(poseStack, facing);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        renderModel(
                poseStack,
                buffer,
                glowModel,
                coreLight,
                overlay,
                brightness
        );
        poseStack.popPose();
    }

    private static void renderModel(
            PoseStack poseStack,
            MultiBufferSource buffer,
            BakedModel model,
            int light,
            int overlay,
            float brightness
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create(42L);

        for (BakedQuad quad : model.getQuads(
                null,
                null,
                random,
                ModelData.EMPTY,
                RenderType.solid()
        )) {
            consumer.putBulkData(
                    poseStack.last(),
                    quad,
                    brightness,
                    brightness,
                    brightness,
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
                    RenderType.solid()
            )) {
                consumer.putBulkData(
                        poseStack.last(),
                        quad,
                        brightness,
                        brightness,
                        brightness,
                        1.0F,
                        light,
                        overlay
                );
            }
        }
    }
}
