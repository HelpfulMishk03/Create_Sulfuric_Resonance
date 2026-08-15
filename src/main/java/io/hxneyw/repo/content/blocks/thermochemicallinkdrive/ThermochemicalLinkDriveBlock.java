package io.hxneyw.repo.content.blocks.thermochemicallinkdrive;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatUpdater;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalHeatResolver;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ThermochemicalLinkDriveBlock
        extends ChainDriveBlock
        implements ThermochemicalConnection {

    public ThermochemicalLinkDriveBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return false;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.THERMOCHEMICAL_LINK_DRIVE.get();
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction face,
                                           BlockState neighbour, LevelAccessor level, BlockPos currentPos,
                                           BlockPos facingPos) {
        if (neighbour.getBlock() instanceof ChainDriveBlock
                && !(neighbour.getBlock() instanceof ThermochemicalLinkDriveBlock)) {
            neighbour = Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, face, neighbour, level, currentPos, facingPos);
    }

    public static class ThermochemicalLinkDriveBlockEntity
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

            heatData.addTooltip(
                    tooltip,
                    isPlayerSneaking,
                    "block.sulfuricresonance.thermochemical_link_drive",
                    getBlockState()
                            .getValue(BlockStateProperties.AXIS)
                            .getName()
                            .toUpperCase(),
                    -1,
                    null
            );
            ThermochemicalHeatResolver.LinkDriveConnectionStats connectionStats =
                    level == null
                            ? ThermochemicalHeatResolver.LinkDriveConnectionStats.NONE
                            : ThermochemicalHeatResolver.getLinkDriveConnectionStats(
                                    level,
                                    worldPosition
                            );
            tooltip.add(Component.translatable(
                    "tooltip.sulfuricresonance.thermochemical_link_drive.chain_connections",
                    connectionStats.connections(),
                    connectionStats.capacity()
            ));
            return true;
        }

        @Override
        protected void write(
                CompoundTag tag,
                HolderLookup.Provider provider,
                boolean clientPacket
        ) {
            super.write(tag, provider, clientPacket);
            heatData.write(tag, clientPacket);
        }

        @Override
        protected void read(
                CompoundTag tag,
                HolderLookup.Provider provider,
                boolean clientPacket
        ) {
            super.read(tag, provider, clientPacket);
            heatData.read(tag, clientPacket);
        }
    }
}
