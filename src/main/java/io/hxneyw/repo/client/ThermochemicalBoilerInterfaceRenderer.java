package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceBlock;
import io.hxneyw.repo.content.blocks.thermochemicalboilerinterface.ThermochemicalBoilerInterfaceBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ThermochemicalBoilerInterfaceRenderer
        extends KineticBlockEntityRenderer<ThermochemicalBoilerInterfaceBlockEntity> {

    public ThermochemicalBoilerInterfaceRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    protected void renderSafe(
            ThermochemicalBoilerInterfaceBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state = blockEntity.getBlockState();

        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (!ThermochemicalBoilerInterfaceBlock.hasPort(state, side)) {
                continue;
            }

            Direction.Axis axis = side.getAxis();
            float angle = getShaftAngle(blockEntity, state, side);
            SuperByteBuffer shaft = CachedBuffers.partialFacing(
                    ClientModEvents.THERMOCHEMICAL_BOILER_INTERFACE_PORT_SHAFT,
                    state,
                    side
            );

            kineticRotationTransform(
                    shaft,
                    blockEntity,
                    axis,
                    angle,
                    light
            );
            shaft.overlay(overlay).renderInto(
                    poseStack,
                    buffer.getBuffer(RenderType.cutout())
            );
        }
    }

    private static float getShaftAngle(
            ThermochemicalBoilerInterfaceBlockEntity blockEntity,
            BlockState state,
            Direction side
    ) {
        if (!state.getValue(ThermochemicalBoilerInterfaceBlock.INPUT_ACTIVE)
                || side != ThermochemicalBoilerInterfaceBlock.inputSide(state)
                || blockEntity.getLevel() == null) {
            return 0.0F;
        }

        BlockEntity adjacent = blockEntity.getLevel().getBlockEntity(
                blockEntity.getBlockPos().relative(side)
        );
        if (!(adjacent instanceof KineticBlockEntity kinetic)
                || getRotationAxisOf(kinetic) != side.getAxis()) {
            return 0.0F;
        }

        return getAngleForBe(kinetic, kinetic.getBlockPos(), side.getAxis());
    }
}
