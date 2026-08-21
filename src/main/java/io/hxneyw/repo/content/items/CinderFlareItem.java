package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CinderFlareItem extends Item {
    public static final int LIGHTING_DURATION = 32;

    public CinderFlareItem(Properties properties) {
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
                Component.translatable("tooltip.sulfuricresonance.cinder_flare.unlit")
                        .withStyle(ChatFormatting.GRAY)
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

        if (hand != InteractionHand.MAIN_HAND) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        if (!player.getOffhandItem().is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.sulfuricresonance.cinder_flare.requires_flint_and_steel"),
                        true
                );
            }
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        player.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }

    @Override
    @NotNull
    public InteractionResult onItemUseFirst(
            @NotNull ItemStack stack,
            @NotNull UseOnContext context
    ) {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!player.getOffhandItem().is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) {
            return InteractionResult.PASS;
        }

        player.startUsingItem(InteractionHand.MAIN_HAND);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(
            @NotNull Level level,
            @NotNull LivingEntity livingEntity,
            @NotNull ItemStack stack,
            int remainingUseDuration
    ) {
        int elapsed = LIGHTING_DURATION - remainingUseDuration;

        if (!level.isClientSide && (elapsed == 12 || elapsed == 21)) {
            level.playSound(
                    null,
                    livingEntity.getX(),
                    livingEntity.getY(),
                    livingEntity.getZ(),
                    SoundEvents.FLINTANDSTEEL_USE,
                    SoundSource.PLAYERS,
                    0.45F,
                    0.9F + level.random.nextFloat() * 0.15F
            );
        }

        if (!level.isClientSide && elapsed == 21 && level instanceof ServerLevel serverLevel) {
            Vec3 particlePos = lightingParticlePosition(livingEntity);
            serverLevel.sendParticles(
                    ModParticles.COMBUSTION_PURPLE_FLAME.get(),
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0.006D,
                    0.006D,
                    0.006D,
                    0.001D
            );
            level.playSound(
                    null,
                    livingEntity.getX(),
                    livingEntity.getY(),
                    livingEntity.getZ(),
                    SoundEvents.FIRECHARGE_USE,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.25F
            );
        }

    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull LivingEntity livingEntity
    ) {
        if (!(livingEntity instanceof Player player)) {
            return stack;
        }

        ItemStack flintAndSteel = player.getOffhandItem();
        if (!flintAndSteel.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) {
            return stack;
        }

        ItemStack litFlare = new ItemStack(Items.LIT_CINDER_FLARE.get());

        if (!level.isClientSide) {
            ItemStack remainder = stack.copy();

            if (!player.getAbilities().instabuild) {
                flintAndSteel.hurtAndBreak(
                        1,
                        player,
                        LivingEntity.getSlotForHand(InteractionHand.OFF_HAND)
                );
                remainder.shrink(1);
            }

            if (!remainder.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remainder);
            }

            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return litFlare;
    }

    @Override
    @NotNull
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return LIGHTING_DURATION;
    }

    private static Vec3 lightingParticlePosition(LivingEntity entity) {
        return entity.getEyePosition()
                .add(entity.getLookAngle().scale(0.58D))
                .add(0.0D, -0.28D, 0.0D);
    }
}
