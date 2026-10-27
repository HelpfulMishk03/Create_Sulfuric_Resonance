package io.hxneyw.repo.content.items;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.AllBlocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import io.hxneyw.repo.content.blocks.BlockCaddyBlockEntity;

@EventBusSubscriber(modid = "sulfuricresonance")
@SuppressWarnings("unused")
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
        if (!BlockCaddyItem.isSupportedCargoStack(held)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        if (!AllBlocks.BELT.has(level.getBlockState(clickedPos))) {
            return;
        }
        BeltBlockEntity segment = BeltHelper.getSegmentBE(level, clickedPos);
        BeltBlockEntity controller = segment == null ? null : segment.getControllerBE();
        if (controller == null || controller.getInventory() == null) {
            return;
        }

        TransportedItemStack nearest = null;
        float clickedSegmentCenter = segment.index + .5f;
        double nearestDistance = Double.MAX_VALUE;
        for (TransportedItemStack transported : controller.getInventory().getTransportedItems()) {
            if (!(transported.stack.getItem() instanceof BlockCaddyItem)
                    || BlockCaddyItem.getCargoItem(transported.stack) != null
                    || Math.abs(transported.beltPosition - clickedSegmentCenter) > .5f) {
                continue;
            }

            Vec3 caddyPosition = BeltHelper.getVectorForOffset(controller, transported.beltPosition);
            double distance = caddyPosition.distanceToSqr(event.getEntity().position());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = transported;
            }
        }

        if (nearest == null) {
            return;
        }

        if (!level.isClientSide()) {
            net.minecraft.world.level.block.state.BlockState placedState =
                    BlockCaddyItem.getPlacementState(level, event.getEntity(),
                            event.getHand(), held, event.getHitVec());
            if (!BlockCaddyItem.tryLoadFromBlockItem(nearest.stack, held, placedState)) {
                return;
            }
            if (!event.getEntity().isCreative()) {
                held.shrink(1);
            }
            controller.setChanged();
            controller.sendData();
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
                && BlockCaddyItem.isStorageCargo(BlockCaddyItem.createStack(placedCaddy.getCargo()))) {
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
                    || !BlockCaddyItem.isStorageCargo(transported.stack)) {
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
            BlockCaddyInventory.openForCaddy(event.getEntity(), nearest.stack, stack -> {
                controller.setChanged();
                controller.sendData();
            });
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
