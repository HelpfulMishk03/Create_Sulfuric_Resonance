package io.hxneyw.repo.compat.jei.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AnimatedMoltenRotor extends AnimatedKinetics {
   private HeatLevel heatLevel = HeatLevel.SEETHING;

   public AnimatedMoltenRotor withHeat(HeatLevel heatLevel) {
      this.heatLevel = heatLevel;
      return this;
   }

   public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
      PoseStack matrixStack = graphics.pose();
      matrixStack.pushPose();
      matrixStack.translate(xOffset, yOffset, 200.0F);
      matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5F));
      matrixStack.mulPose(Axis.YP.rotationDegrees(22.5F));
      int scale = 23;
      BlockState rotorState = AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
              .defaultBlockState()
              .setValue(MoltenRotorBlock.FACING, Direction.SOUTH)
              .setValue(MoltenRotorBlock.HEAT_LEVEL, this.heatLevel);
      matrixStack.pushPose();
      matrixStack.translate(-0.875, 0.0, 0.0);
      matrixStack.scale(scale * 0.125F, scale, scale);
      this.blockElement(this.shaft(net.minecraft.core.Direction.Axis.X)).rotateBlock(getCurrentAngle(), 0.0, 0.0).atLocal(0.0, 1.65, 0.0).render(graphics);
      matrixStack.popPose();
      this.blockElement(rotorState).atLocal(0.0, 1.65, 0.0).scale(scale).render(graphics);
      matrixStack.pushPose();
      matrixStack.translate(0.875, 0.0, 0.0);
      matrixStack.scale(scale * 0.125F, scale, scale);
      this.blockElement(this.shaft(net.minecraft.core.Direction.Axis.X)).rotateBlock(getCurrentAngle(), 0.0, 0.0).atLocal(0.0, 1.65, 0.0).render(graphics);
      matrixStack.popPose();
      matrixStack.popPose();
   }
}
