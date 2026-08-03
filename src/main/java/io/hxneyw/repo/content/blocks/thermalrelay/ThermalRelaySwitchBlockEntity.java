package io.hxneyw.repo.content.blocks.thermalrelay;

import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One placed switch may contain several selected Molten Rotor Furnace links.
 */
public class ThermalRelaySwitchBlockEntity extends BlockEntity {

    private static final String NETWORK_TAG = "RelayNetwork";
    private static final String LINKED_FURNACES_TAG = "LinkedFurnaces";

    private static final String POSITION_TAG = "Position";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String IDENTITY_TAG = "Identity";

    private static final int UPDATE_INTERVAL = 10;

    private static final Set<ThermalRelaySwitchBlockEntity>
            CLIENT_RELAYS = Collections.newSetFromMap(
            new WeakHashMap<>()
    );

    @Nullable
    private UUID networkId;

    private final List<
            ThermalRelaySwitchItem.FurnaceLink
            > linkedFurnaces = new ArrayList<>();

    private int updateTicker;

    public ThermalRelaySwitchBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMAL_RELAY_SWITCH.get(),
                pos,
                state
        );
    }

    public static void serverTick(
            Level level,
            ThermalRelaySwitchBlockEntity relay
    ) {
        if (level.isClientSide) {
            return;
        }

        relay.updateTicker++;

        if (relay.updateTicker < UPDATE_INTERVAL) {
            return;
        }

        relay.updateTicker = 0;

        /*
         * Phase 2 stores and visualizes connections only.
         * Multi-furnace redstone evaluation is added in the next phase.
         */
    }

    public void setConnections(
            UUID networkId,
            List<ThermalRelaySwitchItem.FurnaceLink> links
    ) {
        this.networkId = networkId;
        this.linkedFurnaces.clear();

        Map<FurnaceKey,
                ThermalRelaySwitchItem.FurnaceLink> unique =
                new LinkedHashMap<>();

        for (ThermalRelaySwitchItem.FurnaceLink link :
                links) {
            unique.putIfAbsent(
                    FurnaceKey.from(link),
                    link
            );
        }

        this.linkedFurnaces.addAll(unique.values());

        if (this.linkedFurnaces.isEmpty()) {
            this.networkId = null;
        }

        markAndSync();
    }

    public void clearConnections() {
        if (this.networkId == null
                && this.linkedFurnaces.isEmpty()) {
            return;
        }

        this.networkId = null;
        this.linkedFurnaces.clear();

        markAndSync();
    }

    private ListTag createStoredLinks() {
        ListTag storedLinks = new ListTag();

        for (ThermalRelaySwitchItem.FurnaceLink link
                : this.linkedFurnaces) {
            CompoundTag linkTag = new CompoundTag();

            linkTag.putLong(
                    POSITION_TAG,
                    link.position().asLong()
            );

            linkTag.putString(
                    DIMENSION_TAG,
                    link.dimension()
            );

            linkTag.putUUID(
                    IDENTITY_TAG,
                    link.furnaceIdentity()
            );

            storedLinks.add(linkTag);
        }

        return storedLinks;
    }

    @Nullable
    public UUID getNetworkId() {
        return this.networkId;
    }

    public List<ThermalRelaySwitchItem.FurnaceLink>
    getFurnaceLinks() {
        return List.copyOf(this.linkedFurnaces);
    }

    public static List<ThermalRelaySwitchBlockEntity>
    getLoadedClientRelays() {
        return List.copyOf(CLIENT_RELAYS);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.level != null
                && this.level.isClientSide) {
            CLIENT_RELAYS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        CLIENT_RELAYS.remove(this);
        super.setRemoved();
    }

    private void markAndSync() {
        setChanged();

        if (this.level == null
                || this.level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();

        this.level.sendBlockUpdated(
                this.worldPosition,
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

        UUID savedNetworkId = this.networkId;

        if (savedNetworkId == null
                || this.linkedFurnaces.isEmpty()) {
            return;
        }

        tag.putUUID(
                NETWORK_TAG,
                savedNetworkId
        );

        tag.put(
                LINKED_FURNACES_TAG,
                createStoredLinks()
        );
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        this.networkId = null;
        this.linkedFurnaces.clear();

        if (!tag.hasUUID(NETWORK_TAG)
                || !tag.contains(
                LINKED_FURNACES_TAG,
                Tag.TAG_LIST
        )) {
            this.updateTicker = 0;
            return;
        }

        ListTag storedLinks = tag.getList(
                LINKED_FURNACES_TAG,
                Tag.TAG_COMPOUND
        );

        Map<FurnaceKey,
                ThermalRelaySwitchItem.FurnaceLink> unique =
                new LinkedHashMap<>();

        for (int index = 0;
             index < storedLinks.size();
             index++) {
            CompoundTag linkTag =
                    storedLinks.getCompound(index);

            if (!linkTag.contains(POSITION_TAG)
                    || !linkTag.contains(DIMENSION_TAG)
                    || !linkTag.hasUUID(IDENTITY_TAG)) {
                continue;
            }

            ThermalRelaySwitchItem.FurnaceLink link =
                    new ThermalRelaySwitchItem.FurnaceLink(
                            BlockPos.of(
                                    linkTag.getLong(POSITION_TAG)
                            ),
                            linkTag.getString(DIMENSION_TAG),
                            linkTag.getUUID(IDENTITY_TAG)
                    );

            unique.putIfAbsent(
                    FurnaceKey.from(link),
                    link
            );
        }

        if (!unique.isEmpty()) {
            this.networkId =
                    tag.getUUID(NETWORK_TAG);
            this.linkedFurnaces.addAll(
                    unique.values()
            );
        }

        this.updateTicker = 0;
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
    public @Nullable Packet<ClientGamePacketListener>
    getUpdatePacket() {
        return ClientboundBlockEntityDataPacket
                .create(this);
    }

    @Override
    public void onDataPacket(
            @NotNull Connection connection,
            @NotNull ClientboundBlockEntityDataPacket packet,
            @NotNull HolderLookup.Provider registries
    ) {
        super.onDataPacket(
                connection,
                packet,
                registries
        );
    }

    private record FurnaceKey(
            String dimension,
            BlockPos position,
            UUID furnaceIdentity
    ) {
        private static FurnaceKey from(
                ThermalRelaySwitchItem.FurnaceLink link
        ) {
            return new FurnaceKey(
                    link.dimension(),
                    link.position(),
                    link.furnaceIdentity()
            );
        }
    }
}
