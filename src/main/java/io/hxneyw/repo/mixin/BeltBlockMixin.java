package io.hxneyw.repo.mixin;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents Create's ordinary Belt Connector from slicing or extending a
 * marked Combustion Belt.
 */
@Mixin(
        value = BeltBlock.class,
        remap = false
)
public abstract class BeltBlockMixin {

    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$blockCreateConnectorOnCombustionBelt(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (!AllItems.BELT_CONNECTOR.isIn(stack)) {
            return;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (!(blockEntity instanceof CombustionBeltAccessor accessor)) {
            return;
        }

        if (!accessor.sulfuricresonance$isCombustionBelt()) {
            return;
        }

        /*
         * Consume the block interaction as a failure so Minecraft does not
         * continue into Create's connector item and bypass this guard.
         */
        cir.setReturnValue(ItemInteractionResult.FAIL);
    }
}