package io.hxneyw.repo.content.blocks.resonantheatinjector;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ResonantHeatInjectorBlockEntity extends KineticBlockEntity {

    private final ThermochemicalHeatData heatData = new ThermochemicalHeatData();
    private MoltenRotorBlockEntity.RotorHeatLevel suppliedHeatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;

    public ResonantHeatInjectorBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(AllBlockEntities.RESONANT_HEAT_INJECTOR.get(), position, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        ThermochemicalHeatResolver.Result result =
                ThermochemicalHeatResolver.resolve(this);

        boolean changed = heatData.update(
                result.heatTier(),
                result.sourcePos(),
                result.pathLength(),
                result.spanLimit(),
                result.remainingAllowance(),
                result.temperature()
        );

        if (suppliedHeatTier != result.heatTier()) {
            suppliedHeatTier = result.heatTier();
            changed = true;
        }

        if (changed) {
            notifyTargetAbove();
            setChanged();
            sendData();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide) {
            notifyTargetAbove();
        }
    }

    public MoltenRotorBlockEntity.RotorHeatLevel getSuppliedHeatTier() {
        return suppliedHeatTier;
    }

    public HeatLevel getCreateHeatLevel() {
        return switch (suppliedHeatTier) {
            case NONE -> HeatLevel.NONE;
            case SMOULDERING, FADING -> HeatLevel.SMOULDERING;
            case KINDLED -> HeatLevel.KINDLED;
            case SEETHING, RADIANT -> HeatLevel.SEETHING;
        };
    }

    public boolean hasCompatibleTargetAbove() {
        if (level == null) {
            return false;
        }
        return level.getBlockEntity(worldPosition.above()) instanceof BasinBlockEntity;
    }

    public boolean isApplyingHeat() {
        return suppliedHeatTier != MoltenRotorBlockEntity.RotorHeatLevel.NONE
                && hasCompatibleTargetAbove();
    }

    private void notifyTargetAbove() {
        if (level == null) {
            return;
        }

        BlockEntity target = level.getBlockEntity(worldPosition.above());
        if (target instanceof BasinBlockEntity basin) {
            basin.notifyChangeOfContents();
            basin.notifyUpdate();
        }
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        heatData.addTooltip(
                tooltip,
                isPlayerSneaking,
                "block.sulfuricresonance.resonant_heat_injector",
                ResonantHeatInjectorBlock.inputSide(getBlockState())
                        .getAxis()
                        .getName()
                        .toUpperCase(),
                -1,
                null
        );

        tooltip.add(Component.translatable(
                "tooltip.sulfuricresonance.resonant_heat_injector.output_upward"
        ).withStyle(ChatFormatting.GRAY));

        if (level != null && hasCompatibleTargetAbove()) {
            Component targetName = level.getBlockState(worldPosition.above())
                    .getBlock()
                    .getName();
            tooltip.add(Component.translatable(
                    "tooltip.sulfuricresonance.resonant_heat_injector.target",
                    targetName
            ).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable(
                    "tooltip.sulfuricresonance.resonant_heat_injector.no_target"
            ).withStyle(ChatFormatting.DARK_GRAY));
        }

        Component status = Component.translatable(
                isApplyingHeat()
                        ? "tooltip.sulfuricresonance.resonant_heat_injector.applying"
                        : "tooltip.sulfuricresonance.resonant_heat_injector.waiting"
        );
        tooltip.add(status.copy().withStyle(
                isApplyingHeat() ? ChatFormatting.GREEN : ChatFormatting.YELLOW
        ));
        return true;
    }

    @Override
    protected void write(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.write(tag, provider, clientPacket);
        heatData.write(tag, clientPacket);
        if (clientPacket) {
            tag.putString("SuppliedHeatTier", suppliedHeatTier.serializedId);
        }
    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);
        heatData.read(tag, clientPacket);
        suppliedHeatTier = clientPacket
                ? MoltenRotorBlockEntity.RotorHeatLevel.fromSerializedId(
                tag.getString("SuppliedHeatTier")
        )
                : MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    }
}
