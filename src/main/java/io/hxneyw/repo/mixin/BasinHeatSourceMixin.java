package io.hxneyw.repo.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.content.blocks.resonantheatinjector.ResonantHeatInjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class BasinHeatSourceMixin {

    @Inject(
            method = "getHeatLevel",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sulfuricResonance$injectResonantHeat(
            CallbackInfoReturnable<HeatLevel> cir
    ) {
        BasinBlockEntity basin = (BasinBlockEntity) (Object) this;
        Level level = basin.getLevel();
        if (level == null) {
            return;
        }

        BlockPos heaterPosition = basin.getBlockPos().below();
        if (level.getBlockEntity(heaterPosition)
                instanceof ResonantHeatInjectorBlockEntity injector) {
            cir.setReturnValue(injector.getCreateHeatLevel());
        }
    }
}
