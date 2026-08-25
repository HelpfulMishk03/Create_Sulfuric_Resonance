package io.hxneyw.repo.mixin;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import io.hxneyw.repo.compat.create.ThermochemicalBoilerInterfaceCompat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SteamEngineBlockEntity.class, remap = false)
public abstract class SteamEngineBlockEntityThermochemicalMixin {

    @Shadow
    public abstract FluidTankBlockEntity getTank();

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/steamEngine/PoweredShaftBlockEntity;update(Lnet/minecraft/core/BlockPos;IF)V",
                    ordinal = 1,
                    remap = false
            ),
            index = 2,
            remap = false
    )
    private float sulfuricresonance$allowThermochemicalOverdrive(
            float createEfficiency
    ) {
        FluidTankBlockEntity tank = getTank();
        if (tank == null
                || ThermochemicalBoilerInterfaceCompat.getBoilerBonusSu(tank) <= 0) {
            return createEfficiency;
        }
        return Math.max(
                createEfficiency,
                tank.boiler.getEngineEfficiency(tank.getTotalTankSize())
        );
    }

    @Inject(
            method = "addToGoggleTooltip",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$addThermochemicalSourceTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ThermochemicalBoilerInterfaceCompat.SteamSource source =
                ThermochemicalBoilerInterfaceCompat.resolveSteamSource(
                        (SteamEngineBlockEntity) (Object) this
                );
        if (!source.active()) {
            return;
        }

        tooltip.add(Component.literal(""));
        tooltip.add(sulfuricresonance$line(Component.translatable(
                "tooltip.sulfuricresonance.steam_engine.thermochemical_source"
        ).withStyle(ChatFormatting.GOLD)));
        tooltip.add(sulfuricresonance$line(Component.translatable(
                "tooltip.sulfuricresonance.steam_engine.heat",
                Component.translatable(
                        "tooltip.sulfuricresonance.thermochemical.heat."
                                + source.heatTier().serializedId
                )
        ).append(Component.literal(
                " (" + source.temperature() + "°C)"
        )).withStyle(ChatFormatting.YELLOW)));
        tooltip.add(sulfuricresonance$line(Component.translatable(
                "tooltip.sulfuricresonance.steam_engine.rpm",
                source.rpm()
        ).withStyle(ChatFormatting.GRAY)));
        tooltip.add(sulfuricresonance$line(Component.translatable(
                "tooltip.sulfuricresonance.steam_engine.su",
                source.su()
        ).withStyle(ChatFormatting.AQUA)));
        cir.setReturnValue(true);
    }

    @Unique
    private static Component sulfuricresonance$line(Component component) {
        return Component.literal("    ").append(component);
    }
}
