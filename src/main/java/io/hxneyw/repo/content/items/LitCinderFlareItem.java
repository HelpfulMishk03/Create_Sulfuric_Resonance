package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.entities.CinderFlareEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class LitCinderFlareItem extends Item {
    public static final int THROW_ANIMATION_DURATION = 12;
    private static final int THROW_RELEASE_TICK = 3;
    private static final int COOLDOWN_TICKS = 8;
    private static final String THROW_RELEASED_TAG =
            "sulfuricresonance.cinder_flare_throw_released";

    private static final float THROW_VELOCITY = 0.46F;
    private static final float THROW_INACCURACY = 0.25F;
    private static final float THROW_LOFT_DEGREES = 28.0F;

    private static final double THROW_FORWARD_OFFSET = 0.18D;
    private static final double THROW_SIDE_OFFSET = 0.38D;
    private static final double THROW_HEIGHT_OFFSET = 0.84D;

    public LitCinderFlareItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(
                Component.translatable("tooltip.sulfuricresonance.cinder_flare.lit")
                        .withStyle(ChatFormatting.GRAY)
        );
        tooltipComponents.add(
                Component.translatable("tooltip.sulfuricresonance.reactive_tools.dispenser")
                        .withStyle(ChatFormatting.DARK_AQUA)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND
                || player.getCooldowns().isOnCooldown(this)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        player.getPersistentData().remove(THROW_RELEASED_TAG);
        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }

    @Override
    public void onUseTick(
            @NotNull Level level,
            @NotNull LivingEntity livingEntity,
            @NotNull ItemStack stack,
            int remainingUseDuration
    ) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }

        int elapsed = THROW_ANIMATION_DURATION - remainingUseDuration;
        if (elapsed < THROW_RELEASE_TICK
                || player.getPersistentData().getBoolean(THROW_RELEASED_TAG)) {
            return;
        }

        releaseFlare(level, player);
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull LivingEntity livingEntity
    ) {
        if (!(livingEntity instanceof Player player) || level.isClientSide) {
            return stack;
        }

        if (!player.getPersistentData().getBoolean(THROW_RELEASED_TAG)) {
            releaseFlare(level, player);
        }

        consumeReleasedFlare(stack, player);
        player.getPersistentData().remove(THROW_RELEASED_TAG);
        return stack;
    }

    @Override
    public void releaseUsing(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull LivingEntity livingEntity,
            int timeLeft
    ) {
        if (!(livingEntity instanceof Player player) || level.isClientSide) {
            return;
        }

        if (player.getPersistentData().getBoolean(THROW_RELEASED_TAG)) {
            consumeReleasedFlare(stack, player);
        }
        player.getPersistentData().remove(THROW_RELEASED_TAG);
    }

    @Override
    @NotNull
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return THROW_ANIMATION_DURATION;
    }

    private void releaseFlare(Level level, Player player) {
        player.getPersistentData().putBoolean(THROW_RELEASED_TAG, true);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS,
                0.45F,
                1.25F + level.random.nextFloat() * 0.1F
        );

        CinderFlareEntity flare = new CinderFlareEntity(level, player);
        flare.setItem(
                new ItemStack(io.hxneyw.repo.content.Items.LIT_CINDER_FLARE.get())
        );

        double yawRadians = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yawRadians) * THROW_FORWARD_OFFSET;
        double forwardZ = Math.cos(yawRadians) * THROW_FORWARD_OFFSET;
        double rightX = -Math.cos(yawRadians) * THROW_SIDE_OFFSET;
        double rightZ = -Math.sin(yawRadians) * THROW_SIDE_OFFSET;
        double handSide = player.getMainArm() == HumanoidArm.RIGHT ? 1.0D : -1.0D;

        flare.setPos(
                player.getX() + forwardX + rightX * handSide,
                player.getEyeY() - THROW_HEIGHT_OFFSET,
                player.getZ() + forwardZ + rightZ * handSide
        );

        flare.shootFromRotation(
                player,
                player.getXRot() - THROW_LOFT_DEGREES,
                player.getYRot(),
                0.0F,
                THROW_VELOCITY,
                THROW_INACCURACY
        );

        level.addFreshEntity(flare);

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }

    private static void consumeReleasedFlare(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
