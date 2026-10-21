package io.hxneyw.repo.content.items;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.AllBlocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;
import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import io.hxneyw.repo.content.blocks.BlockCaddyBlockEntity;

@EventBusSubscriber(modid = "sulfuricresonance")
public final class BlockCaddyInteractionHandler {
    private BlockCaddyInteractionHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLoadBlockIntoNearbyBeltCaddy(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()
                || event.getHand() != InteractionHand.MAIN_HAND
                || event.getEntity().isShiftKeyDown()
                || !event.getEntity().mayBuild()) {
            return;
        }

        ItemStack held = event.getEntity().getMainHandItem();
        if (!(held.getItem() instanceof BlockItem)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        Set<BlockPos> checkedControllers = new HashSet<>();
        TransportedItemStack nearest = null;
        BeltBlockEntity nearestController = null;
        double nearestDistance = 25.0;

        // A belt funnel can cover the belt's click target. Check the clicked
        // block and its immediate neighbors so loading still works when the
        // player aims at that attached Create block.
        for (Direction direction : Direction.values()) {
            BlockPos beltPos = clickedPos.relative(direction);
            if (!AllBlocks.BELT.has(level.getBlockState(beltPos))) {
                continue;
            }

            BeltBlockEntity segment = BeltHelper.getSegmentBE(level, beltPos);
            BeltBlockEntity controller = segment == null ? null : segment.getControllerBE();
            if (controller == null
                    || controller.getInventory() == null
                    || !checkedControllers.add(controller.getBlockPos())) {
                continue;
            }

            for (TransportedItemStack transported : controller.getInventory().getTransportedItems()) {
                if (!(transported.stack.getItem() instanceof BlockCaddyItem)
                        || BlockCaddyItem.getCargoItem(transported.stack) != null) {
                    continue;
                }

                Vec3 caddyPosition = BeltHelper.getVectorForOffset(controller, transported.beltPosition);
                double distance = caddyPosition.distanceToSqr(event.getEntity().position());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = transported;
                    nearestController = controller;
                }
            }
        }

        if (nearest == null || nearestController == null) {
            return;
        }

        if (!level.isClientSide()) {
            if (!BlockCaddyItem.tryLoadFromBlockItem(nearest.stack, held)) {
                return;
            }
            if (!event.getEntity().isCreative()) {
                held.shrink(1);
            }
            nearestController.setChanged();
            nearestController.sendData();
        }

        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onShiftRightClickBelt(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !event.getEntity().isShiftKeyDown()) {
            return;
        }

        Level level = event.getLevel();
        if (level.getBlockEntity(event.getPos()) instanceof BlockCaddyBlockEntity placedCaddy
                && BlockCaddyItem.isChestCargo(BlockCaddyItem.createStack(placedCaddy.getCargo()))) {
            if (!level.isClientSide) {
                BlockCaddyInventory.openForCargo(
                        event.getEntity(), placedCaddy.getCargo(), placedCaddy::setCargo);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            event.setCanceled(true);
            return;
        }

        BeltBlockEntity segment = BeltHelper.getSegmentBE(level, event.getPos());
        if (segment == null) {
            return;
        }
        BeltBlockEntity controller = segment.getControllerBE();
        if (controller == null || controller.getInventory() == null) {
            return;
        }

        TransportedItemStack nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        float segmentCenter = segment.index + .5f;
        for (TransportedItemStack transported : controller.getInventory().getTransportedItems()) {
            if (Math.abs(transported.beltPosition - segmentCenter) > .5f
                    || !BlockCaddyItem.isChestCargo(transported.stack)) {
                continue;
            }
            Vec3 position = BeltHelper.getVectorForOffset(controller, transported.beltPosition);
            double distance = position.distanceToSqr(event.getEntity().position());
            if (distance < nearestDistance) {
                nearest = transported;
                nearestDistance = distance;
            }
        }
        if (nearest == null) {
            return;
        }

        if (!level.isClientSide) {
            BlockCaddyInventory.openForCaddy(event.getEntity(), nearest.stack);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
