package io.hxneyw.repo.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.hxneyw.repo.content.registry.AllModFluids;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor;
import org.joml.Matrix4f;
@SuppressWarnings("resource")
@EventBusSubscriber(
   modid = "sulfuricresonance",
   value = {Dist.CLIENT}
)
public class SulfuricAcidFogHandler {
   @SubscribeEvent
   public static void onFogColor(ComputeFogColor event) {
      Camera camera = event.getCamera();
      if (isPlayerInSulfuricAcid(camera)) {
         event.setRed(0.784F);
         event.setGreen(0.765F);
         event.setBlue(0.059F);
      }
   }

   @SubscribeEvent
   public static void onRenderOverlay(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player != null && event.getName().toString().equals("minecraft:camera_overlays")) {
         if (mc.options.getCameraType().isFirstPerson()) {
            Camera camera = mc.gameRenderer.getMainCamera();
            if (isPlayerInSulfuricAcid(camera)) {
               renderColoredOverlay(event.getGuiGraphics().pose(), mc);
            }
         }
      }
   }

   private static void renderColoredOverlay(PoseStack poseStack, Minecraft mc) {
      int width = mc.getWindow().getGuiScaledWidth();
      int height = mc.getWindow().getGuiScaledHeight();
      poseStack.pushPose();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      RenderSystem.depthMask(false);
      Matrix4f matrix = poseStack.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      float r = 0.784F;
      float g = 0.765F;
      float b = 0.059F;
      float a = 0.2F;
      buffer.addVertex(matrix, 0.0F, height, -90.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, width, height, -90.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, width, 0.0F, -90.0F).setColor(r, g, b, a);
      buffer.addVertex(matrix, 0.0F, 0.0F, -90.0F).setColor(r, g, b, a);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      poseStack.popPose();
   }

   private static boolean isPlayerInSulfuricAcid(Camera camera) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player == null) {
         return false;
      } else {
         BlockPos eyePos = BlockPos.containing(camera.getPosition());
         FluidState fluidState = player.level().getFluidState(eyePos);
         return fluidState.getType() == AllModFluids.SULFURIC_ACID.get() || fluidState.getType() == AllModFluids.SULFURIC_ACID_FLOWING.get();
      }
   }
}
