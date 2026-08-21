package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

public class ThermalRelaySwitchItem extends BlockItem {

    private static final String NETWORK_TAG = "RelayNetwork";
    private static final String LINKED_FURNACES_TAG = "LinkedFurnaces";

    private static final String POSITION_TAG = "Position";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String IDENTITY_TAG = "Identity";

    private static final String LEGACY_POSITION_TAG =
            "LinkedFurnacePos";
    private static final String LEGACY_DIMENSION_TAG =
            "LinkedFurnaceDimension";
    private static final String LEGACY_IDENTITY_TAG =
            "LinkedFurnaceIdentity";

    public ThermalRelaySwitchItem(
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

        if (!(level.getBlockEntity(clickedPos)
                instanceof MoltenRotorBlockEntity furnace)) {
            return super.useOn(context);
        }

        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(
                    level.isClientSide
            );
        }

        FurnaceLink clickedFurnace = new FurnaceLink(
                clickedPos.immutable(),
                level.dimension().location().toString(),
                furnace.getFurnaceIdentity()
        );

        if (!level.isClientSide) {
            boolean startsNewNetwork = furnace.getThermalNetworkId() == null;
            UUID networkId = furnace.getOrCreateThermalNetworkId();

            setConnection(stack, networkId, clickedFurnace);
            markInventoryChanged(player);

            if (startsNewNetwork) {
                ThermalNetworkMessages.showStarted(player);
            }
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide
        );
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()
                || !hasConnections(stack)) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            clearConnections(stack);
            markInventoryChanged(player);

            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance."
                                    + "thermal_relay_switch."
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
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasConnections(stack)
                || super.isFoil(stack);
    }

    public static boolean hasConnections(
            ItemStack stack
    ) {
        return getLinkedFurnace(stack) != null;
    }

    @Nullable
    public static UUID getNetworkId(
            ItemStack stack
    ) {
        CompoundTag tag = getCustomTag(stack);

        return tag.hasUUID(NETWORK_TAG)
                ? tag.getUUID(NETWORK_TAG)
                : null;
    }

    @Nullable
    public static FurnaceLink getLinkedFurnace(
            ItemStack stack
    ) {
        return readStoredLink(getCustomTag(stack));
    }

    public static void setLinkedFurnace(
            ItemStack stack,
            FurnaceLink newLink
    ) {
        CustomData.update(
                DataComponents.CUSTOM_DATA,
                stack,
                tag -> {
                    FurnaceLink existing =
                            readStoredLink(tag);

                    UUID networkId =
                            existing != null
                                    && existing.equals(newLink)
                                    && tag.hasUUID(NETWORK_TAG)
                                    ? tag.getUUID(NETWORK_TAG)
                                    : UUID.randomUUID();

                    writeLink(
                            tag,
                            networkId,
                            newLink
                    );
                }
        );

        stack.set(
                DataComponents.ENCHANTMENT_GLINT_OVERRIDE,
                true
        );
    }

    public static void setConnection(
            ItemStack stack,
            UUID networkId,
            FurnaceLink link
    ) {
        CompoundTag tag = getCustomTag(stack);

        writeLink(
                tag,
                networkId,
                link
        );

        CustomData.set(
                DataComponents.CUSTOM_DATA,
                stack,
                tag
        );

        stack.set(
                DataComponents.ENCHANTMENT_GLINT_OVERRIDE,
                true
        );
    }

    public static void clearConnections(
            ItemStack stack
    ) {
        CompoundTag tag = getCustomTag(stack);

        tag.remove(NETWORK_TAG);
        tag.remove(LINKED_FURNACES_TAG);
        removeLegacyKeys(tag);

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

    @Nullable
    private static FurnaceLink readStoredLink(
            CompoundTag tag
    ) {
        if (tag.contains(
                LINKED_FURNACES_TAG,
                Tag.TAG_LIST
        )) {
            ListTag storedLinks = tag.getList(
                    LINKED_FURNACES_TAG,
                    Tag.TAG_COMPOUND
            );

            for (int index = 0;
                 index < storedLinks.size();
                 index++) {
                FurnaceLink link = readLink(
                        storedLinks.getCompound(index)
                );

                if (link != null) {
                    return link;
                }
            }
        }

        if (tag.contains(LEGACY_POSITION_TAG)
                && tag.contains(LEGACY_DIMENSION_TAG)
                && tag.hasUUID(LEGACY_IDENTITY_TAG)) {
            return new FurnaceLink(
                    BlockPos.of(
                            tag.getLong(LEGACY_POSITION_TAG)
                    ),
                    tag.getString(LEGACY_DIMENSION_TAG),
                    tag.getUUID(LEGACY_IDENTITY_TAG)
            );
        }

        return null;
    }

    @Nullable
    private static FurnaceLink readLink(
            CompoundTag linkTag
    ) {
        if (!linkTag.contains(POSITION_TAG)
                || !linkTag.contains(DIMENSION_TAG)
                || !linkTag.hasUUID(IDENTITY_TAG)) {
            return null;
        }

        return new FurnaceLink(
                BlockPos.of(
                        linkTag.getLong(POSITION_TAG)
                ),
                linkTag.getString(DIMENSION_TAG),
                linkTag.getUUID(IDENTITY_TAG)
        );
    }

    private static void writeLink(
            CompoundTag tag,
            UUID networkId,
            FurnaceLink link
    ) {
        CompoundTag linkTag = new CompoundTag();

        linkTag.putLong(
                POSITION_TAG,
                link.position().asLong()
        );
        linkTag.putString(
                DIMENSION_TAG,
                link.dimension()
        );
        linkTag.putUUID(
                IDENTITY_TAG,
                link.furnaceIdentity()
        );

        ListTag storedLinks = new ListTag();
        storedLinks.add(linkTag);

        tag.putUUID(
                NETWORK_TAG,
                networkId
        );
        tag.put(
                LINKED_FURNACES_TAG,
                storedLinks
        );

        removeLegacyKeys(tag);
    }

    private static void removeLegacyKeys(
            CompoundTag tag
    ) {
        tag.remove(LEGACY_POSITION_TAG);
        tag.remove(LEGACY_DIMENSION_TAG);
        tag.remove(LEGACY_IDENTITY_TAG);
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
