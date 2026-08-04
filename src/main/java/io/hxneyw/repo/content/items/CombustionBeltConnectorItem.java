package io.hxneyw.repo.content.items;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

/**
 * Places and extends only marked Combustion Belt chains.
 *
 * Existing normal Create belts are deliberately rejected.
 */
public class CombustionBeltConnectorItem extends Item {

    /*
     * Create may rebuild and initialize a sliced/extended belt over more than
     * one server tick. Re-mark several times so the final rebuilt chain cannot
     * retain ordinary Create belt segments.
     */
    private static final int[] MARK_RETRY_DELAYS = {
            1, 2, 4, 8
    };

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
         * Extension is permitted only when the clicked chain already contains
         * at least one persistent Combustion Belt marker.
         *
         * This also heals a partially marked chain left by an older extension
         * bug: after Create rebuilds it, every resulting segment is re-marked.
         */
        if (AllBlocks.BELT.has(clickedState)) {
            if (player == null) {
                return InteractionResult.FAIL;
            }

            List<BlockPos> originalChainAnchors =
                    getCombustionChainAnchors(
                            level,
                            clickedPosition
                    );

            if (originalChainAnchors.isEmpty()) {
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
                        originalChainAnchors
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
         * Discard an old selection if the original shaft disappeared or the
         * player moved too far away.
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
         * First click: remember this shaft on the custom item.
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

            scheduleCombustionBeltChainMarking(
                    level,
                    List.of(
                            firstShaft,
                            clickedPosition
                    )
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

    /**
     * Returns every position in the clicked chain only when at least one
     * segment is already marked as a Combustion Belt.
     */
    private static List<BlockPos> getCombustionChainAnchors(
            Level level,
            BlockPos beltPosition
    ) {
        BlockEntity blockEntity =
                level.getBlockEntity(beltPosition);

        if (!(blockEntity instanceof BeltBlockEntity belt)) {
            return List.of();
        }

        List<BlockPos> chain =
                BeltBlock.getBeltChain(
                        level,
                        belt.getController()
                );

        boolean combustionChain = false;

        for (BlockPos segmentPosition : chain) {
            BlockEntity segmentEntity =
                    level.getBlockEntity(segmentPosition);

            if (segmentEntity
                    instanceof CombustionBeltAccessor accessor
                    && accessor
                    .sulfuricresonance$isCombustionBelt()) {
                combustionChain = true;
                break;
            }
        }

        if (!combustionChain) {
            return List.of();
        }

        ArrayList<BlockPos> anchors =
                new ArrayList<>(chain.size() + 1);

        anchors.add(beltPosition.immutable());

        for (BlockPos position : chain) {
            anchors.add(position.immutable());
        }

        return anchors;
    }

    private static void scheduleCombustionBeltChainMarking(
            Level level,
            List<BlockPos> anchors
    ) {
        MinecraftServer server = level.getServer();

        if (server == null || anchors.isEmpty()) {
            return;
        }

        LinkedHashSet<BlockPos> uniqueAnchors =
                new LinkedHashSet<>();

        for (BlockPos anchor : anchors) {
            uniqueAnchors.add(anchor.immutable());
        }

        List<BlockPos> savedAnchors =
                List.copyOf(uniqueAnchors);

        /*
         * Mark immediately when Create has already initialized the rebuilt
         * chain, then repeat on later ticks for delayed initialization.
         */
        markCombustionBeltChains(
                level,
                savedAnchors
        );

        for (int delay : MARK_RETRY_DELAYS) {
            server.tell(
                    new TickTask(
                            server.getTickCount() + delay,
                            () -> markCombustionBeltChains(
                                    level,
                                    savedAnchors
                            )
                    )
            );
        }
    }

    /**
     * Resolve every current controller reachable from the saved original chain
     * positions, then mark every segment in each rebuilt chain.
     *
     * This handles controller changes, delayed block-entity initialization,
     * belt slicing, and belt extension.
     */
    private static void markCombustionBeltChains(
            Level level,
            List<BlockPos> anchors
    ) {
        Set<BlockPos> controllerPositions =
                new LinkedHashSet<>();

        for (BlockPos anchor : anchors) {
            if (!level.isLoaded(anchor)) {
                continue;
            }

            BlockEntity blockEntity =
                    level.getBlockEntity(anchor);

            if (blockEntity instanceof BeltBlockEntity belt) {
                controllerPositions.add(
                        belt.getController().immutable()
                );
            }
        }

        for (BlockPos controllerPosition :
                controllerPositions) {

            for (BlockPos segmentPosition :
                    BeltBlock.getBeltChain(
                            level,
                            controllerPosition
                    )) {

                BlockEntity segmentEntity =
                        level.getBlockEntity(
                                segmentPosition
                        );

                if (!(segmentEntity
                        instanceof BeltBlockEntity segment)
                        || !(segment
                        instanceof CombustionBeltAccessor accessor)) {
                    continue;
                }

                accessor
                        .sulfuricresonance$setCombustionBelt(
                                true
                        );

                segment.setChanged();
                segment.sendData();
            }
        }
    }
}
