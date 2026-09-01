package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.entities.SulfuricAcidFlaskEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SulfuricAcidFlaskItem extends Item {
    private static final int COOLDOWN_TICKS = 10;
    private static final float THROW_VELOCITY = 0.8F;
    private static final float THROW_INACCURACY = 0.6F;

    public SulfuricAcidFlaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(Component.translatable(
                "tooltip.sulfuricresonance.sulfuric_acid_flask.throw"
        ).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(
                "tooltip.sulfuricresonance.sulfuric_acid_flask.effect"
        ).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable(
                "tooltip.sulfuricresonance.reactive_tools.dispenser"
        ).withStyle(ChatFormatting.DARK_AQUA));
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
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SPLASH_POTION_THROW,
                SoundSource.PLAYERS,
                0.5F,
                0.9F + level.getRandom().nextFloat() * 0.2F
        );

        if (!level.isClientSide) {
            SulfuricAcidFlaskEntity flask = new SulfuricAcidFlaskEntity(level, player);
            flask.setItem(stack.copyWithCount(1));
            flask.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    THROW_VELOCITY,
                    THROW_INACCURACY
            );
            level.addFreshEntity(flask);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
