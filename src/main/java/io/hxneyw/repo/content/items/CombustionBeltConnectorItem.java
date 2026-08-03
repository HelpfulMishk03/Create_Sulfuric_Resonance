package io.hxneyw.repo.content.items;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class CombustionBeltConnectorItem extends Item {

    public CombustionBeltConnectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(
            UseOnContext context
    ) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedShaft = context.getClickedPos();

        /*
         * Sneaking clears the currently selected first shaft.
         */
        if (player != null && player.isShiftKeyDown()) {
            stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            return InteractionResult.SUCCESS;
        }

        boolean clickedPositionIsValid =
                BeltConnectorItem.validateAxis(
                        level,
                        clickedShaft
                );

        /*
         * Client side only reports whether the click is acceptable.
         * The server performs placement and inventory changes.
         */
        if (level.isClientSide()) {
            return clickedPositionIsValid
                    ? InteractionResult.SUCCESS
                    : InteractionResult.FAIL;
        }

        if (player == null || !clickedPositionIsValid) {
            return InteractionResult.FAIL;
        }

        BlockPos firstShaft =
                stack.get(AllDataComponents.BELT_FIRST_SHAFT);

        /*
         * Discard an old selection if the original shaft disappeared
         * or the player moved too far away.
         */
        if (firstShaft != null
                && (!BeltConnectorItem.validateAxis(level, firstShaft)
                || !firstShaft.closerThan(
                clickedShaft,
                BeltConnectorItem.maxLength() * 2
        ))) {

            stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            firstShaft = null;
        }

        /*
         * First click: remember this shaft on our custom item.
         */
        if (firstShaft == null) {
            stack.set(
                    AllDataComponents.BELT_FIRST_SHAFT,
                    clickedShaft
            );

            player.getCooldowns().addCooldown(this, 5);
            return InteractionResult.SUCCESS;
        }

        /*
         * Second click: use Create's validation rules.
         */
        if (!BeltConnectorItem.canConnect(
                level,
                firstShaft,
                clickedShaft
        )) {
            return InteractionResult.FAIL;
        }

        if (!firstShaft.equals(clickedShaft)) {
            /*
             * Place genuine Create belt blocks while the connector
             * itself remains a Sulfuric Resonance item.
             */
            BeltConnectorItem.createBelts(
                    level,
                    firstShaft,
                    clickedShaft
            );

            scheduleCombustionBeltMarking(
                    level,
                    firstShaft,
                    clickedShaft
            );

            AllAdvancements.BELT.awardTo(player);

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        if (!stack.isEmpty()) {
            stack.remove(AllDataComponents.BELT_FIRST_SHAFT);
            player.getCooldowns().addCooldown(this, 5);
        }

        return InteractionResult.SUCCESS;
    }

    private static void scheduleCombustionBeltMarking(
            Level level,
            BlockPos first,
            BlockPos second
    ) {
        MinecraftServer server = level.getServer();

        if (server == null) {
            return;
        }

        /*
         * Create's belt blocks are placed immediately, but their block
         * entities may not be available until the following server tick.
         */
        BlockPos savedFirst = first.immutable();
        BlockPos savedSecond = second.immutable();

        server.tell(
                new TickTask(
                        server.getTickCount() + 1,
                        () -> markCombustionBeltBetween(
                                level,
                                savedFirst,
                                savedSecond
                        )
                )
        );
    }

    private static void markCombustionBeltBetween(
            Level level,
            BlockPos first,
            BlockPos second
    ) {
        int differenceX = second.getX() - first.getX();
        int differenceY = second.getY() - first.getY();
        int differenceZ = second.getZ() - first.getZ();

        int stepX = Integer.signum(differenceX);
        int stepY = Integer.signum(differenceY);
        int stepZ = Integer.signum(differenceZ);

        int length = Math.max(
                Math.abs(differenceX),
                Math.max(
                        Math.abs(differenceY),
                        Math.abs(differenceZ)
                )
        );

        for (int index = 0; index <= length; index++) {
            BlockPos beltPosition = first.offset(
                    stepX * index,
                    stepY * index,
                    stepZ * index
            );

            BlockEntity blockEntity =
                    level.getBlockEntity(beltPosition);

            if (!(blockEntity instanceof BeltBlockEntity belt)) {
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