package io.hxneyw.repo.content.blocks.thermochemicallinkdrive;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatUpdater;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ThermochemicalLinkDriveBlockEntity
        extends KineticBlockEntity {

    private final ThermochemicalHeatData heatData =
            new ThermochemicalHeatData();

    public ThermochemicalLinkDriveBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_LINK_DRIVE.get(),
                position,
                state
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (ThermochemicalHeatUpdater.update(
                level,
                heatData,
                () -> ThermochemicalHeatResolver.resolve(this)
        )) {
            setChanged();
            sendData();
        }
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

        int connections = level == null
                ? 0
                : ThermochemicalHeatResolver
                .countDirectThermochemicalConnections(
                        level,
                        worldPosition
                );

        heatData.addTooltip(
                tooltip,
                isPlayerSneaking,
                "block.sulfuricresonance.thermochemical_link_drive",
                getBlockState()
                        .getValue(BlockStateProperties.AXIS)
                        .getName()
                        .toUpperCase(),
                connections,
                null
        );

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
    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(tag, provider, clientPacket);
        heatData.read(tag, clientPacket);
    }
}