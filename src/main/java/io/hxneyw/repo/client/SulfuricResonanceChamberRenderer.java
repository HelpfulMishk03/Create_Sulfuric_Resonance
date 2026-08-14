package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlock;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity.ReactionLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
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
    private static final double ITEM_PLATFORM_Y = 5.3D / 16.0D;
    private static final double ITEM_VERTICAL_OFFSET = 0.018D;
    private static final float ITEM_SCALE = 0.32F;
    private static final double ITEM_STACK_SPACING = 0.030D;

    private final ItemRenderer itemRenderer;

    public static RingGlowProfile getRingGlowProfile(
            ReactionLevel reactionLevel
    ) {
        return switch (reactionLevel) {
            case NORMAL -> new RingGlowProfile(
                    0.25F,
                    0.00F,
                    8,
                    15,
                    0.42F
            );
            case RESONANCE -> new RingGlowProfile(
                    0.22F,
                    0.00F,
                    10,
                    15,
                    0.58F
            );
        };
    }

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

        renderHeatInputGlow(
                chamber,
                partialTick,
                poseStack,
                buffer,
                packedOverlay
        );

        renderPlatformGlow(
                chamber,
                partialTick,
                poseStack,
                buffer,
                packedOverlay
        );

        renderRings(
                chamber,
                partialTick,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        renderStoredItems(
                chamber,
                poseStack,
                buffer,
                packedOverlay
        );

        renderRotatingShaft(
                chamber,
                partialTick,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        renderWindow(
                chamber,
                partialTick,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
    }

    private void renderHeatInputGlow(
            SulfuricResonanceChamberBlockEntity chamber,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay
    ) {
        if (chamber.getLevel() == null) {
            return;
        }

        float heatStrength = chamber.getClientHeatVisualStrength();
        if (heatStrength <= 0.0F) {
            return;
        }

        float activation = chamber.getClientVisualActivation(partialTick);
        float time = chamber.getLevel().getGameTime() + partialTick;
        float portPulse = 0.82F
                + 0.18F * (float) Math.sin(time * 0.19F);
        float visibleStrength = heatStrength
                * (0.88F + activation * 0.12F);

        TextureAtlasSprite sprite = getWhiteSprite();
        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());

        Direction facing = chamber.getBlockState()
                .getValue(SulfuricResonanceChamberBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        rotateToFacing(poseStack, facing);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        PoseStack.Pose pose = poseStack.last();

        int portColor = rgba(
                255,
                118,
                38,
                visibleStrength * 0.24F * portPulse
        );

        float sideX = 15.02F / 16.0F;
        float sideY0 = 6.0F / 16.0F;
        float sideY1 = 10.0F / 16.0F;
        float sideZ0 = 6.0F / 16.0F;
        float sideZ1 = 10.0F / 16.0F;

        quad(
                consumer, pose,
                sideX, sideY0, sideZ1,
                sideX, sideY1, sideZ1,
                sideX, sideY1, sideZ0,
                sideX, sideY0, sideZ0,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                portColor, LightTexture.FULL_BRIGHT, overlay,
                1.0F, 0.0F, 0.0F
        );
        quad(
                consumer, pose,
                sideX - 0.001F, sideY0, sideZ0,
                sideX - 0.001F, sideY1, sideZ0,
                sideX - 0.001F, sideY1, sideZ1,
                sideX - 0.001F, sideY0, sideZ1,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                portColor, LightTexture.FULL_BRIGHT, overlay,
                -1.0F, 0.0F, 0.0F
        );

        poseStack.popPose();
    }

    private void renderPlatformGlow(
            SulfuricResonanceChamberBlockEntity chamber,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay
    ) {
        if (chamber.getLevel() == null) {
            return;
        }

        float activation = chamber.getClientVisualActivation(partialTick);
        float completionPulse = chamber.getClientCompletionPulse(partialTick);
        if (activation <= 0.0F && completionPulse <= 0.0F) {
            return;
        }

        float time = chamber.getLevel().getGameTime() + partialTick;
        float engagement = smoothStep(Math.clamp(
                (activation - 0.12F) / 0.88F,
                0.0F,
                1.0F
        ));
        float initialization = Math.clamp(
                activation / 0.18F,
                0.0F,
                1.0F
        );
        float flicker = getInitializationFlicker(
                activation,
                time
        );
        float alpha = 0.055F * initialization * flicker
                + 0.135F * engagement
                + 0.095F * completionPulse;

        if (alpha <= 0.001F) {
            return;
        }

        TextureAtlasSprite sprite = getWhiteSprite();
        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
        PoseStack.Pose pose = poseStack.last();
        int color = rgba(255, 150, 62, alpha);

        float x0 = 5.0F / 16.0F;
        float x1 = 11.0F / 16.0F;
        float z0 = 5.0F / 16.0F;
        float z1 = 11.0F / 16.0F;
        float y = 5.20F / 16.0F;

        quad(
                consumer, pose,
                x0, y, z0,
                x0, y, z1,
                x1, y, z1,
                x1, y, z0,
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(),
                color, LightTexture.FULL_BRIGHT, overlay,
                0.0F, 1.0F, 0.0F
        );
    }

    private void renderRings(
            SulfuricResonanceChamberBlockEntity chamber,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        float innerAngle = chamber.getClientInnerRingAngle(partialTick);
        float outerAngle = chamber.getClientOuterRingAngle(partialTick);
        float activation = chamber.getClientVisualActivation(partialTick);
        float reactionProgress = chamber.getClientReactionProgress(partialTick);
        float completionPulse = chamber.getClientCompletionPulse(partialTick);
        ReactionLevel reactionLevel = chamber.getClientVisualReactionLevel();

        renderRing(
                ClientModEvents.RESONANCE_CHAMBER_RING_TOP.get(),
                chamber,
                -innerAngle,
                poseStack,
                buffer,
                getRingGlowLight(
                        light,
                        activation,
                        true,
                        reactionLevel
                ),
                overlay,
                getRingGlowStrength(
                        activation,
                        reactionProgress,
                        completionPulse,
                        true,
                        reactionLevel
                ),
                reactionLevel,
                activation,
                reactionProgress,
                completionPulse
        );

        renderRing(
                ClientModEvents.RESONANCE_CHAMBER_RING_BOTTOM.get(),
                chamber,
                outerAngle,
                poseStack,
                buffer,
                getRingGlowLight(
                        light,
                        activation,
                        false,
                        reactionLevel
                ),
                overlay,
                getRingGlowStrength(
                        activation,
                        reactionProgress,
                        completionPulse,
                        false,
                        reactionLevel
                ),
                reactionLevel,
                activation,
                reactionProgress,
                completionPulse
        );
    }

    private static int getRingGlowLight(
            int packedLight,
            float activation,
            boolean topRing,
            ReactionLevel reactionLevel
    ) {
        RingGlowProfile profile = getRingGlowProfile(reactionLevel);
        float start = topRing
                ? profile.topStart()
                : profile.bottomStart();
        float ramp = smoothStep(Math.clamp(
                (activation - start) / Math.max(0.001F, 1.0F - start),
                0.0F,
                1.0F
        ));

        int blockLight = packedLight >> 4 & 15;
        int skyLight = packedLight >> 20 & 15;

        if (activation <= start) {
            return packedLight;
        }

        int minimumLight = Math.max(
                blockLight,
                profile.minimumActiveBlockLight()
        );
        int targetLight = Math.max(
                minimumLight,
                profile.maximumBlockLight()
        );
        int boostedBlockLight = Math.round(
                minimumLight + (targetLight - minimumLight) * ramp
        );
        return LightTexture.pack(boostedBlockLight, skyLight);
    }

    private static float getRingGlowStrength(
            float activation,
            float reactionProgress,
            float completionPulse,
            boolean topRing,
            ReactionLevel reactionLevel
    ) {
        RingGlowProfile profile = getRingGlowProfile(reactionLevel);
        float start = topRing
                ? profile.topStart()
                : profile.bottomStart();

        if (activation <= start) {
            return 0.0F;
        }

        float localActivation = smoothStep(Math.clamp(
                (activation - start) / Math.max(0.001F, 1.0F - start),
                0.0F,
                1.0F
        ));
        float thermalMaturity = Math.clamp(
                activation * 0.72F + reactionProgress * 0.28F,
                0.0F,
                1.0F
        );
        float strength = localActivation
                * (0.24F + thermalMaturity * 0.76F)
                + completionPulse * 0.18F * localActivation;
        return Math.clamp(strength, 0.0F, 1.15F);
    }

    private void renderRing(
            BakedModel model,
            SulfuricResonanceChamberBlockEntity chamber,
            float angleDegrees,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            float glowStrength,
            ReactionLevel reactionLevel,
            float activation,
            float reactionProgress,
            float completionPulse
    ) {
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
        poseStack.mulPose(Axis.YP.rotationDegrees(angleDegrees));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        renderBakedModel(
                poseStack,
                buffer,
                model,
                RenderType.solid(),
                light,
                overlay
        );

        if (glowStrength > 0.0F) {
            RingGlowProfile profile = getRingGlowProfile(reactionLevel);
            float thermalMaturity = Math.clamp(
                    activation * 0.70F + reactionProgress * 0.30F,
                    0.0F,
                    1.0F
            );

            float red;
            float green;
            float blue;

            if (reactionLevel == ReactionLevel.RESONANCE) {
                red = mix(0.70F, 1.00F, thermalMaturity);
                green = mix(0.10F, 0.42F, thermalMaturity);
                blue = mix(0.20F, 0.82F, thermalMaturity);
            } else {
                red = mix(0.72F, 1.00F, thermalMaturity);
                green = mix(0.12F, 0.58F, thermalMaturity);
                blue = mix(0.03F, 0.16F, thermalMaturity);
            }

            red = Math.clamp(red + completionPulse * 0.05F, 0.0F, 1.0F);
            green = Math.clamp(
                    green + completionPulse * 0.18F,
                    0.0F,
                    1.0F
            );
            blue = Math.clamp(
                    blue + completionPulse * 0.12F,
                    0.0F,
                    1.0F
            );

            renderTintedBakedModel(
                    poseStack,
                    buffer,
                    model,
                    red,
                    green,
                    blue,
                    Math.clamp(
                            profile.glowAlpha()
                                    * glowStrength
                                    * (1.0F + completionPulse * 0.16F),
                            0.0F,
                            0.82F
                    ),
                    LightTexture.FULL_BRIGHT,
                    overlay
            );
        }

        poseStack.popPose();
    }

    private void renderRotatingShaft(
            SulfuricResonanceChamberBlockEntity chamber,
            float partialTick,
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
            float partialTick,
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

        if (chamber.getLevel() != null) {
            float activation = chamber.getClientVisualActivation(partialTick);
            float completionPulse =
                    chamber.getClientCompletionPulse(partialTick);
            float time = chamber.getLevel().getGameTime() + partialTick;
            float initialization = Math.clamp(
                    activation / 0.18F,
                    0.0F,
                    1.0F
            );
            float engagement = smoothStep(Math.clamp(
                    (activation - 0.12F) / 0.88F,
                    0.0F,
                    1.0F
            ));
            float flicker = getInitializationFlicker(
                    activation,
                    time
            );
            float glow = 0.045F * initialization * flicker
                    + 0.165F * engagement
                    + 0.095F * completionPulse;

            if (glow > 0.001F) {
                renderTintedBakedModel(
                        poseStack,
                        buffer,
                        model,
                        1.0F,
                        0.55F + completionPulse * 0.12F,
                        0.20F + completionPulse * 0.08F,
                        Math.clamp(glow, 0.0F, 0.32F),
                        LightTexture.FULL_BRIGHT,
                        overlay
                );
            }
        }

        poseStack.popPose();
    }

    private void renderStoredItems(
            SulfuricResonanceChamberBlockEntity chamber,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay
    ) {
        ItemStack output = chamber.getItem(
                SulfuricResonanceChamberBlockEntity.OUTPUT
        );

        if (!output.isEmpty()) {
            renderPlatformItem(
                    chamber,
                    output,
                    poseStack,
                    buffer,
                    overlay,
                    0.0D
            );
            return;
        }

        renderPlatformItem(
                chamber,
                chamber.getItem(SulfuricResonanceChamberBlockEntity.INPUT_1),
                poseStack,
                buffer,
                overlay,
                0.0D
        );

        renderPlatformItem(
                chamber,
                chamber.getItem(SulfuricResonanceChamberBlockEntity.INPUT_3),
                poseStack,
                buffer,
                overlay,
                ITEM_STACK_SPACING
        );

        renderPlatformItem(
                chamber,
                chamber.getItem(SulfuricResonanceChamberBlockEntity.INPUT_2),
                poseStack,
                buffer,
                overlay,
                ITEM_STACK_SPACING * 2.0D
        );
    }

    private void renderPlatformItem(
            SulfuricResonanceChamberBlockEntity chamber,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int overlay,
            double additionalHeight
    ) {
        if (stack.isEmpty() || chamber.getLevel() == null) {
            return;
        }

        BakedModel model = itemRenderer.getModel(
                stack,
                chamber.getLevel(),
                null,
                0
        );

        Minecraft minecraft = Minecraft.getInstance();
        if (model == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        Direction facing = chamber.getBlockState()
                .getValue(SulfuricResonanceChamberBlock.FACING);

        










        poseStack.pushPose();

        poseStack.translate(
                0.5D,
                ITEM_PLATFORM_Y + ITEM_VERTICAL_OFFSET + additionalHeight,
                0.5D
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(rotationForItem(facing))
        );
        poseStack.mulPose(
                Axis.XP.rotationDegrees(90.0F)
        );
        poseStack.scale(
                ITEM_SCALE,
                ITEM_SCALE,
                ITEM_SCALE
        );

        
        
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        renderItemBakedModelCutout(
                poseStack,
                buffer,
                model,
                overlay
        );

        poseStack.popPose();
    }

    private static void renderItemBakedModelCutout(
            PoseStack poseStack,
            MultiBufferSource buffer,
            BakedModel model,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());
        RandomSource random = RandomSource.create(42L);

        for (BakedQuad quad : model.getQuads(
                null,
                null,
                random,
                ModelData.EMPTY,
                null
        )) {
            consumer.putBulkData(
                    poseStack.last(),
                    quad,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F,
                    LightTexture.FULL_BRIGHT,
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
                    null
            )) {
                consumer.putBulkData(
                        poseStack.last(),
                        quad,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        LightTexture.FULL_BRIGHT,
                        overlay
                );
            }
        }
    }

    private static float rotationForItem(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
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

    private static void renderTintedBakedModel(
            PoseStack poseStack,
            MultiBufferSource buffer,
            BakedModel model,
            float red,
            float green,
            float blue,
            float alpha,
            int light,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.translucent());
        RandomSource random = RandomSource.create(42L);

        for (BakedQuad quad : model.getQuads(
                null,
                null,
                random,
                ModelData.EMPTY,
                null
        )) {
            consumer.putBulkData(
                    poseStack.last(),
                    quad,
                    red,
                    green,
                    blue,
                    alpha,
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
                    null
            )) {
                consumer.putBulkData(
                        poseStack.last(),
                        quad,
                        red,
                        green,
                        blue,
                        alpha,
                        light,
                        overlay
                );
            }
        }
    }

    private static TextureAtlasSprite getWhiteSprite() {
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(
                        ResourceLocation.fromNamespaceAndPath(
                                "minecraft",
                                "block/white_concrete"
                        )
                );
    }

    private static int rgba(
            int red,
            int green,
            int blue,
            float alpha
    ) {
        int alphaByte = Math.clamp(
                Math.round(Math.clamp(alpha, 0.0F, 1.0F) * 255.0F),
                0,
                255
        );
        return alphaByte << 24
                | red << 16
                | green << 8
                | blue;
    }

    private static float smoothStep(float value) {
        float clamped = Math.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private static float mix(
            float start,
            float end,
            float amount
    ) {
        return start + (end - start) * Math.clamp(
                amount,
                0.0F,
                1.0F
        );
    }

    private static float getInitializationFlicker(
            float activation,
            float time
    ) {
        if (activation >= 0.24F) {
            return 1.0F;
        }

        float rapid = Math.abs(
                (float) Math.sin(time * 1.85F + activation * 19.0F)
        );
        return 0.48F + rapid * 0.52F;
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

    public record RingGlowProfile(
            float bottomStart,
            float topStart,
            int minimumActiveBlockLight,
            int maximumBlockLight,
            float glowAlpha
    ) {
    }

}
