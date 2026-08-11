package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemicallinkdrive.ThermochemicalLinkDriveBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChainDriveBlock.class, remap = false)
public abstract class ChainDriveBlockMixin {
    @Inject(method = "areBlocksConnected", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sulfuricresonance$isolateLinkDrive(
            BlockState first,
            BlockState second,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        boolean firstIsLinkDrive = first.getBlock() instanceof ThermochemicalLinkDriveBlock;
        boolean secondIsLinkDrive = second.getBlock() instanceof ThermochemicalLinkDriveBlock;

        if (firstIsLinkDrive != secondIsLinkDrive) {
            cir.setReturnValue(false);
        }
    }
}
