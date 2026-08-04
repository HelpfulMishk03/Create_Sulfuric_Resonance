package io.hxneyw.repo.content.blocks.thermochemicalconduit;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ThermochemicalConduitBlockEntity
        extends KineticBlockEntity {

    private static final int HEAT_SCAN_INTERVAL = 20;

    /*
     * Start at the interval so a newly placed or loaded conduit scans on its
     * first server tick rather than waiting one full second.
     */
    private int heatScanTicks = HEAT_SCAN_INTERVAL;

    private MoltenRotorBlockEntity.RotorHeatLevel transmittedHeatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;

    @Nullable
    private BlockPos heatSourcePos;

    /*
     * Live recipe validation is cached once per conduit per server game tick.
     * This prevents every transported stack from repeating the same kinetic
     * graph traversal.
     */
    private long liveValidationGameTime = Long.MIN_VALUE;

    private MoltenRotorBlockEntity.RotorHeatLevel
            liveValidatedHeatTier =
            MoltenRotorBlockEntity.RotorHeatLevel.NONE;

    public ThermochemicalConduitBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_CONDUIT.get(),
                position,
                state
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level == null || this.level.isClientSide) {
            return;
        }

        this.heatScanTicks++;

        if (this.heatScanTicks < HEAT_SCAN_INTERVAL) {
            return;
        }

        this.heatScanTicks = 0;
        this.refreshTransmittedHeat();
    }

    private void refreshTransmittedHeat() {
        ThermochemicalHeatResolver.Result result =
                ThermochemicalHeatResolver.resolve(this);

        boolean changed =
                this.transmittedHeatTier != result.heatTier()
                        || !Objects.equals(
                                this.heatSourcePos,
                                result.sourcePos()
                        );

        if (!changed) {
            return;
        }

        this.transmittedHeatTier = result.heatTier();
        this.heatSourcePos = result.sourcePos();

        this.setChanged();
        this.sendData();
    }

    public MoltenRotorBlockEntity.RotorHeatLevel
    getTransmittedHeatTier() {
        return this.transmittedHeatTier;
    }

    public boolean hasHeatSource() {
        return this.heatSourcePos != null
                && this.transmittedHeatTier
                != MoltenRotorBlockEntity.RotorHeatLevel.NONE;
    }



    /**
     * Returns heat from a live kinetic-path validation.
     *
     * Unlike transmittedHeatTier, this is not the one-second display cache.
     * It is refreshed on demand once per server tick and is safe to use as a
     * hard recipe-processing gate.
     */
    public MoltenRotorBlockEntity.RotorHeatLevel
    getLiveValidatedHeatTier() {
        if (this.level == null
                || this.level.isClientSide) {
            return MoltenRotorBlockEntity
                    .RotorHeatLevel.NONE;
        }

        long gameTime = this.level.getGameTime();

        if (this.liveValidationGameTime != gameTime) {
            ThermochemicalHeatResolver.Result result =
                    ThermochemicalHeatResolver.resolve(this);

            this.liveValidatedHeatTier =
                    result.heatTier();

            this.liveValidationGameTime = gameTime;
        }

        return this.liveValidatedHeatTier;
    }

    /**
     * Used by Combustion Belts and later heat-consuming machines.
     */
    public boolean canProvideHeatTo(BlockPos targetPosition) {
        return this.hasHeatSource()
                && ThermochemicalHeatResolver.isWithinRange(
                        this.worldPosition,
                        targetPosition,
                        ThermochemicalHeatResolver.CONDUIT_OUTPUT_RANGE
                );
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        tooltip.add(Component.literal(""));

        tooltip.add(
                Component.translatable(
                        "block.sulfuricresonance.thermochemical_conduit"
                ).withStyle(ChatFormatting.GOLD)
        );

        if (!this.hasHeatSource()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance."
                                    + "thermochemical_conduit.no_source"
                    ).withStyle(ChatFormatting.GRAY)
            );

            return true;
        }

        tooltip.add(
                Component.translatable(
                        "tooltip.sulfuricresonance."
                                + "thermochemical_conduit.transmitting",
                        Component.literal(
                                this.transmittedHeatTier.displayName
                        ).withStyle(
                                getHeatColor(this.transmittedHeatTier)
                        )
                ).withStyle(ChatFormatting.GRAY)
        );

        if (isPlayerSneaking && this.heatSourcePos != null) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance."
                                    + "thermochemical_conduit.source",
                            this.heatSourcePos.getX(),
                            this.heatSourcePos.getY(),
                            this.heatSourcePos.getZ()
                    ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        return true;
    }

    private static ChatFormatting getHeatColor(
            MoltenRotorBlockEntity.RotorHeatLevel heatTier
    ) {
        return switch (heatTier) {
            case NONE -> ChatFormatting.GRAY;
            case SMOULDERING, FADING -> ChatFormatting.YELLOW;
            case KINDLED -> ChatFormatting.RED;
            case SEETHING -> ChatFormatting.DARK_RED;
            case RADIANT -> ChatFormatting.DARK_PURPLE;
        };
    }

    @Override
    protected void write(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.write(tag, provider, clientPacket);

        /*
         * Heat is derived from the live kinetic network, so it is not persisted
         * to disk. It is included only in client update packets for goggles and
         * future conduit visuals.
         */
        if (!clientPacket) {
            return;
        }

        tag.putString(
                "TransmittedHeatTier",
                this.transmittedHeatTier.serializedId
        );

        if (this.heatSourcePos != null) {
            tag.putLong(
                    "HeatSourcePos",
                    this.heatSourcePos.asLong()
            );
        }
    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);

        if (!clientPacket) {
            this.transmittedHeatTier =
                    MoltenRotorBlockEntity.RotorHeatLevel.NONE;
            this.heatSourcePos = null;
            this.heatScanTicks = HEAT_SCAN_INTERVAL;
            this.liveValidationGameTime = Long.MIN_VALUE;
            this.liveValidatedHeatTier =
                    MoltenRotorBlockEntity.RotorHeatLevel.NONE;
            return;
        }

        this.transmittedHeatTier =
                MoltenRotorBlockEntity.RotorHeatLevel.fromSerializedId(
                        tag.getString("TransmittedHeatTier")
                );

        this.heatSourcePos =
                tag.contains("HeatSourcePos", Tag.TAG_LONG)
                        ? BlockPos.of(tag.getLong("HeatSourcePos"))
                        : null;
    }
}
