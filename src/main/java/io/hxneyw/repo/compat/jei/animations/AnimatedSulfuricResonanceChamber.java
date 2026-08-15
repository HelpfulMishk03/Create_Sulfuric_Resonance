package io.hxneyw.repo.compat.jei.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import io.hxneyw.repo.client.ClientModEvents;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@ParametersAreNonnullByDefault
public final class AnimatedSulfuricResonanceChamber extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, 200.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(205.0F));

        float scale = 17.0F;
        float ringAngle = getCurrentAngle() * 0.72F;
        BlockState state = AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
                .defaultBlockState()
                .setValue(SulfuricResonanceChamberBlock.FACING, Direction.NORTH);

        blockElement(state)
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(ClientModEvents.RESONANCE_CHAMBER_RING_BOTTOM)
                .rotateBlock(0.0F, ringAngle, 0.0F)
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(ClientModEvents.RESONANCE_CHAMBER_RING_TOP)
                .rotateBlock(0.0F, -ringAngle, 0.0F)
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(ClientModEvents.RESONANCE_CHAMBER_SHAFT)
                .rotateBlock(getCurrentAngle(), 0.0F, 0.0F)
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(ClientModEvents.RESONANCE_CHAMBER_WINDOW)
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale)
                .render(graphics);

        poseStack.popPose();
    }
}
