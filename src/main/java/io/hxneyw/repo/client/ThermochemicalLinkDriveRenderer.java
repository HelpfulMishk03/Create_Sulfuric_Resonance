package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermochemicallinkdrive.ThermochemicalLinkDriveBlockEntity;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ThermochemicalLinkDriveRenderer
        extends SafeBlockEntityRenderer<
        ThermochemicalLinkDriveBlockEntity> {

    public ThermochemicalLinkDriveRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        Objects.requireNonNull(context, "context");
    }

    @Override
    protected void renderSafe(
            ThermochemicalLinkDriveBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        Direction.Axis axis = blockEntity
                .getBlockState()
                .getValue(BlockStateProperties.AXIS);

        ThermochemicalRenderHelper.renderVerticalPartialOnAxis(
                blockEntity,
                ClientModEvents.THERMOCHEMICAL_LINK_DRIVE_SHAFT,
                axis,
                poseStack,
                buffer,
                light,
                overlay
        );
    }
}