package io.hxneyw.repo.content.blocks.moltenrotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.client.ClientModEvents;
import io.hxneyw.repo.content.Items;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class MoltenRotorRenderer extends SafeBlockEntityRenderer<MoltenRotorBlockEntity> {
   private static boolean hasLoggedOnce = false;
   private static final Map<MoltenRotorBlockEntity, Float> previousAngles = new WeakHashMap<>();
   private static final Map<MoltenRotorBlockEntity, ImpellerRotationState> impellerRotations = new WeakHashMap<>();
   private float currentPartialTicks = 0.0F;

   private static final class ImpellerRotationState {
      private double lastRenderTime;
      private double angleDegrees;
      private boolean initialized;
   }

   public MoltenRotorRenderer(Context ignoredContext) {
   }

   protected void renderSafe(MoltenRotorBlockEntity furnace, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      this.currentPartialTicks = partialTicks;
      BlockState state = furnace.getBlockState();
      Direction facing = state.getValue(MoltenRotorBlock.FACING);
      int actualLight = this.calculateLight(furnace, light);
      ItemStack impellerItem = new ItemStack(Items.INFERNAL_IMPELLER.get());
      if (!impellerItem.isEmpty()) {
         BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getModel(impellerItem, furnace.getLevel(), null, 0);
         if (itemModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
            this.renderImpellerFromItem(
                    furnace,
                    ms,
                    buffer,
                    actualLight,
                    overlay,
                    facing,
                    itemModel
            );
         }
      }

      MoltenRotorFuelRenderer.render(
              furnace,
              ms,
              buffer,
              actualLight,
              overlay,
              facing
      );

      this.renderRotatingShafts(
              furnace,
              ms,
              buffer,
              actualLight,
              overlay,
              facing
      );

      this.renderHeatGaugeNeedle(furnace, ms, buffer, actualLight, overlay, facing
      );
   }

   static void rotateToFacing(PoseStack ms, Direction facing) {
      switch (facing) {
         case SOUTH -> ms.mulPose(Axis.YP.rotationDegrees(180.0F));
         case EAST -> ms.mulPose(Axis.YP.rotationDegrees(-90.0F));
         case WEST -> ms.mulPose(Axis.YP.rotationDegrees(90.0F));
         default -> {
         }
      }
   }

   private void renderHeatGaugeNeedle(
           MoltenRotorBlockEntity furnace,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           Direction facing
   ) {
      BakedModel needleModel = ClientModEvents.ROTOR_HEAT_NEEDLE.get();
      if (needleModel != null && needleModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
         ms.pushPose();
         ms.translate(0.5, 0.5, 0.5);
         rotateToFacing(ms, facing);

         ms.translate(-0.5, -0.5, -0.5);
         double pivotX = 0.6566265060240963;
         double pivotY = 0.6540880503144654;
         double pivotZ = 0.04375;
         ms.translate(pivotX, pivotY, pivotZ);
         float needleAngle = this.calculateSmoothNeedleAngle(furnace);
         ms.mulPose(Axis.ZP.rotationDegrees(needleAngle));
         ms.translate(-pivotX, -pivotY, -pivotZ);
         this.renderModel(ms, buffer, needleModel, light, overlay);
         ms.popPose();
      }
   }

   private float calculateSmoothNeedleAngle(MoltenRotorBlockEntity furnace) {
      float currentTemp = furnace.getDisplayTemperature();
      float tempPercent = Mth.clamp((currentTemp - 20.0F) / 1579.0F, 0.0F, 1.0F);

      // 1. Calculate and smooth ONLY the base temperature angle first
      float baseTargetAngle = tempPercent * 90.0F;
      float prevAngle = previousAngles.getOrDefault(furnace, baseTargetAngle);
      float smoothingFactor = 0.15F;
      float interpolatedAngle = Mth.lerp(smoothingFactor, prevAngle, baseTargetAngle);

      // Store the smooth base angle for the next frame
      previousAngles.put(furnace, interpolatedAngle);

      // 2. Add the violent shaking AFTER smoothing so it doesn't get dampened
      if (tempPercent > 0.95F) {
         float time =
                 (float)(
                         this.getClientRenderTime(furnace)
                                 / 20.0D
                 );
         float shakeIntensity = (tempPercent - 0.95F) / 0.05F;
         float rapidShake = Mth.sin(time * 30.0F) * 2.0F * shakeIntensity;
         float microShake = Mth.sin(time * 50.0F) * 0.5F * shakeIntensity;

         interpolatedAngle += rapidShake + microShake;
         interpolatedAngle = Mth.clamp(interpolatedAngle, 87.0F, 93.0F);
      }

      return interpolatedAngle;
   }

   private void renderModel(PoseStack ms, MultiBufferSource buffer, BakedModel model, int light, int overlay) {
      VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
      RandomSource random = RandomSource.create();
      ModelData modelData = ModelData.EMPTY;

      for (BakedQuad quad : model.getQuads(null, null, random, modelData, RenderType.solid())) {
         vertexConsumer.putBulkData(ms.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, light, overlay);
      }

      for (Direction dir : Direction.values()) {
         for (BakedQuad quad : model.getQuads(null, dir, random, modelData, RenderType.solid())) {
            vertexConsumer.putBulkData(ms.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, light, overlay);
         }
      }
   }

   private int calculateLight(MoltenRotorBlockEntity furnace, int worldLight) {
      int minimumLight = switch (furnace.getCurrentHeatTier()) {
         case NONE -> 0;
         case SMOULDERING, FADING -> 8;
         case KINDLED -> 12;
         case SEETHING, RADIANT -> 15;
      };

      int worldBlockLight = worldLight & 0xFFFF;
      int worldSkyLight = worldLight >> 16 & 0xFFFF;
      int heatedBlockLight = minimumLight << 4;

      return Math.max(worldBlockLight, heatedBlockLight)
              | worldSkyLight << 16;
   }

   private void renderRotatingShafts(
           MoltenRotorBlockEntity furnace,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           Direction facing
   ) {
      try {
         BakedModel leftShaftModel = ClientModEvents.ROTOR_SHAFT_LEFT.get();
         BakedModel rightShaftModel = ClientModEvents.ROTOR_SHAFT_RIGHT.get();
         boolean foundModels = false;

         Direction.Axis shaftAxis =
                 KineticBlockEntityRenderer.getRotationAxisOf(furnace);

         Direction leftDirection = facing.getCounterClockWise();
         Direction rightDirection = facing.getClockWise();

         float leftAngleRadians =
                 this.toLocalShaftAngle(
                         KineticBlockEntityRenderer.getAngleForBe(
                                 furnace,
                                 furnace.getBlockPos().relative(leftDirection),
                                 shaftAxis
                         ),
                         facing
                 );

         float rightAngleRadians =
                 this.toLocalShaftAngle(
                         KineticBlockEntityRenderer.getAngleForBe(
                                 furnace,
                                 furnace.getBlockPos().relative(rightDirection),
                                 shaftAxis
                         ),
                         facing
                 );

         if (leftShaftModel != null
                 && leftShaftModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
            this.renderShaft(
                    ms,
                    buffer,
                    leftShaftModel,
                    light,
                    overlay,
                    leftAngleRadians,
                    facing,
                    true
            );
            foundModels = true;
         }

         if (rightShaftModel != null
                 && rightShaftModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
            this.renderShaft(
                    ms,
                    buffer,
                    rightShaftModel,
                    light,
                    overlay,
                    rightAngleRadians,
                    facing,
                    false
            );
            foundModels = true;
         }

         if (!hasLoggedOnce) {
            CreateSulfuricResonance.LOGGER.info(
                    foundModels
                            ? "✓ Successfully loaded shaft models"
                            : "✗ Could not find shaft models!"
            );
            hasLoggedOnce = true;
         }
      } catch (Exception exception) {
         if (!hasLoggedOnce) {
            CreateSulfuricResonance.LOGGER.error(
                    "Error loading shaft models: {}",
                    exception.getMessage()
            );
            hasLoggedOnce = true;
         }
      }
   }


   private float toLocalShaftAngle(
           float worldAxisAngleRadians,
           Direction facing
   ) {
      /*
       * The base half-shaft models rotate around local +X. After the
       * whole model is turned to face SOUTH or WEST, local +X points
       * along the negative world shaft axis. Negating only those two
       * facings reproduces Create's positive-world-axis rotation.
       */
      return switch (facing) {
         case SOUTH, WEST -> -worldAxisAngleRadians;
         default -> worldAxisAngleRadians;
      };
   }

   private void renderImpellerFromItem(
           MoltenRotorBlockEntity furnace,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           Direction facing,
           BakedModel model
   ) {
      ms.pushPose();
      ms.translate(0.5, 0.5, 0.5);
      rotateToFacing(ms, facing);

      ms.translate(0.19953125, 0.18078125, 0.4);
      float scale = 0.4F;
      ms.scale(scale, scale, scale);
      ms.translate(-0.5, -0.5, -0.46875);
      float angle =
              this.getImpellerRotationAngle(furnace);
      if (angle != 0.0F) {
         /*
          * rotateToFacing() already places the model in its local frame.
          * One local-Z rotation keeps all four horizontal facings
          * visually consistent without extra direction reversals.
          */
         ms.mulPose(Axis.ZP.rotationDegrees(angle));
      }

      Minecraft.getInstance()
              .getItemRenderer()
              .render(
                      new ItemStack(Items.INFERNAL_IMPELLER.get()),
                      ItemDisplayContext.NONE,
                      false,
                      ms,
                      buffer,
                      light,
                      overlay,
                      model
              );
      ms.popPose();
   }

   private double getClientRenderTime(
           MoltenRotorBlockEntity furnace
   ) {
      if (furnace.getLevel() == null) {
         return 0.0D;
      }

      /*
       * Keep time as a double. Converting the world's long game time
       * to float causes visible stepping after the world has been
       * running for a while.
       */
      return furnace.getLevel().getGameTime()
              + (double) this.currentPartialTicks;
   }

   private float getImpellerRotationAngle(
           MoltenRotorBlockEntity furnace
   ) {
      if (furnace.getLevel() == null) {
         return 0.0F;
      }

      double renderTime =
              this.getClientRenderTime(furnace);

      ImpellerRotationState rotationState =
              impellerRotations.computeIfAbsent(
                      furnace,
                      ignored -> new ImpellerRotationState()
              );

      if (!rotationState.initialized
              || renderTime < rotationState.lastRenderTime) {
         rotationState.lastRenderTime = renderTime;
         rotationState.initialized = true;
         return (float) rotationState.angleDegrees;
      }

      double elapsedTicks =
              renderTime - rotationState.lastRenderTime;

      /*
       * Do not integrate a long period while the renderer was not
       * visible. Normal low-FPS frame gaps remain fully accurate.
       */
      if (elapsedTicks < 0.0D || elapsedTicks > 20.0D) {
         elapsedTicks = 0.0D;
      }

      rotationState.lastRenderTime = renderTime;

      /*
       * Exact visual relationship:
       * 1 degree Celsius = 1 RPM.
       *
       * One RPM advances 0.3 degrees per game tick:
       * 360 degrees / 60 seconds / 20 ticks = 0.3.
       * Integrating the angle prevents phase jumps as temperature changes.
       */
      double impellerRpm =
              furnace.getImpellerRpm();

      rotationState.angleDegrees =
              (rotationState.angleDegrees
                      + impellerRpm * 0.3D * elapsedTicks)
                      % 360.0D;

      return (float) rotationState.angleDegrees;
   }

   private void renderShaft(
           PoseStack ms,
           MultiBufferSource buffer,
           BakedModel model,
           int light,
           int overlay,
           float angleRadians,
           Direction facing,
           boolean isLeftShaft
   ) {
      ms.pushPose();
      ms.translate(0.5, 0.5, 0.5);
      rotateToFacing(ms, facing);

      ms.translate(-0.5, -0.5, -0.5);
      double shaftCenterX = isLeftShaft ? 0.0625 : 0.9375;
      double shaftCenterY = 0.5;
      double shaftCenterZ = 0.5;
      ms.translate(shaftCenterX, shaftCenterY, shaftCenterZ);
      ms.mulPose(Axis.XP.rotation(angleRadians));
      ms.translate(-shaftCenterX, -shaftCenterY, -shaftCenterZ);
      this.renderModel(ms, buffer, model, light, overlay);
      ms.popPose();
   }
}