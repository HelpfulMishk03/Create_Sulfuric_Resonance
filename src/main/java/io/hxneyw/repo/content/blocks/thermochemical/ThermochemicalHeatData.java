package io.hxneyw.repo.content.blocks.thermochemical;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class ThermochemicalHeatData {
    private MoltenRotorBlockEntity.RotorHeatLevel heatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    @Nullable
    private BlockPos sourcePosition;
    private int pathLength;
    private int spanLimit;
    private int remainingAllowance;
    private int temperature;

    public boolean update(
            MoltenRotorBlockEntity.RotorHeatLevel newHeatTier,
            @Nullable BlockPos newSourcePosition,
            int newPathLength,
            int newSpanLimit,
            int newRemainingAllowance,
            int newTemperature
    ) {
        boolean changed = heatTier != newHeatTier
                || !Objects.equals(
                sourcePosition,
                newSourcePosition
        )
                || pathLength != newPathLength
                || spanLimit != newSpanLimit
                || remainingAllowance != newRemainingAllowance
                || temperature != newTemperature;

        if (!changed) {
            return false;
        }

        heatTier = newHeatTier;
        sourcePosition = newSourcePosition == null
                ? null
                : newSourcePosition.immutable();
        pathLength = newPathLength;
        spanLimit = newSpanLimit;
        remainingAllowance = newRemainingAllowance;
        temperature = newTemperature;
        return true;
    }

    public boolean hasSource() {
        return sourcePosition != null
                && heatTier
                != MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    }

    public void addTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking,
            String titleKey,
            @Nullable String axis,
            int connections,
            @Nullable Component casingName
    ) {
        tooltip.add(Component.literal(""));
        tooltip.add(
                Component.translatable(titleKey)
                        .withStyle(ChatFormatting.GOLD)
        );

        if (!hasSource()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.no_source"
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.heat",
                            Component.translatable(
                                    "tooltip.sulfuricresonance.thermochemical.heat."
                                            + heatTier.serializedId
                            ).withStyle(heatColor(heatTier))
                    ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.temperature",
                            temperature
                    ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.transmitting"
                    ).withStyle(ChatFormatting.GREEN)
            );
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.path_length",
                            pathLength,
                            spanLimit
                    ).withStyle(ChatFormatting.GRAY)
            );
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.remaining_allowance",
                            remainingAllowance
                    ).withStyle(ChatFormatting.GRAY)
            );
        }

        if (axis != null) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.axis",
                            axis
                    ).withStyle(ChatFormatting.GRAY)
            );
        }

        if (connections >= 0) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.connections",
                            connections,
                            6
                    ).withStyle(ChatFormatting.GRAY)
            );
        }

        if (casingName != null) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.casing",
                            casingName
                    ).withStyle(ChatFormatting.GRAY)
            );
        }

        if (isPlayerSneaking && sourcePosition != null) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.thermochemical.source",
                            sourcePosition.getX(),
                            sourcePosition.getY(),
                            sourcePosition.getZ()
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    public void write(
            CompoundTag tag,
            boolean clientPacket
    ) {
        if (!clientPacket) {
            return;
        }

        tag.putString(
                "TransmittedHeatTier",
                heatTier.serializedId
        );
        tag.putInt(
                "ThermochemicalPathLength",
                pathLength
        );
        tag.putInt(
                "ThermochemicalSpanLimit",
                spanLimit
        );
        tag.putInt(
                "ThermochemicalRemainingAllowance",
                remainingAllowance
        );
        tag.putInt(
                "ThermochemicalTemperature",
                temperature
        );

        if (sourcePosition == null) {
            tag.remove("HeatSourcePos");
        } else {
            tag.putLong(
                    "HeatSourcePos",
                    sourcePosition.asLong()
            );
        }
    }

    public void read(
            CompoundTag tag,
            boolean clientPacket
    ) {
        if (!clientPacket) {
            heatTier =
                    MoltenRotorBlockEntity.RotorHeatLevel.NONE;
            sourcePosition = null;
            pathLength = 0;
            spanLimit = 0;
            remainingAllowance = 0;
            temperature = 0;
            return;
        }

        heatTier =
                MoltenRotorBlockEntity.RotorHeatLevel
                        .fromSerializedId(
                                tag.getString(
                                        "TransmittedHeatTier"
                                )
                        );
        sourcePosition = tag.contains(
                "HeatSourcePos",
                Tag.TAG_LONG
        )
                ? BlockPos.of(
                tag.getLong("HeatSourcePos")
        )
                : null;
        pathLength = tag.getInt(
                "ThermochemicalPathLength"
        );
        spanLimit = tag.getInt(
                "ThermochemicalSpanLimit"
        );
        remainingAllowance = tag.getInt(
                "ThermochemicalRemainingAllowance"
        );
        temperature = tag.getInt(
                "ThermochemicalTemperature"
        );
    }

    private static ChatFormatting heatColor(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    ) {
        return switch (heatTier) {
            case NONE -> ChatFormatting.GRAY;
            case SMOULDERING, FADING ->
                    ChatFormatting.YELLOW;
            case KINDLED -> ChatFormatting.RED;
            case SEETHING -> ChatFormatting.DARK_RED;
            case RADIANT -> ChatFormatting.DARK_PURPLE;
        };
    }
}
