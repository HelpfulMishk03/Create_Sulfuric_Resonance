package io.hxneyw.repo.content.blocks.thermalrelay;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.items.ThermalRelaySwitchItem;
import io.hxneyw.repo.content.logic.LogicCondition;
import io.hxneyw.repo.content.logic.LogicEvaluation;
import io.hxneyw.repo.content.logic.LogicEvaluator;
import io.hxneyw.repo.content.logic.LogicResponse;
import io.hxneyw.repo.content.logic.LogicSource;
import io.hxneyw.repo.content.logic.ThermalLogicLevel;
import io.hxneyw.repo.content.menu.ThermalRelaySwitchMenu;
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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermalRelaySwitchBlockEntity
        extends BlockEntity {

    private static final String NETWORK_TAG = "RelayNetwork";
    private static final String LINKED_FURNACES_TAG =
            "LinkedFurnaces";

    private static final String POSITION_TAG = "Position";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String IDENTITY_TAG = "Identity";

    private static final String MODE_TAG = "RelayMode";
    private static final String LOW_FUEL_SCOPE_TAG =
            "LowFuelScope";

    private static final String HEATED_REDSTONE_TAG =
            "HeatedRedstone";
    private static final String HEATED_GLOW_TAG =
            "HeatedGlow";
    private static final String SUPERHEATED_REDSTONE_TAG =
            "SuperheatedRedstone";
    private static final String SUPERHEATED_GLOW_TAG =
            "SuperheatedGlow";
    private static final String COMBUSTION_REDSTONE_TAG =
            "CombustionRedstone";
    private static final String COMBUSTION_GLOW_TAG =
            "CombustionGlow";
    private static final String LOW_FUEL_REDSTONE_TAG =
            "LowFuelRedstone";
    private static final String LOW_FUEL_GLOW_TAG =
            "LowFuelGlow";

    private static final int UPDATE_INTERVAL = 10;

    public static final int MAX_HEATED_REDSTONE = 7;
    public static final int MAX_SUPERHEATED_REDSTONE = 12;
    public static final int MAX_COMBUSTION_REDSTONE = 15;
    public static final int MAX_GLOW = 5;

    private static final int LOW_FUEL_THRESHOLD_TICKS = 200;


    private static final int PULSE_HALF_PERIOD_TICKS = 10;

    private static final LogicCondition HEATED_CONDITION =
            LogicCondition.highTrip(
                    LogicSource.THERMAL,
                    ThermalLogicLevel.HEATED.value()
            );

    private static final LogicCondition SUPERHEATED_CONDITION =
            LogicCondition.highTrip(
                    LogicSource.THERMAL,
                    ThermalLogicLevel.SUPERHEATED.value()
            );

    private static final LogicCondition COMBUSTION_CONDITION =
            LogicCondition.highTrip(
                    LogicSource.THERMAL,
                    ThermalLogicLevel.COMBUSTION.value()
            );

    private static final Set<ThermalRelaySwitchBlockEntity>
            CLIENT_RELAYS = Collections.newSetFromMap(
            new WeakHashMap<>()
    );

    @Nullable
    private UUID networkId;

    @Nullable
    private ThermalRelaySwitchItem.FurnaceLink linkedFurnace;

    private RelayMode mode = RelayMode.CUSTOM_HEAT;
    private LowFuelScope lowFuelScope =
            LowFuelScope.BOTH;

    private int heatedRedstone = 5;
    private int heatedGlow = 1;

    private int superheatedRedstone = 10;
    private int superheatedGlow = 2;

    private int combustionRedstone = 15;
    private int combustionGlow = 3;

    private int lowFuelRedstone = 15;
    private int lowFuelGlow = 2;

    private int currentHeatBand =
            HeatBand.UNHEATED.ordinal();

    private boolean lowFuelWarningActive;
    private int updateTicker;

    private final ContainerData menuData =
            new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case ThermalRelaySwitchMenu.DATA_MODE -> mode.ordinal();
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_SCOPE -> lowFuelScope.ordinal();
                        case ThermalRelaySwitchMenu
                                     .DATA_HEATED_REDSTONE -> heatedRedstone;
                        case ThermalRelaySwitchMenu
                                     .DATA_HEATED_GLOW -> heatedGlow;
                        case ThermalRelaySwitchMenu
                                     .DATA_SUPERHEATED_REDSTONE -> superheatedRedstone;
                        case ThermalRelaySwitchMenu
                                     .DATA_SUPERHEATED_GLOW -> superheatedGlow;
                        case ThermalRelaySwitchMenu
                                     .DATA_COMBUSTION_REDSTONE -> combustionRedstone;
                        case ThermalRelaySwitchMenu
                                     .DATA_COMBUSTION_GLOW -> combustionGlow;
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_REDSTONE -> lowFuelRedstone;
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_GLOW -> lowFuelGlow;
                        case ThermalRelaySwitchMenu
                                     .DATA_CURRENT_HEAT_BAND -> currentHeatBand;
                        case ThermalRelaySwitchMenu
                                     .DATA_CURRENT_POWER -> getCurrentPower();
                        case ThermalRelaySwitchMenu
                                     .DATA_CURRENT_GLOW -> getCurrentGlow();
                        case ThermalRelaySwitchMenu
                                     .DATA_LINKED_COUNT -> linkedFurnace == null ? 0 : 1;
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_ACTIVE -> lowFuelWarningActive ? 1 : 0;
                        default -> 0;
                    };
                }

                @Override
                public void set(
                        int index,
                        int value
                ) {

                    switch (index) {
                        case ThermalRelaySwitchMenu.DATA_MODE -> mode = RelayMode.fromOrdinal(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_SCOPE -> lowFuelScope =
                                LowFuelScope.fromOrdinal(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_HEATED_REDSTONE -> heatedRedstone =
                                clampHeatedRedstone(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_HEATED_GLOW -> heatedGlow =
                                clampGlow(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_SUPERHEATED_REDSTONE -> superheatedRedstone =
                                clampSuperheatedRedstone(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_SUPERHEATED_GLOW -> superheatedGlow =
                                clampGlow(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_COMBUSTION_REDSTONE -> combustionRedstone =
                                clampCombustionRedstone(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_COMBUSTION_GLOW -> combustionGlow =
                                clampGlow(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_REDSTONE -> lowFuelRedstone =
                                clampRedstone(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_GLOW -> lowFuelGlow =
                                clampGlow(value);
                        case ThermalRelaySwitchMenu
                                     .DATA_CURRENT_HEAT_BAND -> currentHeatBand =
                                Mth.clamp(value, 0, 3);
                        case ThermalRelaySwitchMenu
                                     .DATA_LOW_FUEL_ACTIVE -> lowFuelWarningActive =
                                value != 0;
                        default -> {
                        }
                    }
                }

                @Override
                public int getCount() {
                    return ThermalRelaySwitchMenu.DATA_COUNT;
                }
            };

    public ThermalRelaySwitchBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        super(
                AllBlockEntities.THERMAL_RELAY_SWITCH.get(),
                pos,
                state
        );
    }

    public static void serverTick(
            @NotNull Level level,
            @NotNull ThermalRelaySwitchBlockEntity relay
    ) {
        if (level.isClientSide) {
            return;
        }

        relay.updateTicker++;

        if (relay.updateTicker < UPDATE_INTERVAL) {
            return;
        }

        relay.updateTicker = 0;
        relay.evaluateOutputs(level);
    }

    public void setConnection(
            @NotNull UUID networkId,
            @NotNull ThermalRelaySwitchItem.FurnaceLink link
    ) {
        this.networkId = networkId;
        this.linkedFurnace = link;

        forceImmediateEvaluation();
        markAndSync();
    }

    public void clearConnections() {
        boolean alreadyClear =
                this.networkId == null
                        && this.linkedFurnace == null;

        this.networkId = null;
        this.linkedFurnace = null;
        this.currentHeatBand =
                HeatBand.UNHEATED.ordinal();
        this.lowFuelWarningActive = false;
        this.updateTicker = 0;

        resetOutput();

        if (!alreadyClear) {
            markAndSync();
        }
    }

    @Nullable
    public UUID getNetworkId() {
        return this.networkId;
    }

    @Nullable
    public ThermalRelaySwitchItem.FurnaceLink
    getFurnaceLink() {
        return this.linkedFurnace;
    }

    public ContainerData getMenuData() {
        return this.menuData;
    }

    public static List<ThermalRelaySwitchBlockEntity>
    getLoadedClientRelays() {
        return List.copyOf(CLIENT_RELAYS);
    }


    public boolean handleMenuButton(int buttonId) {
        boolean changed = switch (buttonId) {
            case ThermalRelaySwitchMenu.BUTTON_MODE_CUSTOM_HEAT -> setMode(RelayMode.CUSTOM_HEAT);
            case ThermalRelaySwitchMenu.BUTTON_MODE_LOW_FUEL -> setMode(RelayMode.LOW_FUEL);

            case ThermalRelaySwitchMenu.BUTTON_SCOPE_LOW_HEAT -> setLowFuelScope(LowFuelScope.LOW_HEAT);
            case ThermalRelaySwitchMenu.BUTTON_SCOPE_HIGH_HEAT -> setLowFuelScope(LowFuelScope.HIGH_HEAT);
            case ThermalRelaySwitchMenu.BUTTON_SCOPE_BOTH -> setLowFuelScope(LowFuelScope.BOTH);

            case ThermalRelaySwitchMenu
                         .BUTTON_HEATED_REDSTONE_DOWN -> adjustHeatedRedstone(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_HEATED_REDSTONE_UP -> adjustHeatedRedstone(1);
            case ThermalRelaySwitchMenu
                         .BUTTON_HEATED_GLOW_DOWN -> adjustHeatedGlow(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_HEATED_GLOW_UP -> adjustHeatedGlow(1);

            case ThermalRelaySwitchMenu
                         .BUTTON_SUPERHEATED_REDSTONE_DOWN -> adjustSuperheatedRedstone(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_SUPERHEATED_REDSTONE_UP -> adjustSuperheatedRedstone(1);
            case ThermalRelaySwitchMenu
                         .BUTTON_SUPERHEATED_GLOW_DOWN -> adjustSuperheatedGlow(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_SUPERHEATED_GLOW_UP -> adjustSuperheatedGlow(1);

            case ThermalRelaySwitchMenu
                         .BUTTON_COMBUSTION_REDSTONE_DOWN -> adjustCombustionRedstone(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_COMBUSTION_REDSTONE_UP -> adjustCombustionRedstone(1);
            case ThermalRelaySwitchMenu
                         .BUTTON_COMBUSTION_GLOW_DOWN -> adjustCombustionGlow(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_COMBUSTION_GLOW_UP -> adjustCombustionGlow(1);

            case ThermalRelaySwitchMenu
                         .BUTTON_LOW_FUEL_REDSTONE_DOWN -> adjustLowFuelRedstone(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_LOW_FUEL_REDSTONE_UP -> adjustLowFuelRedstone(1);
            case ThermalRelaySwitchMenu
                         .BUTTON_LOW_FUEL_GLOW_DOWN -> adjustLowFuelGlow(-1);
            case ThermalRelaySwitchMenu
                         .BUTTON_LOW_FUEL_GLOW_UP -> adjustLowFuelGlow(1);

            default -> false;
        };

        if (changed) {
            configurationChanged();
        }

        return changed;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.level == null) {
            return;
        }

        if (this.level.isClientSide) {
            CLIENT_RELAYS.add(this);
        } else {
            forceImmediateEvaluation();
        }
    }

    @Override
    public void setRemoved() {
        CLIENT_RELAYS.remove(this);
        super.setRemoved();
    }

    private void evaluateOutputs(
            @NotNull Level level
    ) {
        HeatBand current = HeatBand.UNHEATED;
        boolean qualifyingLowFuel = false;

        ThermalRelaySwitchItem.FurnaceLink link =
                this.linkedFurnace;

        if (link != null
                && level.dimension()
                .location()
                .toString()
                .equals(link.dimension())) {
            BlockPos furnacePos = link.position();

            if (level.isLoaded(furnacePos)
                    && level.getBlockEntity(furnacePos)
                    instanceof MoltenRotorBlockEntity furnace
                    && link.furnaceIdentity().equals(
                    furnace.getFurnaceIdentity()
            )) {
                current = HeatBand.from(
                        furnace.getCurrentHeatTier()
                );

                boolean activeFuelEndingSoon =
                        this.lowFuelScope.matches(current)
                                && isActiveFuelEndingSoon(
                                furnace,
                                current
                        );

                boolean heatedStateEndingSoon =
                        isHeatedStateEndingSoon(
                                furnace,
                                current
                        );

                qualifyingLowFuel =
                        activeFuelEndingSoon
                                || heatedStateEndingSoon;
            }
        }

        this.currentHeatBand = current.ordinal();
        this.lowFuelWarningActive = qualifyingLowFuel;

        ConfiguredOutput baseOutput =
                configuredOutputFor(current);

        if (qualifyingLowFuel) {
            applyLowFuelWarning(level);
            return;
        }

        applyOutput(
                level,
                baseOutput.redstone(),
                baseOutput.glow()
        );
    }

    private void applyLowFuelWarning(
            @NotNull Level level
    ) {

        if (isPulseOff(level)) {
            applyOutput(level, 0, 0);
            return;
        }

        applyOutput(
                level,
                this.lowFuelRedstone,
                this.lowFuelGlow
        );
    }

    private ConfiguredOutput configuredOutputFor(
            @NotNull HeatBand heatBand
    ) {
        int heatValue = heatBand.logicValue();

        if (evaluateHold(
                heatValue,
                COMBUSTION_CONDITION
        ).outputActive()) {
            return new ConfiguredOutput(
                    this.combustionRedstone,
                    this.combustionGlow
            );
        }

        if (evaluateHold(
                heatValue,
                SUPERHEATED_CONDITION
        ).outputActive()) {
            return new ConfiguredOutput(
                    this.superheatedRedstone,
                    this.superheatedGlow
            );
        }

        if (evaluateHold(
                heatValue,
                HEATED_CONDITION
        ).outputActive()) {
            return new ConfiguredOutput(
                    this.heatedRedstone,
                    this.heatedGlow
            );
        }

        return new ConfiguredOutput(0, 0);
    }

    private static LogicEvaluation evaluateHold(
            int currentValue,
            @NotNull LogicCondition condition
    ) {
        return LogicEvaluator.evaluate(
                currentValue,
                currentValue,
                condition,
                LogicResponse.HOLD
        );
    }

    private static boolean isActiveFuelEndingSoon(
            @NotNull MoltenRotorBlockEntity furnace,
            @NotNull HeatBand heatBand
    ) {
        if (heatBand == HeatBand.UNHEATED
                || furnace.isCreativeMode()
                || !furnace.isFuelQueueEmpty()) {
            return false;
        }

        int remainingFuel =
                furnace.getDisplayFuelTime();

        return remainingFuel > 0
                && remainingFuel
                <= LOW_FUEL_THRESHOLD_TICKS;
    }

    private static boolean isHeatedStateEndingSoon(
            @NotNull MoltenRotorBlockEntity furnace,
            @NotNull HeatBand heatBand
    ) {
        if (heatBand == HeatBand.UNHEATED
                || furnace.isCreativeMode()
                || !furnace.isFuelQueueEmpty()
                || furnace.getDisplayFuelTime() > 0) {
            return false;
        }

        int remainingHeatedTime =
                furnace.getDisplayCooldownTime();

        return remainingHeatedTime > 0
                && remainingHeatedTime
                <= LOW_FUEL_THRESHOLD_TICKS;

    }

    private static boolean isPulseOff(
            @NotNull Level level
    ) {
        return (
                level.getGameTime()
                        / PULSE_HALF_PERIOD_TICKS
        ) % 2L != 0L;
    }

    private void resetOutput() {
        if (this.level == null
                || this.level.isClientSide) {
            return;
        }

        applyOutput(this.level, 0, 0);
    }

    private void applyOutput(
            @NotNull Level level,
            int requestedPower,
            int requestedGlow
    ) {
        int newPower = clampRedstone(requestedPower);
        int newGlow = clampGlow(requestedGlow);

        BlockState currentState = getBlockState();

        if (!currentState.hasProperty(
                ThermalRelaySwitchBlock.POWER
        ) || !currentState.hasProperty(
                ThermalRelaySwitchBlock.GLOW
        )) {
            return;
        }

        int oldPower = currentState.getValue(
                ThermalRelaySwitchBlock.POWER
        );

        int oldGlow = currentState.getValue(
                ThermalRelaySwitchBlock.GLOW
        );

        if (oldPower == newPower
                && oldGlow == newGlow) {
            return;
        }

        BlockState updatedState = currentState
                .setValue(
                        ThermalRelaySwitchBlock.POWER,
                        newPower
                )
                .setValue(
                        ThermalRelaySwitchBlock.GLOW,
                        newGlow
                );

        level.setBlock(
                this.worldPosition,
                updatedState,
                Block.UPDATE_CLIENTS
        );

        if (oldPower != newPower) {
            level.updateNeighborsAt(
                    this.worldPosition,
                    updatedState.getBlock()
            );
        }

        setChanged();
    }

    private int getCurrentPower() {
        BlockState state = getBlockState();

        if (!state.hasProperty(
                ThermalRelaySwitchBlock.POWER
        )) {
            return 0;
        }

        return state.getValue(
                ThermalRelaySwitchBlock.POWER
        );
    }

    private int getCurrentGlow() {
        BlockState state = getBlockState();

        if (!state.hasProperty(
                ThermalRelaySwitchBlock.GLOW
        )) {
            return 0;
        }

        return state.getValue(
                ThermalRelaySwitchBlock.GLOW
        );
    }

    private boolean setMode(
            @NotNull RelayMode requestedMode
    ) {
        if (this.mode == requestedMode) {
            return false;
        }

        this.mode = requestedMode;
        return true;
    }

    private boolean setLowFuelScope(
            @NotNull LowFuelScope requestedScope
    ) {
        if (this.lowFuelScope == requestedScope) {
            return false;
        }

        this.lowFuelScope = requestedScope;
        return true;
    }

    private boolean adjustHeatedRedstone(int amount) {
        int next = clampHeatedRedstone(
                this.heatedRedstone + amount
        );

        if (next == this.heatedRedstone) {
            return false;
        }

        this.heatedRedstone = next;
        return true;
    }

    private boolean adjustHeatedGlow(int amount) {
        int next = clampGlow(
                this.heatedGlow + amount
        );

        if (next == this.heatedGlow) {
            return false;
        }

        this.heatedGlow = next;
        return true;
    }

    private boolean adjustSuperheatedRedstone(int amount) {
        int next = clampSuperheatedRedstone(
                this.superheatedRedstone + amount
        );

        if (next == this.superheatedRedstone) {
            return false;
        }

        this.superheatedRedstone = next;
        return true;
    }

    private boolean adjustSuperheatedGlow(int amount) {
        int next = clampGlow(
                this.superheatedGlow + amount
        );

        if (next == this.superheatedGlow) {
            return false;
        }

        this.superheatedGlow = next;
        return true;
    }

    private boolean adjustCombustionRedstone(int amount) {
        int next = clampCombustionRedstone(
                this.combustionRedstone + amount
        );

        if (next == this.combustionRedstone) {
            return false;
        }

        this.combustionRedstone = next;
        return true;
    }

    private boolean adjustCombustionGlow(int amount) {
        int next = clampGlow(
                this.combustionGlow + amount
        );

        if (next == this.combustionGlow) {
            return false;
        }

        this.combustionGlow = next;
        return true;
    }

    private boolean adjustLowFuelRedstone(int amount) {
        int next = clampRedstone(
                this.lowFuelRedstone + amount
        );

        if (next == this.lowFuelRedstone) {
            return false;
        }

        this.lowFuelRedstone = next;
        return true;
    }

    private boolean adjustLowFuelGlow(int amount) {
        int next = clampGlow(
                this.lowFuelGlow + amount
        );

        if (next == this.lowFuelGlow) {
            return false;
        }

        this.lowFuelGlow = next;
        return true;
    }

    private void configurationChanged() {
        forceImmediateEvaluation();

        if (this.level != null
                && !this.level.isClientSide) {
            evaluateOutputs(this.level);
        }

        markAndSync();
    }

    private void forceImmediateEvaluation() {
        this.updateTicker = UPDATE_INTERVAL;
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

    private ListTag createStoredLinks() {
        ListTag storedLinks = new ListTag();

        if (this.linkedFurnace == null) {
            return storedLinks;
        }

        CompoundTag linkTag = new CompoundTag();

        linkTag.putLong(
                POSITION_TAG,
                this.linkedFurnace.position().asLong()
        );

        linkTag.putString(
                DIMENSION_TAG,
                this.linkedFurnace.dimension()
        );

        linkTag.putUUID(
                IDENTITY_TAG,
                this.linkedFurnace.furnaceIdentity()
        );

        storedLinks.add(linkTag);
        return storedLinks;
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.putString(
                MODE_TAG,
                this.mode.serializedId()
        );

        tag.putString(
                LOW_FUEL_SCOPE_TAG,
                this.lowFuelScope.serializedId()
        );

        tag.putInt(
                HEATED_REDSTONE_TAG,
                this.heatedRedstone
        );
        tag.putInt(
                HEATED_GLOW_TAG,
                this.heatedGlow
        );

        tag.putInt(
                SUPERHEATED_REDSTONE_TAG,
                this.superheatedRedstone
        );
        tag.putInt(
                SUPERHEATED_GLOW_TAG,
                this.superheatedGlow
        );

        tag.putInt(
                COMBUSTION_REDSTONE_TAG,
                this.combustionRedstone
        );
        tag.putInt(
                COMBUSTION_GLOW_TAG,
                this.combustionGlow
        );

        tag.putInt(
                LOW_FUEL_REDSTONE_TAG,
                this.lowFuelRedstone
        );
        tag.putInt(
                LOW_FUEL_GLOW_TAG,
                this.lowFuelGlow
        );

        UUID savedNetworkId = this.networkId;

        if (savedNetworkId == null
                || this.linkedFurnace == null) {
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

        loadConfiguration(tag);

        this.networkId = null;
        this.linkedFurnace = null;
        this.currentHeatBand =
                HeatBand.UNHEATED.ordinal();
        this.lowFuelWarningActive = false;

        if (!tag.hasUUID(NETWORK_TAG)
                || !tag.contains(
                LINKED_FURNACES_TAG,
                Tag.TAG_LIST
        )) {
            forceImmediateEvaluation();
            return;
        }

        ListTag storedLinks = tag.getList(
                LINKED_FURNACES_TAG,
                Tag.TAG_COMPOUND
        );

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

            this.networkId = tag.getUUID(NETWORK_TAG);
            this.linkedFurnace =
                    new ThermalRelaySwitchItem.FurnaceLink(
                            BlockPos.of(
                                    linkTag.getLong(POSITION_TAG)
                            ),
                            linkTag.getString(DIMENSION_TAG),
                            linkTag.getUUID(IDENTITY_TAG)
                    );
            break;
        }

        forceImmediateEvaluation();
    }

    private void loadConfiguration(
            @NotNull CompoundTag tag
    ) {
        this.mode = tag.contains(MODE_TAG)
                ? RelayMode.fromSerializedId(
                tag.getString(MODE_TAG)
        )
                : RelayMode.CUSTOM_HEAT;

        this.lowFuelScope =
                tag.contains(LOW_FUEL_SCOPE_TAG)
                        ? LowFuelScope.fromSerializedId(
                        tag.getString(
                                LOW_FUEL_SCOPE_TAG
                        )
                )
                        : LowFuelScope.BOTH;

        this.heatedRedstone = readIntOrDefault(
                tag,
                HEATED_REDSTONE_TAG,
                5,
                MAX_HEATED_REDSTONE
        );

        this.heatedGlow = readIntOrDefault(
                tag,
                HEATED_GLOW_TAG,
                1,
                MAX_GLOW
        );

        this.superheatedRedstone = readIntOrDefault(
                tag,
                SUPERHEATED_REDSTONE_TAG,
                10,
                MAX_SUPERHEATED_REDSTONE
        );

        this.superheatedGlow = readIntOrDefault(
                tag,
                SUPERHEATED_GLOW_TAG,
                2,
                MAX_GLOW
        );

        this.combustionRedstone = readIntOrDefault(
                tag,
                COMBUSTION_REDSTONE_TAG,
                15,
                MAX_COMBUSTION_REDSTONE
        );

        this.combustionGlow = readIntOrDefault(
                tag,
                COMBUSTION_GLOW_TAG,
                3,
                MAX_GLOW
        );

        this.lowFuelRedstone = readIntOrDefault(
                tag,
                LOW_FUEL_REDSTONE_TAG,
                15,
                15
        );

        this.lowFuelGlow = readIntOrDefault(
                tag,
                LOW_FUEL_GLOW_TAG,
                2,
                MAX_GLOW
        );
    }

    private static int readIntOrDefault(
            @NotNull CompoundTag tag,
            @NotNull String key,
            int defaultValue,
            int maximum
    ) {
        if (!tag.contains(key, Tag.TAG_INT)) {
            return defaultValue;
        }

        return Mth.clamp(
                tag.getInt(key),
                0,
                maximum
        );
    }

    private static int clampRedstone(int value) {
        return Mth.clamp(value, 0, 15);
    }

    private static int clampHeatedRedstone(int value) {
        return Mth.clamp(
                value,
                0,
                MAX_HEATED_REDSTONE
        );
    }

    private static int clampSuperheatedRedstone(int value) {
        return Mth.clamp(
                value,
                0,
                MAX_SUPERHEATED_REDSTONE
        );
    }

    private static int clampCombustionRedstone(int value) {
        return Mth.clamp(
                value,
                0,
                MAX_COMBUSTION_REDSTONE
        );
    }

    private static int clampGlow(int value) {
        return Mth.clamp(value, 0, MAX_GLOW);
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
        return ClientboundBlockEntityDataPacket.create(this);
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

    public enum RelayMode {
        CUSTOM_HEAT("custom_heat"),
        LOW_FUEL("low_fuel");

        private final String serializedId;

        RelayMode(String serializedId) {
            this.serializedId = serializedId;
        }

        public String serializedId() {
            return this.serializedId;
        }

        public static RelayMode fromOrdinal(int value) {
            RelayMode[] values = values();

            return values[
                    Mth.clamp(
                            value,
                            0,
                            values.length - 1
                    )
                    ];
        }

        public static RelayMode fromSerializedId(
                @NotNull String serializedId
        ) {
            for (RelayMode mode : values()) {
                if (mode.serializedId.equals(serializedId)) {
                    return mode;
                }
            }

            return CUSTOM_HEAT;
        }
    }

    public enum LowFuelScope {
        LOW_HEAT("low_heat"),
        HIGH_HEAT("high_heat"),
        BOTH("both");

        private final String serializedId;

        LowFuelScope(String serializedId) {
            this.serializedId = serializedId;
        }

        public String serializedId() {
            return this.serializedId;
        }

        public boolean matches(
                @NotNull HeatBand heatBand
        ) {
            return switch (this) {
                case LOW_HEAT ->
                        heatBand == HeatBand.HEATED;
                case HIGH_HEAT ->
                        heatBand == HeatBand.SUPERHEATED
                                || heatBand
                                == HeatBand.COMBUSTION;
                case BOTH ->
                        heatBand != HeatBand.UNHEATED;
            };
        }

        public static LowFuelScope fromOrdinal(int value) {
            LowFuelScope[] values = values();

            return values[
                    Mth.clamp(
                            value,
                            0,
                            values.length - 1
                    )
                    ];
        }

        public static LowFuelScope fromSerializedId(
                @NotNull String serializedId
        ) {
            for (LowFuelScope scope : values()) {
                if (scope.serializedId.equals(
                        serializedId
                )) {
                    return scope;
                }
            }

            return BOTH;
        }
    }

    public enum HeatBand {
        UNHEATED,
        HEATED,
        SUPERHEATED,
        COMBUSTION;

        public int logicValue() {
            return switch (this) {
                case UNHEATED ->
                        ThermalLogicLevel.UNHEATED.value();
                case HEATED ->
                        ThermalLogicLevel.HEATED.value();
                case SUPERHEATED ->
                        ThermalLogicLevel.SUPERHEATED.value();
                case COMBUSTION ->
                        ThermalLogicLevel.COMBUSTION.value();
            };
        }

        public static HeatBand fromOrdinal(int value) {
            HeatBand[] values = values();

            return values[
                    Mth.clamp(
                            value,
                            0,
                            values.length - 1
                    )
                    ];
        }

        private static HeatBand from(
                @NotNull MoltenRotorBlockEntity.RotorHeatLevel tier
        ) {
            return switch (tier) {
                case NONE -> UNHEATED;
                case SMOULDERING, FADING, KINDLED ->
                        HEATED;
                case SEETHING ->
                        SUPERHEATED;
                case RADIANT ->
                        COMBUSTION;
            };
        }
    }

    private record ConfiguredOutput(
            int redstone,
            int glow
    ) {
    }


}
