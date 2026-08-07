package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalConduitBlock;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalConduitBlockEntity;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ThermochemicalConduitRenderer
        extends SafeBlockEntityRenderer<ThermochemicalConduitBlockEntity> {

    public ThermochemicalConduitRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(
                ClientModEvents.THERMOCHEMICAL_CONDUIT_SHAFT.get(),
                "thermochemical conduit shaft model"
        );
    }

    @Override
    protected void renderSafe(
            ThermochemicalConduitBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        ThermochemicalRenderHelper.renderVerticalPartialOnAxis(
                blockEntity,
                ClientModEvents.THERMOCHEMICAL_CONDUIT_SHAFT,
                blockEntity.getBlockState().getValue(
                        ThermochemicalConduitBlock.AXIS
                ),
                poseStack,
                buffer,
                light,
                overlay
        );
    }
}
