package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlock;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@SuppressWarnings("DuplicatedCode")
public class SulfuricResonanceChamberRenderer
        implements BlockEntityRenderer<SulfuricResonanceChamberBlockEntity> {

    private static final float FLUID_MIN_Y = 2.15F / 16.0F;
    private static final float FLUID_MAX_Y = 4.85F / 16.0F;
    private static final float FLUID_MIN_XZ = 3.15F / 16.0F;
    private static final float FLUID_MAX_XZ = 12.85F / 16.0F;

    private final ItemRenderer itemRenderer;

    public SulfuricResonanceChamberRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            @NotNull SulfuricResonanceChamberBlockEntity chamber,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        renderAcid(
                chamber,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        renderStoredItems(
                chamber,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        renderRotatingShaft(
                chamber,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        renderWindow(
                chamber,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
    }

    private void renderRotatingShaft(
            SulfuricResonanceChamberBlockEntity chamber,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BakedModel model = ClientModEvents.RESONANCE_CHAMBER_SHAFT.get();
        Minecraft minecraft = Minecraft.getInstance();

        if (model == null
                || model == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        BlockState state = chamber.getBlockState();
        Direction facing =
                state.getValue(SulfuricResonanceChamberBlock.FACING);
        Direction shaftSide =
                SulfuricResonanceChamberBlock.heatAndRotationSide(state);
        Direction.Axis shaftAxis = shaftSide.getAxis();

        // Sample the chamber's own kinetic phase. The previous code sampled
        // a neighbouring block position even though the rotating partial is
        // part of this block entity.
        float angleRadians =
                KineticBlockEntityRenderer.getAngleForBe(
                        chamber,
                        chamber.getBlockPos(),
                        shaftAxis
                );

        angleRadians = toLocalShaftAngle(angleRadians, facing);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        rotateToFacing(poseStack, facing);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        double shaftCenterX = 15.0D / 16.0D;
        double shaftCenterY = 0.5D;
        double shaftCenterZ = 0.5D;

        poseStack.translate(
                shaftCenterX,
                shaftCenterY,
                shaftCenterZ
        );
        poseStack.mulPose(Axis.XP.rotation(angleRadians));
        poseStack.translate(
                -shaftCenterX,
                -shaftCenterY,
                -shaftCenterZ
        );

        renderBakedModel(
                poseStack,
                buffer,
                model,
                RenderType.solid(),
                light,
                overlay
        );

        poseStack.popPose();
    }

    private void renderWindow(
            SulfuricResonanceChamberBlockEntity chamber,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BakedModel model = ClientModEvents.RESONANCE_CHAMBER_WINDOW.get();
        Minecraft minecraft = Minecraft.getInstance();

        if (model == null
                || model == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        Direction facing = chamber.getBlockState()
                .getValue(SulfuricResonanceChamberBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        rotateToFacing(poseStack, facing);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        renderBakedModel(
                poseStack,
                buffer,
                model,
                RenderType.translucent(),
                light,
                overlay
        );

        poseStack.popPose();
    }

    private void renderStoredItems(
            SulfuricResonanceChamberBlockEntity chamber,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        ItemStack output = chamber.getRenderedStack(
                SulfuricResonanceChamberBlockEntity.OUTPUT
        );

        if (!output.isEmpty()) {
            renderItem(
                    chamber,
                    output,
                    poseStack,
                    buffer,
                    light,
                    overlay,
                    0.365D,
                    0.34F
            );
            return;
        }

        renderItem(
                chamber,
                chamber.getRenderedStack(
                        SulfuricResonanceChamberBlockEntity.INPUT_1
                ),
                poseStack,
                buffer,
                light,
                overlay,
                0.345D,
                0.25F
        );

        renderItem(
                chamber,
                chamber.getRenderedStack(
                        SulfuricResonanceChamberBlockEntity.INPUT_2
                ),
                poseStack,
                buffer,
                light,
                overlay,
                0.382D,
                0.24F
        );

        renderItem(
                chamber,
                chamber.getRenderedStack(
                        SulfuricResonanceChamberBlockEntity.INPUT_3
                ),
                poseStack,
                buffer,
                light,
                overlay,
                0.419D,
                0.23F
        );
    }

    private void renderItem(
            SulfuricResonanceChamberBlockEntity chamber,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            double y,
            float scale
    ) {
        if (stack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = itemRenderer.getModel(
                stack,
                chamber.getLevel(),
                null,
                0
        );

        if (model == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, y, 0.5D);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(scale, scale, scale);

        itemRenderer.render(
                stack,
                ItemDisplayContext.NONE,
                false,
                poseStack,
                buffer,
                light,
                overlay,
                model
        );

        poseStack.popPose();
    }

    private void renderAcid(
            SulfuricResonanceChamberBlockEntity chamber,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        FluidStack acid = chamber.getRenderedAcid();
        if (acid.isEmpty()) {
            return;
        }

        float fraction = Math.clamp(
                acid.getAmount()
                        / (float) SulfuricResonanceChamberBlockEntity.ACID_CAPACITY,
                0.0F,
                1.0F
        );

        if (fraction <= 0.0F) {
            return;
        }

        float topY =
                FLUID_MIN_Y
                        + (FLUID_MAX_Y - FLUID_MIN_Y) * fraction;

        IClientFluidTypeExtensions extensions =
                IClientFluidTypeExtensions.of(acid.getFluid());
        ResourceLocation stillTexture =
                extensions.getStillTexture(acid);

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        int tint = extensions.getTintColor(acid);
        int color = (0xB8 << 24) | (tint & 0x00FFFFFF);

        VertexConsumer consumer =
                buffer.getBuffer(RenderType.translucent());
        PoseStack.Pose pose = poseStack.last();

        float x0 = FLUID_MIN_XZ;
        float x1 = FLUID_MAX_XZ;
        float z0 = FLUID_MIN_XZ;
        float z1 = FLUID_MAX_XZ;
        float y0 = FLUID_MIN_Y;

        quad(
                consumer, pose,
                x0, topY, z0,
                x0, topY, z1,
                x1, topY, z1,
                x1, topY, z0,
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                color, light, overlay,
                0.0F, 1.0F, 0.0F
        );

        quad(
                consumer, pose,
                x0, y0, z0,
                x0, topY, z0,
                x1, topY, z0,
                x1, y0, z0,
                sprite.getU0(), sprite.getV1(), sprite.getU1(), sprite.getV0(),
                color, light, overlay,
                0.0F, 0.0F, -1.0F
        );

        quad(
                consumer, pose,
                x1, y0, z1,
                x1, topY, z1,
                x0, topY, z1,
                x0, y0, z1,
                sprite.getU0(), sprite.getV1(), sprite.getU1(), sprite.getV0(),
                color, light, overlay,
                0.0F, 0.0F, 1.0F
        );

        quad(
                consumer, pose,
                x0, y0, z1,
                x0, topY, z1,
                x0, topY, z0,
                x0, y0, z0,
                sprite.getU0(), sprite.getV1(), sprite.getU1(), sprite.getV0(),
                color, light, overlay,
                -1.0F, 0.0F, 0.0F
        );

        quad(
                consumer, pose,
                x1, y0, z0,
                x1, topY, z0,
                x1, topY, z1,
                x1, y0, z1,
                sprite.getU0(), sprite.getV1(), sprite.getU1(), sprite.getV0(),
                color, light, overlay,
                1.0F, 0.0F, 0.0F
        );
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float u0, float v0,
            float u1, float v1,
            int color,
            int light,
            int overlay,
            float nx,
            float ny,
            float nz
    ) {
        vertex(
                consumer, pose,
                x1, y1, z1,
                u0, v0,
                color, light, overlay,
                nx, ny, nz
        );
        vertex(
                consumer, pose,
                x2, y2, z2,
                u0, v1,
                color, light, overlay,
                nx, ny, nz
        );
        vertex(
                consumer, pose,
                x3, y3, z3,
                u1, v1,
                color, light, overlay,
                nx, ny, nz
        );
        vertex(
                consumer, pose,
                x4, y4, z4,
                u1, v0,
                color, light, overlay,
                nx, ny, nz
        );
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            int light,
            int overlay,
            float nx,
            float ny,
            float nz
    ) {
        consumer.addVertex(
                        pose,
                        new Vector3f(x, y, z)
                )
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void renderBakedModel(
            PoseStack poseStack,
            MultiBufferSource buffer,
            BakedModel model,
            RenderType renderType,
            int light,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(renderType);
        RandomSource random = RandomSource.create(42L);

        for (BakedQuad quad : model.getQuads(
                null,
                null,
                random,
                ModelData.EMPTY,
                renderType
        )) {
            consumer.putBulkData(
                    poseStack.last(),
                    quad,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F,
                    light,
                    overlay
            );
        }

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);

            for (BakedQuad quad : model.getQuads(
                    null,
                    direction,
                    random,
                    ModelData.EMPTY,
                    renderType
            )) {
                consumer.putBulkData(
                        poseStack.last(),
                        quad,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        light,
                        overlay
                );
            }
        }
    }

    private static float toLocalShaftAngle(
            float worldAxisAngleRadians,
            Direction facing
    ) {
        return switch (facing) {
            case SOUTH, WEST -> -worldAxisAngleRadians;
            default -> worldAxisAngleRadians;
        };
    }

    private static void rotateToFacing(
            PoseStack poseStack,
            Direction facing
    ) {
        switch (facing) {
            case SOUTH ->
                    poseStack.mulPose(
                            Axis.YP.rotationDegrees(180.0F)
                    );
            case EAST ->
                    poseStack.mulPose(
                            Axis.YP.rotationDegrees(-90.0F)
                    );
            case WEST ->
                    poseStack.mulPose(
                            Axis.YP.rotationDegrees(90.0F)
                    );
            default -> {
            }
        }
    }
}
