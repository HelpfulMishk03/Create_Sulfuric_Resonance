package io.hxneyw.repo.content.blocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.items.BlockCaddyItem;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockCaddyBlockEntity extends BlockEntity {
    private static final String CARGO_TAG = "Cargo";
    private ItemStack cargo = ItemStack.EMPTY;

    public BlockCaddyBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.BLOCK_CADDY.get(), pos, state);
    }

    public ItemStack getCargo() {
        return cargo;
    }

    public void setCargo(ItemStack stack) {
        cargo = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        BlockCaddyItem.ensureCargoBlockEntityDataId(cargo);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!cargo.isEmpty()) {
            BlockCaddyItem.ensureCargoBlockEntityDataId(cargo);
            tag.put(CARGO_TAG, cargo.save(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(CARGO_TAG, CompoundTag.TAG_COMPOUND)) {
            CompoundTag serializedCargo = tag.getCompound(CARGO_TAG).copy();
            BlockCaddyItem.repairSerializedCargoStack(serializedCargo);
            cargo = ItemStack.parse(registries, serializedCargo).orElse(ItemStack.EMPTY);
            BlockCaddyItem.ensureCargoBlockEntityDataId(cargo);
        } else {
            cargo = ItemStack.EMPTY;
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            @NotNull Connection connection,
            @NotNull ClientboundBlockEntityDataPacket packet,
            @NotNull HolderLookup.Provider registries
    ) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleUpdateTag(tag, registries);
        }
    }
}
