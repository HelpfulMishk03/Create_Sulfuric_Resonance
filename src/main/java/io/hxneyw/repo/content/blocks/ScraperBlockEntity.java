package io.hxneyw.repo.content.blocks;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ScraperBlockEntity extends BlockEntity {
    private static final String FLAP_TAG = "ScraperFlap";
    private final Set<TransportedItemStack> triggeredFlaps =
            Collections.newSetFromMap(new WeakHashMap<>());

    private final LerpedFloat flap = LerpedFloat.linear()
            .startWithValue(0)
            .chase(0, .05f, Chaser.EXP);

    public ScraperBlockEntity(BlockPos pos, BlockState state) {
        super(io.hxneyw.repo.content.registry.AllBlockEntities.SCRAPER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ScraperBlockEntity scraper) {
        scraper.flap.tickChaser();
        if (level.isClientSide) return;
        BeltBlockEntity segment = BeltHelper.getSegmentBE(level, pos.below());
        if (segment == null) return;
        BeltBlockEntity controller = segment.getControllerBE();
        if (controller == null || controller.getInventory() == null) return;
        // Belt inventories are controller-wide. Confirm that this segment index
        // resolves to the belt block directly beneath the Scraper before looking
        // at carriers, or a stale/mismatched segment can capture remote cargo.
        if (!BeltHelper.getPositionForOffset(controller, segment.index).equals(pos.below())) return;

        Direction facing = state.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
        BlockPos storagePos = pos.relative(facing);
        Direction storageSide = facing.getOpposite();
        IItemHandler storage = level.getCapability(Capabilities.ItemHandler.BLOCK, storagePos, storageSide);
        float movement = controller.getDirectionAwareBeltMovementSpeed();
        if (movement == 0) return;
        float scoopPosition = segment.index + (movement > 0 ? .875f : .125f);
        // Belt positions are controller-wide offsets. Inspect the whole segment
        // occupied by the Scraper, then require the Caddy's live position to be
        // at or to have crossed the scoop center. The wider candidate window is
        // only an inventory lookup; it never pulls or captures a remote Caddy.
        float crossingWindow = controller.beltLength;
        controller.getInventory().applyToEachWithin(scoopPosition, crossingWindow, transported -> {
            if (!(transported.stack.getItem() instanceof BlockCaddyItem)) return TransportedResult.doNothing();
            // Use the same world-space belt position as rendering. No historical
            // crossing is required: carriers inserted nearby must be detected too.
            net.minecraft.world.phys.Vec3 center = BeltHelper.getVectorForOffset(
                    controller, transported.beltPosition);
            double dx = Math.abs(center.x - (pos.getX() + .5));
            double dz = Math.abs(center.z - (pos.getZ() + .5));
            boolean alongX = controller.getBlockState().getValue(com.simibubi.create.content.kinetics.belt.BeltBlock.HORIZONTAL_FACING).getAxis() == Direction.Axis.X;
            if (dx > (alongX ? .20 : .50) || dz > (alongX ? .50 : .20)) {
                return TransportedResult.doNothing();
            }
            if (scraper.triggeredFlaps.add(transported)) scraper.openFlaps();
            io.hxneyw.repo.CreateSulfuricResonance.LOGGER.info(
                    "CSR_PICKUP scraper={} controller={} index={} beltOffset={} carrierWorld={} previousOffset={}",
                    pos, controller.getBlockPos(), segment.index, transported.beltPosition,
                    center, transported.prevBeltPosition);
            ItemStack cargo = BlockCaddyItem.getCargoStack(transported.stack);
            ItemStack output = transported.stack.copyWithCount(transported.stack.getCount());
            BlockCaddyItem.clearBeltTransition(output);
            if (storage != null && (cargo.isEmpty() || BlockCaddyItem.isStorageCargo(transported.stack))) {
                if (cannotFitBoth(storage, output, ItemStack.EMPTY)) {
                    transported.lockedExternally = true;
                    return TransportedResult.doNothing();
                }
                if (!insert(storage, output).isEmpty()) {
                    transported.lockedExternally = true;
                    return TransportedResult.doNothing();
                }
                return TransportedResult.removeItem();
            }
            if (!cargo.isEmpty() && storage != null) {
                ItemStack cargoBatch = cargo.copyWithCount(transported.stack.getCount());
                ItemStack caddyBatch = output.copy();
                BlockCaddyItem.setCargoStack(caddyBatch, ItemStack.EMPTY);
                if (cannotFitBoth(storage, cargoBatch, caddyBatch)) {
                    transported.lockedExternally = true;
                    return TransportedResult.doNothing();
                }
                ItemStack remainder = insert(storage, cargoBatch);
                if (!remainder.isEmpty()) {
                    transported.lockedExternally = true;
                    return TransportedResult.doNothing();
                }
                if (!insert(storage, caddyBatch).isEmpty()) {
                    // The simulation pass reserves both stacks before either is
                    // inserted, so this is only reachable if storage changed mid-tick.
                    transported.lockedExternally = true;
                    return TransportedResult.doNothing();
                }
                return TransportedResult.removeItem();
            }

            var outputPos = pos.relative(facing).getCenter();
            if (!cargo.isEmpty() && !BlockCaddyItem.isStorageCargo(transported.stack)) {
                ItemStack emptyCaddy = output.copy();
                BlockCaddyItem.setCargoStack(emptyCaddy, ItemStack.EMPTY);
                eject(level, outputPos, facing, cargo.copyWithCount(transported.stack.getCount()));
                eject(level, outputPos, facing, emptyCaddy);
            } else {
                eject(level, outputPos, facing, output);
            }
            return TransportedResult.removeItem();
        });
    }

    private static void eject(Level level, net.minecraft.world.phys.Vec3 outputPos,
            Direction facing, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(level, outputPos.x, outputPos.y, outputPos.z, stack);
        itemEntity.setDeltaMovement(facing.getStepX() * .12, .08, facing.getStepZ() * .12);
        level.addFreshEntity(itemEntity);
    }

    public void openFlaps() {
        flap.setValue(-1);
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public float getFlapValue(float partialTick) {
        return flap.getValue(partialTick);
    }

    private static boolean cannotFitBoth(IItemHandler handler, ItemStack first, ItemStack second) {
        ItemStack[] simulatedSlots = new ItemStack[handler.getSlots()];
        for (int slot = 0; slot < simulatedSlots.length; slot++) {
            simulatedSlots[slot] = handler.getStackInSlot(slot).copy();
        }
        return !simulateInsert(handler, simulatedSlots, first).isEmpty()
                || !simulateInsert(handler, simulatedSlots, second).isEmpty();
    }

    private static ItemStack simulateInsert(IItemHandler handler, ItemStack[] slots, ItemStack stack) {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < slots.length && !remainder.isEmpty(); slot++) {
            ItemStack existing = slots[slot];
            if (!handler.isItemValid(slot, remainder)) {
                continue;
            }
            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, remainder)) {
                continue;
            }
            int limit = Math.min(handler.getSlotLimit(slot), remainder.getMaxStackSize());
            int existingCount = existing.isEmpty() ? 0 : existing.getCount();
            int inserted = Math.min(remainder.getCount(), Math.clamp(limit - existingCount, 0, limit));
            if (inserted == 0) {
                continue;
            }
            slots[slot] = existing.isEmpty()
                    ? remainder.copyWithCount(inserted)
                    : existing.copyWithCount(existingCount + inserted);
            remainder.shrink(inserted);
        }
        return remainder;
    }

    private static ItemStack insert(IItemHandler handler, ItemStack stack) {
        for (int slot = 0; slot < handler.getSlots() && !stack.isEmpty(); slot++)
            stack = handler.insertItem(slot, stack, false);
        return stack;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat(FLAP_TAG, flap.getValue(1));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        flap.setValue(tag.getFloat(FLAP_TAG));
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
