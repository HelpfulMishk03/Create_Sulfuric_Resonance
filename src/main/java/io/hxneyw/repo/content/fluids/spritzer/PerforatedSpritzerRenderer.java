package io.hxneyw.repo.content.fluids.spritzer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import java.util.function.Function;
import net.createmod.catnip.render.FluidRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class PerforatedSpritzerRenderer extends SmartBlockEntityRenderer<PerforatedSpritzerBlockEntity> {
   public PerforatedSpritzerRenderer(Context context) {
      super(context);
   }

   protected void renderSafe(PerforatedSpritzerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      FluidStack fluidStack = be.getTankInventory().getFluid();
      if (!fluidStack.isEmpty()) {
         if (be.getFluidLevel() != null) {
            float fillLevel = be.getFluidLevel().getValue(partialTicks);
            if (!(fillLevel <= 0.01F)) {
               this.renderFluid(fluidStack, fillLevel, ms, buffer, light);
            }
         }
      }
   }

   private void renderFluid(FluidStack fluidStack, float fillLevel, PoseStack ms, MultiBufferSource buffer, int light) {
      Fluid fluid = fluidStack.getFluid();
      IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid);
      Function<ResourceLocation, TextureAtlasSprite> spriteAtlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
      TextureAtlasSprite stillTexture = spriteAtlas.apply(clientFluid.getStillTexture(fluidStack));
      int color = clientFluid.getTintColor(fluidStack);
      int fluidLuminosity = fluid.getFluidType().getLightLevel(fluidStack);
      int blockLight = Math.max(light >> 4 & 15, fluidLuminosity);
      light = light & 15728640 | blockLight << 4;
      VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
      float xMin = 0.125F;
      float xMax = 0.875F;
      float zMin = 0.125F;
      float zMax = 0.875F;
      float yMin = 0.3125F;
      float yMax = 0.8125F;
      float fluidHeight = yMin + (yMax - yMin) * fillLevel;
      ms.pushPose();
      FluidRenderHelper.renderStillTiledFace(Direction.NORTH, xMin, yMin, xMax, fluidHeight, zMin, builder, ms, light, color, stillTexture);
      FluidRenderHelper.renderStillTiledFace(Direction.SOUTH, xMin, yMin, xMax, fluidHeight, zMax, builder, ms, light, color, stillTexture);
      FluidRenderHelper.renderStillTiledFace(Direction.WEST, zMin, yMin, zMax, fluidHeight, xMin, builder, ms, light, color, stillTexture);
      FluidRenderHelper.renderStillTiledFace(Direction.EAST, zMin, yMin, zMax, fluidHeight, xMax, builder, ms, light, color, stillTexture);
      if (fillLevel < 0.99F) {
         FluidRenderHelper.renderStillTiledFace(Direction.UP, xMin, zMin, xMax, zMax, fluidHeight, builder, ms, light, color, stillTexture);
      }

      ms.popPose();
   }

   public int getViewDistance() {
      return 16;
   }
}
