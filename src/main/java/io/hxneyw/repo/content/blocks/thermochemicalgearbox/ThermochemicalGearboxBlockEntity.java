package io.hxneyw.repo.content.blocks.thermochemicalgearbox;

import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class ThermochemicalGearboxBlockEntity
        extends GearboxBlockEntity {
    private final ThermochemicalHeatData heatData =
            new ThermochemicalHeatData();

    public ThermochemicalGearboxBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_GEARBOX.get(),
                position,
                state
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        ThermochemicalHeatResolver.Result result =
                ThermochemicalHeatResolver.resolve(this);

        if (!heatData.update(
                result.heatTier(),
                result.sourcePos(),
                result.pathLength(),
                result.spanLimit(),
                result.remainingAllowance(),
                result.temperature()
        )) {
            return;
        }

        setChanged();
        sendData();
    }

    public float getVisualSpeedMultiplier(
            Direction face
    ) {
        if (!hasSource()) {
            return 1.0F;
        }

        Direction sourceFace = getSourceFacing();

        if (sourceFace == null) {
            return 1.0F;
        }

        if (face.getAxis() == sourceFace.getAxis()) {
            return face == sourceFace
                    ? 1.0F
                    : -1.0F;
        }

        return face.getAxisDirection()
                == sourceFace.getAxisDirection()
                ? -1.0F
                : 1.0F;
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
                "block.sulfuricresonance.thermochemical_gearbox",
                null,
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
