package io.hxneyw.repo.content.blocks;

import com.mojang.serialization.MapCodec;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import io.hxneyw.repo.content.items.BlockCaddyInventory;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockCaddyBlock extends BaseEntityBlock {
    public static final MapCodec<BlockCaddyBlock> CODEC = simpleCodec(BlockCaddyBlock::new);
    private static final VoxelShape CADDY_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 1.1, 0.5),
            Block.box(0, 0, 15.5, 16, 1.1, 16),
            Block.box(0, 0, 0.5, 0.5, 1.1, 15.5),
            Block.box(15.5, 0, 0.5, 16, 1.1, 15.5),
            Block.box(0, 1.1, 0, 2.5, 2.7, 2.5),
            Block.box(13.5, 1.1, 0, 16, 2.7, 2.5),
            Block.box(0, 1.1, 13.5, 2.5, 2.7, 16),
            Block.box(13.5, 1.1, 13.5, 16, 2.7, 16)
    );
    private static final VoxelShape CARGO_SHAPE = Block.box(0.5, 1.1, 0.5, 15.5, 15.1, 15.5);

    public BlockCaddyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeAt(level, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return shapeAt(level, pos);
    }

    private static VoxelShape shapeAt(BlockGetter level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof BlockCaddyBlockEntity caddy && !caddy.getCargo().isEmpty()
                ? Shapes.or(CADDY_SHAPE, CARGO_SHAPE)
                : CADDY_SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockCaddyBlockEntity(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level,
            BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof BlockCaddyBlockEntity caddy) {
            return BlockCaddyItem.createStack(caddy.getCargo());
        }
        return BlockCaddyItem.createStack(ItemStack.EMPTY);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return null;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Entity breaker = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (breaker instanceof Player player && player.isCreative()) {
            return List.of();
        }

        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack drop = new ItemStack(Items.BLOCK_CADDY.get());
        if (entity instanceof BlockCaddyBlockEntity caddy && !caddy.getCargo().isEmpty()) {
            BlockCaddyItem.setCargoStack(drop, caddy.getCargo());
        }
        return List.of(drop);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (canOpenChestCargo(level, pos, player, hand)) {
            if (!level.isClientSide) {
                BlockCaddyBlockEntity caddy = (BlockCaddyBlockEntity) level.getBlockEntity(pos);
                BlockCaddyInventory.openForCargo(player, caddy.getCargo(), caddy::setCargo);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!player.isShiftKeyDown()
                && stack.getItem() instanceof net.minecraft.world.item.BlockItem
                && level.getBlockEntity(pos) instanceof BlockCaddyBlockEntity caddy
                && caddy.getCargo().isEmpty()) {
            ItemStack candidate = BlockCaddyItem.createStack(ItemStack.EMPTY);
            if (!BlockCaddyItem.tryLoadFromBlockItem(candidate, stack)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                caddy.setCargo(BlockCaddyItem.getCargoStack(candidate));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!canOpenChestCargo(level, pos, player, InteractionHand.MAIN_HAND)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockCaddyBlockEntity caddy = (BlockCaddyBlockEntity) level.getBlockEntity(pos);
            BlockCaddyInventory.openForCargo(player, caddy.getCargo(), caddy::setCargo);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean canOpenChestCargo(Level level, BlockPos pos, Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()
                && level.getBlockEntity(pos) instanceof BlockCaddyBlockEntity caddy
                && BlockCaddyItem.isChestCargo(BlockCaddyItem.createStack(caddy.getCargo()));
    }
}
