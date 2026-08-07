package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalshaft.ThermochemicalShaftBlock;
import io.hxneyw.repo.content.blocks.thermochemicalshaft.ThermochemicalShaftBlockEntity;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ThermochemicalShaftRenderer
        extends SafeBlockEntityRenderer<ThermochemicalShaftBlockEntity> {

    public ThermochemicalShaftRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        Objects.requireNonNull(context, "context");
    }

    @Override
    protected void renderSafe(
            ThermochemicalShaftBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        ThermochemicalRenderHelper.renderVerticalPartialOnAxis(
                blockEntity,
                ClientModEvents.THERMOCHEMICAL_SHAFT,
                blockEntity.getBlockState().getValue(
                        ThermochemicalShaftBlock.AXIS
                ),
                poseStack,
                buffer,
                light,
                overlay
        );
    }
}
