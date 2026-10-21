package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import io.hxneyw.repo.client.BlockCaddyBeltRenderer;
import io.hxneyw.repo.client.CombustionBeltClientAssets;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import io.hxneyw.repo.content.transport.CaddyBeltTransition;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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

        float offset = belt.getSpeed() == 0
                ? transported.beltPosition
                : Mth.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
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
        poseStack.translate(offsetVec.x, offsetVec.y, offsetVec.z);
        if (transported instanceof CaddyBeltTransition transition
                && belt.getLevel() != null) {
            Vec3 previousPosition = transition.sulfuricresonance$getPreviousWorldPosition();
            if (previousPosition != null) {
                long age = belt.getLevel().getGameTime()
                        - transition.sulfuricresonance$getTransitionTick();
                if (age >= 0 && age < 3) {
                Vec3 current = BeltHelper.getVectorForOffset(belt, offset);
                double amount = 1 - Mth.clamp((age + partialTicks) / 2f, 0, 1);
                poseStack.translate(
                        (previousPosition.x - current.x) * amount,
                        (previousPosition.y - current.y) * amount,
                        (previousPosition.z - current.z) * amount
                );
                }
            }
        }
        if (onSlope) {
            poseStack.mulPose((slopeAlongX ? Axis.ZP : Axis.XP)
                    .rotationDegrees(slopeAngle));
            poseStack.translate(0, 1 / 8f, 0);
        }

        var minecraft = net.minecraft.client.Minecraft.getInstance();
        var itemRenderer = minecraft.getItemRenderer();
        BakedModel model = itemRenderer.getModel(
                stack,
                belt.getLevel(),
                null,
                0
        );

        int cargoLight = light;
        if (!onContraption) {
            int segment = (int) Math.floor(offset);
            net.minecraft.core.BlockPos.MutableBlockPos lightPos =
                    new net.minecraft.core.BlockPos.MutableBlockPos();
            lightPos.set(belt.getBlockPos()).move(
                    directionVec.getX() * segment,
                    verticality * segment,
                    directionVec.getZ() * segment
            );
            cargoLight = net.minecraft.client.renderer.LevelRenderer.getLightColor(
                    belt.getLevel(),
                    lightPos
            );
        }
        poseStack.pushPose();
        // Lower the carrier so its belt grips seat into the belt surface.
        poseStack.translate(0, 0.375f, 0);
        // ItemRenderer recenters item-model coordinates around the pose origin.
        // Scale directly so the Caddy stays centered over the belt segment.
        poseStack.scale(15f / 16f, 1, 15f / 16f);
        itemRenderer.render(
                stack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                buffer,
                cargoLight,
                overlay,
                model
        );
        poseStack.popPose();

        poseStack.popPose();
        ci.cancel();
    }
}
