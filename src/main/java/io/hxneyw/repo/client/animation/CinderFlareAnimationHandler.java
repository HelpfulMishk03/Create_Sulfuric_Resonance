package io.hxneyw.repo.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.items.CinderFlareItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class CinderFlareAnimationHandler {
    private CinderFlareAnimationHandler() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isUsingItem()) {
            return;
        }

        if (player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!player.getUseItem().is(Items.CINDER_FLARE.get())) {
            return;
        }

        if (!player.getOffhandItem().is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) {
            return;
        }

        float remaining = player.getUseItemRemainingTicks() - event.getPartialTick();
        float progress = Mth.clamp(
                1.0F - remaining / CinderFlareItem.LIGHTING_DURATION,
                0.0F,
                1.0F
        );

        float setup = smooth(Mth.clamp(progress / 0.26F, 0.0F, 1.0F));
        float settle = smooth(Mth.clamp((progress - 0.74F) / 0.26F, 0.0F, 1.0F));
        float held = setup * (1.0F - settle);
        float firstStrike = pulse(progress, 0.30F, 0.45F);
        float recoil = pulse(progress, 0.47F, 0.56F);
        float secondStrike = pulse(progress, 0.58F, 0.70F);
        float ignition = pulse(progress, 0.68F, 0.78F);

        if (event.getHand() == InteractionHand.MAIN_HAND) {
            transformFlare(
                    event.getPoseStack(),
                    player,
                    held,
                    firstStrike,
                    secondStrike,
                    ignition
            );
        } else {
            transformFlintAndSteel(
                    event.getPoseStack(),
                    player,
                    held,
                    firstStrike,
                    recoil,
                    secondStrike,
                    ignition
            );
        }
    }

    private static void transformFlare(
            PoseStack poseStack,
            LocalPlayer player,
            float held,
            float firstStrike,
            float secondStrike,
            float ignition
    ) {
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float strikeReaction = firstStrike * 0.55F + secondStrike;

        poseStack.translate(
                -0.26F * side * held + 0.024F * side * strikeReaction,
                0.11F * held - 0.014F * strikeReaction + 0.012F * ignition,
                -0.14F * held + 0.014F * strikeReaction
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(-24.0F * held + 4.0F * strikeReaction - 3.0F * ignition));
        poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * side * held - 3.0F * side * strikeReaction));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-31.0F * side * held + 5.0F * side * strikeReaction));
    }

    private static void transformFlintAndSteel(
            PoseStack poseStack,
            LocalPlayer player,
            float held,
            float firstStrike,
            float recoil,
            float secondStrike,
            float ignition
    ) {
        float towardCenter = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float strikeTravel = firstStrike * 0.78F + secondStrike;

        poseStack.translate(
                0.27F * towardCenter * held
                        + 0.18F * towardCenter * strikeTravel
                        - 0.065F * towardCenter * recoil,
                0.10F * held
                        - 0.065F * strikeTravel
                        + 0.025F * recoil
                        + 0.018F * ignition,
                -0.16F * held
                        - 0.095F * strikeTravel
                        + 0.035F * recoil
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(
                -24.0F * held
                        + 36.0F * strikeTravel
                        - 10.0F * recoil
                        - 5.0F * ignition
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                18.0F * towardCenter * held
                        - 12.0F * towardCenter * strikeTravel
                        + 4.0F * towardCenter * recoil
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                -22.0F * towardCenter * held
                        + 44.0F * towardCenter * strikeTravel
                        - 14.0F * towardCenter * recoil
        ));
    }

    private static float pulse(float progress, float start, float end) {
        if (progress <= start || progress >= end) {
            return 0.0F;
        }

        float local = (progress - start) / (end - start);
        return Mth.sin(local * (float) Math.PI);
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}
