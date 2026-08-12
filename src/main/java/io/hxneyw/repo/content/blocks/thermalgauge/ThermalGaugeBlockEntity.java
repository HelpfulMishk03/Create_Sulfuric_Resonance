package io.hxneyw.repo.content.blocks.thermalgauge;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalGaugeBlockEntity extends BlockEntity {

    private static final Set<ThermalGaugeBlockEntity> CLIENT_GAUGES =
            Collections.newSetFromMap(new WeakHashMap<>());

    public static final int MIN_TEMPERATURE = 20;
    public static final int MAX_TEMPERATURE = 1599;

    private static final String NETWORK_TAG = "RelayNetwork";
    private static final String LINKED_FURNACES_TAG = "LinkedFurnaces";
    private static final String POSITION_TAG = "Position";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String IDENTITY_TAG = "Identity";
    private static final String LEGACY_POSITION_TAG = "LinkedFurnacePos";
    private static final String LEGACY_DIMENSION_TAG = "LinkedFurnaceDimension";
    private static final String LEGACY_IDENTITY_TAG = "LinkedFurnaceIdentity";
    private static final String TEMPERATURE_TAG = "DisplayTemperature";
    private static final String CONNECTED_TAG = "NetworkConnected";
    private static final int UPDATE_INTERVAL = 5;

    @Nullable
    private UUID networkId;

    @Nullable
    private ThermalRelaySwitchItem.FurnaceLink linkedFurnace;

    private int displayTemperature = MIN_TEMPERATURE;
    private boolean networkConnected;
    private int updateTicker;

    public ThermalGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.THERMAL_GAUGE.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockState state,
            ThermalGaugeBlockEntity gauge
    ) {
        if (level.isClientSide) {
            return;
        }

        gauge.updateTicker++;
        if (gauge.updateTicker < UPDATE_INTERVAL) {
            return;
        }

        gauge.updateTicker = 0;
        gauge.evaluateNetwork(level, state);
    }

    public void setConnection(
            @Nullable UUID networkId,
            @NotNull ThermalRelaySwitchItem.FurnaceLink link
    ) {
        this.networkId = networkId != null
                ? networkId
                : link.furnaceIdentity();
        this.linkedFurnace = link;
        this.updateTicker = UPDATE_INTERVAL;
        this.setChanged();

        if (this.level != null && !this.level.isClientSide) {
            evaluateNetwork(this.level, getBlockState());
        }
    }

    public void clearConnection() {
        this.networkId = null;
        this.linkedFurnace = null;
        this.displayTemperature = MIN_TEMPERATURE;
        this.networkConnected = false;
        this.updateTicker = 0;
        markAndSync();
    }

    @Nullable
    public UUID getNetworkId() {
        return networkId;
    }

    @Nullable
    public ThermalRelaySwitchItem.FurnaceLink getFurnaceLink() {
        return linkedFurnace;
    }

    public int getDisplayTemperature() {
        return displayTemperature;
    }

    public boolean isNetworkConnected() {
        return networkConnected;
    }

    public boolean doesNotMatchLink(
            @NotNull ThermalRelaySwitchItem.FurnaceLink link
    ) {
        return linkedFurnace == null || !linkedFurnace.equals(link);
    }

    public static List<ThermalGaugeBlockEntity> getLoadedClientGauges() {
        return List.copyOf(CLIENT_GAUGES);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level == null) {
            return;
        }

        if (level.isClientSide) {
            CLIENT_GAUGES.add(this);
        } else {
            updateTicker = UPDATE_INTERVAL;
        }
    }

    @Override
    public void setRemoved() {
        CLIENT_GAUGES.remove(this);
        super.setRemoved();
    }

    private void evaluateNetwork(Level level, BlockState state) {
        int nextTemperature = MIN_TEMPERATURE;
        boolean nextConnected = false;
        ThermalRelaySwitchItem.FurnaceLink link = linkedFurnace;

        if (link != null
                && level.dimension().location().toString().equals(link.dimension())) {
            BlockPos furnacePos = link.position();

            if (level.isLoaded(furnacePos)
                    && level.getBlockEntity(furnacePos) instanceof MoltenRotorBlockEntity furnace
                    && link.furnaceIdentity().equals(furnace.getFurnaceIdentity())) {
                nextTemperature = furnace.getDisplayTemperature();
                nextConnected = true;
            }
        }

        nextTemperature = Math.clamp(nextTemperature,
                MIN_TEMPERATURE, MAX_TEMPERATURE);

        if (displayTemperature == nextTemperature
                && networkConnected == nextConnected) {
            return;
        }

        displayTemperature = nextTemperature;
        networkConnected = nextConnected;
        setChanged();
        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
    }

    private void markAndSync() {
        setChanged();

        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        if (networkId != null) {
            tag.putUUID(NETWORK_TAG, networkId);
        }

        ThermalRelaySwitchItem.FurnaceLink link = linkedFurnace;
        if (link != null) {
            CompoundTag linkTag = new CompoundTag();
            linkTag.putLong(POSITION_TAG, link.position().asLong());
            linkTag.putString(DIMENSION_TAG, link.dimension());
            linkTag.putUUID(IDENTITY_TAG, link.furnaceIdentity());
            ListTag links = new ListTag();
            links.add(linkTag);
            tag.put(LINKED_FURNACES_TAG, links);
        }

        tag.putInt(TEMPERATURE_TAG, displayTemperature);
        tag.putBoolean(CONNECTED_TAG, networkConnected);
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        linkedFurnace = readLink(tag);
        networkId = tag.hasUUID(NETWORK_TAG)
                ? tag.getUUID(NETWORK_TAG)
                : linkedFurnace != null
                ? linkedFurnace.furnaceIdentity()
                : null;
        displayTemperature = tag.contains(TEMPERATURE_TAG, Tag.TAG_INT)
                ? Math.clamp(tag.getInt(TEMPERATURE_TAG),
                MIN_TEMPERATURE, MAX_TEMPERATURE)
                : MIN_TEMPERATURE;
        networkConnected = tag.getBoolean(CONNECTED_TAG);
    }

    @Nullable
    private static ThermalRelaySwitchItem.FurnaceLink readLink(
            CompoundTag tag
    ) {
        if (tag.contains(LINKED_FURNACES_TAG, Tag.TAG_LIST)) {
            ListTag links = tag.getList(
                    LINKED_FURNACES_TAG,
                    Tag.TAG_COMPOUND
            );

            for (int index = 0; index < links.size(); index++) {
                CompoundTag linkTag = links.getCompound(index);

                if (!linkTag.contains(POSITION_TAG, Tag.TAG_LONG)
                        || !linkTag.contains(DIMENSION_TAG, Tag.TAG_STRING)
                        || !linkTag.hasUUID(IDENTITY_TAG)) {
                    continue;
                }

                return new ThermalRelaySwitchItem.FurnaceLink(
                        BlockPos.of(linkTag.getLong(POSITION_TAG)),
                        linkTag.getString(DIMENSION_TAG),
                        linkTag.getUUID(IDENTITY_TAG)
                );
            }
        }

        if (tag.contains(LEGACY_POSITION_TAG, Tag.TAG_LONG)
                && tag.contains(LEGACY_DIMENSION_TAG, Tag.TAG_STRING)
                && tag.hasUUID(LEGACY_IDENTITY_TAG)) {
            return new ThermalRelaySwitchItem.FurnaceLink(
                    BlockPos.of(tag.getLong(LEGACY_POSITION_TAG)),
                    tag.getString(LEGACY_DIMENSION_TAG),
                    tag.getUUID(LEGACY_IDENTITY_TAG)
            );
        }

        return null;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(
            @NotNull HolderLookup.Provider registries
    ) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
