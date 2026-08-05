package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalgearbox.ThermochemicalGearboxBlockEntity;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class ThermochemicalGearboxRenderer
        extends SafeBlockEntityRenderer<ThermochemicalGearboxBlockEntity> {

    public ThermochemicalGearboxRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        Objects.requireNonNull(context, "context");
    }

    @Override
    protected void renderSafe(
            ThermochemicalGearboxBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        for (Direction face : Direction.values()) {
            ThermochemicalRenderHelper
                    .renderVerticalHalfTowardFace(
                            blockEntity,
                            face,
                            blockEntity
                                    .getVisualSpeedMultiplier(face),
                            poseStack,
                            buffer,
                            light,
                            overlay
                    );
        }
    }
}
