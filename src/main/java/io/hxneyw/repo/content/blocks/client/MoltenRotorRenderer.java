package io.hxneyw.repo.content.blocks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.client.ClientModEvents;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import java.util.HashMap;
import java.util.Map;
import net.createmod.catnip.animation.AnimationTickHolder;
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
   private static final Map<MoltenRotorBlockEntity, Float> previousAngles = new HashMap<>();

   public MoltenRotorRenderer(Context ignoredContext) {
   }

   protected void renderSafe(MoltenRotorBlockEntity furnace, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
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

      float angle = this.getRotationAngle(furnace);
      this.renderRotatingShafts(furnace, ms, buffer, actualLight, overlay, facing, angle);
      this.renderHeatGaugeNeedle(furnace, ms, buffer, actualLight, overlay, facing);
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
      float targetAngle = tempPercent * 90.0F;
      if (tempPercent > 0.95F) {
         float time = AnimationTickHolder.getRenderTime(furnace.getLevel()) / 20.0F;
         float shakeIntensity = (tempPercent - 0.95F) / 0.05F;
         float rapidShake = Mth.sin(time * 30.0F) * 2.0F * shakeIntensity;
         float microShake = Mth.sin(time * 50.0F) * 0.5F * shakeIntensity;
         targetAngle += rapidShake + microShake;
         targetAngle = Mth.clamp(targetAngle, 87.0F, 93.0F);
      }

      float prevAngle = previousAngles.getOrDefault(furnace, targetAngle);
      float smoothingFactor = 0.15F;
      float interpolatedAngle = Mth.lerp(smoothingFactor, prevAngle, targetAngle);
      previousAngles.put(furnace, interpolatedAngle);
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
      return switch (furnace.getCurrentHeatTier()) {
         case NONE -> worldLight;
         case SMOULDERING, FADING -> Math.max(worldLight, 4210688);
         case KINDLED -> Math.max(worldLight, 8421376);
         case SEETHING -> Math.max(worldLight, 12615680);
         case RADIANT -> 15728880;
      };
   }

   private void renderRotatingShafts(
      MoltenRotorBlockEntity furnace, PoseStack ms, MultiBufferSource buffer, int light, int overlay, Direction facing, float angle
   ) {
      try {
         BakedModel leftShaftModel = ClientModEvents.ROTOR_SHAFT_LEFT.get();
         BakedModel rightShaftModel = ClientModEvents.ROTOR_SHAFT_RIGHT.get();
         boolean foundModels = false;
         net.minecraft.core.Direction.Axis shaftAxis = facing.getCounterClockWise().getAxis();
         float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(furnace, furnace.getBlockPos(), shaftAxis);
         angle += offset;
         float adjustedAngle = facing != Direction.WEST && facing != Direction.SOUTH ? angle : -angle;
         if (leftShaftModel != null && leftShaftModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
            this.renderShaft(ms, buffer, leftShaftModel, light, overlay, adjustedAngle, facing, true);
            foundModels = true;
         }

         if (rightShaftModel != null && rightShaftModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
            this.renderShaft(ms, buffer, rightShaftModel, light, overlay, adjustedAngle, facing, false);
            foundModels = true;
         }

         if (!hasLoggedOnce) {
            CreateSulfuricResonance.LOGGER.info(foundModels ? "✓ Successfully loaded shaft models" : "✗ Could not find shaft models!");
            hasLoggedOnce = true;
         }
      } catch (Exception var14) {
         if (!hasLoggedOnce) {
            CreateSulfuricResonance.LOGGER.error("Error loading shaft models: {}", var14.getMessage());
            hasLoggedOnce = true;
         }
      }
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
      float angle = this.getRotationAngle(furnace);
      float adjustedAngle = facing != Direction.NORTH && facing != Direction.EAST ? angle : -angle;
      if (adjustedAngle != 0.0F) {
         ms.mulPose(Axis.ZP.rotationDegrees(adjustedAngle));
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

   private float getRotationAngle(MoltenRotorBlockEntity furnace) {
      if (furnace.getLevel() == null) {
         return 0.0F;
      } else {
         float speed = furnace.getSpeed();
         if (speed == 0.0F) {
            return 0.0F;
         } else {
            float time = AnimationTickHolder.getRenderTime(furnace.getLevel());
            return time * speed * 3.0F / 10.0F % 360.0F;
         }
      }
   }

   private void renderShaft(
      PoseStack ms, MultiBufferSource buffer, BakedModel model, int light, int overlay, float angle, Direction facing, boolean isLeftShaft
   ) {
      ms.pushPose();
      ms.translate(0.5, 0.5, 0.5);
      this.rotateToFacing(ms, facing);

      ms.translate(-0.5, -0.5, -0.5);
      double shaftCenterX = isLeftShaft ? 0.0625 : 0.9375;
      double shaftCenterY = 0.5;
      double shaftCenterZ = 0.5;
      ms.translate(shaftCenterX, shaftCenterY, shaftCenterZ);
      ms.mulPose(Axis.XP.rotation(angle / 180.0F * (float) Math.PI));
      ms.translate(-shaftCenterX, -shaftCenterY, -shaftCenterZ);
      this.renderModel(ms, buffer, model, light, overlay);
      ms.popPose();
   }
}
