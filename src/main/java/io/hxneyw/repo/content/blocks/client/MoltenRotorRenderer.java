package io.hxneyw.repo.content.blocks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.client.ClientModEvents;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import static com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getRotationOffsetForPosition;

public class MoltenRotorRenderer extends SafeBlockEntityRenderer<MoltenRotorBlockEntity> {
    private static boolean hasLoggedOnce = false;

    public MoltenRotorRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    protected void renderSafe(MoltenRotorBlockEntity furnace, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = furnace.getBlockState();
        Direction facing = state.getValue(io.hxneyw.repo.content.blocks.MoltenRotorBlock.FACING);
        int actualLight = calculateLight(furnace, light);

        // Render the impeller
        ItemStack impellerItem = new ItemStack(io.hxneyw.repo.content.Items.INFERNAL_IMPELLER.get());
        if (!impellerItem.isEmpty()) {
            BakedModel itemModel = Minecraft.getInstance().getItemRenderer()
                    .getModel(impellerItem, furnace.getLevel(), null, 0);
            if (itemModel != null && itemModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                renderImpellerFromItem(furnace, ms, buffer, actualLight, overlay, facing, partialTicks, itemModel);
            }
        }

        // Render rotating shafts
        float angle = getRotationAngle(furnace, partialTicks);
        renderRotatingShafts(furnace, ms, buffer, actualLight, overlay, facing, angle);
    }

    private int calculateLight(MoltenRotorBlockEntity furnace, int worldLight) {
        MoltenRotorBlockEntity.RotorHeatLevel heatLevel = furnace.getCurrentHeatTier();
        return switch (heatLevel) {
            case NONE -> worldLight;
            case SMOULDERING, FADING -> Math.max(worldLight, 0x404000);
            case KINDLED -> Math.max(worldLight, 0x808000);
            case SEETHING -> Math.max(worldLight, 0xC08000);
            case RADIANT -> 0xF000F0;
        };
    }

    private void renderRotatingShafts(MoltenRotorBlockEntity furnace, PoseStack ms, MultiBufferSource buffer, int light, int overlay, Direction facing, float angle) {
        try {
            BakedModel leftShaftModel = ClientModEvents.ROTOR_SHAFT_LEFT.get();
            BakedModel rightShaftModel = ClientModEvents.ROTOR_SHAFT_RIGHT.get();
            boolean foundModels = false;

            // Apply the Create offset system for perfect alignment
            Direction.Axis shaftAxis = facing.getCounterClockWise().getAxis();
            float offset = getRotationOffsetForPosition(furnace, furnace.getBlockPos(), shaftAxis);
            angle += offset;

            float adjustedAngle = switch (facing) {
                case NORTH -> angle;
                case WEST -> -angle;
                case EAST -> angle;
                case SOUTH -> -angle;
                default -> angle;
            };

            if (leftShaftModel != null && leftShaftModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                renderShaft(ms, buffer, leftShaftModel, light, overlay, adjustedAngle, facing, true);
                foundModels = true;
            }

            if (rightShaftModel != null && rightShaftModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                renderShaft(ms, buffer, rightShaftModel, light, overlay, adjustedAngle, facing, false);
                foundModels = true;
            }

            if (!hasLoggedOnce) {
                if (foundModels) {
                    CreateSulfuricResonance.LOGGER.info("✓ Successfully loaded shaft models");
                } else {
                    CreateSulfuricResonance.LOGGER.warn("✗ Could not find shaft models!");
                }
                hasLoggedOnce = true;
            }
        } catch (Exception e) {
            if (!hasLoggedOnce) {
                CreateSulfuricResonance.LOGGER.error("Error loading shaft models: {}", e.getMessage());
                hasLoggedOnce = true;
            }
        }
    }

    private void renderImpellerFromItem(MoltenRotorBlockEntity furnace, PoseStack ms, MultiBufferSource buffer, int light, int overlay, Direction facing, float partialTicks, BakedModel model) {
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);

        switch (facing) {
            case NORTH: break;
            case SOUTH: ms.mulPose(Axis.YP.rotationDegrees(180f)); break;
            case EAST: ms.mulPose(Axis.YP.rotationDegrees(-90f)); break;
            case WEST: ms.mulPose(Axis.YP.rotationDegrees(90f)); break;
            default: break;
        }

        double localX = (7.9925 / 16.0) - 0.5 + (3.2 / 16.0);
        double localY = (7.6925 / 16.0) - 0.5 + (3.2 / 16.0);
        double localZ = (11.3 / 16.0) - 0.5 + (3.1 / 16.0);
        ms.translate(localX, localY, localZ);

        float scale = 0.4f;
        ms.scale(scale, scale, scale);
        ms.translate(-0.5, -0.5, -0.46875);

        float angle = getRotationAngle(furnace, partialTicks);

        // Apply directional adjustment for impeller (same as shafts)
        float adjustedAngle = switch (facing) {
            case NORTH -> -angle;
            case WEST -> angle;
            case EAST -> -angle;
            case SOUTH -> angle;
            default -> angle;
        };

        if (adjustedAngle != 0) {
            ms.mulPose(Axis.ZP.rotationDegrees(adjustedAngle));
        }

        Minecraft.getInstance().getItemRenderer().render(
                new ItemStack(io.hxneyw.repo.content.Items.INFERNAL_IMPELLER.get()),
                ItemDisplayContext.NONE, false, ms, buffer, light, overlay, model
        );

        ms.popPose();
    }

    private float getRotationAngle(MoltenRotorBlockEntity furnace, float partialTicks) {
        if (furnace.getLevel() == null) return 0;

        float speed = furnace.getSpeed();
        if (speed == 0) return 0;

        // Use Create's AnimationTickHolder instead of game time
        float time = AnimationTickHolder.getRenderTime(furnace.getLevel());
        float angle = (time * speed * 3f / 10f) % 360;
        return angle;
    }

    private void renderShaft(PoseStack ms, MultiBufferSource buffer, BakedModel model, int light, int overlay, float angle, Direction facing, boolean isLeftShaft) {
        ms.pushPose();

        // Apply block facing rotation at the origin
        ms.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH -> ms.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> ms.mulPose(Axis.YP.rotationDegrees(-90));
            case WEST -> ms.mulPose(Axis.YP.rotationDegrees(90));
            default -> {} // NORTH
        }
        ms.translate(-0.5, -0.5, -0.5);

        // Move to shaft center
        double shaftCenterX = isLeftShaft ? 0.0625 : 0.9375;
        double shaftCenterY = 0.5;
        double shaftCenterZ = 0.5;
        ms.translate(shaftCenterX, shaftCenterY, shaftCenterZ);

        // Convert angle to radians (modifier already applied in renderRotatingShafts)
        float angleRad = angle / 180f * (float) Math.PI;

        // Rotate around X-axis (the shaft's length in model space)
        ms.mulPose(Axis.XP.rotation(angleRad));
        ms.translate(-shaftCenterX, -shaftCenterY, -shaftCenterZ);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create();
        ModelData modelData = ModelData.EMPTY;

        for (var quad : model.getQuads(null, null, random, modelData, RenderType.solid())) {
            vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, overlay);
        }

        for (Direction dir : Direction.values()) {
            for (var quad : model.getQuads(null, dir, random, modelData, RenderType.solid())) {
                vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, overlay);
            }
        }

        ms.popPose();
    }
}