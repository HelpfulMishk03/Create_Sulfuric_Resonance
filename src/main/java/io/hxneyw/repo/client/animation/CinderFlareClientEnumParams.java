package io.hxneyw.repo.client.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class CinderFlareClientEnumParams {
    public static final EnumProxy<HumanoidModel.ArmPose> CINDER_FLARE_LIGHTING =
            new EnumProxy<>(
                    HumanoidModel.ArmPose.class,
                    true,
                    (IArmPoseTransformer) CinderFlareClientEnumParams::applyPose
            );

    private CinderFlareClientEnumParams() {
    }

    private static void applyPose(
            HumanoidModel<?> model,
            LivingEntity entity,
            HumanoidArm arm
    ) {
        CinderFlareAnimationHandler.applyThirdPersonPose(
                model.rightArm,
                model.leftArm,
                entity
        );
    }
}
