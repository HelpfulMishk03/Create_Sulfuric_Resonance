package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class LivingEmberLampItem extends BlockItem {

    private static final String POSITION_TAG = "LinkedFurnacePos";
    private static final String DIMENSION_TAG = "LinkedFurnaceDimension";
    private static final String IDENTITY_TAG = "LinkedFurnaceIdentity";

    public LivingEmberLampItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        if (!(level.getBlockEntity(clickedPos)
                instanceof MoltenRotorBlockEntity furnace)) {
            return super.useOn(context);
        }

        if (!level.isClientSide) {
            setLink(
                    context.getItemInHand(),
                    clickedPos,
                    level.dimension().location().toString(),
                    furnace.getFurnaceIdentity()
            );

            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable(
                                "message.sulfuricresonance.living_ember_lamp.linked",
                                clickedPos.getX(),
                                clickedPos.getY(),
                                clickedPos.getZ()
                        ),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull Item.TooltipContext context,
            @NotNull List<Component> tooltip,
            @NotNull TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);

        Optional<LampLink> link = getLink(stack);
        if (link.isEmpty()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance.living_ember_lamp.unlinked"
                    ).withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        BlockPos position = link.get().position();
        tooltip.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.living_ember_lamp.linked",
                        position.getX(),
                        position.getY(),
                        position.getZ()
                ).withStyle(ChatFormatting.AQUA)
        );
        tooltip.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.living_ember_lamp.dimension",
                        link.get().dimension()
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    public static void setLink(
            ItemStack stack,
            BlockPos position,
            String dimension,
            UUID furnaceIdentity
    ) {
        CompoundTag tag = stack.getOrDefault(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY
        ).copyTag();

        tag.putLong(POSITION_TAG, position.asLong());
        tag.putString(DIMENSION_TAG, dimension);
        tag.putUUID(IDENTITY_TAG, furnaceIdentity);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static Optional<LampLink> getLink(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return Optional.empty();
        }

        CompoundTag tag = data.copyTag();
        if (!tag.contains(POSITION_TAG)
                || !tag.contains(DIMENSION_TAG)
                || !tag.hasUUID(IDENTITY_TAG)) {
            return Optional.empty();
        }

        return Optional.of(
                new LampLink(
                        BlockPos.of(tag.getLong(POSITION_TAG)),
                        tag.getString(DIMENSION_TAG),
                        tag.getUUID(IDENTITY_TAG)
                )
        );
    }

    public record LampLink(
            BlockPos position,
            String dimension,
            UUID furnaceIdentity
    ) {
    }
}
