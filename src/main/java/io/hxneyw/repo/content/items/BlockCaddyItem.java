package io.hxneyw.repo.content.items;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import io.hxneyw.repo.content.blocks.BlockCaddyBlockEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class BlockCaddyItem extends BlockItem {
    private static final String CARGO_ITEM_KEY = "sulfuricresonance:BlockCaddyItem";
    private static final String CARGO_STATE_KEY = "sulfuricresonance:CargoState";
    private static final String CARGO_OPEN_KEY = "sulfuricresonance:CargoOpen";
    static final String CARGO_BLOCK_ENTITY_KEY = "sulfuricresonance:CargoBlockEntity";

    public BlockCaddyItem(Block block, Properties properties) {
        super(block, properties.stacksTo(64));
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack caddy = context.getItemInHand();
        ItemStack cargo = getCargoStack(caddy);
        InteractionResult result = super.place(context);
        if (result.consumesAction() && !context.getLevel().isClientSide) {
            BlockEntity entity = context.getLevel().getBlockEntity(context.getClickedPos());
            if (entity instanceof BlockCaddyBlockEntity blockCaddy) {
                blockCaddy.setCargo(cargo);
            }
        }
        return result;
    }

    public static ItemStack createStack(@Nullable ItemStack cargo) {
        ItemStack stack = new ItemStack(io.hxneyw.repo.content.Items.BLOCK_CADDY.get());
        if (cargo != null && !cargo.isEmpty()) {
            setCargoStack(stack, cargo);
        }
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack caddy = player.getItemInHand(hand);
        Item cargo = getCargoItem(caddy);

        if (player.isShiftKeyDown()) {
            if (cargo == null) {
                return InteractionResultHolder.pass(caddy);
            }

            if (isChestCargo(caddy)) {
                if (!level.isClientSide) {
                    BlockCaddyInventory.openForCaddy(player, caddy,
                            stack -> player.getInventory().setChanged());
                }
                return InteractionResultHolder.sidedSuccess(caddy, level.isClientSide);
            }

            if (!level.isClientSide) {
                ItemStack removed = getCargoStack(caddy).copyWithCount(caddy.getCount());
                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
                setCargoItem(caddy, null);
            }
            return InteractionResultHolder.sidedSuccess(caddy, level.isClientSide);
        }

        return InteractionResultHolder.pass(caddy);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        Item cargo = getCargoItem(stack);
        tooltipComponents.add(cargo == null
                ? Component.translatable("item.sulfuricresonance.block_caddy.empty")
                : Component.translatable(
                        "item.sulfuricresonance.block_caddy.contents",
                        new ItemStack(cargo).getHoverName()
                ));

        if (!net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltipComponents.add(TooltipHelper.holdShift(
                    FontHelper.Palette.GRAY_AND_WHITE,
                    false
            ));
            return;
        }

        tooltipComponents.add(Component.translatable(
                "item.sulfuricresonance.block_caddy.belt_use"
        ));
        tooltipComponents.add(Component.translatable(
                "item.sulfuricresonance.block_caddy.depot_use"
        ));
        tooltipComponents.add(Component.translatable(
                "item.sulfuricresonance.block_caddy.deployer_use"
        ));
        tooltipComponents.add(Component.translatable(
                cargo == null
                        ? "item.sulfuricresonance.block_caddy.load_hint"
                        : "item.sulfuricresonance.block_caddy.unload_hint"
        ));
        if (isStorageCargo(stack)) {
            appendChestContentsPreview(stack, tooltipComponents);
        }
    }

    private static void appendChestContentsPreview(ItemStack caddy, List<Component> tooltipComponents) {
        ItemStack chest = getCargoStack(caddy);
        CustomData data = chest.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return;
        }
        ListTag items = data.copyTag().getList("Items", Tag.TAG_COMPOUND);
        if (items.isEmpty()) {
            return;
        }
        tooltipComponents.add(Component.translatable(
                "item.sulfuricresonance.block_caddy.chest_preview"));
        for (int i = 0; i < items.size() && i < 9; i++) {
            CompoundTag entry = items.getCompound(i);
            CompoundTag serialized = entry.contains("Stack", Tag.TAG_COMPOUND)
                    ? entry.getCompound("Stack") : entry;
            ResourceLocation itemId = ResourceLocation.tryParse(serialized.getString("id"));
            if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(itemId);
            int count = Math.max(1, serialized.getInt("count"));
            tooltipComponents.add(Component.literal("  " + count + " ")
                    .append(new ItemStack(item).getHoverName()));
        }
    }

    @Nullable
    public static Item getCargoItem(ItemStack caddy) {
        CustomData customData = caddy.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }

        CompoundTag tag = customData.copyTag();
        if (!tag.contains(CARGO_ITEM_KEY, CompoundTag.TAG_STRING)) {
            return null;
        }

        ResourceLocation id = ResourceLocation.tryParse(
                tag.getString(CARGO_ITEM_KEY)
        );
        if (id == null) {
            return null;
        }

        Item item = BuiltInRegistries.ITEM.get(id);
        return isSupportedCargoItem(item) ? item : null;
    }

    public static boolean isSupportedCargoStack(ItemStack stack) {
        return !stack.isEmpty() && isSupportedCargoItem(stack.getItem());
    }

    private static boolean isSupportedCargoItem(Item item) {
        return item instanceof BlockItem || item == net.minecraft.world.item.Items.REDSTONE;
    }

    public static void setCargoItem(ItemStack caddy, @Nullable Item item) {
        CustomData customData = caddy.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData == null
                ? new CompoundTag()
                : customData.copyTag();

        if (item == null) {
            tag.remove(CARGO_ITEM_KEY);
            tag.remove(CARGO_STATE_KEY);
            tag.remove(CARGO_BLOCK_ENTITY_KEY);
        } else {
            tag.putString(
                    CARGO_ITEM_KEY,
                    BuiltInRegistries.ITEM.getKey(item).toString()
            );
        }

        caddy.set(DataComponents.CUSTOM_DATA, tag.isEmpty()
                ? null
                : CustomData.of(tag));
    }

    public static void setCargoStack(ItemStack caddy, @Nullable ItemStack cargo) {
        setCargoStack(caddy, cargo, null);
    }

    public static void setCargoStack(ItemStack caddy, @Nullable ItemStack cargo,
            @Nullable BlockState placedState) {
        if (cargo == null || !isSupportedCargoStack(cargo)) {
            setCargoItem(caddy, null);
            return;
        }
        ensureCargoBlockEntityDataId(cargo);
        setCargoItem(caddy, cargo.getItem());
        CustomData data = caddy.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        tag.remove(CARGO_STATE_KEY);
        BlockItemStateProperties properties = cargo.get(DataComponents.BLOCK_STATE);
        if (placedState != null) {
            CompoundTag state = serializeState(placedState);
            tag.put(CARGO_STATE_KEY, state);
            caddy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        } else if (properties != null && !properties.properties().isEmpty()) {
            CompoundTag state = new CompoundTag();
            properties.properties().forEach(state::putString);
            tag.put(CARGO_STATE_KEY, state);
            caddy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        CustomData blockEntityData = cargo.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            tag.put(CARGO_BLOCK_ENTITY_KEY, blockEntityData.copyTag());
            caddy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        } else {
            tag.remove(CARGO_BLOCK_ENTITY_KEY);
            caddy.set(DataComponents.CUSTOM_DATA, tag.isEmpty() ? null : CustomData.of(tag));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static CompoundTag serializeState(BlockState state) {
        CompoundTag tag = new CompoundTag();
        for (var entry : state.getValues().entrySet()) {
            net.minecraft.world.level.block.state.properties.Property property =
                    entry.getKey();
            Comparable value = entry.getValue();
            tag.putString(property.getName(), property.getName(value));
        }
        return tag;
    }

    public static BlockState getCargoBlockState(ItemStack caddy) {
        Item item = getCargoItem(caddy);
        BlockState state;
        if (item == net.minecraft.world.item.Items.REDSTONE) {
            state = net.minecraft.world.level.block.Blocks.REDSTONE_WIRE.defaultBlockState();
            for (String direction : List.of("north", "east", "south", "west")) {
                var property = state.getBlock().getStateDefinition().getProperty(direction);
                if (property != null) state = applyProperty(state, property, "side");
            }
        } else if (item instanceof BlockItem blockItem) {
            state = blockItem.getBlock().defaultBlockState();
        } else {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        CustomData data = caddy.get(DataComponents.CUSTOM_DATA);
        if (data == null) return state;
        CompoundTag properties = data.copyTag().getCompound(CARGO_STATE_KEY);
        for (String key : properties.getAllKeys()) {
            var property = state.getBlock().getStateDefinition().getProperty(key);
            if (property == null) continue;
            state = applyProperty(state, property, properties.getString(key));
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState applyProperty(BlockState state, net.minecraft.world.level.block.state.properties.Property property, String value) {
        java.util.Optional parsed = property.getValue(value);
        return parsed.isPresent() ? state.setValue(property, (Comparable) parsed.get()) : state;
    }

    public static ItemStack getCargoStack(ItemStack caddy) {
        Item item = getCargoItem(caddy);
        if (item == null) return ItemStack.EMPTY;
        ItemStack cargo = new ItemStack(item);
        CustomData data = caddy.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag properties = data.copyTag().getCompound(CARGO_STATE_KEY);
            java.util.Map<String, String> values = new java.util.HashMap<>();
            for (String key : properties.getAllKeys()) values.put(key, properties.getString(key));
            if (!values.isEmpty()) cargo.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(values));
            CompoundTag blockEntityTag = data.copyTag().getCompound(CARGO_BLOCK_ENTITY_KEY);
            if (!blockEntityTag.isEmpty()) {
                cargo.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(blockEntityTag));
            }
        }
        ensureCargoBlockEntityDataId(cargo);
        return cargo;
    }

    /** Repairs legacy chest cargo data and ensures new cargo stacks have valid BE component data. */
    public static void ensureCargoBlockEntityDataId(ItemStack cargo) {
        CustomData current = cargo.get(DataComponents.BLOCK_ENTITY_DATA);
        if (cargo.isEmpty() || current == null
                || !(cargo.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof EntityBlock entityBlock)) {
            return;
        }
        CompoundTag blockEntityData = current.copyTag();
        if (blockEntityData.contains("id", CompoundTag.TAG_STRING)) {
            return;
        }
        BlockEntity blockEntity = entityBlock.newBlockEntity(
                BlockPos.ZERO,
                blockItem.getBlock().defaultBlockState()
        );
        if (blockEntity == null) {
            return;
        }
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        if (typeId == null) {
            return;
        }
        blockEntityData.putString("id", typeId.toString());
        cargo.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(blockEntityData));
    }

    /** Adds the required block-entity id to pre-fix saved cargo ItemStacks before decoding them. */
    public static void repairSerializedCargoStack(CompoundTag serializedStack) {
        if (!serializedStack.contains("id", CompoundTag.TAG_STRING)) {
            return;
        }
        ResourceLocation itemId = ResourceLocation.tryParse(serializedStack.getString("id"));
        if (itemId == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (!(item instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof EntityBlock entityBlock)) {
            return;
        }
        CompoundTag components = serializedStack.getCompound("components");
        CompoundTag blockEntityData = components.getCompound("minecraft:block_entity_data");
        if (blockEntityData.isEmpty() || blockEntityData.contains("id", CompoundTag.TAG_STRING)) {
            return;
        }
        BlockEntity blockEntity = entityBlock.newBlockEntity(
                BlockPos.ZERO,
                blockItem.getBlock().defaultBlockState()
        );
        if (blockEntity == null) {
            return;
        }
        ResourceLocation typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        if (typeId != null) {
            blockEntityData.putString("id", typeId.toString());
            components.put("minecraft:block_entity_data", blockEntityData);
            serializedStack.put("components", components);
        }
    }

    public static boolean isChestCargo(ItemStack caddy) {
        return isStorageCargo(caddy);
    }

    /** Storage cargo keeps its inventory-bearing block attached to the Caddy. */
    public static boolean isStorageCargo(ItemStack caddy) {
        Item item = getCargoItem(caddy);
        if (!(item instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        if (block instanceof net.minecraft.world.level.block.ChestBlock
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock) {
            return true;
        }

        CustomData blockEntityData = getCargoStack(caddy).get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) {
            return false;
        }
        CompoundTag tag = blockEntityData.copyTag();
        for (String key : List.of("Items", "Inventory", "inventory", "Contents")) {
            if (tag.contains(key, Tag.TAG_LIST) && !tag.getList(key, Tag.TAG_COMPOUND).isEmpty()) {
                return true;
            }
        }
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return path.contains("vault");
    }

    public static void setStorageCargoOpen(ItemStack caddy, boolean open) {
        CustomData current = caddy.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = current == null ? new CompoundTag() : current.copyTag();
        if (open) root.putBoolean(CARGO_OPEN_KEY, true);
        else root.remove(CARGO_OPEN_KEY);
        caddy.set(DataComponents.CUSTOM_DATA, root.isEmpty() ? null : CustomData.of(root));
    }

    public static boolean isStorageCargoOpen(ItemStack caddy) {
        CustomData rootData = caddy.get(DataComponents.CUSTOM_DATA);
        if (rootData != null && rootData.copyTag().getBoolean(CARGO_OPEN_KEY)) return true;
        CustomData cargoData = getCargoStack(caddy).get(DataComponents.BLOCK_ENTITY_DATA);
        return cargoData != null && cargoData.copyTag().getBoolean("CSRStorageOpen");
    }

    public static void clearBeltTransition(ItemStack caddy) {
        CustomData data = caddy.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag tag = data.copyTag();
        tag.remove("CSRTransferX");
        tag.remove("CSRTransferY");
        tag.remove("CSRTransferZ");
        tag.remove("CSRTransferTick");
        caddy.set(DataComponents.CUSTOM_DATA, tag.isEmpty() ? null : CustomData.of(tag));
    }

    public static boolean tryLoadFromBlockItem(
            ItemStack caddy,
            ItemStack blockStack
    ) {
        return tryLoadFromBlockItem(caddy, blockStack, null);
    }

    public static boolean tryLoadFromBlockItem(ItemStack caddy, ItemStack blockStack,
            @Nullable BlockState placedState) {
        if (!(caddy.getItem() instanceof BlockCaddyItem)
                || getCargoItem(caddy) != null
                || !isSupportedCargoStack(blockStack)) {
            return false;
        }

        setCargoStack(caddy, blockStack, placedState);
        return true;
    }

    @Nullable
    public static BlockState getPlacementState(Level level, Player player,
            InteractionHand hand, ItemStack stack, net.minecraft.world.phys.BlockHitResult hit) {
        if (stack.getItem() == net.minecraft.world.item.Items.REDSTONE) {
            return getCargoBlockState(createStack(stack));
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        BlockPlaceContext context = new BlockPlaceContext(
                new UseOnContext(level, player, hand, stack, hit));
        return blockItem.getBlock().getStateForPlacement(context);
    }

}
