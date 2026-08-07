package io.hxneyw.repo.content.blocks.thermochemicalshaft;

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

public class ThermochemicalShaftBlockEntity
        extends KineticBlockEntity {
    private final ThermochemicalHeatData heatData =
            new ThermochemicalHeatData();

    public ThermochemicalShaftBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_SHAFT.get(),
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

        ThermochemicalShaftBlock.CasingType casing =
                getBlockState().getValue(
                        ThermochemicalShaftBlock.CASING
                );

        heatData.addTooltip(
                tooltip,
                isPlayerSneaking,
                "block.sulfuricresonance.thermochemical_shaft",
                getBlockState()
                        .getValue(
                                ThermochemicalShaftBlock.AXIS
                        )
                        .getName()
                        .toUpperCase(),
                -1,
                Component.translatable(
                        casing.translationKey()
                )
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
