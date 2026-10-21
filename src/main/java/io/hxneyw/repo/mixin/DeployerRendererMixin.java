package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.deployer.DeployerRenderer;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DeployerRenderer.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DeployerRendererMixin {

    @WrapOperation(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(" +
                            "Lnet/minecraft/world/item/ItemStack;" +
                            "Lnet/minecraft/world/item/ItemDisplayContext;Z" +
                            "Lcom/mojang/blaze3d/vertex/PoseStack;" +
                            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
                            "IILnet/minecraft/client/resources/model/BakedModel;)V"
            )
    )
    private void sulfuricresonance$renderDeployerCaddyAsFullModel(
            ItemRenderer renderer,
            ItemStack stack,
            ItemDisplayContext context,
            boolean leftHanded,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            BakedModel model,
            Operation<Void> original
    ) {
        if (!(stack.getItem() instanceof BlockCaddyItem)) {
            original.call(
                    renderer,
                    stack,
                    context,
                    leftHanded,
                    poseStack,
                    buffer,
                    light,
                    overlay,
                    model
            );
            return;
        }

        float scaleCorrection = switch (context) {
            case GROUND -> 0.8f;
            case FIXED -> 1f / (0.75f - 1f / 64f);
            case THIRD_PERSON_RIGHT_HAND -> 4f / 3f;
            default -> 1f;
        };

        poseStack.pushPose();
        poseStack.scale(scaleCorrection, scaleCorrection, scaleCorrection);
        original.call(
                renderer,
                stack,
                ItemDisplayContext.FIXED,
                leftHanded,
                poseStack,
                buffer,
                light,
                overlay,
                model
        );
        poseStack.popPose();
    }
}
