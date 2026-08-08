package io.hxneyw.repo.content.blocks.sulfurburner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import io.hxneyw.repo.content.Items;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SulfurBurnerRenderer
        implements BlockEntityRenderer<SulfurBurnerBlockEntity> {

    private static final double CORE_PLATFORM_Y =
            8.0D / 16.0D;

    private static final double CORE_INWARD_OFFSET =
            3.0D / 16.0D;

    private static final double CORE_VERTICAL_OFFSET =
            0.018D;

    private static final float CORE_SCALE =
            0.52F;

    private static final double FUEL_PLATFORM_Y =
            3.3D / 16.0D;

    private static final double FUEL_INWARD_OFFSET =
            2.5D / 16.0D;

    private static final double FUEL_VERTICAL_OFFSET =
            0.018D;

    private static final float FUEL_SCALE =
            0.32F;

    private final ItemRenderer itemRenderer;
    private final ItemStack brimstoneCore;

    public SulfurBurnerRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.itemRenderer =
                context.getItemRenderer();

        this.brimstoneCore =
                new ItemStack(
                        Items.BRIMSTONE_CORE.get()
                );
    }

    @Override
    public void render(
            SulfurBurnerBlockEntity blockEntity,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Level level = blockEntity.getLevel();

        if (level == null) {
            return;
        }

        Direction facing =
                blockEntity.getBlockState()
                        .getValue(
                                SulfurBurnerBlock.FACING
                        );

        renderCore(
                blockEntity,
                level,
                facing,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        renderFuel(
                blockEntity,
                level,
                facing,
                poseStack,
                buffer,
                packedOverlay
        );
    }

    private void renderCore(
            SulfurBurnerBlockEntity blockEntity,
            Level level,
            Direction facing,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        double x =
                0.5D
                        - facing.getStepX()
                        * CORE_INWARD_OFFSET;

        double z =
                0.5D
                        - facing.getStepZ()
                        * CORE_INWARD_OFFSET;

        BlazeBurnerBlock.HeatLevel heat =
                blockEntity.getBlockState()
                        .getValue(
                                SulfurBurnerBlock.HEAT_LEVEL
                        );

        int coreLight =
                switch (heat) {
                    case SEETHING ->
                            LightTexture.FULL_BRIGHT;

                    case KINDLED ->
                            LightTexture.pack(
                                    8,
                                    LightTexture.sky(packedLight)
                            );

                    default ->
                            packedLight;
                };

        poseStack.pushPose();

        poseStack.translate(
                x,
                CORE_PLATFORM_Y
                        + CORE_VERTICAL_OFFSET,
                z
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        rotationFor(facing)
                )
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(90.0F)
        );

        poseStack.scale(
                CORE_SCALE,
                CORE_SCALE,
                CORE_SCALE
        );

        itemRenderer.renderStatic(
                brimstoneCore,
                ItemDisplayContext.FIXED,
                coreLight,
                packedOverlay,
                poseStack,
                buffer,
                level,
                0
        );

        poseStack.popPose();
    }

    private void renderFuel(
            SulfurBurnerBlockEntity blockEntity,
            Level level,
            Direction facing,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedOverlay
    ) {
        ItemStack fuel =
                blockEntity.getRenderedFuelStack();

        if (fuel.isEmpty()) {
            return;
        }

        double x =
                0.5D
                        - facing.getStepX()
                        * FUEL_INWARD_OFFSET;

        double z =
                0.5D
                        - facing.getStepZ()
                        * FUEL_INWARD_OFFSET;

        poseStack.pushPose();

        poseStack.translate(
                x,
                FUEL_PLATFORM_Y
                        + FUEL_VERTICAL_OFFSET,
                z
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        rotationFor(facing)
                )
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(90.0F)
        );

        poseStack.scale(
                FUEL_SCALE,
                FUEL_SCALE,
                FUEL_SCALE
        );

        itemRenderer.renderStatic(
                fuel,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                poseStack,
                buffer,
                level,
                0
        );

        poseStack.popPose();
    }

    private static float rotationFor(
            Direction facing
    ) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}