package io.hxneyw.repo.compat.emi.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class EmiAnimatedCeramicCrucibleMixer
        extends EmiAnimatedKinetics {

    public void draw(
            GuiGraphics graphics,
            int xOffset,
            int yOffset
    ) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200.0F);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5F));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5F));

        int scale = 23;

        blockElement(cogwheel())
                .rotateBlock(0.0F, getCurrentAngle() * 2.0F, 0.0F)
                .atLocal(0.0, 0.0, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(AllBlocks.MECHANICAL_MIXER.getDefaultState())
                .atLocal(0.0, 0.0, 0.0)
                .scale(scale)
                .render(graphics);

        float animation = (
                (Mth.sin(AnimationTickHolder.getRenderTime() / 32.0F) + 1.0F)
                        / 5.0F
        ) + 0.5F;

        blockElement(AllPartialModels.MECHANICAL_MIXER_POLE)
                .atLocal(0.0, animation, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(AllPartialModels.MECHANICAL_MIXER_HEAD)
                .rotateBlock(0.0F, getCurrentAngle() * 4.0F, 0.0F)
                .atLocal(0.0, animation, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(AllBlocks.BASIN.getDefaultState())
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale)
                .render(graphics);

        blockElement(
                AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()
                        .defaultBlockState()
        )
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }
}
