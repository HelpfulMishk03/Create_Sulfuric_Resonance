package io.hxneyw.repo.mixin;

import io.hxneyw.repo.client.SableLivingEmberLampLightMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the parent client light engine see Living Ember Lamps in Sable plots. */
@Mixin(BlockLightEngine.class)
@SuppressWarnings({"unused", "DataFlowIssue"})
public abstract class LivingEmberLampLightMixin {
    @Inject(method = "getEmission", at = @At("RETURN"), cancellable = true, remap = false)
    private void sulfuricresonance$includePlotLampEmission(long packedPosition, BlockState state,
                                                          CallbackInfoReturnable<Integer> cir) {
        int plotEmission = SableLivingEmberLampLightMap.emissionAt((BlockLightEngine) (Object) this, packedPosition);
        if (plotEmission > cir.getReturnValue()) {
            cir.setReturnValue(plotEmission);
        }
    }
}
