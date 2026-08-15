package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemicallinkdrive.ThermochemicalLinkDriveBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RotationPropagator.class, remap = false)
public abstract class RotationPropagatorMixin {

    @Inject(
            method = "getRotationSpeedModifier",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void sulfuricresonance$isolateLinkDriveKinetics(
            KineticBlockEntity from,
            KineticBlockEntity _to,
            CallbackInfoReturnable<Float> cir
    ) {
        Block fromBlock =
                from.getBlockState().getBlock();

        Block toBlock =
                _to.getBlockState().getBlock();

        boolean fromIsLinkDrive =
                fromBlock instanceof ThermochemicalLinkDriveBlock;

        boolean toIsLinkDrive =
                toBlock instanceof ThermochemicalLinkDriveBlock;

        if (fromIsLinkDrive == toIsLinkDrive) {
            return;
        }

        if (fromBlock instanceof ChainDriveBlock
                && toBlock instanceof ChainDriveBlock) {
            cir.setReturnValue(0.0F);
        }
    }
}
