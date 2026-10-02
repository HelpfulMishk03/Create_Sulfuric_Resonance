package io.hxneyw.repo.compat.jei.animations;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import io.hxneyw.repo.client.ClientModEvents;
import io.hxneyw.repo.content.blocks.rotaryleacher.RotaryLeacherBlock;
import io.hxneyw.repo.content.recipes.rotaryleaching.RotaryLeachingRecipe;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModFluids;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.fluids.FluidStack;

public final class AnimatedRotaryLeacher extends AnimatedKinetics {
    private RotaryLeachingRecipe recipe;

    public AnimatedRotaryLeacher withRecipe(RotaryLeachingRecipe recipe) {
        this.recipe = recipe;
        return this;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 200.0F);
        pose.mulPose(Axis.XP.rotationDegrees(-15.5F));
        pose.mulPose(Axis.YP.rotationDegrees(205.0F));

        float scale = 20.0F;
        float time = AnimationTickHolder.getRenderTime();
        float gearAngle = time * recipe.minimumSpeed() * 0.3F;
        float whiskAngle = time * 32.0F * 0.3F;
        BlockState lower = AllModBlocks.ROTARY_LEACHER.get().defaultBlockState()
                .setValue(RotaryLeacherBlock.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(RotaryLeacherBlock.FACING, Direction.EAST)
                .setValue(RotaryLeacherBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(RotaryLeacherBlock.HALF, DoubleBlockHalf.UPPER);

        blockElement(lower).atLocal(0, 0.5, 0).scale(scale).render(graphics);
        blockElement(upper).atLocal(0, -0.5, 0).scale(scale).render(graphics);
        blockElement(ClientModEvents.ROTARY_LEACHER_GEAR).rotateBlock(0, gearAngle, 0)
                .atLocal(0, 0.5, 0).scale(scale).render(graphics);
        blockElement(ClientModEvents.ROTARY_LEACHER_WHISK_GASKET)
                .atLocal(0, 0.5, 0).scale(scale).render(graphics);
        float fill = Math.min(1.0F, recipe.fluidAmount() / 3500.0F);
        FluidStack acid = new FluidStack(AllModFluids.SULFURIC_ACID.get(), recipe.fluidAmount());
        DEFAULT_LIGHTING.applyLighting();
        pose.pushPose();
        pose.translate(0, -scale / 2.0F, 0);
        UIRenderHelper.flipForGuiRender(pose);
        pose.scale(scale, scale, scale);
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(acid, 0.13F, 0.08F, 0.13F,
                0.87F, 0.08F + 0.70F * fill, 0.87F, graphics.bufferSource(), pose,
                LightTexture.FULL_BRIGHT, false, true);
        pose.popPose();
        graphics.flush();
        Lighting.setupFor3DItems();

        blockElement(ClientModEvents.ROTARY_LEACHER_WHISK).rotateBlock(0, whiskAngle, 0)
                .atLocal(0, -0.5, 0).scale(scale).render(graphics);

        blockElement(ClientModEvents.ROTARY_LEACHER_GLASS).atLocal(0, -0.5, 0).scale(scale).render(graphics);
        blockElement(ClientModEvents.ROTARY_LEACHER_GLASS_LEFT).atLocal(0, -0.5, 0).scale(scale).render(graphics);
        pose.popPose();
        graphics.flush();
    }

}
