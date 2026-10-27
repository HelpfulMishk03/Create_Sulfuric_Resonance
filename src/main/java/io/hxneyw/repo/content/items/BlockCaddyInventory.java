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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

/** A chest inventory persisted directly on the carried Caddy or chest cargo stack. */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("resource")
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

    public static void openForCaddy(Player player, ItemStack caddy, Consumer<ItemStack> onSave) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        open(serverPlayer, new BlockCaddyInventory(
                caddy, serverPlayer.registryAccess(), true, onSave));
    }

    public static void openForCargo(Player player, ItemStack cargo, Consumer<ItemStack> onSave) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        open(serverPlayer, new BlockCaddyInventory(
                cargo, serverPlayer.registryAccess(), false, onSave));
    }

    private static void open(ServerPlayer player, BlockCaddyInventory inventory) {
        inventory.setStorageOpen(true);
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, opener) ->
                        new StorageCargoMenu(containerId, playerInventory, inventory),
                Component.translatable("container.chest")
        ));
        inventory.playStorageSound(player, true);
    }

    private void playStorageSound(Player player, boolean opening) {
        ItemStack cargo = backingCaddy
                ? BlockCaddyItem.getCargoStack(backingStack)
                : backingStack;
        if (!(cargo.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)) return;
        var block = blockItem.getBlock();
        var sound = block instanceof ShulkerBoxBlock
                ? (opening ? SoundEvents.SHULKER_BOX_OPEN : SoundEvents.SHULKER_BOX_CLOSE)
                : (opening ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE);
        player.level().playSound(null, player.blockPosition(), sound,
                SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    private static final class StorageCargoMenu extends ChestMenu {
        private final BlockCaddyInventory cargo;

        private StorageCargoMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory,
                BlockCaddyInventory cargo) {
            super(MenuType.GENERIC_9x3, containerId, playerInventory, cargo, 3);
            this.cargo = cargo;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            cargo.setStorageOpen(false);
            cargo.playStorageSound(player, false);
        }
    }

    private void setStorageOpen(boolean open) {
        if (backingCaddy) {
            BlockCaddyItem.setStorageCargoOpen(backingStack, open);
        } else {
            CustomData current = backingStack.get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag data = current == null ? new CompoundTag() : current.copyTag();
            if (open) data.putBoolean("CSRStorageOpen", true);
            else data.remove("CSRStorageOpen");
            backingStack.set(DataComponents.BLOCK_ENTITY_DATA,
                    data.isEmpty() ? null : CustomData.of(data));
        }
        onSave.accept(backingStack);
    }

    private void loadContents() {
        CompoundTag data = getBackingData();
        ListTag items = data.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = Byte.toUnsignedInt(entry.getByte("Slot"));
            if (slot < getContainerSize()) {
                CompoundTag stackData = entry.contains("Stack", Tag.TAG_COMPOUND)
                        ? entry.getCompound("Stack") : entry;
                ItemStack.parse(registries, stackData)
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
            CompoundTag entry = (CompoundTag) stack.save(registries);
            entry.putByte("Slot", (byte) slot);
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
