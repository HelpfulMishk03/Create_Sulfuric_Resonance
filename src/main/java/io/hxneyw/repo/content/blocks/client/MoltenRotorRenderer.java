package io.hxneyw.repo.content.blocks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class MoltenRotorRenderer extends SafeBlockEntityRenderer<MoltenRotorBlockEntity> {

    private static boolean hasLoggedOnce = false;

    public MoltenRotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(MoltenRotorBlockEntity furnace, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {

        BlockState state = furnace.getBlockState();
        Direction facing = state.getValue(io.hxneyw.repo.content.blocks.MoltenRotorBlock.FACING);

        int actualLight = calculateLight(furnace, light);

        ItemStack impellerItem = new ItemStack(io.hxneyw.repo.content.Items.INFERNAL_IMPELLER.get());

        if (!impellerItem.isEmpty()) {
            BakedModel itemModel = Minecraft.getInstance().getItemRenderer()
                    .getModel(impellerItem, furnace.getLevel(), null, 0);

            if (itemModel != null && itemModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                renderImpellerFromItem(furnace, ms, buffer, actualLight, overlay, facing, partialTicks, itemModel);
                return;
            }
        }

        ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "impeller");

        try {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                    ModelResourceLocation.standalone(modelLocation)
            );

            if (model != null && model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                renderImpellerFromModel(furnace, ms, buffer, actualLight, overlay, state, facing, partialTicks, model);
                return;
            }
        } catch (Exception e) {
            // Silently fail
        }

        if (!hasLoggedOnce) {
            CreateSulfuricResonance.LOGGER.error("========================================");
            CreateSulfuricResonance.LOGGER.error("IMPELLER MODEL NOT FOUND!");
            CreateSulfuricResonance.LOGGER.error("Tried:");
            CreateSulfuricResonance.LOGGER.error("  1. Item model from INFERNAL_IMPELLER item");
            CreateSulfuricResonance.LOGGER.error("  2. Standalone model at: sulfuricresonance:impeller");
            CreateSulfuricResonance.LOGGER.error("Expected file: assets/sulfuricresonance/models/item/impeller.json");
            CreateSulfuricResonance.LOGGER.error("========================================");
            hasLoggedOnce = true;
        }
    }

    private int calculateLight(MoltenRotorBlockEntity furnace, int worldLight) {
        MoltenRotorBlockEntity.RotorHeatLevel heatLevel = furnace.getCurrentHeatTier();

        // Base the light level on heat tier, not always max brightness
        return switch (heatLevel) {
            case NONE -> worldLight;  // Use ambient light when off
            case SMOULDERING, FADING -> Math.max(worldLight, 0x404000);  // Dim glow
            case KINDLED -> Math.max(worldLight, 0x808000);  // Medium glow
            case SEETHING -> Math.max(worldLight, 0xC08000);  // Bright orange glow
            case RADIANT -> 0xF000F0;  // Maximum brightness at highest tier
        };
    }

    private void renderImpellerFromItem(MoltenRotorBlockEntity furnace, PoseStack ms,
                                        MultiBufferSource buffer, int light, int overlay,
                                        Direction facing, float partialTicks, BakedModel model) {
        ms.pushPose();

        // CONNECTION PART center in NORTH-facing furnace (pixel coordinates):
        // X: 7.9925, Y: 7.6925, Z: 11.3
        // In block units: X: 0.4995, Y: 0.4808, Z: 0.7063

        // STEP 1: Move to block center
        ms.translate(0.5, 0.5, 0.5);

        // STEP 2: Rotate for furnace facing - rotates coordinate system
        switch (facing) {
            case NORTH:
                break;
            case SOUTH:
                ms.mulPose(Axis.YP.rotationDegrees(180f));
                break;
            case EAST:
                ms.mulPose(Axis.YP.rotationDegrees(-90f));
                break;
            case WEST:
                ms.mulPose(Axis.YP.rotationDegrees(90f));
                break;
            default:
                break;
        }

        // STEP 3: Move to connection part in LOCAL space (relative to block center)
        // Connection part center: X=7.9925px, Y=7.6925px, Z=11.3px

        double localX = (7.9925 / 16.0) - 0.5 + (3.2 / 16.0);  // Left 3.2 pixels
        double localY = (7.6925 / 16.0) - 0.5 + (3.2 / 16.0);  // Up 3.2 pixels
        double localZ = (11.3 / 16.0) - 0.5 + (3.1 / 16.0);    // Back 3.1 pixels
        ms.translate(localX, localY, localZ);

        // STEP 4: Scale FIRST before centering pivot
        float scale = 0.4f;
        ms.scale(scale, scale, scale);

        // STEP 5: Center on the impeller's pivot point [8, 8, 7.5] = [0.5, 0.5, 0.46875]
        // This moves the impeller so its pivot is at the origin (0,0,0) in local space
        ms.translate(-0.5, -0.5, -0.46875);

        // STEP 6: NOW rotate - FIXED to use actual kinetic network speed
        float angle = getRotationAngle(furnace, partialTicks);
        if (angle != 0) {
            ms.mulPose(Axis.ZP.rotationDegrees(angle));
        }

        // STEP 7: Render
        Minecraft.getInstance().getItemRenderer().render(
                new ItemStack(io.hxneyw.repo.content.Items.INFERNAL_IMPELLER.get()),
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

    private void renderImpellerFromModel(MoltenRotorBlockEntity furnace, PoseStack ms,
                                         MultiBufferSource buffer, int light, int overlay,
                                         BlockState state, Direction facing, float partialTicks,
                                         BakedModel model) {
        ms.pushPose();

        ms.translate(0.5, 0.5, 0.5);

        switch (facing) {
            case NORTH:
                break;
            case SOUTH:
                ms.mulPose(Axis.YP.rotationDegrees(180f));
                break;
            case EAST:
                ms.mulPose(Axis.YP.rotationDegrees(-90f));
                break;
            case WEST:
                ms.mulPose(Axis.YP.rotationDegrees(90f));
                break;
            default:
                break;
        }

        double localX = (7.9925 / 16.0) - 0.5 + (3.2 / 16.0);  // Left 3.2 pixels
        double localY = (7.6925 / 16.0) - 0.5 + (3.2 / 16.0);  // Up 3.2 pixels
        double localZ = (11.3 / 16.0) - 0.5 + (3.1 / 16.0);    // Back 3.1 pixels
        ms.translate(localX, localY, localZ);

        float scale = 0.4f;
        ms.scale(scale, scale, scale);

        ms.translate(-0.5, -0.5, -0.46875);

        float angle = getRotationAngle(furnace, partialTicks);
        if (angle != 0) {
            ms.mulPose(Axis.ZP.rotationDegrees(angle));
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create();
        ModelData modelData = ModelData.EMPTY;

        for (var quad : model.getQuads(state, null, random, modelData, RenderType.solid())) {
            vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, overlay);
        }

        for (Direction dir : Direction.values()) {
            for (var quad : model.getQuads(state, dir, random, modelData, RenderType.solid())) {
                vertexConsumer.putBulkData(ms.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, overlay);
            }
        }

        ms.popPose();
    }

    /**
     * FIXED: Calculate rotation angle from ACTUAL kinetic network speed
     * Now responds to both self-generated speed AND externally applied rotation
     */
    private float getRotationAngle(MoltenRotorBlockEntity furnace, float partialTicks) {
        if (furnace.getLevel() == null) return 0;

        // Get the ACTUAL speed from the kinetic network (includes external rotation)
        float speed = furnace.getSpeed();

        // If there's no network speed, fall back to generated speed (when acting as generator)
        if (speed == 0) {
            speed = furnace.getGeneratedSpeed();
        }

        if (speed == 0) return 0;

        long time = furnace.getLevel().getGameTime();

        // Convert RPM to degrees per tick
        // RPM = rotations per minute
        // 1 minute = 60 seconds = 1200 ticks (at 20 ticks/second)
        // degrees per tick = (RPM × 360°) / 1200
        float degreesPerTick = (speed * 360f) / 1200f;

        return (time + partialTicks) * degreesPerTick;
    }
}