package io.hxneyw.repo.content.blocks.thermochemicalcogwheel;

import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatTickHelper;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ThermochemicalCogwheelBlockEntity
        extends BracketedKineticBlockEntity {

    private final ThermochemicalHeatData heatData =
            new ThermochemicalHeatData();

    public ThermochemicalCogwheelBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_COGWHEEL.get(),
                pos,
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

        String titleKey = getBlockState().is(
                AllModBlocks.LARGE_THERMOCHEMICAL_COGWHEEL.get()
        )
                ? "block.sulfuricresonance.large_thermochemical_cogwheel"
                : "block.sulfuricresonance.thermochemical_cogwheel";

        heatData.addTooltip(
                tooltip,
                isPlayerSneaking,
                titleKey,
                getBlockState()
                        .getValue(BlockStateProperties.AXIS)
                        .getName()
                        .toUpperCase(),
                -1,
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