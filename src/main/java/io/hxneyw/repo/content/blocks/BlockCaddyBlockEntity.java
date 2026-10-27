package io.hxneyw.repo.content.blocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockCaddyBlockEntity extends BlockEntity {
    private static final String CARGO_TAG = "Cargo";
    private ItemStack cargo = ItemStack.EMPTY;
    private BlockState cachedCargoState;
    private BlockState cachedCargoBlockEntityState;
    private BlockEntity cachedCargoBlockEntity;
    private BlockState cachedOutlineState;
    private VoxelShape cachedCargoOutline;
    private long cachedOutlineTick = Long.MIN_VALUE;

    public BlockCaddyBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.BLOCK_CADDY.get(), pos, state);
    }

    public ItemStack getCargo() {
        return cargo;
    }

    /** Resolves the saved cargo block state once instead of rebuilding it for every shape query. */
    @Nullable
    public BlockState getCargoBlockState() {
        if (cargo.isEmpty()) return null;
        if (cachedCargoState == null) {
            cachedCargoState = BlockCaddyItem.getCargoBlockState(
                    BlockCaddyItem.createStack(cargo));
        }
        return cachedCargoState;
    }

    @Nullable
    public BlockEntity getCargoBlockEntity(BlockState cargoState) {
        if (level == null) return null;
        if (cargoState.equals(cachedCargoBlockEntityState)) return cachedCargoBlockEntity;
        cachedCargoBlockEntityState = cargoState;
        cachedCargoBlockEntity = null;
        if (!(cargoState.getBlock() instanceof EntityBlock entityBlock)
                || !cargoState.hasBlockEntity()) return null;
        BlockEntity cargoEntity = entityBlock.newBlockEntity(worldPosition, cargoState);
        if (cargoEntity == null) return null;
        cargoEntity.setLevel(level);
        ItemStack cargoStack = BlockCaddyItem.getCargoStack(
                BlockCaddyItem.createStack(cargo));
        CustomData data = cargoStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            cargoEntity.loadWithComponents(data.copyTag(), level.registryAccess());
        }
        cachedCargoBlockEntity = cargoEntity;
        return cachedCargoBlockEntity;
    }

    /** Outline shapes are queried repeatedly while pointing at a Caddy; cache this static cargo geometry. */
    @Nullable
    public VoxelShape getCachedCargoOutline(BlockState cargoState) {
        long gameTime = level == null ? Long.MIN_VALUE : level.getGameTime();
        return gameTime == cachedOutlineTick && cargoState.equals(cachedOutlineState)
                ? cachedCargoOutline : null;
    }

    public void cacheCargoOutline(BlockState cargoState, VoxelShape shape) {
        cachedOutlineState = cargoState;
        cachedCargoOutline = shape;
        cachedOutlineTick = level == null ? Long.MIN_VALUE : level.getGameTime();
    }

    private void invalidateCargoShapeCache() {
        cachedCargoState = null;
        cachedCargoBlockEntityState = null;
        cachedCargoBlockEntity = null;
        cachedOutlineState = null;
        cachedCargoOutline = null;
        cachedOutlineTick = Long.MIN_VALUE;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateCargoBlockState();
    }

    public void setCargo(ItemStack stack) {
        cargo = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        invalidateCargoShapeCache();
        BlockCaddyItem.ensureCargoBlockEntityDataId(cargo);
        setChanged();
        if (level != null) {
            updateCargoBlockState();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void updateCargoBlockState() {
        if (level == null) return;
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BlockCaddyBlock block)
                || !state.hasProperty(BlockCaddyBlock.HAS_CARGO)
                || !state.hasProperty(BlockCaddyBlock.CARGO_SHAPE_ID)) return;

        boolean hasCargo = !cargo.isEmpty();
        int oldId = state.getValue(BlockCaddyBlock.CARGO_SHAPE_ID);
        int shapeId = 0;
        if (hasCargo) {
            VoxelShape fullShape = block.getCollisionShape(
                    state, level, worldPosition, CollisionContext.empty());
            if (level.isClientSide) {
                CaddyCollisionShapeRegistry.bind(oldId, fullShape);
                shapeId = oldId > 0 ? oldId : CaddyCollisionShapeRegistry.FALLBACK_ID;
            } else {
                shapeId = CaddyCollisionShapeRegistry.register(fullShape, oldId);
            }
        }

        if (level.isClientSide) return;
        BlockState updated = state.setValue(BlockCaddyBlock.HAS_CARGO, hasCargo)
                .setValue(BlockCaddyBlock.CARGO_SHAPE_ID, shapeId);
        if (!updated.equals(state)) level.setBlock(worldPosition, updated, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!cargo.isEmpty()) {
            BlockCaddyItem.ensureCargoBlockEntityDataId(cargo);
            tag.put(CARGO_TAG, cargo.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(CARGO_TAG, CompoundTag.TAG_COMPOUND)) {
            CompoundTag serializedCargo = tag.getCompound(CARGO_TAG).copy();
            BlockCaddyItem.repairSerializedCargoStack(serializedCargo);
            cargo = ItemStack.parse(registries, serializedCargo).orElse(ItemStack.EMPTY);
            BlockCaddyItem.ensureCargoBlockEntityDataId(cargo);
        } else {
            cargo = ItemStack.EMPTY;
        }
        invalidateCargoShapeCache();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries
    ) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
