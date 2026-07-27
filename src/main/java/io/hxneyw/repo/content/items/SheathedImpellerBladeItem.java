package io.hxneyw.repo.content.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SheathedImpellerBladeItem extends Item {
   public SheathedImpellerBladeItem(Properties properties) {
      super(
         properties.durability(300)
            .component(
               DataComponents.ATTRIBUTE_MODIFIERS,
               ItemAttributeModifiers.builder()
                  .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 6.0, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                  .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.0, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                  .build()
            )
      );
   }

   public boolean supportsEnchantment(@NotNull ItemStack stack, Holder<Enchantment> enchantment) {
      return enchantment.is(Enchantments.SHARPNESS)
         || enchantment.is(Enchantments.KNOCKBACK)
         || enchantment.is(Enchantments.FIRE_ASPECT)
         || enchantment.is(Enchantments.UNBREAKING)
         || enchantment.is(Enchantments.MENDING)
         || super.supportsEnchantment(stack, enchantment);
   }

   public int getEnchantmentValue(@NotNull ItemStack stack) {
      return 15;
   }

   public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
      stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
      return true;
   }

   public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
      return false;
   }

   public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
      return 0.0F;
   }

   public boolean isFoil(@NotNull ItemStack stack) {
      return stack.isEnchanted() || super.isFoil(stack);
   }
}
