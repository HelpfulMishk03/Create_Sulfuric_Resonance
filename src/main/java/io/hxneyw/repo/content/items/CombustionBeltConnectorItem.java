package io.hxneyw.repo.content.items;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
        BlockPos clickedPosition = context.getClickedPos();
        BlockState clickedState =
                level.getBlockState(clickedPosition);

        /*
         * Existing belts may only be extended by their matching connector:
         *
         * - Combustion Belt Connector -> Combustion Belt
         * - Create Belt Connector     -> normal Create Belt
         *
         * This prevents the custom connector from converting or extending
         * an ordinary Create belt.
         */
        if (AllBlocks.BELT.has(clickedState)) {
            if (player == null
                    || !isCombustionBelt(
                    level,
                    clickedPosition
            )) {
                return InteractionResult.FAIL;
            }

            BlockHitResult hitResult =
                    new BlockHitResult(
                            context.getClickLocation(),
                            context.getClickedFace(),
                            clickedPosition,
                            context.isInside()
                    );

            ItemInteractionResult extensionResult =
                    BeltSlicer.useConnector(
                            clickedState,
                            level,
                            clickedPosition,
                            player,
                            context.getHand(),
                            hitResult,
                            new BeltSlicer.Feedback()
                    );

            if (!level.isClientSide()
                    && extensionResult.consumesAction()) {
                scheduleCombustionBeltChainMarking(
                        level,
                        clickedPosition
                );
            }

            return extensionResult.result();
        }

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
                        clickedPosition
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
                && (!BeltConnectorItem.validateAxis(
                level,
                firstShaft
        )
                || !firstShaft.closerThan(
                clickedPosition,
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
                    clickedPosition
            );

            player.getCooldowns().addCooldown(this, 5);
            return InteractionResult.SUCCESS;
        }

        /*
         * Second click: use Create's normal belt validation rules.
         */
        if (!BeltConnectorItem.canConnect(
                level,
                firstShaft,
                clickedPosition
        )) {
            return InteractionResult.FAIL;
        }

        if (!firstShaft.equals(clickedPosition)) {
            BeltConnectorItem.createBelts(
                    level,
                    firstShaft,
                    clickedPosition
            );

            /*
             * Create initializes the placed BeltBlockEntities after
             * placement. Mark the completed chain on the following
             * server tick.
             */
            scheduleCombustionBeltChainMarking(
                    level,
                    firstShaft
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

    private static boolean isCombustionBelt(
            Level level,
            BlockPos position
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(position);

        return blockEntity instanceof CombustionBeltAccessor accessor
                && accessor.sulfuricresonance$isCombustionBelt();
    }

    private static void scheduleCombustionBeltChainMarking(
            Level level,
            BlockPos beltPosition
    ) {
        MinecraftServer server = level.getServer();

        if (server == null) {
            return;
        }

        BlockPos savedPosition = beltPosition.immutable();

        server.tell(
                new TickTask(
                        server.getTickCount() + 1,
                        () -> markCombustionBeltChain(
                                level,
                                savedPosition
                        )
                )
        );
    }

    private static void markCombustionBeltChain(
            Level level,
            BlockPos beltPosition
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(beltPosition);

        if (!(blockEntity instanceof BeltBlockEntity belt)) {
            return;
        }

        BlockPos controllerPosition =
                belt.getController();

        for (BlockPos segmentPosition :
                BeltBlock.getBeltChain(
                        level,
                        controllerPosition
                )) {

            BlockEntity segmentEntity =
                    level.getBlockEntity(segmentPosition);

            if (!(segmentEntity instanceof BeltBlockEntity segment)) {
                continue;
            }

            if (!(segment instanceof CombustionBeltAccessor accessor)) {
                continue;
            }

            accessor.sulfuricresonance$setCombustionBelt(true);
            segment.setChanged();
            segment.sendData();
        }
    }
}