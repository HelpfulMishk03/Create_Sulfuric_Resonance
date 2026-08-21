package io.hxneyw.repo.client.screen;

import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity;
import io.hxneyw.repo.content.fluids.spritzer.PerforatedSpritzerBlockEntity.PrecisionFilterMode;
import io.hxneyw.repo.content.menu.PrecisionSpritzerMenu;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public final class PrecisionSpritzerScreen
        extends AbstractContainerScreen<PrecisionSpritzerMenu> {

    private static final int PANEL = 0xF0181B20;
    private static final int PANEL_INNER = 0xF0222730;
    private static final int ROW = 0xD02B313B;
    private static final int ROW_ALT = 0xD0323944;
    private static final int ROW_SELECTED = 0xD06A4A2B;
    private static final int BORDER = 0xFFB56F36;
    private static final int BORDER_DARK = 0xFF5B3822;
    private static final int TEXT = 0xFFF1F1F1;
    private static final int MUTED = 0xFFAAB0BA;
    private static final int ACCENT = 0xFFE0A14B;
    private static final int LIST_X = 16;
    private static final int LIST_Y = 88;
    private static final int LIST_WIDTH = 278;
    private static final int ROW_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 5;
    private static final int WHEEL_SCROLL_ROWS = 3;
    private static final int SEARCH_DEBOUNCE_TICKS = 2;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_GAP = 3;

    private static final List<ItemIndexEntry> ITEM_INDEX = BuiltInRegistries.ITEM.stream()
            .filter(item -> item != Items.AIR)
            .map(PrecisionSpritzerScreen::createItemIndexEntry)
            .sorted(Comparator.comparing(ItemIndexEntry::registryName))
            .toList();

    private static final List<EntityIndexEntry> ENTITY_INDEX = BuiltInRegistries.ENTITY_TYPE.stream()
            .map(PrecisionSpritzerScreen::createEntityIndexEntry)
            .sorted(Comparator.comparing(EntityIndexEntry::registryName))
            .toList();

    private final Map<Item, ItemView> itemViews = new IdentityHashMap<>();
    private final Map<EntityType<?>, EntityView> entityViews = new IdentityHashMap<>();
    private final Set<Integer> selectedItemIds = new HashSet<>();
    private final Set<Integer> selectedEntityIds = new HashSet<>();
    private final int[] itemSelectionSlots =
            new int[PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES];
    private final int[] entitySelectionSlots =
            new int[PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES];

    private List<ItemIndexEntry> itemCandidates = ITEM_INDEX;
    private List<EntityIndexEntry> entityCandidates = ENTITY_INDEX;
    private Button itemModeButton;
    private Button entityModeButton;
    private Button clearButton;
    private Button selectedOnlyButton;
    private EditBox searchBox;
    private PrecisionFilterMode cachedMode;
    private String cachedQuery = "";
    private int scrollOffset;
    private boolean selectedOnly;
    private boolean draggingScrollbar;
    private int scrollbarGrabOffset;
    private int searchDebounceTicks;
    private boolean candidateCacheDirty = true;
    private boolean selectionCacheInitialized;

    public PrecisionSpritzerScreen(
            @NotNull PrecisionSpritzerMenu menu,
            @NotNull Inventory playerInventory,
            @NotNull Component title
    ) {
        super(menu, playerInventory, title);
        this.imageWidth = 310;
        this.imageHeight = 242;
        this.titleLabelX = 12;
        this.titleLabelY = 10;
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        this.itemModeButton = this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.sulfuricresonance.precision_spritzer.mode.item"),
                                button -> this.setMode(PrecisionFilterMode.ITEM)
                        )
                        .bounds(left + 16, top + 34, 132, 22)
                        .build()
        );

        this.entityModeButton = this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.sulfuricresonance.precision_spritzer.mode.entity"),
                                button -> this.setMode(PrecisionFilterMode.ENTITY)
                        )
                        .bounds(left + 162, top + 34, 132, 22)
                        .build()
        );

        this.searchBox = new EditBox(
                this.font,
                left + 16,
                top + 62,
                278,
                20,
                Component.translatable("gui.sulfuricresonance.precision_spritzer.search")
        );
        this.searchBox.setResponder(value -> {
            this.scrollOffset = 0;
            this.searchDebounceTicks = SEARCH_DEBOUNCE_TICKS;
            this.candidateCacheDirty = true;
        });
        this.addRenderableWidget(this.searchBox);

        this.selectedOnlyButton = this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.sulfuricresonance.precision_spritzer.selected_only"),
                                button -> this.toggleSelectedOnly()
                        )
                        .bounds(left + 16, top + 190, 132, 20)
                        .build()
        );

        this.clearButton = this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.sulfuricresonance.precision_spritzer.clear"),
                                button -> this.sendButton(PrecisionSpritzerMenu.BUTTON_CLEAR)
                        )
                        .bounds(left + 162, top + 190, 132, 20)
                        .build()
        );

        this.refreshSelectionCache();
        this.refreshWidgetState();
        this.refreshCandidateCache();
        this.clampScroll();
    }

    private static ItemIndexEntry createItemIndexEntry(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String registryName = id.toString();
        return new ItemIndexEntry(
                item,
                BuiltInRegistries.ITEM.getId(item),
                registryName,
                registryName.toLowerCase(Locale.ROOT)
        );
    }

    private static EntityIndexEntry createEntityIndexEntry(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        String registryName = id.toString();
        return new EntityIndexEntry(
                type,
                BuiltInRegistries.ENTITY_TYPE.getId(type),
                registryName,
                registryName.toLowerCase(Locale.ROOT)
        );
    }

    private ItemView itemView(ItemIndexEntry entry) {
        return this.itemViews.computeIfAbsent(entry.item(), item -> {
            ItemStack stack = new ItemStack(item);
            String displayName = stack.getHoverName().getString();
            return new ItemView(
                    stack,
                    displayName,
                    displayName.toLowerCase(Locale.ROOT)
            );
        });
    }

    private EntityView entityView(EntityIndexEntry entry) {
        return this.entityViews.computeIfAbsent(entry.type(), type -> {
            String displayName = type.getDescription().getString();
            return new EntityView(
                    displayName,
                    displayName.toLowerCase(Locale.ROOT)
            );
        });
    }

    private void setMode(PrecisionFilterMode mode) {
        this.scrollOffset = 0;
        this.selectedOnly = false;
        this.candidateCacheDirty = true;
        this.searchBox.setValue("");
        this.sendButton(
                mode == PrecisionFilterMode.ITEM
                        ? PrecisionSpritzerMenu.BUTTON_MODE_ITEM
                        : PrecisionSpritzerMenu.BUTTON_MODE_ENTITY
        );
    }

    private void toggleSelectedOnly() {
        this.selectedOnly = !this.selectedOnly;
        this.scrollOffset = 0;
        this.candidateCacheDirty = true;
        this.searchBox.setValue("");
        this.refreshWidgetState();
    }

    private void sendButton(int buttonId) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
    }

    private String query() {
        return this.searchBox == null ? "" : this.searchBox.getValue().trim().toLowerCase(Locale.ROOT);
    }

    private boolean includeItem(ItemIndexEntry entry, String query) {
        if (this.selectedOnly && !this.selectedItemIds.contains(entry.registryId())) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        return entry.registrySearch().contains(query)
                || this.itemView(entry).displaySearch().contains(query);
    }

    private boolean includeEntity(EntityIndexEntry entry, String query) {
        if (this.selectedOnly && !this.selectedEntityIds.contains(entry.registryId())) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        return entry.registrySearch().contains(query)
                || this.entityView(entry).displaySearch().contains(query);
    }

    private void refreshCandidateCache() {
        PrecisionFilterMode mode = this.menu.getMode();
        String query = this.query();
        if (!this.candidateCacheDirty
                && mode == this.cachedMode
                && query.equals(this.cachedQuery)) {
            return;
        }

        this.cachedMode = mode;
        this.cachedQuery = query;

        if (mode == PrecisionFilterMode.ITEM) {
            if (!this.selectedOnly && query.isEmpty()) {
                this.itemCandidates = ITEM_INDEX;
            } else {
                this.itemCandidates = ITEM_INDEX.stream()
                        .filter(entry -> this.includeItem(entry, query))
                        .toList();
            }
        } else if (!this.selectedOnly && query.isEmpty()) {
            this.entityCandidates = ENTITY_INDEX;
        } else {
            this.entityCandidates = ENTITY_INDEX.stream()
                    .filter(entry -> this.includeEntity(entry, query))
                    .toList();
        }

        this.candidateCacheDirty = false;
    }

    private boolean refreshSelectionCache() {
        boolean changed = !this.selectionCacheInitialized;

        for (int i = 0; i < PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES; i++) {
            int itemId = this.menu.getItemFilterRegistryId(i);
            int entityId = this.menu.getEntityFilterRegistryId(i);
            if (!this.selectionCacheInitialized
                    || this.itemSelectionSlots[i] != itemId
                    || this.entitySelectionSlots[i] != entityId) {
                changed = true;
            }
            this.itemSelectionSlots[i] = itemId;
            this.entitySelectionSlots[i] = entityId;
        }

        if (!changed) {
            return false;
        }

        this.selectedItemIds.clear();
        this.selectedEntityIds.clear();
        for (int i = 0; i < PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES; i++) {
            if (this.itemSelectionSlots[i] >= 0) {
                this.selectedItemIds.add(this.itemSelectionSlots[i]);
            }
            if (this.entitySelectionSlots[i] >= 0) {
                this.selectedEntityIds.add(this.entitySelectionSlots[i]);
            }
        }
        this.selectionCacheInitialized = true;
        return true;
    }

    private int candidateCount() {
        return this.menu.getMode() == PrecisionFilterMode.ITEM
                ? this.itemCandidates.size()
                : this.entityCandidates.size();
    }

    private int selectedCount() {
        return this.menu.getMode() == PrecisionFilterMode.ITEM
                ? this.selectedItemIds.size()
                : this.selectedEntityIds.size();
    }

    private int maxScroll() {
        return Math.max(0, this.candidateCount() - VISIBLE_ROWS);
    }

    private void clampScroll() {
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll()));
    }

    private int scrollbarTrackHeight() {
        return ROW_HEIGHT * VISIBLE_ROWS;
    }

    private int scrollbarHandleHeight() {
        int total = this.candidateCount();
        int trackHeight = this.scrollbarTrackHeight();
        if (total <= VISIBLE_ROWS) {
            return trackHeight;
        }
        return Math.max(14, trackHeight * VISIBLE_ROWS / total);
    }

    private int scrollbarHandleOffset() {
        int maxScroll = this.maxScroll();
        if (maxScroll <= 0) {
            return 0;
        }
        int travel = this.scrollbarTrackHeight() - this.scrollbarHandleHeight();
        return Math.round(travel * (this.scrollOffset / (float) maxScroll));
    }

    private int scrollbarLeft() {
        return this.leftPos + LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH;
    }

    private int scrollbarTop() {
        return this.topPos + LIST_Y;
    }

    private boolean isInsideScrollbar(double mouseX, double mouseY) {
        return mouseX >= this.scrollbarLeft()
                && mouseX < this.scrollbarLeft() + SCROLLBAR_WIDTH
                && mouseY >= this.scrollbarTop()
                && mouseY < this.scrollbarTop() + this.scrollbarTrackHeight();
    }

    private boolean isInsideRows(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + LIST_X
                && mouseX < this.leftPos + LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH - SCROLLBAR_GAP
                && mouseY >= this.topPos + LIST_Y
                && mouseY < this.topPos + LIST_Y + ROW_HEIGHT * VISIBLE_ROWS;
    }

    private void scrollFromScrollbar(double mouseY) {
        int maxScroll = this.maxScroll();
        if (maxScroll <= 0) {
            this.scrollOffset = 0;
            return;
        }
        int travel = this.scrollbarTrackHeight() - this.scrollbarHandleHeight();
        if (travel <= 0) {
            this.scrollOffset = 0;
            return;
        }
        double handleTop = mouseY - this.scrollbarGrabOffset - this.scrollbarTop();
        handleTop = Math.max(0.0D, Math.min(handleTop, travel));
        this.scrollOffset = (int) Math.round(handleTop * maxScroll / travel);
        this.clampScroll();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean selectionChanged = this.refreshSelectionCache();
        PrecisionFilterMode mode = this.menu.getMode();
        if (mode != this.cachedMode) {
            this.scrollOffset = 0;
            this.candidateCacheDirty = true;
        }
        if (selectionChanged && this.selectedOnly) {
            this.candidateCacheDirty = true;
        }
        this.refreshWidgetState();
        if (this.searchDebounceTicks > 0) {
            this.searchDebounceTicks--;
        }
        if (this.searchDebounceTicks == 0) {
            this.refreshCandidateCache();
        }
        this.clampScroll();
    }

    private void refreshWidgetState() {
        if (this.itemModeButton == null || this.searchBox == null) {
            return;
        }

        boolean itemMode = this.menu.getMode() == PrecisionFilterMode.ITEM;
        int selected = this.selectedCount();
        if (this.selectedOnly && selected == 0) {
            this.selectedOnly = false;
            this.scrollOffset = 0;
            this.candidateCacheDirty = true;
        }
        this.itemModeButton.active = !itemMode;
        this.entityModeButton.active = itemMode;
        this.searchBox.setHint(Component.translatable(
                itemMode
                        ? "gui.sulfuricresonance.precision_spritzer.search.item"
                        : "gui.sulfuricresonance.precision_spritzer.search.entity"
        ));
        this.selectedOnlyButton.active = selected > 0;
        this.selectedOnlyButton.setMessage(Component.translatable(
                this.selectedOnly
                        ? "gui.sulfuricresonance.precision_spritzer.show_all"
                        : "gui.sulfuricresonance.precision_spritzer.selected_only"
        ));
        this.clearButton.active = selected > 0;
    }

    private boolean isInsideList(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + LIST_X
                && mouseX < this.leftPos + LIST_X + LIST_WIDTH
                && mouseY >= this.topPos + LIST_Y
                && mouseY < this.topPos + LIST_Y + ROW_HEIGHT * VISIBLE_ROWS;
    }

    private boolean toggleRowAt(double mouseY) {
        this.refreshCandidateCache();
        int row = (int) ((mouseY - (this.topPos + LIST_Y)) / ROW_HEIGHT);
        int index = this.scrollOffset + row;

        if (this.menu.getMode() == PrecisionFilterMode.ITEM) {
            if (index < 0 || index >= this.itemCandidates.size()) {
                return false;
            }
            int registryId = this.itemCandidates.get(index).registryId();
            this.sendButton(PrecisionSpritzerMenu.BUTTON_ITEM_BASE + registryId);
            return true;
        }

        if (index < 0 || index >= this.entityCandidates.size()) {
            return false;
        }
        int registryId = this.entityCandidates.get(index).registryId();
        this.sendButton(PrecisionSpritzerMenu.BUTTON_ENTITY_BASE + registryId);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null
                && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isInsideScrollbar(mouseX, mouseY)) {
            int handleTop = this.scrollbarTop() + this.scrollbarHandleOffset();
            int handleHeight = this.scrollbarHandleHeight();
            if (mouseY >= handleTop && mouseY < handleTop + handleHeight) {
                this.scrollbarGrabOffset = (int) mouseY - handleTop;
            } else {
                this.scrollbarGrabOffset = handleHeight / 2;
                this.scrollFromScrollbar(mouseY);
            }
            this.draggingScrollbar = this.maxScroll() > 0;
            return true;
        }

        if (button == 0 && this.isInsideRows(mouseX, mouseY)) {
            return this.toggleRowAt(mouseY);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (button == 0 && this.draggingScrollbar) {
            this.scrollFromScrollbar(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (!this.isInsideList(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (scrollY > 0.0D) {
            this.scrollOffset -= WHEEL_SCROLL_ROWS;
        } else if (scrollY < 0.0D) {
            this.scrollOffset += WHEEL_SCROLL_ROWS;
        }
        this.clampScroll();
        return true;
    }

    @Override
    public void render(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            @NotNull GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int left = this.leftPos;
        int top = this.topPos;
        int right = left + this.imageWidth;
        int bottom = top + this.imageHeight;

        graphics.fill(left, top, right, bottom, BORDER_DARK);
        graphics.fill(left + 2, top + 2, right - 2, bottom - 2, BORDER);
        graphics.fill(left + 4, top + 4, right - 4, bottom - 4, PANEL);
        graphics.fill(
                left + LIST_X - 2,
                top + LIST_Y - 2,
                left + LIST_X + LIST_WIDTH + 2,
                top + LIST_Y + ROW_HEIGHT * VISIBLE_ROWS + 2,
                PANEL_INNER
        );
    }

    @Override
    protected void renderLabels(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT, false);

        int selected = this.selectedCount();
        graphics.drawString(
                this.font,
                Component.translatable(
                        "gui.sulfuricresonance.precision_spritzer.selected_count",
                        selected,
                        PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES
                ),
                16,
                218,
                selected >= PerforatedSpritzerBlockEntity.MAX_FILTER_ENTRIES ? ACCENT : MUTED,
                false
        );
        graphics.drawString(
                this.font,
                Component.translatable("gui.sulfuricresonance.precision_spritzer.filters_run_together"),
                126,
                218,
                MUTED,
                false
        );

        if (this.menu.getMode() == PrecisionFilterMode.ITEM) {
            this.renderItemRows(graphics);
        } else {
            this.renderEntityRows(graphics);
        }
        this.renderScrollbar(graphics);
    }

    private void renderItemRows(GuiGraphics graphics) {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = this.scrollOffset + row;
            if (index >= this.itemCandidates.size()) {
                break;
            }
            ItemIndexEntry entry = this.itemCandidates.get(index);
            boolean selected = this.selectedItemIds.contains(entry.registryId());
            int y = LIST_Y + row * ROW_HEIGHT;
            graphics.fill(
                    LIST_X,
                    y,
                    LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH - SCROLLBAR_GAP,
                    y + ROW_HEIGHT - 1,
                    selected ? ROW_SELECTED : (row % 2 == 0 ? ROW : ROW_ALT)
            );
            ItemView view = this.itemView(entry);
            graphics.renderItem(view.stack(), LIST_X + 2, y + 2);
            String name = this.font.plainSubstrByWidth(view.displayName(), 202);
            graphics.drawString(this.font, name, LIST_X + 23, y + 6, TEXT, false);
            if (selected) {
                graphics.drawString(
                        this.font,
                        Component.literal("✓"),
                        LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH - 17,
                        y + 6,
                        ACCENT,
                        false
                );
            }
        }
    }

    private void renderEntityRows(GuiGraphics graphics) {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = this.scrollOffset + row;
            if (index >= this.entityCandidates.size()) {
                break;
            }
            EntityIndexEntry entry = this.entityCandidates.get(index);
            boolean selected = this.selectedEntityIds.contains(entry.registryId());
            int y = LIST_Y + row * ROW_HEIGHT;
            graphics.fill(
                    LIST_X,
                    y,
                    LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH - SCROLLBAR_GAP,
                    y + ROW_HEIGHT - 1,
                    selected ? ROW_SELECTED : (row % 2 == 0 ? ROW : ROW_ALT)
            );
            EntityView view = this.entityView(entry);
            String name = this.font.plainSubstrByWidth(view.displayName(), 154);
            String registryName = this.font.plainSubstrByWidth(entry.registryName(), 94);
            graphics.drawString(this.font, name, LIST_X + 4, y + 6, TEXT, false);
            graphics.drawString(this.font, registryName, LIST_X + 164, y + 6, MUTED, false);
            if (selected) {
                graphics.drawString(
                        this.font,
                        Component.literal("✓"),
                        LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH - 17,
                        y + 6,
                        ACCENT,
                        false
                );
            }
        }
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int barX = LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH;
        int barY = LIST_Y;
        int barHeight = this.scrollbarTrackHeight();
        int handleHeight = this.scrollbarHandleHeight();
        int handleY = barY + this.scrollbarHandleOffset();
        graphics.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + barHeight, BORDER_DARK);
        graphics.fill(barX, handleY, barX + SCROLLBAR_WIDTH, handleY + handleHeight, BORDER);
    }

    private record ItemIndexEntry(
            Item item,
            int registryId,
            String registryName,
            String registrySearch
    ) {
    }

    private record EntityIndexEntry(
            EntityType<?> type,
            int registryId,
            String registryName,
            String registrySearch
    ) {
    }

    private record ItemView(
            ItemStack stack,
            String displayName,
            String displaySearch
    ) {
    }

    private record EntityView(
            String displayName,
            String displaySearch
    ) {
    }
}
