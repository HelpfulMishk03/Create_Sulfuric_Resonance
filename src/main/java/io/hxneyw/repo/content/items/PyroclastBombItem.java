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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PyroclastBombItem extends Item {
   private static final int COOLDOWN_TICKS = 10;

   public PyroclastBombItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(
      @NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag
   ) {
      tooltipComponents.add(Component.literal("Try pairing this with a potato cannon!").withStyle(ChatFormatting.GRAY));
      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      level.playSound(
         null,
         player.getX(),
         player.getY(),
         player.getZ(),
         SoundEvents.FIRECHARGE_USE,
         SoundSource.NEUTRAL,
         0.5F,
         0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
      );
      if (!level.isClientSide) {
         PyroclastBombEntity bomb = new PyroclastBombEntity(level, player);
         bomb.setItem(itemStack);
         bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.0F, 1.2F);
         level.addFreshEntity(bomb);
      }

      player.awardStat(Stats.ITEM_USED.get(this));
      player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
      if (!player.getAbilities().instabuild) {
         itemStack.shrink(1);
      }

      return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
   }
}
