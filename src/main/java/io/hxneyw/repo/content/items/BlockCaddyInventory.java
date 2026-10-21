package io.hxneyw.repo.content.items;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

/** A chest inventory persisted directly on the carried Caddy or chest cargo stack. */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class BlockCaddyInventory extends SimpleContainer {
    private static final int SIZE = 27;
    private static final String ITEMS_KEY = "Items";

    private final ItemStack backingStack;
    private final HolderLookup.Provider registries;
    private final boolean backingCaddy;
    private final Consumer<ItemStack> onSave;
    private boolean loading = true;

    private BlockCaddyInventory(ItemStack backingStack, HolderLookup.Provider registries,
            boolean backingCaddy, Consumer<ItemStack> onSave) {
        super(SIZE);
        this.backingStack = backingStack;
        this.registries = registries;
        this.backingCaddy = backingCaddy;
        this.onSave = onSave;
        loadContents();
        loading = false;
    }

    public static void openForCaddy(Player player, ItemStack caddy) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        open(serverPlayer, new BlockCaddyInventory(
                caddy, serverPlayer.registryAccess(), true, stack -> {}));
    }

    public static void openForCargo(Player player, ItemStack cargo, Consumer<ItemStack> onSave) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        open(serverPlayer, new BlockCaddyInventory(
                cargo, serverPlayer.registryAccess(), false, onSave));
    }

    private static void open(ServerPlayer player, BlockCaddyInventory inventory) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, opener) ->
                        ChestMenu.threeRows(containerId, playerInventory, inventory),
                Component.translatable("container.chest")
        ));
    }

    private void loadContents() {
        CompoundTag data = getBackingData();
        ListTag items = data.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = Byte.toUnsignedInt(entry.getByte("Slot"));
            if (slot < getContainerSize()) {
                ItemStack.parse(registries, entry.getCompound("Stack"))
                        .ifPresent(stack -> super.setItem(slot, stack));
            }
        }
    }

    private CompoundTag getBackingData() {
        CustomData customData = backingStack.get(backingCaddy
                ? DataComponents.CUSTOM_DATA
                : DataComponents.BLOCK_ENTITY_DATA);
        if (customData == null) {
            return new CompoundTag();
        }
        CompoundTag root = customData.copyTag();
        return backingCaddy
                ? root.getCompound(BlockCaddyItem.CARGO_BLOCK_ENTITY_KEY)
                : root;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (loading) {
            return;
        }

        if (backingCaddy) {
            ItemStack cargo = BlockCaddyItem.getCargoStack(backingStack);
            BlockCaddyItem.ensureCargoBlockEntityDataId(cargo);
            BlockCaddyItem.setCargoStack(backingStack, cargo);
        } else {
            BlockCaddyItem.ensureCargoBlockEntityDataId(backingStack);
        }

        // Preserve the block-entity id and any other native data on the cargo stack.
        CompoundTag cargoData = getBackingData();
        ListTag items = new ListTag();
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putByte("Slot", (byte) slot);
            entry.put("Stack", stack.save(registries));
            items.add(entry);
        }
        cargoData.put(ITEMS_KEY, items);

        if (backingCaddy) {
            CustomData current = backingStack.get(DataComponents.CUSTOM_DATA);
            CompoundTag root = current == null ? new CompoundTag() : current.copyTag();
            root.put(BlockCaddyItem.CARGO_BLOCK_ENTITY_KEY, cargoData);
            backingStack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        } else {
            backingStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(cargoData));
        }
        onSave.accept(backingStack);
    }
}
