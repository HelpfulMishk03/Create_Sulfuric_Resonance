package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import io.hxneyw.repo.client.BlockCaddyBeltRenderer;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemRendererMixin {
    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void sulfuricresonance$renderCaddyCargo(ItemStack stack, ItemDisplayContext context, boolean leftHanded,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (!(stack.getItem() instanceof BlockCaddyItem)) return;
        poseStack.pushPose();
        // ItemRenderer restores its pose before returning; reproduce the Caddy's
        // display transform so cargo is drawn in the same frame as its model.
        model.getTransforms().getTransform(context).apply(leftHanded, poseStack);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        BlockCaddyBeltRenderer.renderCargoItem(stack, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }
}
