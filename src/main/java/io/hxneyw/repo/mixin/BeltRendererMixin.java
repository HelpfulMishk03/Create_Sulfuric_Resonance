package io.hxneyw.repo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import io.hxneyw.repo.client.CombustionBeltClientAssets;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps Create's original belt geometry and replaces only its scrolling
 * texture for marked Combustion Belts.
 */
@Mixin(
        value = BeltRenderer.class,
        remap = false
)
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
            PoseStack ms,
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
}