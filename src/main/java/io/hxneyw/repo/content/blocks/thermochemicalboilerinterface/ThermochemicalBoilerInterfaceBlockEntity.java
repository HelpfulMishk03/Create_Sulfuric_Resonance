package io.hxneyw.repo.content.blocks.thermochemicalboilerinterface;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ThermochemicalBoilerInterfaceBlockEntity extends KineticBlockEntity {

    private ThermochemicalBoilerInterfaceArray.Snapshot snapshot =
            ThermochemicalBoilerInterfaceArray.Snapshot.NONE;
    private int cachedArraySize;
    private boolean cachedArrayValid;
    private MoltenRotorBlockEntity.RotorHeatLevel cachedHeatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    private int cachedTemperature;
    private int cachedGrossSu;
    private boolean cachedInput;
    private boolean cachedBoiler;
    private int refreshCooldown;

    public ThermochemicalBoilerInterfaceBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(AllBlockEntities.THERMOCHEMICAL_BOILER_INTERFACE.get(), position, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        if (refreshCooldown-- <= 0) {
            refreshCooldown = 4;
            refreshArrayState();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide) {
            refreshArrayState();
        }
    }

    public ThermochemicalHeatResolver.Result resolveDirectInput() {
        if (level == null
                || level.isClientSide
                || !getBlockState().getValue(
                ThermochemicalBoilerInterfaceBlock.INPUT_ACTIVE
        )
                || !ThermochemicalBoilerInterfaceBlock.hasValidInputNeighbour(
                level,
                worldPosition,
                ThermochemicalBoilerInterfaceBlock.inputSide(getBlockState())
        )) {
            return ThermochemicalHeatResolver.Result.NONE;
        }
        Direction inputSide = ThermochemicalBoilerInterfaceBlock.inputSide(
                getBlockState()
        );
        return ThermochemicalHeatResolver.resolveNetworkOnly(
                level,
                worldPosition.relative(inputSide)
        );
    }

    public void requestImmediateRefresh() {
        refreshCooldown = 0;
    }

    public ThermochemicalBoilerInterfaceArray.Snapshot getSnapshot() {
        if (level != null && !level.isClientSide) {
            return ThermochemicalBoilerInterfaceArray.resolve(level, worldPosition);
        }
        return snapshot;
    }

    public MoltenRotorBlockEntity.RotorHeatLevel getDisplayedHeatTier() {
        return level != null && level.isClientSide
                ? cachedHeatTier
                : getSnapshot().heatTier();
    }

    public int getDisplayedTemperature() {
        return level != null && level.isClientSide
                ? cachedTemperature
                : getSnapshot().temperature();
    }

    public boolean hasDisplayedBoiler() {
        if (level == null) {
            return false;
        }
        return level.isClientSide
                ? cachedBoiler
                : ThermochemicalBoilerInterfaceArray.hasBoilerTarget(
                level,
                getSnapshot()
        );
    }

    public int getBoilerHeatContribution() {
        if (level == null) {
            return 0;
        }
        ThermochemicalBoilerInterfaceArray.Snapshot current = getSnapshot();
        return ThermochemicalBoilerInterfaceArray.getMemberHeatContribution(
                level,
                worldPosition,
                current
        );
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = 0.0F;
        return 0.0F;
    }

    private void refreshArrayState() {
        if (level == null) {
            return;
        }

        ThermochemicalBoilerInterfaceArray.Snapshot current =
                ThermochemicalBoilerInterfaceArray.resolve(level, worldPosition);
        boolean boiler = ThermochemicalBoilerInterfaceArray.hasBoilerTarget(
                level,
                current
        );
        boolean changed = current.size() != cachedArraySize
                || current.valid() != cachedArrayValid
                || current.heatTier() != cachedHeatTier
                || current.temperature() != cachedTemperature
                || current.targetGrossSu() != cachedGrossSu
                || current.isInput(worldPosition) != cachedInput
                || boiler != cachedBoiler;

        ThermochemicalBoilerInterfaceArray.synchronizeState(level, current);

        snapshot = current;
        cachedArraySize = current.size();
        cachedArrayValid = current.valid();
        cachedHeatTier = current.heatTier();
        cachedTemperature = current.temperature();
        cachedGrossSu = current.targetGrossSu();
        cachedInput = current.isInput(worldPosition);
        cachedBoiler = boiler;

        if (!changed) {
            return;
        }

        notifyBoiler(current);
        setChanged();
        sendData();
    }

    private void notifyBoiler(ThermochemicalBoilerInterfaceArray.Snapshot current) {
        FluidTankBlockEntity controller =
                ThermochemicalBoilerInterfaceArray.findBoilerController(level, current);
        if (controller != null) {
            controller.boiler.needsHeatLevelUpdate = true;
            controller.updateBoilerTemperature();
            controller.notifyUpdate();
        }
        for (BlockPos member : current.members()) {
            BlockEntity above = level.getBlockEntity(member.above());
            if (above instanceof FluidTankBlockEntity tank) {
                FluidTankBlockEntity memberController = tank.getControllerBE();
                if (memberController != null) {
                    memberController.boiler.needsHeatLevelUpdate = true;
                }
                tank.updateBoilerTemperature();
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal(""));
        tooltip.add(goggleLine(Component.translatable(
                "block.sulfuricresonance.thermochemical_boiler_interface"
        ).withStyle(ChatFormatting.GOLD)));
        tooltip.add(goggleLine(Component.translatable(
                "tooltip.sulfuricresonance.thermochemical_boiler_interface.array",
                cachedArraySize
        ).withStyle(cachedArrayValid ? ChatFormatting.GRAY : ChatFormatting.RED)));

        if (!cachedArrayValid) {
            tooltip.add(goggleLine(Component.translatable(
                    "tooltip.sulfuricresonance.thermochemical_boiler_interface.invalid_array"
            ).withStyle(ChatFormatting.RED)));
            return true;
        }

        MutableComponent heat = Component.translatable(
                "tooltip.sulfuricresonance.thermochemical_boiler_interface.heat",
                Component.translatable(
                        "tooltip.sulfuricresonance.thermochemical.heat."
                                + cachedHeatTier.serializedId
                )
        );
        if (cachedHeatTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            heat.append(Component.literal(
                    " (" + cachedTemperature + "°C)"
            ));
        }
        tooltip.add(goggleLine(heat.withStyle(
                cachedHeatTier == MoltenRotorBlockEntity.RotorHeatLevel.NONE
                        ? ChatFormatting.DARK_GRAY
                        : ChatFormatting.YELLOW
        )));

        if (cachedHeatTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE) {
            tooltip.add(goggleLine(Component.translatable(
                    "tooltip.sulfuricresonance.thermochemical_boiler_interface.gross_output",
                    cachedGrossSu
            ).withStyle(ChatFormatting.AQUA)));
        }

        tooltip.add(goggleLine(Component.translatable(
                cachedInput
                        ? "tooltip.sulfuricresonance.thermochemical_boiler_interface.input"
                        : "tooltip.sulfuricresonance.thermochemical_boiler_interface.shared"
        ).withStyle(cachedInput ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)));

        if (!cachedBoiler) {
            tooltip.add(goggleLine(Component.translatable(
                    "tooltip.sulfuricresonance.thermochemical_boiler_interface.no_boiler"
            ).withStyle(ChatFormatting.DARK_GRAY)));
        }
        return true;
    }

    private static Component goggleLine(Component component) {
        return Component.literal("    ").append(component);
    }

    @Override
    protected void write(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.write(tag, provider, clientPacket);
        if (clientPacket) {
            tag.putInt("BoilerArraySize", cachedArraySize);
            tag.putBoolean("BoilerArrayValid", cachedArrayValid);
            tag.putString("BoilerHeatTier", cachedHeatTier.serializedId);
            tag.putInt("BoilerTemperature", cachedTemperature);
            tag.putInt("BoilerGrossSu", cachedGrossSu);
            tag.putBoolean("BoilerInput", cachedInput);
            tag.putBoolean("BoilerPresent", cachedBoiler);
        }
    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);
        if (clientPacket) {
            cachedArraySize = tag.getInt("BoilerArraySize");
            cachedArrayValid = tag.getBoolean("BoilerArrayValid");
            cachedHeatTier = MoltenRotorBlockEntity.RotorHeatLevel.fromSerializedId(
                    tag.getString("BoilerHeatTier")
            );
            cachedTemperature = tag.getInt("BoilerTemperature");
            cachedGrossSu = tag.getInt("BoilerGrossSu");
            cachedInput = tag.getBoolean("BoilerInput");
            cachedBoiler = tag.getBoolean("BoilerPresent");
        }
    }
}
