package io.hxneyw.repo.content.items;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CombustionBeltConnectorItem extends BeltConnectorItem {

    public CombustionBeltConnectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack heldStack = context.getItemInHand();

        /*
         * Before Create processes the second click, remember the shaft
         * selected by the first click.
         */
        BlockPos firstShaft =
                heldStack.get(AllDataComponents.BELT_FIRST_SHAFT);

        InteractionResult result = super.useOn(context);

        if (level.isClientSide()) {
            return result;
        }

        /*
         * firstShaft is null during the initial shaft selection.
         * It is populated during the second click that creates the belt.
         */
        if (firstShaft == null) {
            return result;
        }

        if (result != InteractionResult.SUCCESS) {
            return result;
        }

        BlockPos secondShaft = context.getClickedPos();

        if (firstShaft.equals(secondShaft)) {
            return result;
        }

        markCombustionBeltChain(level, firstShaft);

        return result;
    }

    private static void markCombustionBeltChain(
            Level level,
            BlockPos anyBeltPosition
    ) {
        BlockState state = level.getBlockState(anyBeltPosition);

        if (!AllBlocks.BELT.has(state)) {
            return;
        }

        BlockEntity blockEntity =
                level.getBlockEntity(anyBeltPosition);

        if (!(blockEntity instanceof BeltBlockEntity firstBelt)) {
            return;
        }

        BlockPos controllerPosition =
                firstBelt.getController();

        for (BlockPos beltPosition :
                BeltBlock.getBeltChain(level, controllerPosition)) {

            BlockEntity segmentEntity =
                    level.getBlockEntity(beltPosition);

            if (!(segmentEntity instanceof BeltBlockEntity belt)) {
                continue;
            }

            if (!(belt instanceof CombustionBeltAccessor accessor)) {
                continue;
            }

            accessor.sulfuricresonance$setCombustionBelt(true);

            belt.setChanged();
            belt.sendData();
        }
    }
}