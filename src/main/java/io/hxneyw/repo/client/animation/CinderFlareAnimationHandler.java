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

        float presentation = smooth(Mth.clamp(progress / 0.18F, 0.0F, 1.0F));
        float withdrawal = smooth(Mth.clamp((progress - 0.80F) / 0.20F, 0.0F, 1.0F));
        float held = presentation * (1.0F - withdrawal);
        float firstStrike = stroke(progress, 0.29F, 0.38F, 0.46F);
        float reset = stroke(progress, 0.44F, 0.50F, 0.56F);
        float secondStrike = stroke(progress, 0.55F, 0.66F, 0.74F);
        float ignition = smooth(Mth.clamp((progress - 0.67F) / 0.09F, 0.0F, 1.0F))
                * (1.0F - withdrawal);

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
                    reset,
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
        float strikeReaction = firstStrike * 0.34F + secondStrike;

        poseStack.translate(
                -0.72F * side * held + 0.014F * side * strikeReaction,
                0.075F * held - 0.012F * strikeReaction + 0.025F * ignition,
                0.10F * held - 0.018F * strikeReaction
        );
        float scale = 1.0F - 0.22F * held;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(
                -12.0F * held + 3.0F * strikeReaction - 3.0F * ignition
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                7.0F * side * held - 2.0F * side * strikeReaction
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                -28.0F * side * held + 3.0F * side * strikeReaction + 3.0F * side * ignition
        ));
    }

    private static void transformFlintAndSteel(
            PoseStack poseStack,
            LocalPlayer player,
            float held,
            float firstStrike,
            float reset,
            float secondStrike,
            float ignition
    ) {
        float towardCenter = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float strikeTravel = firstStrike * 0.78F + secondStrike;

        poseStack.translate(
                0.72F * towardCenter * held
                        + 0.075F * towardCenter * strikeTravel
                        - 0.11F * towardCenter * reset,
                0.055F * held
                        - 0.032F * strikeTravel
                        + 0.03F * reset
                        + 0.016F * ignition,
                0.12F * held
                        - 0.035F * strikeTravel
                        + 0.025F * reset
                        + 0.025F * ignition
        );
        float scale = 1.0F - 0.28F * held;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(
                -12.0F * held
                        + 13.0F * strikeTravel
                        - 6.0F * reset
                        - 3.0F * ignition
        ));
        poseStack.mulPose(Axis.YP.rotationDegrees(
                8.0F * towardCenter * held
                        - 5.0F * towardCenter * strikeTravel
                        + 3.0F * towardCenter * reset
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                24.0F * towardCenter * held
                        - 17.0F * towardCenter * strikeTravel
                        + 9.0F * towardCenter * reset
                        - 3.0F * towardCenter * ignition
        ));
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
}
