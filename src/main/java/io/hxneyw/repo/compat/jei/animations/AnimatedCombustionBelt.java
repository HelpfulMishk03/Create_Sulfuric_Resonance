package io.hxneyw.repo.compat.jei.animations;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import io.hxneyw.repo.client.CombustionBeltClientAssets;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@ParametersAreNonnullByDefault
public final class AnimatedCombustionBelt extends AnimatedKinetics {

    private static final long ITEM_CYCLE_MILLIS = 2800L;
    private static final float BELT_SPEED = 32.0F;
    private static final Direction BELT_FACING = Direction.EAST;

    private int displayedSegments = 4;

    public AnimatedCombustionBelt withDisplayedSegments(
            int displayedSegments
    ) {
        this.displayedSegments = Math.clamp(
                displayedSegments,
                3,
                5
        );
        return this;
    }

    @Override
    public void draw(
            GuiGraphics graphics,
            int xOffset,
            int yOffset
    ) {
        draw(graphics, xOffset, yOffset, ItemStack.EMPTY);
    }

    public void draw(
            GuiGraphics graphics,
            int xOffset,
            int yOffset,
            ItemStack transportedStack
    ) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset - 10.2F, yOffset + 9.2F, 200.0F);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-26.5F));
        matrixStack.mulPose(Axis.YP.rotationDegrees(15.0F));
        matrixStack.scale(17.8F, 17.8F, 17.8F);
        matrixStack.translate(
                -(this.displayedSegments - 1) / 2.0,
                -0.46,
                0.0
        );

        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer =
                bufferSource.getBuffer(RenderType.solid());

        BlockState beltState = AllBlocks.BELT.getDefaultState()
                .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL)
                .setValue(BeltBlock.HORIZONTAL_FACING, BELT_FACING);

        float scroll = beltScroll();

        Lighting.setupFor3DItems();
        RenderSystem.setShaderColor(
                1.33F,
                1.33F,
                1.33F,
                1.0F
        );

        renderPulley(
                matrixStack,
                vertexConsumer,
                beltState.setValue(BeltBlock.PART, BeltPart.START),
                0.25F,
                false
        );
        renderPulley(
                matrixStack,
                vertexConsumer,
                beltState.setValue(BeltBlock.PART, BeltPart.END),
                this.displayedSegments - 1.25F,
                true
        );

        for (int index = 0;
             index < this.displayedSegments;
             index++) {
            BeltPart part = index == 0
                    ? BeltPart.START
                    : index == this.displayedSegments - 1
                    ? BeltPart.END
                    : BeltPart.MIDDLE;

            renderBeltSegment(
                    matrixStack,
                    vertexConsumer,
                    beltState.setValue(BeltBlock.PART, part),
                    index,
                    part,
                    scroll
            );
        }

        bufferSource.endBatch(RenderType.solid());
        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        matrixStack.popPose();

        renderTransportedItem(
                graphics,
                xOffset,
                yOffset,
                transportedStack
        );
    }

    private static void renderBeltSegment(
            PoseStack matrixStack,
            VertexConsumer vertexConsumer,
            BlockState beltState,
            int index,
            BeltPart part,
            float scroll
    ) {
        boolean start = part == BeltPart.START;
        boolean end = part == BeltPart.END;

        for (boolean bottom : new boolean[]{false, true}) {
            SuperByteBuffer beltBuffer = CachedBuffers.partial(
                    BeltRenderer.getBeltPartial(
                            false,
                            start,
                            end,
                            bottom
                    ),
                    beltState
            ).light(LightTexture.FULL_BRIGHT);

            SpriteShiftEntry spriteShift =
                    CombustionBeltClientAssets.getSpriteShift(
                            false,
                            bottom
                    );

            float spriteSize = spriteShift.getTarget().getV1()
                    - spriteShift.getTarget().getV0();
            float shiftedScroll = scroll + (bottom ? 0.5F : 0.0F);
            shiftedScroll -= (float) Math.floor(shiftedScroll);
            beltBuffer.shiftUVScrolling(
                    spriteShift,
                    shiftedScroll * spriteSize * 0.5F
            );

            PoseStack localTransforms = new PoseStack();
            localTransforms.translate(index, -0.05, 0.0);
            TransformStack.of(localTransforms)
                    .center()
                    .rotateYDegrees(
                            AngleHelper.horizontalAngle(BELT_FACING)
                    )
                    .uncenter();

            beltBuffer
                    .transform(localTransforms)
                    .renderInto(matrixStack, vertexConsumer);
        }
    }

    private static void renderPulley(
            PoseStack matrixStack,
            VertexConsumer vertexConsumer,
            BlockState beltState,
            float position,
            boolean end
    ) {
        Direction pulleyDirection = end
                ? BELT_FACING.getCounterClockWise()
                : BELT_FACING.getClockWise();

        Supplier<PoseStack> orientation = () -> {
            PoseStack pulleyStack = new PoseStack();
            var pulleyTransform = TransformStack.of(pulleyStack);

            pulleyTransform.center();
            if (pulleyDirection.getAxis() == Direction.Axis.X) {
                pulleyTransform.rotateYDegrees(90.0F);
            }
            if (pulleyDirection.getAxis() == Direction.Axis.Y) {
                pulleyTransform.rotateXDegrees(90.0F);
            }
            pulleyTransform.rotateXDegrees(90.0F);
            pulleyTransform.uncenter();
            return pulleyStack;
        };

        SuperByteBuffer pulleyBuffer =
                CachedBuffers.partialDirectional(
                        AllPartialModels.BELT_PULLEY,
                        beltState,
                        pulleyDirection,
                        orientation
                ).light(LightTexture.FULL_BRIGHT);

        PoseStack localTransforms = new PoseStack();
        localTransforms.translate(position, 0.10, 0.0);
        TransformStack.of(localTransforms)
                .center()
                .rotateZDegrees(getCurrentAngle())
                .uncenter();

        pulleyBuffer
                .transform(localTransforms)
                .renderInto(matrixStack, vertexConsumer);
    }

    private static void renderTransportedItem(
            GuiGraphics graphics,
            int xOffset,
            int yOffset,
            ItemStack transportedStack
    ) {
        if (transportedStack.isEmpty()) {
            return;
        }

        float progress = (Util.getMillis() % ITEM_CYCLE_MILLIS)
                / (float) ITEM_CYCLE_MILLIS;
        float itemX = xOffset - 24.0F + progress * 48.0F;
        float itemY = yOffset + 9.0F - progress * 6.7F;

        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(itemX, itemY, 350.0F);
        matrixStack.scale(0.58F, 0.58F, 0.58F);
        graphics.renderItem(transportedStack, -8, -8);
        matrixStack.popPose();
    }

    private static float beltScroll() {
        float time = Util.getMillis() / 50.0F;
        float speed = -BELT_SPEED;
        double scroll = speed * time / (31.5F * 16.0F);
        return (float) (scroll - Math.floor(scroll));
    }
}