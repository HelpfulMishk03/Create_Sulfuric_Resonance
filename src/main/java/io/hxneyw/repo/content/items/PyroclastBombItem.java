package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.entities.PyroclastBombEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PyroclastBombItem extends Item {
   private static final int COOLDOWN_TICKS = 10;
   private static final int THROW_DURATION_TICKS = 9;
   private static final float THROW_VELOCITY = 0.78F;
   private static final float THROW_INACCURACY = 1.4F;
   private static final float THROW_ARC_DEGREES = -8.0F;

   public PyroclastBombItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag
   ) {
      tooltipComponents.add(Component.translatable("tooltip.sulfuricresonance.pyroclast_bomb.demolition").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.translatable("tooltip.sulfuricresonance.pyroclast_bomb.combat").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.translatable("tooltip.sulfuricresonance.pyroclast_bomb.ignition").withStyle(ChatFormatting.GRAY));
      tooltipComponents.add(Component.translatable("tooltip.sulfuricresonance.pyroclast_bomb.water").withStyle(ChatFormatting.DARK_AQUA));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(itemStack);
   }

   @Override
   @NotNull
   public ItemStack finishUsingItem(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
      if (!(livingEntity instanceof Player player)) {
         return itemStack;
      }

      if (!level.isClientSide) {
         level.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.TRIDENT_THROW,
            SoundSource.PLAYERS,
            0.55F,
            0.72F + level.getRandom().nextFloat() * 0.08F
         );
         PyroclastBombEntity bomb = new PyroclastBombEntity(level, player);
         bomb.setItem(itemStack);
         bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), THROW_ARC_DEGREES, THROW_VELOCITY, THROW_INACCURACY);
         level.addFreshEntity(bomb);
      }

      player.awardStat(Stats.ITEM_USED.get(this));
      player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
      if (!player.getAbilities().instabuild) {
         itemStack.shrink(1);
      }

      return itemStack;
   }

   @Override
   @NotNull
   public UseAnim getUseAnimation(@NotNull ItemStack stack) {
      return UseAnim.SPEAR;
   }

   @Override
   public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
      return THROW_DURATION_TICKS;
   }
}
