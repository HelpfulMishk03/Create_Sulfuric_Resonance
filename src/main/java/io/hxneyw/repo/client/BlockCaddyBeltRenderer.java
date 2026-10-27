package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import io.hxneyw.repo.mixin.ShulkerBoxAnimationAccessor;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;

/** Renders carried cargo using its placed block model, including multipart blocks. */
public final class BlockCaddyBeltRenderer {
    private static final ThreadLocal<Boolean> BELT_CADDY_RENDER =
            ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> CARGO_RENDER_COUNT =
            ThreadLocal.withInitial(() -> 0);
    private static final Map<Object, CargoRendererState> CARGO_RENDERERS = new IdentityHashMap<>();
    private static long lastRendererSweep;

    @SuppressWarnings("unused")
    private BlockCaddyBeltRenderer() {}

    public static void setBeltCaddyRender(boolean rendering) {
        BELT_CADDY_RENDER.set(rendering);
    }

    public static int cargoRenderCount() {
        return CARGO_RENDER_COUNT.get();
    }

    public static void renderCargoItem(ItemStack caddy, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        renderCargoItem(caddy, poseStack, buffer, light, overlay,
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
    }

    public static void renderCargoItem(ItemStack caddy, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay, float partialTick) {
        CARGO_RENDER_COUNT.set(CARGO_RENDER_COUNT.get() + 1);
        if (BELT_CADDY_RENDER.get()) {
            BlockState cargoState = BlockCaddyItem.getCargoBlockState(caddy);
            var level = Minecraft.getInstance().level;
            if (level != null) {
                light = LightTexture.pack(Math.max(
                        cargoState.getLightEmission(level, BlockPos.ZERO), 8), 15);
            }
        }
        renderCargo(caddy, poseStack, buffer, light, overlay, partialTick, caddy);
    }

    public static void renderCargoOnBlock(ItemStack caddy, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay, float partialTick,
            Object rendererKey) {
        renderCargo(caddy, poseStack, buffer, light, overlay, partialTick, rendererKey);
    }

    @SuppressWarnings("DataFlowIssue")
    private static void renderCargo(ItemStack caddy, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay,
            float partialTick, Object rendererKey) {
        BlockState state = BlockCaddyItem.getCargoBlockState(caddy);
        if (state.isAir()) return;

        var renderer = Minecraft.getInstance().getBlockRenderer();
        float cargoSize = 15f / 16f;
        float inset = (1f - cargoSize) / 2f;
        poseStack.pushPose();
        poseStack.translate(inset, 1.1f / 16f, inset);

        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            BlockState lower = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                    DoubleBlockHalf.LOWER);
            BlockState upper = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                    DoubleBlockHalf.UPPER);
            poseStack.pushPose();
            poseStack.scale(cargoSize, 7f / 16f, cargoSize);
            renderer.renderSingleBlock(lower, poseStack, buffer, light, overlay, ModelData.EMPTY, null);
            poseStack.translate(0, 1, 0);
            renderer.renderSingleBlock(upper, poseStack, buffer, light, overlay, ModelData.EMPTY, null);
            poseStack.popPose();
        } else {
            poseStack.scale(cargoSize, 14f / 16f, cargoSize);
            if (state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED) {
                renderer.renderSingleBlock(state, poseStack, buffer, light, overlay, ModelData.EMPTY, null);
            }
        }
        // Entity-animated blocks (for example chests and shulker boxes) have
        // no baked block model. Kinetic cargo also needs its Create renderer
        // so shafts and moving parts remain visible while carried.
        renderCargoBlockEntity(caddy, state, poseStack, buffer, light, overlay,
                partialTick, rendererKey);
        poseStack.popPose();
    }

    @SuppressWarnings("DataFlowIssue")
    private static void renderCargoBlockEntity(ItemStack caddy, BlockState state,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay,
            float partialTick, Object rendererKey) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(state.getBlock() instanceof EntityBlock entityBlock)
                || !state.hasBlockEntity()) return;
        long gameTime = minecraft.level == null ? System.nanoTime() / 50_000_000L
                : minecraft.level.getGameTime();
        if (minecraft.level != null && gameTime - lastRendererSweep >= 200) {
            lastRendererSweep = gameTime;
            long staleBefore = gameTime - 1200;
            CARGO_RENDERERS.values().removeIf(renderer -> renderer.lastUsedTick < staleBefore);
        }
        CargoRendererState cached = CARGO_RENDERERS.get(rendererKey);
        if (cached == null || !cached.state.equals(state)) {
            BlockEntity cargoEntity = entityBlock.newBlockEntity(BlockPos.ZERO, state);
            if (cargoEntity == null) return;
            if (minecraft.level != null
                    && !(cargoEntity instanceof ChestBlockEntity)
                    && !(cargoEntity instanceof ShulkerBoxBlockEntity)) {
                cargoEntity.setLevel(minecraft.level);
            }
            ItemStack cargo = BlockCaddyItem.getCargoStack(caddy);
            CustomData data = cargo.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data != null && minecraft.level != null) {
                cargoEntity.loadWithComponents(data.copyTag(), minecraft.level.registryAccess());
            }
            cached = new CargoRendererState(state, cargoEntity);
            CARGO_RENDERERS.put(rendererKey, cached);
        }
        cached.lastUsedTick = gameTime;
        BlockEntity cargoEntity = cached.entity;
        updateStorageAnimation(caddy, state, cached, gameTime, minecraft);
        BlockEntityRenderer<BlockEntity> blockEntityRenderer = getRenderer(cargoEntity);
        if (blockEntityRenderer == null) return;
        if (state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
            blockEntityRenderer.render(cargoEntity, partialTick, poseStack, buffer, light, overlay);
            return;
        }
        if (state.getRenderShape() == RenderShape.MODEL
                && !cached.hasBakedGeometry(minecraft)) {
            blockEntityRenderer.render(cargoEntity, partialTick, poseStack, buffer, light, overlay);
            return;
        }
        if (state.getBlock() instanceof IRotate kineticBlock
                && cargoEntity instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity
        ) {
            if (blockEntityRenderer instanceof KineticBlockEntityRenderer<?>) {
                blockEntityRenderer.render(cargoEntity, partialTick, poseStack, buffer, light, overlay);
                if (minecraft.level == null) return;
                Direction.Axis axis = kineticBlock.getRotationAxis(state);
                boolean shaftPresent = false;
                for (Direction direction : Direction.values()) {
                    if (direction.getAxis() == axis
                            && kineticBlock.hasShaftTowards(minecraft.level, BlockPos.ZERO, state, direction)) {
                        shaftPresent = true;
                        break;
                    }
                }
                if (shaftPresent) {
                    minecraft.getBlockRenderer().renderSingleBlock(
                            KineticBlockEntityRenderer.shaft(axis), poseStack, buffer, light, overlay,
                            ModelData.EMPTY, null);
                }
            } else {
                // CSR's custom shaft renderers are regular block-entity renderers.
                blockEntityRenderer.render(cargoEntity, partialTick, poseStack, buffer, light, overlay);
            }
        }
    }

    private static void updateStorageAnimation(ItemStack caddy, BlockState state,
            CargoRendererState cached, long gameTime, Minecraft minecraft) {
        if (cached.lastAnimationTick == gameTime) return;
        cached.lastAnimationTick = gameTime;
        boolean open = BlockCaddyItem.isStorageCargoOpen(caddy);
        if (cached.entity instanceof ChestBlockEntity chest) {
            if (cached.open != open) {
                chest.triggerEvent(1, open ? 1 : 0);
                cached.open = open;
            }
            ChestBlockEntity.lidAnimateTick(minecraft.level, BlockPos.ZERO, state, chest);
        } else if (cached.entity instanceof ShulkerBoxBlockEntity shulker) {
            if (cached.open != open) {
                shulker.triggerEvent(1, open ? 1 : 0);
                cached.open = open;
            }
            ShulkerBoxAnimationAccessor animation =
                    (ShulkerBoxAnimationAccessor) shulker;
            float progress = shulker.getProgress(1);
            float next = open ? Math.min(1, progress + 0.1f) : Math.max(0, progress - 0.1f);
            animation.sulfuricresonance$setProgressOld(progress);
            animation.sulfuricresonance$setProgress(next);
            if (next >= 1) {
                animation.sulfuricresonance$setAnimationStatus(
                        ShulkerBoxBlockEntity.AnimationStatus.OPENED);
            } else if (next <= 0) {
                animation.sulfuricresonance$setAnimationStatus(
                        ShulkerBoxBlockEntity.AnimationStatus.CLOSED);
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class CargoRendererState {
        private final BlockState state;
        private final BlockEntity entity;
        private long lastUsedTick;
        private long lastAnimationTick = Long.MIN_VALUE;
        private boolean open;
        private Boolean hasBakedGeometry;

        private CargoRendererState(BlockState state, BlockEntity entity) {
            this.state = state;
            this.entity = entity;
        }

        private boolean hasBakedGeometry(Minecraft minecraft) {
            if (hasBakedGeometry == null) {
                BakedModel model = minecraft.getBlockRenderer().getBlockModelShaper()
                        .getBlockModel(state);
                RandomSource random = RandomSource.create(42L);
                boolean found = !model.getQuads(state, null, random, ModelData.EMPTY, null).isEmpty();
                if (!found) {
                    for (Direction direction : Direction.values()) {
                        if (!model.getQuads(state, direction, random, ModelData.EMPTY, null).isEmpty()) {
                            found = true;
                            break;
                        }
                    }
                }
                hasBakedGeometry = found;
            }
            return hasBakedGeometry;
        }
    }

    private static BlockEntityRenderer<BlockEntity> getRenderer(BlockEntity blockEntity) {
        return Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
    }
}
