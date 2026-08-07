package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class LivingEmberLampItem extends BlockItem {

    private static final String LINKED_POSITION_TAG =
            "LinkedFurnacePos";

    private static final String LINKED_DIMENSION_TAG =
            "LinkedFurnaceDimension";

    private static final String LINKED_IDENTITY_TAG =
            "LinkedFurnaceIdentity";

    public LivingEmberLampItem(
            Block block,
            Properties properties
    ) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(
            @NotNull UseOnContext context
    ) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (level.getBlockEntity(clickedPos)
                instanceof MoltenRotorBlockEntity furnace) {


            if (player != null && player.isShiftKeyDown()) {
                return InteractionResult.sidedSuccess(
                        level.isClientSide
                );
            }

            if (!level.isClientSide) {
                setLink(
                        stack,
                        new FurnaceLink(
                                clickedPos.immutable(),
                                level.dimension()
                                        .location()
                                        .toString(),
                                furnace.getFurnaceIdentity()
                        )
                );

                markInventoryChanged(player);
            }

            return InteractionResult.sidedSuccess(
                    level.isClientSide
            );
        }

        return super.useOn(context);
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()
                || getLink(stack).isEmpty()) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            clearLink(stack);
            markInventoryChanged(player);

            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance."
                                    + "living_ember_lamp."
                                    + "network_removed"
                    ),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
        );
    }

    @Override
    public boolean isFoil(
            @NotNull ItemStack stack
    ) {
        return getLink(stack).isPresent()
                || super.isFoil(stack);
    }

    public static Optional<FurnaceLink> getLink(
            ItemStack stack
    ) {
        CompoundTag tag = getCustomTag(stack);

        if (!tag.contains(
                LINKED_POSITION_TAG,
                Tag.TAG_LONG
        ) || !tag.contains(
                LINKED_DIMENSION_TAG,
                Tag.TAG_STRING
        ) || !tag.hasUUID(LINKED_IDENTITY_TAG)) {
            return Optional.empty();
        }

        return Optional.of(
                new FurnaceLink(
                        BlockPos.of(
                                tag.getLong(
                                        LINKED_POSITION_TAG
                                )
                        ),
                        tag.getString(
                                LINKED_DIMENSION_TAG
                        ),
                        tag.getUUID(
                                LINKED_IDENTITY_TAG
                        )
                )
        );
    }

    public static void setLink(
            ItemStack stack,
            FurnaceLink link
    ) {
        CustomData.update(
                DataComponents.CUSTOM_DATA,
                stack,
                tag -> {
                    tag.putLong(
                            LINKED_POSITION_TAG,
                            link.position().asLong()
                    );

                    tag.putString(
                            LINKED_DIMENSION_TAG,
                            link.dimension()
                    );

                    tag.putUUID(
                            LINKED_IDENTITY_TAG,
                            link.furnaceIdentity()
                    );
                }
        );

        stack.set(
                DataComponents.ENCHANTMENT_GLINT_OVERRIDE,
                true
        );
    }

    public static void clearLink(
            ItemStack stack
    ) {
        CompoundTag tag = getCustomTag(stack);

        tag.remove(LINKED_POSITION_TAG);
        tag.remove(LINKED_DIMENSION_TAG);
        tag.remove(LINKED_IDENTITY_TAG);

        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CustomData.set(
                    DataComponents.CUSTOM_DATA,
                    stack,
                    tag
            );
        }

        stack.remove(
                DataComponents.ENCHANTMENT_GLINT_OVERRIDE
        );
    }

    private static CompoundTag getCustomTag(
            ItemStack stack
    ) {
        return stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();
    }

    private static void markInventoryChanged(
            @Nullable Player player
    ) {
        if (player != null) {
            player.getInventory().setChanged();
        }
    }

    public record FurnaceLink(
            BlockPos position,
            String dimension,
            UUID furnaceIdentity
    ) {
    }
}
