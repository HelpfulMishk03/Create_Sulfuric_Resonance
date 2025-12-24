package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModArmInteractionPoints {

    // Use DeferredRegister for proper registration timing
    public static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINTS =
            DeferredRegister.create(
                    com.simibubi.create.api.registry.CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE,
                    CreateSulfuricResonance.MODID
            );

    // Register the Molten Rotor arm interaction point type
    public static final DeferredHolder<ArmInteractionPointType, MoltenRotorType> MOLTEN_ROTOR =
            ARM_INTERACTION_POINTS.register("molten_rotor", MoltenRotorType::new);

    // Call this from your main mod class constructor
    public static void register(IEventBus eventBus) {
        ARM_INTERACTION_POINTS.register(eventBus);
        CreateSulfuricResonance.LOGGER.info("Registered Molten Rotor arm interaction point");
    }

    public static class MoltenRotorType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof MoltenRotorBlock;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new MoltenRotorArmPoint(this, level, pos, state);
        }
    }
}