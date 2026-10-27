package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import io.hxneyw.repo.client.CombustionBeltClientAssets;
import io.hxneyw.repo.client.BlockCaddyBeltRenderer;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        value = BeltRenderer.class,
        remap = false
)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BeltRendererMixin {

    @WrapOperation(
            method =
                    "renderSafe(Lcom/simibubi/create/content/kinetics/belt/" +
                            "BeltBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lcom/simibubi/create/content/kinetics/belt/" +
                                    "BeltRenderer;getSpriteShiftEntry(" +
                                    "Lnet/minecraft/world/item/DyeColor;ZZ)" +
                                    "Lnet/createmod/catnip/render/SpriteShiftEntry;"
            )
    )
    private SpriteShiftEntry
    sulfuricresonance$useCombustionBeltTexture(
            DyeColor color,
            boolean diagonal,
            boolean bottom,
            Operation<SpriteShiftEntry> original,
            BeltBlockEntity be,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (CombustionBeltClientAssets.isCombustionBelt(be)) {
            return CombustionBeltClientAssets.getSpriteShift(
                    diagonal,
                    bottom
            );
        }

        return original.call(color, diagonal, bottom);
    }

    @Inject(
            method = "renderItem",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$renderBlockCaddyAsBeltCargo(
            BeltBlockEntity belt,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            Direction beltFacing,
            Vec3i directionVec,
            com.simibubi.create.content.kinetics.belt.BeltSlope slope,
            int verticality,
            boolean slopeAlongX,
            boolean onContraption,
            TransportedItemStack transported,
            Vec3 beltStartOffset,
            CallbackInfo ci
    ) {
        ItemStack stack = transported.stack;
        if (!(stack.getItem() instanceof BlockCaddyItem)) {
            return;
        }
        net.minecraft.world.level.Level level = belt.getLevel();
        if (level == null) {
            ci.cancel();
            return;
        }

        float offset = belt.getSpeed() == 0
                ? transported.beltPosition
                : Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
        float sideOffset = belt.getSpeed() == 0
                ? transported.sideOffset
                : Mth.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
        float verticalMovement = offset < 0.5f
                ? 0
                : verticality * (Math.min(offset, belt.beltLength - 0.5f) - 0.5f);
        Vec3 offsetVec = Vec3.atLowerCornerOf(directionVec).scale(offset);
        if (verticalMovement != 0) {
            offsetVec = offsetVec.add(0, verticalMovement, 0);
        }

        boolean onSlope = slope
                != com.simibubi.create.content.kinetics.belt.BeltSlope.HORIZONTAL
                && Mth.clamp(offset, 0.5f, belt.beltLength - 0.5f) == offset;
        boolean tiltForward = (slope
                == com.simibubi.create.content.kinetics.belt.BeltSlope.DOWNWARD
                ^ beltFacing.getAxisDirection()
                == net.minecraft.core.Direction.AxisDirection.POSITIVE)
                == (beltFacing.getAxis() == Direction.Axis.Z);
        float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

        poseStack.pushPose();
        TransformStack.of(poseStack).nudge(transported.angle);
        poseStack.translate(offsetVec.x, offsetVec.y, offsetVec.z);
        boolean alongX = beltFacing.getClockWise().getAxis() == Direction.Axis.X;
        if (!alongX) {
            sideOffset *= -1;
        }
        poseStack.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
        if (onSlope) {
            poseStack.mulPose((slopeAlongX ? Axis.ZP : Axis.XP)
                    .rotationDegrees(slopeAngle));
            poseStack.translate(0, 1 / 8f, 0);
        }

        var minecraft = net.minecraft.client.Minecraft.getInstance();
        // A cargo block is carried by the Caddy, so sample light from the
        // cargo itself rather than the changing block light along the Belt.
        // Full sky light keeps it consistent by day and night; a small block
        // light floor preserves readable colors for non-emissive cargo.
        var cargoState = BlockCaddyItem.getCargoBlockState(stack);
        int cargoLight = LightTexture.pack(
                Math.max(cargoState.getLightEmission(level, belt.getBlockPos()), 8),
                15
        );
        poseStack.pushPose();
        var caddyModel = minecraft.getItemRenderer().getModel(
                stack,
                level,
                null,
                0
        );
        // Preserve the original carrier scale and seating height. The Caddy
        // block model occupies block space, so Create's generic half-scale
        // item transform makes it visibly smaller than its established belt size.
        poseStack.translate(0, 0.375f, 0);
        poseStack.scale(15f / 16f, 1, 15f / 16f);
        BlockCaddyBeltRenderer.setBeltCaddyRender(true);
        int cargoRendersBeforeItem = BlockCaddyBeltRenderer.cargoRenderCount();
        try {
            minecraft.getItemRenderer().render(
                    stack,
                    ItemDisplayContext.NONE,
                    false,
                    poseStack,
                    buffer,
                    light,
                    overlay,
                    caddyModel
            );
        } finally {
            BlockCaddyBeltRenderer.setBeltCaddyRender(false);
        }
        // Some model paths invoke the registered custom renderer; others
        // render only the baked shell. Fall back only when cargo was not drawn.
        if (BlockCaddyBeltRenderer.cargoRenderCount() == cargoRendersBeforeItem) {
            poseStack.pushPose();
            caddyModel.applyTransform(ItemDisplayContext.NONE, poseStack, false);
            poseStack.translate(-0.5f, -0.5f, -0.5f);
            BlockCaddyBeltRenderer.renderCargoItem(
                    stack, poseStack, buffer, cargoLight, overlay, partialTicks);
            poseStack.popPose();
        }
        poseStack.popPose();

        poseStack.popPose();
        ci.cancel();
    }
}
