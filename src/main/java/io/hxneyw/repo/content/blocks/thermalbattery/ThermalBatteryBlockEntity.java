package io.hxneyw.repo.content.blocks.thermalbattery;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public final class ThermalBatteryBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    public static final float MAX_STORED_HEAT = 2000.0F * 5.0F;
    public static final float MAX_CHARGE_PER_TICK = 1.0F;
    public static final float HEATED_DRAIN_PER_TICK = 0.25F;
    public static final float SUPERHEATED_DRAIN_PER_TICK = 0.50F;
    public static final int DEMAND_GRACE_TICKS = 4;
    public static final int MENU_DATA_COUNT = 9;

    private float heatedHeat;
    private float superheatedHeat;
    private OutputMode outputMode = OutputMode.HEATED;
    private int demandGrace;
    private int lastInputTemperature;
    private MoltenRotorBlockEntity.RotorHeatLevel lastInputTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    private boolean interfaceConnected;
    private boolean fault;
    private float lastGeneratedSpeed;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> Math.round(heatedHeat * 10.0F);
                case 1 -> Math.round(superheatedHeat * 10.0F);
                case 2 -> Math.round(MAX_STORED_HEAT * 10.0F);
                case 3 -> outputMode.ordinal();
                case 4 -> lastInputTemperature;
                case 5 -> indicatorState().ordinal();
                case 6 -> estimatedRuntimeTicks();
                case 7 -> demandGrace > 0 ? 1 : 0;
                case 8 -> interfaceConnected ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return MENU_DATA_COUNT;
        }
    };

    public ThermalBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.THERMAL_BATTERY.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        boolean changed = false;
        boolean wasConnected = interfaceConnected;
        boolean wasFault = fault;
        int previousInputTemperature = lastInputTemperature;
        MoltenRotorBlockEntity.RotorHeatLevel previousInputTier = lastInputTier;

        Direction side = ThermalBatteryBlock.interfaceSide(getBlockState());
        BlockPos neighbourPos = worldPosition.relative(side);
        interfaceConnected = hasAlignedThermochemicalInterface(
                side,
                neighbourPos
        );

        lastInputTier = MoltenRotorBlockEntity.RotorHeatLevel.NONE;
        lastInputTemperature = 0;

        if (interfaceConnected) {
            ThermochemicalHeatResolver.Result live = resolveChargeSource();

            if (live.heatTier()
                    != MoltenRotorBlockEntity.RotorHeatLevel.NONE
                    && live.sourcePos() != null
                    && !(level.getBlockEntity(live.sourcePos())
                    instanceof ThermalBatteryBlockEntity)) {
                lastInputTier = live.heatTier();
                lastInputTemperature = live.temperature();
                changed |= chargeFrom(live);
            }
        }

        if (lastInputTier
                != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            if (demandGrace != 0) {
                demandGrace = 0;
                changed = true;
            }
        } else if (demandGrace > 0) {
            if (!isRedstoneEnabled()) {
                demandGrace = 0;
                changed = true;
            } else {
                demandGrace--;
                changed |= dischargeForDemand();
            }
        }

        fault = calculateFault();
        changed |= updateIndicatorBlockState();
        changed |= updateMotorRotation();

        if (wasConnected != interfaceConnected
                || wasFault != fault
                || previousInputTemperature != lastInputTemperature
                || previousInputTier != lastInputTier) {
            changed = true;
        }

        if (changed) {
            setChanged();
            sendData();
        }
    }


    private boolean hasAlignedThermochemicalInterface(
            Direction side,
            BlockPos neighbourPos
    ) {
        if (level == null
                || !ThermalBatteryBlock.exposesThermochemicalShaftTowards(
                level,
                neighbourPos,
                side.getOpposite()
        )) {
            return false;
        }

        return ThermochemicalHeatResolver.hasPhysicalConnection(
                level,
                worldPosition,
                neighbourPos
        );
    }

    private ThermochemicalHeatResolver.Result resolveChargeSource() {
        return ThermochemicalHeatResolver.resolveNetworkOnly(
                level,
                worldPosition
        );
    }

    private boolean chargeFrom(ThermochemicalHeatResolver.Result source) {
        float room = MAX_STORED_HEAT - getStoredHeat();
        if (room <= 0.0001F) {
            return false;
        }

        float normalized = Mth.clamp(
                source.temperature() / 1599.0F,
                0.15F,
                1.0F
        );
        float amount = Math.min(room, MAX_CHARGE_PER_TICK * normalized);
        if (amount <= 0.0F) {
            return false;
        }

        if (source.heatTier().rank
                >= MoltenRotorBlockEntity.RotorHeatLevel.SEETHING.rank) {
            superheatedHeat += amount;
        } else {
            heatedHeat += amount;
        }
        clampStorage();
        return true;
    }

    private boolean dischargeForDemand() {
        float drain = outputMode.drainPerTick();
        if (drain <= 0.0F) {
            return false;
        }

        if (outputMode == OutputMode.SUPERHEATED) {
            if (superheatedHeat < drain) {
                return false;
            }
            superheatedHeat -= drain;
            return true;
        }

        float remaining = drain;
        float fromHeated = Math.min(heatedHeat, remaining);
        heatedHeat -= fromHeated;
        remaining -= fromHeated;

        if (remaining > 0.0F) {
            float fromSuperheated = Math.min(superheatedHeat, remaining);
            superheatedHeat -= fromSuperheated;
            remaining -= fromSuperheated;
        }

        return remaining < drain;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!canSupply()
                || lastInputTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            return 0.0F;
        }

        float baseSpeed = outputMode.heatTier().rpmCap;
        Direction facing = ThermalBatteryBlock.interfaceSide(getBlockState());
        return facing != Direction.NORTH && facing != Direction.EAST
                ? baseSpeed
                : -baseSpeed;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float speed = Math.abs(getGeneratedSpeed());
        if (speed == 0.0F) {
            return lastCapacityProvided = 0.0F;
        }

        return lastCapacityProvided =
                outputMode.heatTier().baseStressCapacity / speed;
    }

    @Override
    public float calculateStressApplied() {
        return lastStressApplied = 0.0F;
    }

    private boolean updateMotorRotation() {
        return updateMotorRotation(false);
    }

    private boolean updateMotorRotation(boolean force) {
        float generatedSpeed = getGeneratedSpeed();
        if (!force && Float.compare(generatedSpeed, lastGeneratedSpeed) == 0) {
            return false;
        }

        lastGeneratedSpeed = generatedSpeed;
        super.updateGeneratedRotation();
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        return true;
    }

    public void onRedstonePowerChanged() {
        if (level == null || level.isClientSide) {
            return;
        }

        Direction side = ThermalBatteryBlock.interfaceSide(getBlockState());
        interfaceConnected = hasAlignedThermochemicalInterface(
                side,
                worldPosition.relative(side)
        );
        updateMotorRotation(true);
        fault = calculateFault();
        updateIndicatorBlockState();
        setChanged();
        sendData();
    }

    private boolean calculateFault() {
        boolean overstressed = Math.abs(getTheoreticalSpeed()) > 0.01F
                && Math.abs(getSpeed()) <= 0.01F;
        boolean unableToMeetDemand = isRedstoneEnabled()
                && demandGrace > 0
                && !canSupply();
        return !interfaceConnected || overstressed || unableToMeetDemand;
    }

    private boolean updateIndicatorBlockState() {
        Level currentLevel = level;
        if (currentLevel == null) {
            return false;
        }

        ThermalBatteryBlock.IndicatorState desired = indicatorState();
        BlockState state = getBlockState();
        if (state.getValue(ThermalBatteryBlock.INDICATOR) == desired) {
            return false;
        }

        currentLevel.setBlock(
                worldPosition,
                state.setValue(ThermalBatteryBlock.INDICATOR, desired),
                3
        );
        return true;
    }

    public void markNetworkDemand() {
        if (level == null || level.isClientSide) {
            return;
        }
        demandGrace = DEMAND_GRACE_TICKS;
    }

    public boolean canSupply() {
        if (!interfaceConnected || !isRedstoneEnabled()) {
            return false;
        }
        return outputMode == OutputMode.SUPERHEATED
                ? superheatedHeat >= outputMode.drainPerTick()
                : getStoredHeat() >= outputMode.drainPerTick();
    }

    public boolean isRedstoneEnabled() {
        return getBlockState().hasProperty(ThermalBatteryBlock.POWERED)
                && getBlockState().getValue(ThermalBatteryBlock.POWERED);
    }

    public MoltenRotorBlockEntity.RotorHeatLevel getOutputHeatTier() {
        return canSupply()
                ? outputMode.heatTier()
                : MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    }

    public int getOutputTemperature() {
        return canSupply() ? outputMode.temperature() : 0;
    }

    public float getStoredHeat() {
        return heatedHeat + superheatedHeat;
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        tooltip.add(Component.empty());
        int charge = Math.round(
                Mth.clamp(
                        getStoredHeat() / MAX_STORED_HEAT,
                        0.0F,
                        1.0F
                ) * 100.0F
        );
        tooltip.add(
                Component.translatable(
                                "tooltip.sulfuricresonance.thermal_battery.stored_heat",
                                charge
                        )
                        .withStyle(ChatFormatting.GOLD)
        );
        return true;
    }

    public float getHeatedHeat() {
        return heatedHeat;
    }

    public float getSuperheatedHeat() {
        return superheatedHeat;
    }

    public OutputMode getOutputMode() {
        return outputMode;
    }

    public void restoreStoredState(ItemStack stack) {
        CompoundTag stored = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
        if (!stored.contains("HeatedHeat")
                && !stored.contains("SuperheatedHeat")
                && !stored.contains("OutputMode")) {
            return;
        }

        heatedHeat = stored.getFloat("HeatedHeat");
        superheatedHeat = stored.getFloat("SuperheatedHeat");
        if (stored.contains("OutputMode")) {
            outputMode = OutputMode.fromSerializedName(
                    stored.getString("OutputMode")
            );
        }
        clampStorage();
        fault = calculateFault();
        updateIndicatorBlockState();
        setChanged();
        sendData();
    }

    public boolean setOutputMode(OutputMode mode) {
        if (mode == null || mode == outputMode) {
            return false;
        }
        outputMode = mode;
        if (level != null && !level.isClientSide) {
            fault = calculateFault();
            updateIndicatorBlockState();
            updateMotorRotation(true);
        }
        setChanged();
        sendData();
        return true;
    }

    public int estimatedRuntimeTicks() {
        float available = outputMode == OutputMode.SUPERHEATED
                ? superheatedHeat
                : getStoredHeat();
        float rate = outputMode.drainPerTick();
        if (rate <= 0.0F) {
            return 0;
        }
        return Math.max(0, (int) Math.floor(available / rate));
    }

    public ThermalBatteryBlock.IndicatorState indicatorState() {
        if (fault) {
            return ThermalBatteryBlock.IndicatorState.FAULT;
        }
        return outputMode == OutputMode.SUPERHEATED
                ? ThermalBatteryBlock.IndicatorState.SUPERHEATED
                : ThermalBatteryBlock.IndicatorState.HEATED;
    }

    public ContainerData getMenuData() {
        return menuData;
    }

    @Override
    protected void collectImplicitComponents(
            @NotNull DataComponentMap.Builder components
    ) {
        super.collectImplicitComponents(components);
        CompoundTag stored = new CompoundTag();
        stored.putFloat("HeatedHeat", heatedHeat);
        stored.putFloat("SuperheatedHeat", superheatedHeat);
        stored.putString("OutputMode", outputMode.serializedName());
        components.set(DataComponents.CUSTOM_DATA, CustomData.of(stored));
    }

    @Override
    protected void applyImplicitComponents(
            @NotNull BlockEntity.DataComponentInput input
    ) {
        super.applyImplicitComponents(input);
        CompoundTag stored = input.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();

        heatedHeat = stored.getFloat("HeatedHeat");
        superheatedHeat = stored.getFloat("SuperheatedHeat");
        if (stored.contains("OutputMode")) {
            outputMode = OutputMode.fromSerializedName(
                    stored.getString("OutputMode")
            );
        }
        clampStorage();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("HeatedHeat");
        tag.remove("SuperheatedHeat");
        tag.remove("OutputMode");
    }

    private void clampStorage() {
        heatedHeat = Math.max(0.0F, heatedHeat);
        superheatedHeat = Math.max(0.0F, superheatedHeat);
        float total = heatedHeat + superheatedHeat;
        if (total <= MAX_STORED_HEAT) {
            return;
        }
        float scale = MAX_STORED_HEAT / total;
        heatedHeat *= scale;
        superheatedHeat *= scale;
    }

    @Override
    protected void write(
            CompoundTag tag,
            HolderLookup.Provider provider,
            boolean clientPacket
    ) {
        super.write(tag, provider, clientPacket);
        tag.putFloat("HeatedHeat", heatedHeat);
        tag.putFloat("SuperheatedHeat", superheatedHeat);
        tag.putString("OutputMode", outputMode.serializedName());
        tag.putInt("InputTemperature", lastInputTemperature);
        tag.putString("InputTier", lastInputTier.serializedId);
        tag.putBoolean("InterfaceConnected", interfaceConnected);
        tag.putBoolean("Fault", fault);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);
        heatedHeat = tag.getFloat("HeatedHeat");
        superheatedHeat = tag.getFloat("SuperheatedHeat");
        outputMode = OutputMode.fromSerializedName(tag.getString("OutputMode"));
        lastInputTemperature = tag.getInt("InputTemperature");
        lastInputTier = MoltenRotorBlockEntity.RotorHeatLevel.fromSerializedId(
                tag.getString("InputTier")
        );
        interfaceConnected = tag.getBoolean("InterfaceConnected");
        fault = tag.getBoolean("Fault");
        clampStorage();
    }

    public enum OutputMode {
        HEATED(
                "heated",
                650,
                HEATED_DRAIN_PER_TICK,
                MoltenRotorBlockEntity.RotorHeatLevel.KINDLED
        ),
        SUPERHEATED(
                "superheated",
                950,
                SUPERHEATED_DRAIN_PER_TICK,
                MoltenRotorBlockEntity.RotorHeatLevel.SEETHING
        );

        private final String serializedName;
        private final int temperature;
        private final float drainPerTick;
        private final MoltenRotorBlockEntity.RotorHeatLevel heatTier;

        OutputMode(
                String serializedName,
                int temperature,
                float drainPerTick,
                MoltenRotorBlockEntity.RotorHeatLevel heatTier
        ) {
            this.serializedName = serializedName;
            this.temperature = temperature;
            this.drainPerTick = drainPerTick;
            this.heatTier = heatTier;
        }

        public String serializedName() {
            return serializedName;
        }

        public int temperature() {
            return temperature;
        }

        public float drainPerTick() {
            return drainPerTick;
        }

        public MoltenRotorBlockEntity.RotorHeatLevel heatTier() {
            return heatTier;
        }

        public static OutputMode fromSerializedName(String name) {
            for (OutputMode mode : values()) {
                if (mode.serializedName.equals(name)) {
                    return mode;
                }
            }
            return HEATED;
        }
    }
}
