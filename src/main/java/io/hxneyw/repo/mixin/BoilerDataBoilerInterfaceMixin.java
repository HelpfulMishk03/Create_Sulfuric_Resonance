package io.hxneyw.repo.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import io.hxneyw.repo.compat.create.ThermochemicalBoilerBonusAccess;
import io.hxneyw.repo.compat.create.ThermochemicalBoilerInterfaceCompat;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BoilerData.class, remap = false)
public abstract class BoilerDataBoilerInterfaceMixin
        implements ThermochemicalBoilerBonusAccess {

    @Shadow
    public int attachedEngines;

    @Unique
    private int sulfuricresonance$thermochemicalBonusSu;

    @Inject(
            method = "updateTemperature",
            at = @At("RETURN"),
            remap = false
    )
    private void sulfuricresonance$captureBoilerBonus(
            FluidTankBlockEntity controller,
            CallbackInfoReturnable<Boolean> cir
    ) {
        sulfuricresonance$thermochemicalBonusSu =
                ThermochemicalBoilerInterfaceCompat.getBoilerTargetGrossSu(controller);
    }

    @Inject(
            method = "getEngineEfficiency",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$addThermochemicalCapacity(
            int boilerSize,
            CallbackInfoReturnable<Float> cir
    ) {
        float baseEfficiency = cir.getReturnValue();
        if (sulfuricresonance$thermochemicalBonusSu <= 0
                || attachedEngines <= 0
                || baseEfficiency <= 0.0F) {
            return;
        }

        float suPerFullEngine = 16.0F
                * (float) BlockStressValues.getCapacity(AllBlocks.STEAM_ENGINE.get());
        if (suPerFullEngine <= 0.0F) {
            return;
        }

        float bonusEfficiency = sulfuricresonance$thermochemicalBonusSu
                / (attachedEngines * suPerFullEngine);
        cir.setReturnValue(baseEfficiency + bonusEfficiency);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    private void sulfuricresonance$writeThermochemicalBonus(
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        cir.getReturnValue().putInt(
                "SulfuricResonanceThermochemicalBonusSu",
                sulfuricresonance$thermochemicalBonusSu
        );
    }

    @Inject(method = "read", at = @At("TAIL"), remap = false)
    private void sulfuricresonance$readThermochemicalBonus(
            CompoundTag tag,
            int boilerSize,
            CallbackInfo ci
    ) {
        sulfuricresonance$thermochemicalBonusSu = tag.getInt(
                "SulfuricResonanceThermochemicalBonusSu"
        );
    }

    @Override
    public int sulfuricresonance$getThermochemicalBonusSu() {
        return sulfuricresonance$thermochemicalBonusSu;
    }
}
