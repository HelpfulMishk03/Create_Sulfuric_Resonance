package io.hxneyw.repo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.hxneyw.repo.client.CombustionBeltClientAssets;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(
        value = BeltVisual.class,
        remap = false
)
public abstract class BeltVisualMixin {

    @Unique
    private BeltBlockEntity sulfuricresonance$beltBlockEntity;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void sulfuricresonance$captureBeltBlockEntity(
            VisualizationContext context,
            BeltBlockEntity blockEntity,
            float partialTick,
            CallbackInfo ci
    ) {
        sulfuricresonance$beltBlockEntity = blockEntity;
    }

    @WrapOperation(
            method = "<init>",
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
    sulfuricresonance$useCombustionTextureDuringCreation(
            DyeColor color,
            boolean diagonal,
            boolean bottom,
            Operation<SpriteShiftEntry> original,
            VisualizationContext context,
            BeltBlockEntity blockEntity,
            float partialTick
    ) {
        if (CombustionBeltClientAssets.isCombustionBelt(
                blockEntity
        )) {
            return CombustionBeltClientAssets.getSpriteShift(
                    diagonal,
                    bottom
            );
        }

        return original.call(color, diagonal, bottom);
    }

    @WrapOperation(
            method = "update(F)V",
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
    sulfuricresonance$keepCombustionTextureDuringUpdates(
            DyeColor color,
            boolean diagonal,
            boolean bottom,
            Operation<SpriteShiftEntry> original,
            float pt
    ) {
        if (CombustionBeltClientAssets.isCombustionBelt(
                sulfuricresonance$beltBlockEntity
        )) {
            return CombustionBeltClientAssets.getSpriteShift(
                    diagonal,
                    bottom
            );
        }

        return original.call(color, diagonal, bottom);
    }
}