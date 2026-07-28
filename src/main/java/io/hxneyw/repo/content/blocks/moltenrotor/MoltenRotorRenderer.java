package io.hxneyw.repo.content.blocks.moltenrotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.client.ClientModEvents;
import io.hxneyw.repo.content.Items;

import java.util.List;
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

      Direction.Axis shaftAxis =
              KineticBlockEntityRenderer.getRotationAxisOf(furnace);
      float shaftAngleRadians =
              KineticBlockEntityRenderer.getAngleForBe(
                      furnace,
                      furnace.getBlockPos(),
                      shaftAxis
              );

      this.renderActiveFuel(furnace, ms, buffer, actualLight, overlay, facing
      );

      this.renderRotatingShafts(
              ms,
              buffer,
              actualLight,
              overlay,
              facing,
              shaftAngleRadians
      );

      this.renderHeatGaugeNeedle(furnace, ms, buffer, actualLight, overlay, facing
      );
   }

   private void rotateToFacing(PoseStack ms, Direction facing) {
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
         this.rotateToFacing(ms, facing);

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
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           Direction facing,
           float shaftAngleRadians
   ) {
      try {
         BakedModel leftShaftModel = ClientModEvents.ROTOR_SHAFT_LEFT.get();
         BakedModel rightShaftModel = ClientModEvents.ROTOR_SHAFT_RIGHT.get();
         boolean foundModels = false;

         /*
          * Use Create's exact kinetic angle, including its positional
          * rotation offset. The custom half-shafts now share the same
          * phase, signed speed and checkerboard alignment as connected
          * real Create shafts.
          */
         float localShaftAngleRadians =
                 this.toLocalShaftAngle(shaftAngleRadians, facing);

         if (leftShaftModel != null
                 && leftShaftModel
                 != Minecraft.getInstance().getModelManager().getMissingModel()) {
            this.renderShaft(
                    ms,
                    buffer,
                    leftShaftModel,
                    light,
                    overlay,
                    localShaftAngleRadians,
                    facing,
                    true
            );
            foundModels = true;
         }

         if (rightShaftModel != null
                 && rightShaftModel
                 != Minecraft.getInstance().getModelManager().getMissingModel()) {
            this.renderShaft(
                    ms,
                    buffer,
                    rightShaftModel,
                    light,
                    overlay,
                    localShaftAngleRadians,
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

   private void renderActiveFuel(
           MoltenRotorBlockEntity furnace,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           Direction facing
   ) {
      ItemStack fuelStack = furnace.getRenderedFuelStack();

      if (fuelStack.isEmpty()) {
         return;
      }

      Minecraft minecraft = Minecraft.getInstance();
      BakedModel fuelModel = minecraft
              .getItemRenderer()
              .getModel(fuelStack, furnace.getLevel(), null, 0);

      if (fuelModel == minecraft.getModelManager().getMissingModel()) {
         return;
      }

      MoltenRotorBlockEntity.FuelType fuelType =
              furnace.getRenderedFuelType();

      ms.pushPose();
      ms.translate(0.5, 0.5, 0.5);
      this.rotateToFacing(ms, facing);

      if (furnace.getDisplayFuelTime() > 0) {
         this.renderHeatingKindling(
                 furnace,
                 ms,
                 buffer,
                 light,
                 overlay
         );
      }

      switch (fuelType) {
         case COAL, CHARCOAL -> this.renderCoalPile(
                 furnace,
                 fuelStack,
                 fuelModel,
                 ms,
                 buffer,
                 light,
                 overlay
         );

         case LOG -> this.renderLogFuelScene(
                 furnace,
                 fuelStack,
                 fuelModel,
                 ms,
                 buffer,
                 light,
                 overlay
         );

         case BLAZE_CAKE, SOUL_FIRED_BLAZE_CAKE -> this.renderSpecialFuel(
                 fuelStack,
                 fuelModel,
                 ms,
                 buffer,
                 light,
                 overlay
         );

         case TNT -> this.renderRestingFuel(
                 fuelStack,
                 fuelModel,
                 ms,
                 buffer,
                 light,
                 overlay,
                 0.27F
         );

         default -> this.renderRestingFuel(
                 fuelStack,
                 fuelModel,
                 ms,
                 buffer,
                 light,
                 overlay,
                 0.28F
         );
      }

      ms.popPose();
   }

   private void renderCoalPile(
           MoltenRotorBlockEntity furnace,
           ItemStack fuelStack,
           BakedModel fuelModel,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay
   ) {
      int visibleCount = Math.min(
              furnace.getRenderedFuelUnitCount(),
              3
      );

      double[][] positions =
              getCoalPositions(visibleCount);

      for (double[] position : positions) {
         ms.pushPose();

         if (visibleCount == 1) {
            /*
             * One coal needs a dedicated transform because the item model's
             * visual center sits lower than the multi-coal arrangement.
             */
            ms.translate(0.0, -0.165, 0.045);
            ms.mulPose(Axis.ZP.rotationDegrees(-8.0F));
            ms.scale(0.23F, 0.23F, 0.23F);
         } else {
            /*
             * Preserve the existing two- and three-coal arrangements.
             */
            ms.translate(position[0], position[1], position[2]);
            ms.mulPose(
                    Axis.ZP.rotationDegrees((float) position[3])
            );
            ms.mulPose(
                    Axis.YP.rotationDegrees(
                            (float) (position[3] * 0.35)
                    )
            );
            ms.scale(0.20F, 0.20F, 0.20F);
         }

         Minecraft.getInstance()
                 .getItemRenderer()
                 .render(
                         fuelStack,
                         ItemDisplayContext.FIXED,
                         false,
                         ms,
                         buffer,
                         light,
                         overlay,
                         fuelModel
                 );

         ms.popPose();
      }
   }



   private static double[][] getCoalPositions(
           int visibleCount
   ) {
      return switch (visibleCount) {
         case 1 -> new double[][]{
                 {0.00, -0.215, 0.050, 0.0}
         };

         case 2 -> new double[][]{
                 {-0.10, -0.235, 0.020, -14.0},
                 {0.10, -0.235, 0.020, 14.0}
         };

         case 3 -> new double[][]{
                 {-0.11, -0.235, 0.000, -14.0},
                 {0.11, -0.235, 0.000, 14.0},
                 {0.00, -0.195, 0.110, 4.0}
         };

         default -> new double[][]{
                 {-0.10, -0.235, -0.010, -14.0},
                 {0.10, -0.235, -0.010, 14.0},
                 {-0.10, -0.205, 0.110, 8.0},
                 {0.10, -0.205, 0.110, -8.0}
         };
      };
   }

   private static double[][] getLogPositions(int visibleLogs) {
      return visibleLogs == 2
              ? new double[][]{
              {0.0, -0.205, -0.050}, // BACKLOG: Changed from -0.100 to -0.050 (moves it forward)
              {0.0, -0.205, 0.130}   // FRONT LOG: Changed from 0.180 to 0.130 (moves it backward)
      }
              : new double[][]{
              {0.0, -0.205, 0.040}   // Single center log
      };
   }

   private void renderRestingFuel(
           ItemStack fuelStack,
           BakedModel fuelModel,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           float scale
   ) {
      ms.pushPose();
      ms.translate(0.0, -0.22, 0.075);
      ms.mulPose(Axis.XP.rotationDegrees(90.0F));
      ms.scale(scale, scale, scale);

      Minecraft.getInstance()
              .getItemRenderer()
              .render(
                      fuelStack,
                      ItemDisplayContext.FIXED,
                      false,
                      ms,
                      buffer,
                      light,
                      overlay,
                      fuelModel
              );

      ms.popPose();
   }

   private void renderHeatingKindling(
           MoltenRotorBlockEntity furnace,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay
   ) {
      ItemStack stickStack =
              new ItemStack(net.minecraft.world.item.Items.STICK);

      Minecraft minecraft = Minecraft.getInstance();
      BakedModel stickModel = minecraft
              .getItemRenderer()
              .getModel(
                      stickStack,
                      furnace.getLevel(),
                      null,
                      0
              );

      if (stickModel
              == minecraft.getModelManager().getMissingModel()) {
         return;
      }

      /*
       * Kindling always stays on the chamber floor for every fuel type.
       * Logs are raised independently above it in renderLogFuelScene().
       */
      double kindlingY = -0.285;
      double kindlingDepth = 0.060;
      float kindlingScale = 0.12F;

      float[] rotations = {
              -38.0F,
              38.0F,
              90.0F
      };

      for (float rotation : rotations) {
         this.renderKindlingPiece(
                 stickStack,
                 stickModel,
                 ms,
                 buffer,
                 light,
                 overlay,
                 rotation,
                 kindlingY,
                 kindlingDepth,
                 kindlingScale
         );
      }
   }

   private void renderKindlingPiece(
           ItemStack stickStack,
           BakedModel stickModel,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           float rotation,
           double y,
           double depth,
           float scale
   ) {
      ms.pushPose();

      ms.translate(0.0, y, depth);
      ms.mulPose(Axis.YP.rotationDegrees(rotation));
      ms.mulPose(Axis.XP.rotationDegrees(90.0F));
      ms.scale(scale, scale, scale);

      Minecraft.getInstance()
              .getItemRenderer()
              .render(
                      stickStack,
                      ItemDisplayContext.FIXED,
                      false,
                      ms,
                      buffer,
                      light,
                      overlay,
                      stickModel
              );

      ms.popPose();
   }

   private void renderLogFuelScene(
           MoltenRotorBlockEntity furnace,
           ItemStack fuelStack,
           BakedModel fuelModel,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay
   ) {
      int visibleLogs = Math.clamp(
              furnace.getRenderedFuelUnitCount(),
              1,
              2
      );

      double[][] logPositions = getLogPositions(visibleLogs);
      List<ItemStack> renderedStickStacks =
              furnace.getRenderedLogStickStacks();
      int stickIndex = 0;

      Minecraft minecraft = Minecraft.getInstance();

      for (double[] logPos : logPositions) {
         ms.pushPose();

         ms.translate(
                 logPos[0],
                 logPos[1],
                 logPos[2]
         );

         ms.scale(0.28F, 0.28F, 0.28F);

         minecraft.getItemRenderer()
                 .render(
                         fuelStack,
                         ItemDisplayContext.FIXED,
                         false,
                         ms,
                         buffer,
                         light,
                         overlay,
                         fuelModel
                 );

         ms.popPose();

         if (stickIndex >= renderedStickStacks.size()) {
            continue;
         }

         double stickZ = logPos[2] - 0.015;

         ItemStack leftStickStack = renderedStickStacks.get(stickIndex++);
         this.renderLogTopStick(
                 leftStickStack,
                 minecraft.getItemRenderer().getModel(
                         leftStickStack,
                         furnace.getLevel(),
                         null,
                         0
                 ),
                 ms,
                 buffer,
                 light,
                 overlay,
                 logPos[0] - 0.040,
                 stickZ,
                 34.0F
         );

         if (stickIndex < renderedStickStacks.size()) {
            ItemStack rightStickStack =
                    renderedStickStacks.get(stickIndex++);
            this.renderLogTopStick(
                    rightStickStack,
                    minecraft.getItemRenderer().getModel(
                            rightStickStack,
                            furnace.getLevel(),
                            null,
                            0
                    ),
                    ms,
                    buffer,
                    light,
                    overlay,
                    logPos[0] + 0.040,
                    stickZ,
                    -34.0F
            );
         }
      }
   }

   private void renderLogTopStick(
           ItemStack stickStack,
           BakedModel stickModel,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay,
           double x,
           double z,
           float angle
   ) {
      if (stickStack.isEmpty()
              || stickModel == Minecraft.getInstance()
              .getModelManager()
              .getMissingModel()) {
         return;
      }

      ms.pushPose();
      ms.translate(x, -0.118, z);
      ms.mulPose(Axis.ZP.rotationDegrees(45.0F));
      ms.mulPose(Axis.ZP.rotationDegrees(angle));
      ms.scale(0.125F, 0.125F, 0.125F);

      Minecraft.getInstance()
              .getItemRenderer()
              .render(
                      stickStack,
                      ItemDisplayContext.FIXED,
                      false,
                      ms,
                      buffer,
                      light,
                      overlay,
                      stickModel
              );

      ms.popPose();
   }

   private void renderSpecialFuel(
           ItemStack fuelStack,
           BakedModel fuelModel,
           PoseStack ms,
           MultiBufferSource buffer,
           int light,
           int overlay
   ) {
      ms.pushPose();

      /*
       * GROUND already gives flat item models the correct seated
       * orientation. Do not add another 90-degree rotation.
       */
      ms.translate(0.0, -0.245, 0.078);
      ms.scale(0.48F, 0.48F, 0.48F);

      Minecraft.getInstance()
              .getItemRenderer()
              .render(
                      fuelStack,
                      ItemDisplayContext.GROUND,
                      false,
                      ms,
                      buffer,
                      light,
                      overlay,
                      fuelModel
              );

      ms.popPose();
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
      this.rotateToFacing(ms, facing);

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
      this.rotateToFacing(ms, facing);

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