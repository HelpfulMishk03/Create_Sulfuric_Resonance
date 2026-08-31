package io.hxneyw.repo.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.items.CinderFlareItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class CinderFlareAnimationHandler {
    private CinderFlareAnimationHandler() {
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        if (isLighting(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !isLighting(player)) {
            return;
        }

        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        AnimationState state = animationState(player, event.getPartialTick());
        event.getPoseStack().pushPose();
        renderFirstPersonArm(
                event,
                player,
                arm,
                state
        );
        event.getPoseStack().popPose();
    }

    private static void renderFirstPersonArm(
            RenderHandEvent event,
            LocalPlayer player,
            HumanoidArm arm,
            AnimationState state
    ) {
        float armSide = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float mainSide = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float strike = state.firstStrike() * 0.78F + state.secondStrike();
        boolean flareArm = arm == player.getMainArm();
        float targetX = flareArm
                ? -0.06F * mainSide
                : 0.24F * mainSide + 0.24F * mainSide * strike
                        - 0.10F * mainSide * state.reset();
        float targetY = flareArm
                ? -0.18F + 0.012F * strike
                : -0.34F + 0.24F * strike - 0.10F * state.reset();
        float targetZ = flareArm
                ? -0.70F - 0.012F * strike
                : -0.69F - 0.025F * strike + 0.018F * state.reset();
        float idleY = -0.60F - 0.60F * event.getEquipProgress();
        PoseStack poseStack = event.getPoseStack();

        poseStack.translate(
                Mth.lerp(state.held(), 0.64F * armSide, targetX),
                Mth.lerp(state.held(), idleY, targetY),
                Mth.lerp(state.held(), -0.72F, targetZ)
        );
        float armScale = 1.0F - 0.32F * state.held();
        poseStack.scale(armScale, armScale, armScale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                flareArm
                        ? -8.0F * mainSide * state.held()
                        : -32.0F * mainSide * strike + 14.0F * mainSide * state.reset()
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * armSide));
        poseStack.translate(-1.0F * armSide, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(120.0F * armSide));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-135.0F * armSide));
        poseStack.translate(5.6F * armSide, 0.0F, 0.0F);

        PlayerRenderer renderer = (PlayerRenderer) Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .getRenderer(player);
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    player
            );
        } else {
            renderer.renderLeftHand(
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    player
            );
        }
    }

    public static boolean applyFirstPersonItemTransform(
            PoseStack poseStack,
            LocalPlayer player,
            HumanoidArm arm,
            ItemStack itemInHand,
            float partialTick,
            float equipProcess
    ) {
        if (!isLighting(player)) {
            return false;
        }

        boolean flare = itemInHand.is(Items.CINDER_FLARE.get())
                && arm == player.getMainArm();
        boolean flint = itemInHand.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)
                && arm != player.getMainArm();
        if (!flare && !flint) {
            return false;
        }

        AnimationState state = animationState(player, partialTick);
        if (flare) {
            transformFlare(poseStack, player, state, equipProcess);
        } else {
            transformFlintAndSteel(poseStack, player, state, equipProcess);
        }
        return true;
    }

    private static void transformFlare(
            PoseStack poseStack,
            LocalPlayer player,
            AnimationState state,
            float equipProcess
    ) {
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float strikeReaction = state.firstStrike() * 0.34F + state.secondStrike();
        float idleY = -0.52F - 0.60F * equipProcess;

        poseStack.translate(
                Mth.lerp(state.held(), 0.56F * side, -0.06F * side)
                        + 0.010F * side * strikeReaction,
                Mth.lerp(state.held(), idleY, -0.18F)
                        + 0.012F * strikeReaction
                        - 0.010F * state.ignition(),
                Mth.lerp(state.held(), -0.72F, -0.70F)
                        - 0.012F * strikeReaction
        );
        float scale = 1.0F - 0.28F * state.held();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(
                -12.0F * state.held()
                        + 2.0F * strikeReaction
                        - 3.0F * state.ignition()
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                6.0F * side * state.held() - 1.5F * side * strikeReaction
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                -22.0F * side * state.held()
                        + 2.0F * side * strikeReaction
                        + 3.0F * side * state.ignition()
        ));
    }

    private static void transformFlintAndSteel(
            PoseStack poseStack,
            LocalPlayer player,
            AnimationState state,
            float equipProcess
    ) {
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float strikeTravel = state.firstStrike() * 0.78F + state.secondStrike();
        float idleY = -0.52F - 0.60F * equipProcess;
        float targetX = 0.24F * side + 0.24F * side * strikeTravel
                - 0.10F * side * state.reset();
        float targetY = -0.34F + 0.24F * strikeTravel
                - 0.10F * state.reset();

        poseStack.translate(
                Mth.lerp(state.held(), -0.56F * side, targetX),
                Mth.lerp(state.held(), idleY, targetY),
                Mth.lerp(state.held(), -0.72F, -0.69F)
                        - 0.025F * strikeTravel
                        + 0.018F * state.reset()
                        + 0.018F * state.ignition()
        );
        float scale = 1.0F - 0.28F * state.held();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(
                -10.0F * state.held()
                        + 16.0F * strikeTravel
                        - 8.0F * state.reset()
                        - 3.0F * state.ignition()
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                7.0F * side * state.held()
                        - 12.0F * side * strikeTravel
                        + 6.0F * side * state.reset()
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                20.0F * side * state.held()
                        - 65.0F * side * strikeTravel
                        + 25.0F * side * state.reset()
                        - 3.0F * side * state.ignition()
        ));
    }

    public static void applyThirdPersonPose(
            ModelPart rightArm,
            ModelPart leftArm,
            LivingEntity entity
    ) {
        if (!isLighting(entity)) {
            return;
        }

        float progress = Mth.clamp(
                1.0F - (float) entity.getUseItemRemainingTicks() / CinderFlareItem.LIGHTING_DURATION,
                0.0F,
                1.0F
        );
        float presentation = smooth(Mth.clamp(progress / 0.18F, 0.0F, 1.0F));
        float withdrawal = smooth(Mth.clamp((progress - 0.80F) / 0.20F, 0.0F, 1.0F));
        float held = presentation * (1.0F - withdrawal);
        float firstStrike = stroke(progress, 0.29F, 0.38F, 0.46F);
        float reset = stroke(progress, 0.44F, 0.50F, 0.56F);
        float secondStrike = stroke(progress, 0.55F, 0.66F, 0.74F);
        float strike = firstStrike * 0.72F + secondStrike;
        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        float side = rightHanded ? 1.0F : -1.0F;
        ModelPart flareArm = rightHanded ? rightArm : leftArm;
        ModelPart flintArm = rightHanded ? leftArm : rightArm;

        flareArm.xRot = Mth.lerp(held, flareArm.xRot, -1.10F + 0.035F * strike);
        flareArm.yRot = Mth.lerp(held, flareArm.yRot, -0.42F * side + 0.025F * side * strike);
        flareArm.zRot = Mth.lerp(held, flareArm.zRot, 0.10F * side - 0.025F * side * strike);

        flintArm.xRot = Mth.lerp(
                held,
                flintArm.xRot,
                -1.28F + 0.30F * strike - 0.14F * reset
        );
        flintArm.yRot = Mth.lerp(
                held,
                flintArm.yRot,
                0.58F * side - 0.38F * side * strike + 0.18F * side * reset
        );
        flintArm.zRot = Mth.lerp(
                held,
                flintArm.zRot,
                -0.22F * side + 0.18F * side * strike - 0.08F * side * reset
        );
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
        float presentation = smooth(Mth.clamp(progress / 0.18F, 0.0F, 1.0F));
        float withdrawal = smooth(Mth.clamp((progress - 0.80F) / 0.20F, 0.0F, 1.0F));
        float held = presentation * (1.0F - withdrawal);
        float firstStrike = stroke(progress, 0.29F, 0.38F, 0.46F);
        float reset = stroke(progress, 0.44F, 0.50F, 0.56F);
        float secondStrike = stroke(progress, 0.55F, 0.66F, 0.74F);
        float ignition = smooth(Mth.clamp((progress - 0.67F) / 0.09F, 0.0F, 1.0F))
                * (1.0F - withdrawal);
        return new AnimationState(
                held,
                firstStrike,
                reset,
                secondStrike,
                ignition
        );
    }

    private static float stroke(float progress, float start, float contact, float end) {
        if (progress <= start || progress >= end) {
            return 0.0F;
        }

        if (progress < contact) {
            return smooth((progress - start) / (contact - start));
        }

        return 1.0F - smooth((progress - contact) / (end - contact));
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private record AnimationState(
            float held,
            float firstStrike,
            float reset,
            float secondStrike,
            float ignition
    ) {
    }
}
