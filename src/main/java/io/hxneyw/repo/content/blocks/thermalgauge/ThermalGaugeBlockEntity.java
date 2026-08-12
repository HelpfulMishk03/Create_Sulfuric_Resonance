package io.hxneyw.repo.content.blocks.thermalgauge;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.WeakHashMap;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalGaugeBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    private static final Set<ThermalGaugeBlockEntity> CLIENT_GAUGES =
            Collections.newSetFromMap(new WeakHashMap<>());

    private static Function<ThermalGaugeBlockEntity, PanelSlot> targetedSlotResolver =
            gauge -> null;

    public static final int MIN_TEMPERATURE = 20;
    public static final int MAX_TEMPERATURE = 1599;

    private static final String GAUGES_TAG = "Gauges";
    private static final String SLOT_TAG = "Slot";
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
    private static final String HEAT_TIER_TAG = "HeatTier";
    private static final int UPDATE_INTERVAL = 1;

    private final EnumMap<PanelSlot, GaugeData> gauges = new EnumMap<>(PanelSlot.class);
    private int updateTicker;

    public ThermalGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.THERMAL_GAUGE.get(), pos, state);
    }

    public static void setTargetedSlotResolver(
            Function<ThermalGaugeBlockEntity, PanelSlot> resolver
    ) {
        targetedSlotResolver = resolver;
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
        gauge.evaluateNetworks(level, state);
    }

    public boolean addGauge(
            PanelSlot slot,
            @Nullable UUID networkId,
            @Nullable ThermalRelaySwitchItem.FurnaceLink link
    ) {
        if (gauges.containsKey(slot)) {
            return false;
        }

        UUID resolvedNetworkId = networkId != null
                ? networkId
                : link != null
                ? link.furnaceIdentity()
                : null;

        gauges.put(slot, new GaugeData(resolvedNetworkId, link));
        updateTicker = UPDATE_INTERVAL;
        markAndSync();

        if (level != null && !level.isClientSide) {
            evaluateNetworks(level, getBlockState());
        }

        return true;
    }

    public boolean removeGauge(PanelSlot slot) {
        if (gauges.remove(slot) == null) {
            return false;
        }

        markAndSync();
        return true;
    }

    public boolean hasGauge(PanelSlot slot) {
        return gauges.containsKey(slot);
    }

    public int activeGaugeCount() {
        return gauges.size();
    }

    public List<PanelSlot> getActiveSlots() {
        return List.copyOf(gauges.keySet());
    }

    public void clearConnection(PanelSlot slot) {
        GaugeData data = gauges.get(slot);
        if (data == null) {
            return;
        }

        data.networkId = null;
        data.linkedFurnace = null;
        data.displayTemperature = MIN_TEMPERATURE;
        data.networkConnected = false;
        data.heatTier = MoltenRotorBlockEntity.RotorHeatLevel.NONE;
        updateTicker = 0;
        markAndSync();
    }

    @Nullable
    public UUID getNetworkId(PanelSlot slot) {
        GaugeData data = gauges.get(slot);
        return data != null ? data.networkId : null;
    }

    @Nullable
    public ThermalRelaySwitchItem.FurnaceLink getFurnaceLink(PanelSlot slot) {
        GaugeData data = gauges.get(slot);
        return data != null ? data.linkedFurnace : null;
    }

    public int getDisplayTemperature(PanelSlot slot) {
        GaugeData data = gauges.get(slot);
        return data != null ? data.displayTemperature : MIN_TEMPERATURE;
    }

    public boolean isNetworkConnected(PanelSlot slot) {
        GaugeData data = gauges.get(slot);
        return data != null && data.networkConnected;
    }


    public ItemStack createItemStack(PanelSlot slot) {
        ItemStack stack = new ItemStack(getBlockState().getBlock().asItem());
        GaugeData data = gauges.get(slot);

        if (data != null && data.linkedFurnace != null) {
            ThermalRelaySwitchItem.setConnection(
                    stack,
                    data.networkId != null
                            ? data.networkId
                            : data.linkedFurnace.furnaceIdentity(),
                    data.linkedFurnace
            );
        }

        return stack;
    }

    public VoxelShape getShape() {
        VoxelShape shape = Shapes.empty();

        for (PanelSlot slot : gauges.keySet()) {
            shape = Shapes.or(shape, getSlotShape(slot));
        }

        return shape;
    }

    public VoxelShape getSlotShape(PanelSlot slot) {
        if (!gauges.containsKey(slot)) {
            return Shapes.empty();
        }

        float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(getBlockState()) + 90.0F;
        float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(getBlockState());
        Direction connectedDirection = FactoryPanelBlock.connectedDirection(getBlockState());
        Vec3 inflateAxes = VecHelper.axisAlingedPlaneOf(connectedDirection);
        Vec3 center = new Vec3(
                0.25D + slot.xOffset * 0.5D,
                1.0D / 16.0D,
                0.25D + slot.yOffset * 0.5D
        );
        center = VecHelper.rotateCentered(center, 180.0D, Direction.Axis.Y);
        center = VecHelper.rotateCentered(center, xRot, Direction.Axis.X);
        center = VecHelper.rotateCentered(center, yRot, Direction.Axis.Y);

        AABB box = new AABB(center, center)
                .inflate(1.0D / 16.0D)
                .inflate(
                        inflateAxes.x * 3.0D / 16.0D,
                        inflateAxes.y * 3.0D / 16.0D,
                        inflateAxes.z * 3.0D / 16.0D
                );

        return Shapes.create(box);
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

    private void evaluateNetworks(Level level, BlockState state) {
        boolean changed = false;

        for (GaugeData data : gauges.values()) {
            int nextTemperature = MIN_TEMPERATURE;
            boolean nextConnected = false;
            MoltenRotorBlockEntity.RotorHeatLevel nextHeatTier =
                    MoltenRotorBlockEntity.RotorHeatLevel.NONE;
            ThermalRelaySwitchItem.FurnaceLink link = data.linkedFurnace;

            if (link != null
                    && level.dimension().location().toString().equals(link.dimension())) {
                BlockPos furnacePos = link.position();

                if (level.isLoaded(furnacePos)
                        && level.getBlockEntity(furnacePos) instanceof MoltenRotorBlockEntity furnace
                        && link.furnaceIdentity().equals(furnace.getFurnaceIdentity())) {
                    nextTemperature = furnace.getDisplayTemperature();
                    nextConnected = true;
                    nextHeatTier = furnace.getCurrentHeatTier();
                }
            }

            nextTemperature = Math.clamp(
                    nextTemperature,
                    MIN_TEMPERATURE,
                    MAX_TEMPERATURE
            );

            if (data.displayTemperature != nextTemperature
                    || data.networkConnected != nextConnected
                    || data.heatTier != nextHeatTier) {
                data.displayTemperature = nextTemperature;
                data.networkConnected = nextConnected;
                data.heatTier = nextHeatTier;
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

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
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        PanelSlot targetedSlot = targetedSlotResolver.apply(this);

        if (targetedSlot == null) {
            if (gauges.size() != 1) {
                return false;
            }

            targetedSlot = gauges.keySet().iterator().next();
        }

        GaugeData data = gauges.get(targetedSlot);

        if (data == null) {
            return false;
        }

        tooltip.add(Component.literal(""));
        tooltip.add(
                Component.literal("Thermochemical Gauge")
                        .withStyle(ChatFormatting.GOLD)
        );

        if (!data.networkConnected) {
            tooltip.add(
                    Component.literal("Heat: No Network")
                            .withStyle(ChatFormatting.GRAY)
            );
            return true;
        }

        tooltip.add(
                Component.literal("Heat: " + data.heatTier.displayName)
                        .withStyle(colorFor(data.heatTier))
        );
        tooltip.add(
                Component.literal("Temperature: " + data.displayTemperature + "°C")
                        .withStyle(ChatFormatting.GRAY)
        );

        return true;
    }

    private static ChatFormatting colorFor(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    ) {
        if (heatTier == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT) {
            return ChatFormatting.DARK_PURPLE;
        }

        if (heatTier == MoltenRotorBlockEntity.RotorHeatLevel.SEETHING) {
            return ChatFormatting.DARK_RED;
        }

        if (heatTier == MoltenRotorBlockEntity.RotorHeatLevel.KINDLED) {
            return ChatFormatting.RED;
        }

        if (heatTier == MoltenRotorBlockEntity.RotorHeatLevel.SMOULDERING
                || heatTier == MoltenRotorBlockEntity.RotorHeatLevel.FADING) {
            return ChatFormatting.YELLOW;
        }

        return ChatFormatting.GRAY;
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        ListTag storedGauges = new ListTag();
        for (Map.Entry<PanelSlot, GaugeData> entry : gauges.entrySet()) {
            CompoundTag gaugeTag = new CompoundTag();
            GaugeData data = entry.getValue();

            gaugeTag.putString(SLOT_TAG, entry.getKey().getSerializedName());

            if (data.networkId != null) {
                gaugeTag.putUUID(NETWORK_TAG, data.networkId);
            }

            if (data.linkedFurnace != null) {
                gaugeTag.putLong(POSITION_TAG, data.linkedFurnace.position().asLong());
                gaugeTag.putString(DIMENSION_TAG, data.linkedFurnace.dimension());
                gaugeTag.putUUID(IDENTITY_TAG, data.linkedFurnace.furnaceIdentity());
            }

            gaugeTag.putInt(TEMPERATURE_TAG, data.displayTemperature);
            gaugeTag.putBoolean(CONNECTED_TAG, data.networkConnected);
            gaugeTag.putString(HEAT_TIER_TAG, data.heatTier.serializedId);
            storedGauges.add(gaugeTag);
        }

        tag.put(GAUGES_TAG, storedGauges);
    }

    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        gauges.clear();

        if (tag.contains(GAUGES_TAG, Tag.TAG_LIST)) {
            ListTag storedGauges = tag.getList(GAUGES_TAG, Tag.TAG_COMPOUND);

            for (int index = 0; index < storedGauges.size(); index++) {
                CompoundTag gaugeTag = storedGauges.getCompound(index);
                PanelSlot slot = readSlot(gaugeTag.getString(SLOT_TAG));
                if (slot == null) {
                    continue;
                }

                gauges.put(
                        slot,
                        readGaugeData(gaugeTag)
                );
            }

            return;
        }

        if (tag.contains(TEMPERATURE_TAG, Tag.TAG_INT)
                || tag.contains(LINKED_FURNACES_TAG, Tag.TAG_LIST)
                || tag.contains(LEGACY_POSITION_TAG, Tag.TAG_LONG)) {
            gauges.put(
                    PanelSlot.BOTTOM_LEFT,
                    readGaugeData(tag)
            );
        }
    }

    private static GaugeData readGaugeData(
            CompoundTag tag
    ) {
        ThermalRelaySwitchItem.FurnaceLink link =
                readLink(tag);

        UUID networkId = tag.hasUUID(NETWORK_TAG)
                ? tag.getUUID(NETWORK_TAG)
                : link != null
                ? link.furnaceIdentity()
                : null;

        GaugeData data = new GaugeData(
                networkId,
                link
        );

        data.displayTemperature =
                tag.contains(TEMPERATURE_TAG, Tag.TAG_INT)
                        ? Math.clamp(
                                tag.getInt(TEMPERATURE_TAG),
                                MIN_TEMPERATURE,
                                MAX_TEMPERATURE
                        )
                        : MIN_TEMPERATURE;

        data.networkConnected =
                tag.getBoolean(CONNECTED_TAG);

        data.heatTier =
                tag.contains(HEAT_TIER_TAG, Tag.TAG_STRING)
                        ? MoltenRotorBlockEntity.RotorHeatLevel
                        .fromSerializedId(
                                tag.getString(HEAT_TIER_TAG)
                        )
                        : MoltenRotorBlockEntity.RotorHeatLevel.NONE;

        return data;
    }

    @Nullable
    private static ThermalRelaySwitchItem.FurnaceLink readLink(CompoundTag tag) {
        if (tag.contains(POSITION_TAG, Tag.TAG_LONG)
                && tag.contains(DIMENSION_TAG, Tag.TAG_STRING)
                && tag.hasUUID(IDENTITY_TAG)) {
            return new ThermalRelaySwitchItem.FurnaceLink(
                    BlockPos.of(tag.getLong(POSITION_TAG)),
                    tag.getString(DIMENSION_TAG),
                    tag.getUUID(IDENTITY_TAG)
            );
        }

        if (tag.contains(LINKED_FURNACES_TAG, Tag.TAG_LIST)) {
            ListTag links = tag.getList(LINKED_FURNACES_TAG, Tag.TAG_COMPOUND);

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

    @Nullable
    private static PanelSlot readSlot(String value) {
        return switch (value) {
            case "top_left" -> PanelSlot.TOP_LEFT;
            case "top_right" -> PanelSlot.TOP_RIGHT;
            case "bottom_left" -> PanelSlot.BOTTOM_LEFT;
            case "bottom_right" -> PanelSlot.BOTTOM_RIGHT;
            default -> null;
        };
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

    private static final class GaugeData {
        @Nullable
        private UUID networkId;
        @Nullable
        private ThermalRelaySwitchItem.FurnaceLink linkedFurnace;
        private int displayTemperature = MIN_TEMPERATURE;
        private boolean networkConnected;
        private MoltenRotorBlockEntity.RotorHeatLevel heatTier =
                MoltenRotorBlockEntity.RotorHeatLevel.NONE;

        private GaugeData(
                @Nullable UUID networkId,
                @Nullable ThermalRelaySwitchItem.FurnaceLink linkedFurnace
        ) {
            this.networkId = networkId;
            this.linkedFurnace = linkedFurnace;
        }
    }
}
