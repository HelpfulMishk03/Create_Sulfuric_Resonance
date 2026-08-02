package io.hxneyw.repo.content.blocks.crucible;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class AshCeramicCrucibleBlockEntity extends BasinBlockEntity {

    public AshCeramicCrucibleBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                AllBlockEntities.ASH_CERAMIC_CRUCIBLE.get(),
                pos,
                state
        );
    }
    public IItemHandler getItemCapability() {
        return itemCapability;
    }

    public IFluidHandler getFluidCapability() {
        return fluidCapability;
    }
}