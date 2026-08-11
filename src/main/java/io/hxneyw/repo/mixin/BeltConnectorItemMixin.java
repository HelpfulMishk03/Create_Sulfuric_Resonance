package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltConnectorItem.class, remap = false)
public abstract class BeltConnectorItemMixin {

    @Inject(
            method = "maxLength",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void sulfuricresonance$useCombustionBeltLength(
            CallbackInfoReturnable<Integer> cir
    ) {
        Integer length = CombustionBeltLengthOverride.get();
        if (length != null) {
            cir.setReturnValue(length);
        }
    }
}
