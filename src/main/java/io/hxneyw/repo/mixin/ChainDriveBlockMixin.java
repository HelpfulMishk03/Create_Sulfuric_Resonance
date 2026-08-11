package io.hxneyw.repo.mixin;

import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemicallinkdrive.ThermochemicalLinkDriveBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChainDriveBlock.class, remap = false)
public abstract class ChainDriveBlockMixin {
    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true, remap = false)
    private void sulfuricresonance$isolateLinkDrive(BlockState stateIn, Direction face,
                                                    BlockState neighbour, LevelAccessor worldIn, BlockPos currentPos,
                                                    BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
        if (neighbour.getBlock() instanceof ThermochemicalLinkDriveBlock) {
            cir.setReturnValue(((ChainDriveBlock) (Object) this).updateShape(
                    stateIn, face, Blocks.AIR.defaultBlockState(), worldIn,
                    currentPos, facingPos));
        }
    }
}
