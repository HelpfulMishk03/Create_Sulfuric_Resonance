package io.hxneyw.repo.content.blocks.moltenrotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.client.ClientModEvents;
import io.hxneyw.repo.content.Items;
import net.createmod.catnip.animation.AnimationTickHolder;

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
import net.minecraft.world.level.block.entity.BlockEntity;
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
      ms.mulPose(
              Axis.YP.rotationDegrees(
                      180.0F - facing.toYRot()
              )
      );
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
      float currentTemp = Mth.clamp(
              furnace.getExactTemperature(),
              20.0F,
              1599.0F
      );
      float tempPercent = Mth.clamp(
              (currentTemp - 20.0F) / 1579.0F,
              0.0F,
              1.0F
      );
      float targetAngle = tempPercent * 90.0F;
      float previousAngle = previousAngles.getOrDefault(
              furnace,
              targetAngle
      );
      float currentAngle = Math.abs(targetAngle - previousAngle) < 0.01F
              ? targetAngle
              : Mth.lerp(
                      0.15F,
                      previousAngle,
                      targetAngle
              );

      previousAngles.put(furnace, currentAngle);

      if (currentTemp >= 1599.0F
              && furnace.getLevel() != null) {
         float renderTime =
                 AnimationTickHolder.getRenderTime(
                         furnace.getLevel()
                 );

         float rapidShake =
                 Mth.sin(renderTime * 0.75F) * 1.15F;

         float microShake =
                 Mth.sin(renderTime * 1.35F) * 0.35F;

         return Mth.clamp(
                 90.0F + rapidShake + microShake,
                 88.5F,
                 91.5F
         );
      }

      return currentAngle;
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
                         this.getConnectedShaftAngle(
                                 furnace,
                                 leftDirection,
                                 shaftAxis
                         ),
                         facing
                 );

         float rightAngleRadians =
                 this.toLocalShaftAngle(
                         this.getConnectedShaftAngle(
                                 furnace,
                                 rightDirection,
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


   private float getConnectedShaftAngle(
           MoltenRotorBlockEntity furnace,
           Direction direction,
           Direction.Axis shaftAxis
   ) {
      if (furnace.getLevel() != null) {
         BlockEntity adjacentBlockEntity =
                 furnace.getLevel().getBlockEntity(
                         furnace.getBlockPos().relative(direction)
                 );

         if (adjacentBlockEntity
                 instanceof KineticBlockEntity adjacentKinetic
                 && KineticBlockEntityRenderer
                 .getRotationAxisOf(adjacentKinetic)
                 == shaftAxis) {
            return KineticBlockEntityRenderer.getAngleForBe(
                    adjacentKinetic,
                    adjacentKinetic.getBlockPos(),
                    shaftAxis
            );
         }
      }

      return KineticBlockEntityRenderer.getAngleForBe(
              furnace,
              furnace.getBlockPos(),
              shaftAxis
      );
   }


   private float toLocalShaftAngle(
           float worldAxisAngleRadians,
           Direction facing
   ) {

      return facing == Direction.SOUTH
              || facing == Direction.WEST
              ? -worldAxisAngleRadians
              : worldAxisAngleRadians;
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

      if (elapsedTicks < 0.0D || elapsedTicks > 20.0D) {
         elapsedTicks = 0.0D;
      }

      rotationState.lastRenderTime = renderTime;

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