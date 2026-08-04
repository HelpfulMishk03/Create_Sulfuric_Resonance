package io.hxneyw.repo.mixin;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps complete Combustion Belt chains distinct from ordinary Create belts.
 */
@Mixin(
        value = BeltBlock.class,
        remap = false
)
public abstract class BeltBlockMixin {

    /**
     * A partially marked chain is still a Combustion Belt chain.
     *
     * This keeps middle-click correct while an extension is being rebuilt and
     * also repairs UX for chains produced by the older one-tick marking race.
     */
    @Inject(
            method = "getCloneItemStack",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sulfuricresonance$pickCombustionBeltConnector(
            BlockState state,
            HitResult target,
            LevelReader level,
            BlockPos position,
            Player player,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!sulfuricresonance$isCombustionBeltChain(
                level,
                position
        )) {
            return;
        }

        cir.setReturnValue(
                new ItemStack(
                        Items.COMBUSTION_BELT_CONNECTOR.get()
                )
        );
    }

    /**
     * Create's normal connector is blocked when any segment in the clicked
     * chain carries the Combustion Belt marker.
     */
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
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (!AllItems.BELT_CONNECTOR.isIn(stack)
                || !sulfuricresonance$isCombustionBeltChain(
                        level,
                        position
                )) {
            return;
        }

        cir.setReturnValue(
                ItemInteractionResult.FAIL
        );
    }

    @Unique
    private static boolean
    sulfuricresonance$isCombustionBeltChain(
            LevelReader level,
            BlockPos position
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(position);

        if (!(blockEntity instanceof BeltBlockEntity belt)) {
            return false;
        }

        if (belt instanceof CombustionBeltAccessor accessor
                && accessor
                .sulfuricresonance$isCombustionBelt()) {
            return true;
        }

        BlockPos controllerPosition =
                belt.getController();

        BlockEntity controllerEntity =
                level.getBlockEntity(controllerPosition);

        if (controllerEntity
                instanceof CombustionBeltAccessor controllerAccessor
                && controllerAccessor
                .sulfuricresonance$isCombustionBelt()) {
            return true;
        }

        if (!(level instanceof LevelAccessor levelAccessor)) {
            return false;
        }

        for (BlockPos segmentPosition :
                BeltBlock.getBeltChain(
                        levelAccessor,
                        controllerPosition
                )) {

            BlockEntity segmentEntity =
                    level.getBlockEntity(segmentPosition);

            if (segmentEntity
                    instanceof CombustionBeltAccessor segmentAccessor
                    && segmentAccessor
                    .sulfuricresonance$isCombustionBelt()) {
                return true;
            }
        }

        return false;
    }
}
