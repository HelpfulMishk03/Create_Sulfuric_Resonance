package io.hxneyw.repo.content.blocks.parallelthermochemicalgearbox;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalConnection;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;





public class ParallelThermochemicalGearboxBlock
        extends KineticBlock
        implements IBE<ParallelThermochemicalGearboxBlockEntity>,
        ThermochemicalConnection {

    public ParallelThermochemicalGearboxBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos position,
            BlockState state,
            Direction face
    ) {
        return true;
    }

    @Override
    public boolean doesNotHaveThermochemicalConnection(
            BlockState state,
            Direction face
    ) {
        return false;
    }

    




    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public Class<ParallelThermochemicalGearboxBlockEntity>
    getBlockEntityClass() {
        return ParallelThermochemicalGearboxBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ParallelThermochemicalGearboxBlockEntity>
    getBlockEntityType() {
        return AllBlockEntities.PARALLEL_THERMOCHEMICAL_GEARBOX.get();
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(
            @NotNull BlockState state
    ) {
        return PushReaction.PUSH_ONLY;
    }

    @Override
    public float getParticleTargetRadius() {
        return 0.75F;
    }
}
