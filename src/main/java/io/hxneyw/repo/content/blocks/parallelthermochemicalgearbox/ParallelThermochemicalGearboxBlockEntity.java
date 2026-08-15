package io.hxneyw.repo.content.blocks.parallelthermochemicalgearbox;

import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatTickHelper;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;









public class ParallelThermochemicalGearboxBlockEntity
        extends SplitShaftBlockEntity {

    private final ThermochemicalHeatData heatData =
            new ThermochemicalHeatData();

    public ParallelThermochemicalGearboxBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.PARALLEL_THERMOCHEMICAL_GEARBOX.get(),
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
    public float getRotationSpeedModifier(
            Direction face
    ) {
        if (!hasSource()) {
            return 1.0F;
        }

        Direction sourceFace = getSourceFacing();

        if (sourceFace == null || face == sourceFace) {
            return 1.0F;
        }

        float normalModifier =
                face.getAxisDirection()
                        == sourceFace.getAxisDirection()
                        ? 1.0F
                        : -1.0F;

        boolean faceVertical =
                face.getAxis() == Direction.Axis.Y;
        boolean sourceVertical =
                sourceFace.getAxis() == Direction.Axis.Y;

        if (faceVertical != sourceVertical) {
            normalModifier *= -1.0F;
        }

        return -normalModifier;
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
                "block.sulfuricresonance.parallel_thermochemical_gearbox",
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
