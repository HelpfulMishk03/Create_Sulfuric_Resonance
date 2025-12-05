package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.entities.PyroclastBombEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PyroclastBombItem extends Item {
    private static final int COOLDOWN_TICKS = 10; // 10 ticks = 0.5 seconds (20 ticks = 1 second)

    public PyroclastBombItem(Properties properties) {
        super(properties);
    }

    // Called when player RIGHT-CLICKS with the item
    // Instantly throws the bomb - no charging required!
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Play throw sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        // SERVER-SIDE: Spawn and throw the bomb immediately
        if (!level.isClientSide) {
            // Create the bomb entity
            PyroclastBombEntity bomb = new PyroclastBombEntity(level, player);
            bomb.setItem(itemStack);

            // Throw with fixed power - fast like a snowball
            bomb.shootFromRotation(
                    player,              // Who's throwing it
                    player.getXRot(),    // Vertical aim (pitch)
                    player.getYRot(),    // Horizontal aim (yaw)
                    0.0F,                // Roll (always 0 for normal throws)
                    1.4F,                // Throw velocity - faster than before!
                    1.0F                 // Inaccuracy (1.0 = slight randomness)
            );

            level.addFreshEntity(bomb); // Add entity to the world
        }

        player.awardStat(Stats.ITEM_USED.get(this)); // Track statistics

        // Apply cooldown (10 ticks = 0.5 seconds)
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        // Consume one bomb (unless creative mode)
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}