package io.hxneyw.repo.mixin;

import io.hxneyw.repo.content.blocks.BlockCaddyBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.ryanhcode.sable.api.block.BlockSubLevelCollisionShape;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockCaddyBlock.class)
@SuppressWarnings("unused")
public abstract class SableCaddyCollisionMixin implements BlockSubLevelCollisionShape {
    @Override
    public VoxelShape getSubLevelCollisionShape(BlockGetter blockGetter, BlockState state) {
        return BlockCaddyBlock.getCargoAwareShape(state);
    }
}
