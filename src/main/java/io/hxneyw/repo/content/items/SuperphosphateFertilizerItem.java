package io.hxneyw.repo.content.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SuperphosphateFertilizerItem extends Item {
    private static final int TARGET_GROWTH_ATTEMPTS = 2;
    private static final float TARGET_GROWTH_CHANCE = 0.75F;
    private static final float ADJACENT_GROWTH_CHANCE = 0.35F;
    private static final int GROWTH_PARTICLE_EVENT = 1505;
    private static final Direction[] ADJACENT_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    public SuperphosphateFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos();
        BlockState targetState = level.getBlockState(targetPos);

        if (!(targetState.getBlock() instanceof BonemealableBlock target)
                || !target.isValidBonemealTarget(
                level,
                targetPos,
                targetState
        )) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            RandomSource random = serverLevel.random;

            if (random.nextFloat() < TARGET_GROWTH_CHANCE) {
                for (int attempt = 0;
                     attempt < TARGET_GROWTH_ATTEMPTS;
                     attempt++) {
                    growPlant(serverLevel, targetPos, random);
                }
            }

            for (Direction direction : ADJACENT_DIRECTIONS) {
                if (random.nextFloat() < ADJACENT_GROWTH_CHANCE) {
                    growPlant(
                            serverLevel,
                            targetPos.relative(direction),
                            random
                    );
                }
            }

            Player player = context.getPlayer();
            ItemStack stack = context.getItemInHand();

            if (player == null || !player.isCreative()) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void growPlant(
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof BonemealableBlock plant)
                || !plant.isValidBonemealTarget(level, pos, state)
                || !plant.isBonemealSuccess(
                level,
                random,
                pos,
                state
        )) {
            return;
        }

        plant.performBonemeal(level, random, pos, state);
        level.levelEvent(GROWTH_PARTICLE_EVENT, pos, 0);
    }
}
