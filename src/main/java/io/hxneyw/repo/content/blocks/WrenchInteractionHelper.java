package io.hxneyw.repo.content.blocks;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WrenchInteractionHelper {

    private WrenchInteractionHelper() {
    }

    public static @Nullable ItemInteractionResult handle(
            @NotNull IWrenchable block,
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hit
    ) {
        if (!(stack.getItem() instanceof WrenchItem)) {
            return null;
        }

        UseOnContext context = new UseOnContext(
                player,
                hand,
                hit
        );

        InteractionResult result = player.isShiftKeyDown()
                ? block.onSneakWrenched(state, context)
                : block.onWrenched(state, context);

        return result == InteractionResult.SUCCESS
                ? ItemInteractionResult.SUCCESS
                : ItemInteractionResult.FAIL;
    }
}