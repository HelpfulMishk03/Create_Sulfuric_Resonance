package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlock;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrecisionSpritzerBlockItem extends BlockItem {

    public PrecisionSpritzerBlockItem(
            @NotNull Block block,
            @NotNull Properties properties
    ) {
        super(block, properties);
    }

    @Override
    protected @Nullable BlockState getPlacementState(
            @NotNull BlockPlaceContext context
    ) {
        BlockState state = super.getPlacementState(context);
        return state == null
                ? null
                : state.setValue(
                        PerforatedSpritzerBlock.PRECISION,
                        true
                );
    }

    @Override
    public void registerBlocks(
            @NotNull Map<Block, Item> blockToItemMap,
            @NotNull Item item
    ) {
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.sulfuricresonance.precision_spritzer";
    }
}
