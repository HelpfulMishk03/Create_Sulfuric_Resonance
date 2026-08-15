package io.hxneyw.repo.compat.arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;










public final class SulfuricResonanceChamberArmPoint
        extends ArmInteractionPoint {

    public SulfuricResonanceChamberArmPoint(
            ArmInteractionPointType type,
            Level level,
            BlockPos pos,
            BlockState state
    ) {
        super(type, level, pos, state);
    }

    @Override
    protected Vec3 getInteractionPositionVector() {
        
        return Vec3.atCenterOf(pos).add(0.0D, 0.50D, 0.0D);
    }

}
