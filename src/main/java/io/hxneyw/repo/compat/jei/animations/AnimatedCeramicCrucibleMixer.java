package io.hxneyw.repo.compat.jei.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import io.hxneyw.repo.content.registry.AllModBlocks;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;

@ParametersAreNonnullByDefault
public final class AnimatedCeramicCrucibleMixer
        extends AnimatedKinetics {

    private final AnimatedMixer mixer = new AnimatedMixer();

    @Override
    public void draw(
            GuiGraphics graphics,
            int xOffset,
            int yOffset
    ) {
        this.mixer.draw(graphics, xOffset, yOffset);

        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 210.0F);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5F));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5F));

        this.blockElement(
                        AllModBlocks.ASH_CERAMIC_CRUCIBLE.get()
                                .defaultBlockState()
                )
                .atLocal(0.0, 1.65, 0.0)
                .scale(23.08F)
                .render(graphics);

        matrixStack.popPose();
    }
}
