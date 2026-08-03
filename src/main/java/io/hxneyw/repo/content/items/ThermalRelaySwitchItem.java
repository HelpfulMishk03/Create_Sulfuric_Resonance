package io.hxneyw.repo.content.items;

import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Thermal Relay Switch item.
 *
 * <p>The held stack stores a persistent, deduplicated list of selected Molten
 * Rotor Furnaces. Each normal furnace right-click adds to the existing list;
 * it never replaces the previous selection.</p>
 */
public class ThermalRelaySwitchItem extends BlockItem {

    private static final String NETWORK_TAG = "RelayNetwork";
    private static final String LINKED_FURNACES_TAG = "LinkedFurnaces";

    private static final String POSITION_TAG = "Position";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String IDENTITY_TAG = "Identity";

    /*
     * Legacy single-selection keys from the first Phase 2 attempt.
     * They are migrated automatically when encountered.
     */
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

        if (level.getBlockEntity(clickedPos)
                instanceof MoltenRotorBlockEntity furnace) {

            /*
             * Sneak-right-clicking a furnace deliberately does nothing.
             * Connections are cleared only by sneak-right-clicking air.
             */
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
                addFurnace(
                        stack,
                        clickedFurnace
                );
                markInventoryChanged(player);
            }

            return InteractionResult.sidedSuccess(
                    level.isClientSide
            );
        }

        return super.useOn(context);
    }

    /**
     * Sneak-right-clicking air clears the entire held selection.
     */
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

    /**
     * Keep the item-level fallback as well as the explicit glint component.
     */
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasConnections(stack)
                || super.isFoil(stack);
    }

    public static boolean hasConnections(
            ItemStack stack
    ) {
        return !getLinks(stack).isEmpty();
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

    public static List<FurnaceLink> getLinks(
            ItemStack stack
    ) {
        return readLinks(
                getCustomTag(stack)
        );
    }

    /**
     * Adds a furnace without replacing any existing endpoint.
     */
    public static void addFurnace(
            ItemStack stack,
            FurnaceLink newLink
    ) {
        CustomData.update(
                DataComponents.CUSTOM_DATA,
                stack,
                tag -> {
                    UUID networkId = tag.hasUUID(NETWORK_TAG)
                            ? tag.getUUID(NETWORK_TAG)
                            : UUID.randomUUID();

                    Map<FurnaceKey, FurnaceLink> links =
                            readUniqueLinks(tag);

                    links.putIfAbsent(
                            FurnaceKey.from(newLink),
                            newLink
                    );

                    writeLinks(
                            tag,
                            networkId,
                            links.values()
                    );
                }
        );

        /*
         * Do not depend only on Item#isFoil. The stack component guarantees
         * Minecraft's normal enchantment-glint renderer is enabled.
         */
        stack.set(
                DataComponents.ENCHANTMENT_GLINT_OVERRIDE,
                true
        );
    }

    public static void setConnections(
            ItemStack stack,
            UUID networkId,
            List<FurnaceLink> links
    ) {
        Map<FurnaceKey, FurnaceLink> unique =
                new LinkedHashMap<>();

        for (FurnaceLink link : links) {
            unique.putIfAbsent(
                    FurnaceKey.from(link),
                    link
            );
        }

        if (unique.isEmpty()) {
            clearConnections(stack);
            return;
        }

        CompoundTag tag = getCustomTag(stack);

        writeLinks(
                tag,
                networkId,
                unique.values()
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

    private static List<FurnaceLink> readLinks(
            CompoundTag tag
    ) {
        return List.copyOf(
                readUniqueLinks(tag).values()
        );
    }

    private static Map<FurnaceKey, FurnaceLink>
    readUniqueLinks(CompoundTag tag) {
        Map<FurnaceKey, FurnaceLink> unique =
                new LinkedHashMap<>();

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
                CompoundTag linkTag =
                        storedLinks.getCompound(index);

                FurnaceLink link =
                        readLink(linkTag);

                if (link != null) {
                    unique.putIfAbsent(
                            FurnaceKey.from(link),
                            link
                    );
                }
            }
        }

        /*
         * Migrate a stack produced by the earlier one-furnace implementation.
         */
        if (tag.contains(LEGACY_POSITION_TAG)
                && tag.contains(LEGACY_DIMENSION_TAG)
                && tag.hasUUID(LEGACY_IDENTITY_TAG)) {
            FurnaceLink legacyLink =
                    new FurnaceLink(
                            BlockPos.of(
                                    tag.getLong(
                                            LEGACY_POSITION_TAG
                                    )
                            ),
                            tag.getString(
                                    LEGACY_DIMENSION_TAG
                            ),
                            tag.getUUID(
                                    LEGACY_IDENTITY_TAG
                            )
                    );

            unique.putIfAbsent(
                    FurnaceKey.from(legacyLink),
                    legacyLink
            );
        }

        return unique;
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

    private static void writeLinks(
            CompoundTag tag,
            UUID networkId,
            Iterable<FurnaceLink> links
    ) {
        ListTag storedLinks = new ListTag();

        for (FurnaceLink link : links) {
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

            storedLinks.add(linkTag);
        }

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

    private record FurnaceKey(
            String dimension,
            BlockPos position,
            UUID furnaceIdentity
    ) {
        private static FurnaceKey from(
                FurnaceLink link
        ) {
            return new FurnaceKey(
                    link.dimension(),
                    link.position(),
                    link.furnaceIdentity()
            );
        }
    }
}
