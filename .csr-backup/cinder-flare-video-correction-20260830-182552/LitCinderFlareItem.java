package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.entities.CinderFlareEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class LitCinderFlareItem extends Item {
    private static final int COOLDOWN_TICKS = 8;
    private static final float THROW_VELOCITY = 0.50F;
    private static final float THROW_INACCURACY = 0.35F;
    private static final float THROW_LOFT_DEGREES = 22.0F;
    private static final double THROW_FORWARD_OFFSET = 0.24D;
    private static final double THROW_SIDE_OFFSET = 0.18D;
    private static final double THROW_HEIGHT_OFFSET = 0.48D;

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

        // Force a short swing state so the client-side custom underhand pose begins
        // immediately when the flare is thrown.
        player.swing(hand, true);

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

        if (!level.isClientSide) {
            CinderFlareEntity flare = new CinderFlareEntity(level, player);
            flare.setItem(new ItemStack(io.hxneyw.repo.content.Items.LIT_CINDER_FLARE.get()));

            double yawRadians = Math.toRadians(player.getYRot());
            double forwardX = -Math.sin(yawRadians) * THROW_FORWARD_OFFSET;
            double forwardZ = Math.cos(yawRadians) * THROW_FORWARD_OFFSET;
            double rightX = -Math.cos(yawRadians) * THROW_SIDE_OFFSET;
            double rightZ = -Math.sin(yawRadians) * THROW_SIDE_OFFSET;
            double handSide = player.getMainArm() == HumanoidArm.RIGHT ? 1.0D : -1.0D;

            // Spawn from roughly hand/chest height instead of the eye/crosshair.
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
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}