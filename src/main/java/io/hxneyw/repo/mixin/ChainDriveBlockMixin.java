package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemicallinkdrive.ThermochemicalLinkDriveBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChainDriveBlock.class, remap = false)
public abstract class ChainDriveBlockMixin {
    @ModifyVariable(
            method = "updateShape",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1
    )
    private BlockState sulfuricresonance$isolateLinkDriveVisualConnection(
            BlockState neighbour
    ) {
        boolean thisIsLinkDrive =
                (Object) this instanceof ThermochemicalLinkDriveBlock;

        boolean neighbourIsLinkDrive =
                neighbour.getBlock()
                        instanceof ThermochemicalLinkDriveBlock;

        if (neighbour.getBlock() instanceof ChainDriveBlock
                && thisIsLinkDrive != neighbourIsLinkDrive) {
            return Blocks.AIR.defaultBlockState();
        }

        return neighbour;
    }

    @Inject(method = "areBlocksConnected", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sulfuricresonance$isolateLinkDrive(
            BlockState state,
            BlockState other,
            Direction facing,
            CallbackInfoReturnable<Boolean> cir
    ) {
        boolean firstIsLinkDrive = state.getBlock() instanceof ThermochemicalLinkDriveBlock;
        boolean secondIsLinkDrive = other.getBlock() instanceof ThermochemicalLinkDriveBlock;

        if (firstIsLinkDrive != secondIsLinkDrive) {
            cir.setReturnValue(false);
        }
    }
}
