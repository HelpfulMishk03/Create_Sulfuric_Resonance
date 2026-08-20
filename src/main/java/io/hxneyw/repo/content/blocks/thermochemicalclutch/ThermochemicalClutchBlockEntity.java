package io.hxneyw.repo.content.blocks.thermochemicalclutch;

import com.simibubi.create.content.kinetics.transmission.ClutchBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatTickHelper;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class ThermochemicalClutchBlockEntity
        extends ClutchBlockEntity {
    private final ThermochemicalHeatData heatData =
            new ThermochemicalHeatData();

    public ThermochemicalClutchBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_CLUTCH.get(),
                position,
                state
        );
    }

    @Override
    public void tick() {
        super.tick();

        ThermochemicalHeatTickHelper.tick(
                this,
                heatData,
                this::sendData
        );
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        super.addToGoggleTooltip(
                tooltip,
                isPlayerSneaking
        );

        heatData.addTooltip(
                tooltip,
                isPlayerSneaking,
                "block.sulfuricresonance.thermochemical_clutch",
                getBlockState()
                        .getValue(ThermochemicalClutchBlock.AXIS)
                        .getName()
                        .toUpperCase(),
                -1,
                null
        );

        boolean powered = getBlockState().getValue(
                ThermochemicalClutchBlock.POWERED
        );
        tooltip.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.thermochemical_clutch.state",
                        Component.translatable(
                                powered
                                        ? "tooltip.sulfuricresonance.thermochemical_clutch.blocked"
                                        : "tooltip.sulfuricresonance.thermochemical_clutch.passing"
                        ).withStyle(
                                powered
                                        ? ChatFormatting.RED
                                        : ChatFormatting.GREEN
                        )
                ).withStyle(ChatFormatting.GRAY)
        );

        return true;
    }

    @Override
    protected void write(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.write(
                tag,
                provider,
                clientPacket
        );
        heatData.write(
                tag,
                clientPacket
        );
    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(
                tag,
                provider,
                clientPacket
        );
        heatData.read(
                tag,
                clientPacket
        );
    }
}
