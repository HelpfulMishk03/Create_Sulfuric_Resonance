package io.hxneyw.repo.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.items.CinderFlareItem;
import io.hxneyw.repo.content.items.LitCinderFlareItem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CinderFlareAnimationHandler {
    private CinderFlareAnimationHandler() {
    }

    public static boolean applyFirstPersonItemTransform(
            PoseStack poseStack,
            LocalPlayer player,
            HumanoidArm arm,
            ItemStack itemInHand,
            float partialTick,
            float equipProcess,
            float swingProcess
    ) {
        if (isLighting(player)) {
            boolean flare = itemInHand.is(Items.CINDER_FLARE.get())
                    && arm == player.getMainArm();
            boolean flint = itemInHand.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)
                    && arm != player.getMainArm();

            if (!flare && !flint) {
                return false;
            }

            AnimationState state = animationState(player, partialTick);
            if (flare) {
                transformFlare(poseStack, arm, state, equipProcess);
            } else {
                transformFlintAndSteel(poseStack, arm, state, equipProcess);
            }
            return true;
        }

        if (itemInHand.is(Items.LIT_CINDER_FLARE.get())
                && arm == player.getMainArm()
                && isThrowing(player, partialTick)) {
            transformThrownFlare(
                    poseStack,
                    player,
                    arm,
                    partialTick,
                    equipProcess
            );
            return true;
        }

        return false;
    }

    private static void transformFlare(
            PoseStack poseStack,
            HumanoidArm arm,
            AnimationState state,
            float equipProcess
    ) {
        float side = armSide(arm);
        float held = state.flareHeld();
        float strike = state.firstStrike() * 0.30F + state.secondStrike();
        float idleY = -0.52F - 0.60F * equipProcess;
        float presentationArc = state.flarePresentationArc();
        float withdrawalArc = state.flareWithdrawalArc();
        float swingArc = state.swingArc();
        float response = state.response();

        poseStack.translate(
                Mth.lerp(held, 0.56F * side, 0.02F * side)
                        - 0.010F * side * strike
                        + (0.014F * presentationArc
                        + 0.010F * withdrawalArc
                        + 0.006F * response
                        - 0.006F * swingArc) * side,
                Mth.lerp(held, idleY, -0.49F)
                        + 0.012F * strike
                        - 0.026F * presentationArc
                        - 0.018F * withdrawalArc
                        - 0.008F * response
                        + 0.004F * swingArc,
                Mth.lerp(held, -0.72F, -0.76F)
                        - 0.010F * strike
                        + 0.022F * presentationArc
                        + 0.016F * withdrawalArc
                        + 0.006F * response
                        - 0.008F * swingArc
        );

        float scale = 1.0F - 0.22F * held;
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.XP.rotationDegrees(
                -6.0F * held
                        + 1.0F * strike
                        - 2.0F * presentationArc
                        + 1.2F * withdrawalArc
                        - 0.8F * response
                        + 0.8F * swingArc
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                3.0F * side * held
                        + 1.5F * side * presentationArc
                        - 1.0F * side * withdrawalArc
                        - 0.8F * side * swingArc
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                -16.0F * side * held
                        + 1.0F * side * strike
                        + 2.2F * side * presentationArc
                        - 1.4F * side * withdrawalArc
                        - 1.0F * side * response
                        - 1.5F * side * swingArc
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
    }

    private static void transformFlintAndSteel(
            PoseStack poseStack,
            HumanoidArm arm,
            AnimationState state,
            float equipProcess
    ) {
        float offSide = armSide(arm);
        float held = state.flintHeld();
        float reach = state.firstStrike() * 0.92F + state.secondStrike();
        float force = state.firstStrike() * 0.68F + state.secondStrike();
        float idleY = -0.52F - 0.60F * equipProcess;
        float presentationArc = state.flintPresentationArc();
        float withdrawalArc = state.flintWithdrawalArc();
        float swingArc = state.swingArc();
        float response = state.response();

        float targetX = (0.26F - 0.24F * reach + 0.020F * state.reset()) * offSide;
        float targetY = -0.50F + 0.08F * reach - 0.015F * state.reset();

        poseStack.translate(
                Mth.lerp(held, 0.56F * offSide, targetX)
                        + (0.012F * presentationArc
                        + 0.009F * withdrawalArc
                        + 0.032F * swingArc
                        + 0.004F * response) * offSide,
                Mth.lerp(held, idleY, targetY)
                        - 0.025F * presentationArc
                        - 0.018F * withdrawalArc
                        - 0.028F * swingArc
                        - 0.006F * response,
                Mth.lerp(held, -0.72F, -0.80F)
                        - 0.070F * reach
                        + 0.010F * state.reset()
                        + 0.020F * presentationArc
                        + 0.014F * withdrawalArc
                        + 0.038F * swingArc
                        + 0.006F * response
        );

        float scale = 1.0F - 0.28F * held;
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.XP.rotationDegrees(
                -6.0F * held
                        + 10.0F * force
                        - 2.0F * state.reset()
                        - 6.0F * swingArc
                        - 1.0F * response
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                2.0F * offSide * held
                        - 4.0F * offSide * force
                        + 3.0F * offSide * swingArc
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                10.0F * offSide * held
                        + 40.0F * offSide * reach
                        - 7.0F * offSide * state.reset()
                        + 10.0F * offSide * swingArc
                        - 2.0F * offSide * response
        ));
    }

    private static void transformThrownFlare(
            PoseStack poseStack,
            LocalPlayer player,
            HumanoidArm arm,
            float partialTick,
            float equipProcess
    ) {
        float side = armSide(arm);
        ThrowState state = throwState(throwProgress(player, partialTick));
        float idleY = -0.52F - 0.60F * equipProcess;

        poseStack.translate(
                0.56F * side
                        + 0.020F * side * state.back()
                        - 0.010F * side * state.forward(),
                idleY
                        - 0.34F * state.back()
                        + 0.24F * state.forward(),
                -0.72F
                        + 0.14F * state.back()
                        - 0.20F * state.forward()
        );

        poseStack.mulPose(Axis.XP.rotationDegrees(
                -14.0F * state.back()
                        + 18.0F * state.forward()
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                -0.5F * side * state.forward()
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                1.0F * side * state.back()
                        - 1.5F * side * state.forward()
        ));
    }

    public static void applyThirdPersonPose(
            ModelPart rightArm,
            ModelPart leftArm,
            LivingEntity entity
    ) {
        if (isThrowing(entity, 0.0F)) {
            applyThirdPersonThrowPose(rightArm, leftArm, entity);
            return;
        }

        if (!isLighting(entity)) {
            return;
        }

        AnimationState state = animationState(entity, 0.0F);
        float reach = state.firstStrike() * 0.92F + state.secondStrike();
        float force = state.firstStrike() * 0.68F + state.secondStrike();
        float flareTransitionArc = state.flarePresentationArc()
                - state.flareWithdrawalArc();
        float flintTransitionArc = state.flintPresentationArc()
                - state.flintWithdrawalArc();

        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        float mainSide = rightHanded ? 1.0F : -1.0F;

        ModelPart flareArm = rightHanded ? rightArm : leftArm;
        ModelPart flintArm = rightHanded ? leftArm : rightArm;

        flareArm.xRot = Mth.lerp(
                state.flareHeld(),
                flareArm.xRot,
                -0.80F + 0.02F * force
                        - 0.035F * flareTransitionArc
                        - 0.015F * state.response()
                        + 0.012F * state.swingArc()
        );
        flareArm.yRot = Mth.lerp(
                state.flareHeld(),
                flareArm.yRot,
                -0.18F * mainSide
                        + 0.020F * mainSide * flareTransitionArc
                        - 0.012F * mainSide * state.swingArc()
        );
        flareArm.zRot = Mth.lerp(
                state.flareHeld(),
                flareArm.zRot,
                -0.16F * mainSide
                        + 0.025F * mainSide * flareTransitionArc
                        - 0.012F * mainSide * state.response()
                        - 0.020F * mainSide * state.swingArc()
        );

        flintArm.xRot = Mth.lerp(
                state.flintHeld(),
                flintArm.xRot,
                -0.82F - 0.12F * force
                        + 0.025F * state.reset()
                        - 0.080F * state.swingArc()
                        - 0.020F * state.response()
        );
        flintArm.yRot = Mth.lerp(
                state.flintHeld(),
                flintArm.yRot,
                0.18F * mainSide
                        + 0.04F * mainSide * force
                        - 0.01F * mainSide * state.reset()
                        + 0.040F * mainSide * state.swingArc()
        );
        flintArm.zRot = Mth.lerp(
                state.flintHeld(),
                flintArm.zRot,
                0.18F * mainSide
                        + 0.26F * mainSide * reach
                        - 0.05F * mainSide * state.reset()
                        + 0.12F * mainSide * state.swingArc()
                        - 0.025F * mainSide * state.response()
        );
    }

    private static void applyThirdPersonThrowPose(
            ModelPart rightArm,
            ModelPart leftArm,
            LivingEntity entity
    ) {
        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        ModelPart throwArm = rightHanded ? rightArm : leftArm;
        ThrowState state = throwState(throwProgress(entity, 0.0F));

        float blend = Mth.clamp(state.back() + state.forward(), 0.0F, 1.0F);

        float targetX = 0.52F * state.back() - 1.08F * state.forward();
        float targetY = 0.0F;
        float targetZ = 0.0F;

        throwArm.xRot = Mth.lerp(blend, throwArm.xRot, targetX);
        throwArm.yRot = Mth.lerp(blend, throwArm.yRot, targetY);
        throwArm.zRot = Mth.lerp(blend, throwArm.zRot, targetZ);
    }

    public static boolean isThrowing(LivingEntity entity, float partialTick) {
        return entity.isUsingItem()
                && entity.getUsedItemHand() == InteractionHand.MAIN_HAND
                && entity.getUseItem().is(Items.LIT_CINDER_FLARE.get());
    }

    private static boolean isLighting(LivingEntity entity) {
        return entity.isUsingItem()
                && entity.getUsedItemHand() == InteractionHand.MAIN_HAND
                && entity.getUseItem().is(Items.CINDER_FLARE.get())
                && entity.getOffhandItem().is(net.minecraft.world.item.Items.FLINT_AND_STEEL);
    }

    private static AnimationState animationState(
            LivingEntity entity,
            float partialTick
    ) {
        float remaining = entity.getUseItemRemainingTicks() - partialTick;
        float progress = Mth.clamp(
                1.0F - remaining / CinderFlareItem.LIGHTING_DURATION,
                0.0F,
                1.0F
        );

        float flarePresentationPhase = Mth.clamp(progress / 0.16F, 0.0F, 1.0F);
        float flintPresentationPhase = Mth.clamp(
                (progress - 0.06F) / 0.18F,
                0.0F,
                1.0F
        );
        float flareWithdrawalPhase = Mth.clamp(
                (progress - 0.93F) / 0.07F,
                0.0F,
                1.0F
        );
        float flintWithdrawalPhase = Mth.clamp(
                (progress - 0.90F) / 0.10F,
                0.0F,
                1.0F
        );
        float flareHeld = smoother(flarePresentationPhase)
                * (1.0F - smoother(flareWithdrawalPhase));
        float flintHeld = smoother(flintPresentationPhase)
                * (1.0F - smoother(flintWithdrawalPhase));

        float firstContact = (float) CinderFlareItem.FIRST_STRIKE_TICK
                / CinderFlareItem.LIGHTING_DURATION;
        float secondContact = (float) CinderFlareItem.SECOND_STRIKE_TICK
                / CinderFlareItem.LIGHTING_DURATION;
        float firstStrike = impactStroke(progress, 0.28F, firstContact, 0.58F);
        float reset = stroke(progress, 0.58F, 0.63F, 0.68F);
        float secondStrike = impactStroke(progress, 0.66F, secondContact, 1.0F);
        float swingArc = swingArc(progress, 0.28F, firstContact, 0.58F) * 0.70F
                + swingArc(progress, 0.66F, secondContact, 1.0F);
        float response = stroke(progress, firstContact, 0.52F, 0.63F) * 0.35F
                + stroke(progress, secondContact, 0.95F, 1.0F);

        float ignition = smoother(
                Mth.clamp((progress - 0.88F) / 0.10F, 0.0F, 1.0F)
        ) * (1.0F - smoother(flareWithdrawalPhase));

        return new AnimationState(
                flareHeld,
                flintHeld,
                bell(flarePresentationPhase),
                bell(flintPresentationPhase),
                bell(flareWithdrawalPhase),
                bell(flintWithdrawalPhase),
                firstStrike,
                reset,
                secondStrike,
                swingArc,
                response,
                ignition
        );
    }

    private static ThrowState throwState(float progress) {
        float back = stroke(progress, 0.00F, 0.12F, 0.40F);
        float forward = stroke(progress, 0.14F, 0.52F, 0.96F);
        return new ThrowState(back, forward);
    }

    private static float throwProgress(LivingEntity entity, float partialTick) {
        float remaining = entity.getUseItemRemainingTicks() - partialTick;
        return Mth.clamp(
                1.0F - remaining / LitCinderFlareItem.THROW_ANIMATION_DURATION,
                0.0F,
                1.0F
        );
    }

    private static float armSide(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
    }

    private static float stroke(
            float progress,
            float start,
            float contact,
            float end
    ) {
        if (progress <= start || progress >= end) {
            return 0.0F;
        }

        if (progress < contact) {
            return smooth((progress - start) / (contact - start));
        }

        return 1.0F - smooth((progress - contact) / (end - contact));
    }

    private static float impactStroke(
            float progress,
            float start,
            float contact,
            float end
    ) {
        if (progress <= start || progress >= end) {
            return 0.0F;
        }

        if (progress < contact) {
            float value = (progress - start) / (contact - start);
            return value * value * (2.0F - value);
        }

        float value = (progress - contact) / (end - contact);
        float inverse = 1.0F - value;
        return inverse * inverse * (1.0F + value);
    }

    private static float swingArc(
            float progress,
            float start,
            float contact,
            float end
    ) {
        if (progress <= start || progress >= end) {
            return 0.0F;
        }

        if (progress < contact) {
            return bell((progress - start) / (contact - start));
        }

        return -0.55F * bell((progress - contact) / (end - contact));
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static float smoother(float value) {
        return value * value * value
                * (value * (value * 6.0F - 15.0F) + 10.0F);
    }

    private static float bell(float value) {
        float curve = value * (1.0F - value);
        return 16.0F * curve * curve;
    }

    private record AnimationState(
            float flareHeld,
            float flintHeld,
            float flarePresentationArc,
            float flintPresentationArc,
            float flareWithdrawalArc,
            float flintWithdrawalArc,
            float firstStrike,
            float reset,
            float secondStrike,
            float swingArc,
            float response,
            float ignition
    ) {
    }

    private record ThrowState(
            float back,
            float forward
    ) {
    }
}
