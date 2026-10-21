package io.hxneyw.repo.client;

import java.util.Map;
import java.util.WeakHashMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class BlockCaddyBeltRenderer {
    private record ModelCenter(float x, float z) {}

    private static final Map<BakedModel, ModelCenter> STANDALONE_MODEL_CENTERS = new WeakHashMap<>();

    private BlockCaddyBeltRenderer() {}

    public static void renderCargo(
            ItemStack caddy,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        // Fill the 15-pixel opening between the tray walls.
        renderCargo(caddy, poseStack, buffer, light, overlay, 15f / 16f, true);
    }

    public static void renderCargoItem(
            ItemStack caddy,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        // ItemRendererMixin reproduces ItemRenderer's display transform and
        // its -0.5 model-origin shift. Cargo must stay in block-local [0, 1]
        // coordinates so that shared shift centers it exactly with the Caddy.
        renderCargo(caddy, poseStack, buffer, light, overlay, 15f / 16f, false);
    }

    public static void renderCargoOnBlock(
            ItemStack caddy,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        renderCargo(caddy, poseStack, buffer, light, overlay, 15f / 16f, false);
    }

    private static void renderCargo(
            ItemStack caddy,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            float cargoSize,
            boolean centeredOnOrigin
    ) {
        BlockState state = BlockCaddyItem.getCargoBlockState(caddy);
        if (state.isAir()) {
            return;
        }

        poseStack.pushPose();
        float inset = (1f - cargoSize) / 2f;
        // Seat the cargo above the tray floor, whose rim is 1.1 model pixels tall.
        poseStack.translate(centeredOnOrigin ? -cargoSize / 2f : inset, 1.1f / 16f,
                centeredOnOrigin ? -cargoSize / 2f : inset);
        // The carrier bed is 1.1 pixels above the block base. A 14-pixel
        // vertical cargo span leaves the top flush with the one-pixel rim
        // instead of letting a full 15-pixel cube poke above the frame.
        poseStack.scale(cargoSize, 14f / 16f, cargoSize);
        Minecraft minecraft = Minecraft.getInstance();
        boolean renderedByEntity = state.getRenderShape()
                == net.minecraft.world.level.block.RenderShape.ENTITYBLOCK_ANIMATED;
        ItemStack cargoStack = BlockCaddyItem.getCargoStack(caddy);
        BlockEntity cargoEntity = null;
        BlockEntityRenderer<BlockEntity> cargoRenderer = null;
        if (minecraft.level != null && state.hasBlockEntity()
                && state.getBlock() instanceof EntityBlock entityBlock) {
            cargoEntity = entityBlock.newBlockEntity(BlockPos.ZERO, state);
            if (cargoEntity != null) {
                cargoEntity.setLevel(minecraft.level);
                cargoRenderer = getRenderer(cargoEntity);
            }
        }
        boolean hasDynamicModel = renderedByEntity && cargoRenderer != null;
        // Use each BlockItem's baked item model for cargo. Rendering every
        // blockstate with ModelData.EMPTY loses multipart/model-data-driven
        // geometry (notably Create's unattached fluid pipe) and can leave cargo
        // invisible with alternate renderers such as Sodium. Entity-rendered
        // blocks use their BER in-world, but still need this item-model fallback
        // for inventory/GUI previews where no level or block entity exists.
        if (!hasDynamicModel) {
            var itemRenderer = minecraft.getItemRenderer();
            BakedModel cargoModel = itemRenderer.getModel(cargoStack, minecraft.level, null, 0);
            centerStandaloneModel(cargoModel, poseStack);
            // ItemRenderer shifts item-model vertices by -0.5 on every axis.
            // Cargo is already positioned in the Caddy's block-local space, so
            // cancel that shift before asking ItemRenderer to draw pipe models.
            poseStack.translate(0.5f, 0.5f, 0.5f);
            itemRenderer.render(
                    cargoStack,
                    ItemDisplayContext.NONE,
                    false,
                    poseStack,
                    buffer,
                    light,
                    overlay,
                    cargoModel
            );
        }

        // Kinetic blocks use their baked item model plus the dynamic entity renderer.
        if (cargoEntity != null && cargoRenderer != null) {
            cargoRenderer.render(cargoEntity, 0.0F, poseStack, buffer, light, overlay);
            if (cargoRenderer instanceof KineticBlockEntityRenderer<?>
                    && VisualizationManager.supportsVisualization(minecraft.level)) {
                renderFlywheelShaft(state, cargoEntity, poseStack, buffer, light, overlay);
            }
        }
        poseStack.popPose();
    }

    private static void centerStandaloneModel(BakedModel model, PoseStack poseStack) {
        ModelCenter center = STANDALONE_MODEL_CENTERS.computeIfAbsent(
                model,
                BlockCaddyBeltRenderer::findStandaloneModelCenter
        );
        poseStack.translate(0.5f - center.x(), 0, 0.5f - center.z());
    }

    private static ModelCenter findStandaloneModelCenter(BakedModel model) {
        return findModelCenter(model, null);
    }

    private static ModelCenter findModelCenter(BakedModel model, BlockState state) {
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        RandomSource random = RandomSource.create(0);
        RenderType[] renderTypes = {
                RenderType.solid(),
                RenderType.cutout(),
                RenderType.cutoutMipped(),
                RenderType.translucent()
        };

        for (RenderType renderType : renderTypes) {
            for (Direction direction : Direction.values()) {
                for (var quad : model.getQuads(state, direction, random, ModelData.EMPTY, renderType)) {
                    int[] vertices = quad.getVertices();
                    for (int vertex = 0; vertex < 4; vertex++) {
                        int offset = vertex * 8;
                        float x = Float.intBitsToFloat(vertices[offset]);
                        float z = Float.intBitsToFloat(vertices[offset + 2]);
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minZ = Math.min(minZ, z);
                        maxZ = Math.max(maxZ, z);
                    }
                }
            }

            for (var quad : model.getQuads(state, null, random, ModelData.EMPTY, renderType)) {
                int[] vertices = quad.getVertices();
                for (int vertex = 0; vertex < 4; vertex++) {
                    int offset = vertex * 8;
                    float x = Float.intBitsToFloat(vertices[offset]);
                    float z = Float.intBitsToFloat(vertices[offset + 2]);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minZ = Math.min(minZ, z);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }

        if (!Float.isFinite(minX) || !Float.isFinite(minZ)) {
            return new ModelCenter(0.5f, 0.5f);
        }
        return new ModelCenter((minX + maxX) / 2f, (minZ + maxZ) / 2f);
    }

    private static void renderFlywheelShaft(
            BlockState state,
            BlockEntity cargoEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (!(state.getBlock() instanceof IRotate kineticBlock)) {
            return;
        }

        Direction.Axis axis = kineticBlock.getRotationAxis(state);
        boolean shaftPresent = false;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis
                    && kineticBlock.hasShaftTowards(
                            cargoEntity.getLevel(),
                            BlockPos.ZERO,
                            state,
                            direction
                    )) {
                shaftPresent = true;
                break;
            }
        }
        if (!shaftPresent) {
            return;
        }

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                KineticBlockEntityRenderer.shaft(axis),
                poseStack,
                buffer,
                light,
                overlay
        );
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityRenderer<BlockEntity> getRenderer(BlockEntity blockEntity) {
        return (BlockEntityRenderer<BlockEntity>) Minecraft.getInstance()
                .getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
    }
}
