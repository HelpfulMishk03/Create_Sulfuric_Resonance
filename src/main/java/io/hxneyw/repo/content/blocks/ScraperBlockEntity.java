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
import net.minecraft.world.level.block.entity.BlockEntityType;
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
        if (level.isClientSide || level.getGameTime() % 2 != 0) return;
        BeltBlockEntity segment = BeltHelper.getSegmentBE(level, pos.below());
        if (segment == null) return;
        BeltBlockEntity controller = segment.getControllerBE();
        if (controller == null || controller.getInventory() == null) return;

        Direction facing = state.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
        BlockPos storagePos = pos.relative(facing);
        Direction storageSide = facing.getOpposite();
        IItemHandler storage = level.getCapability(Capabilities.ItemHandler.BLOCK, storagePos, storageSide);
        controller.getInventory().applyToEachWithin(segment.index + .5f, .36f, transported -> {
            if (!(transported.stack.getItem() instanceof BlockCaddyItem)) return TransportedResult.doNothing();
            if (scraper.triggeredFlaps.add(transported)) scraper.openFlaps();
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

    private void openFlaps() {
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
            int inserted = Math.min(remainder.getCount(), Math.max(0, limit - existingCount));
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
