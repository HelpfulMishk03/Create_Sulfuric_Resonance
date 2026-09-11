package io.hxneyw.repo.client.screen;

import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlock;
import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryBlockEntity;
import io.hxneyw.repo.content.blocks.thermalbattery.ThermalBatteryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public final class ThermalBatteryScreen
        extends AbstractContainerScreen<ThermalBatteryMenu> {

    private static final int PANEL = 0xF01A1D20;
    private static final int INNER = 0xF025292D;
    private static final int BORDER = 0xFF81563B;
    private static final int TEXT = 0xFFE9E9E9;
    private static final int MUTED = 0xFF9EA4A8;
    private static final int YELLOW = 0xFFE1C65A;
    private static final int ORANGE = 0xFFE47B36;
    private static final int RED = 0xFFD84A42;

    private Button heatedButton;
    private Button superheatedButton;

    public ThermalBatteryScreen(
            @NotNull ThermalBatteryMenu menu,
            @NotNull Inventory inventory,
            @NotNull Component title
    ) {
        super(menu, inventory, title);
        imageWidth = 210;
        imageHeight = 150;
        inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();

        heatedButton = addRenderableWidget(Button.builder(
                Component.translatable(
                        "gui.sulfuricresonance.thermal_battery.heated"
                ),
                button -> sendButton(ThermalBatteryMenu.BUTTON_HEATED)
        ).bounds(leftPos + 18, topPos + 112, 82, 20).build());

        superheatedButton = addRenderableWidget(Button.builder(
                Component.translatable(
                        "gui.sulfuricresonance.thermal_battery.superheated"
                ),
                button -> sendButton(ThermalBatteryMenu.BUTTON_SUPERHEATED)
        ).bounds(leftPos + 110, topPos + 112, 82, 20).build());

        refreshButtons();
    }

    private void sendButton(int id) {
        if (minecraft == null || minecraft.gameMode == null) {
            return;
        }
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshButtons();
    }

    private void refreshButtons() {
        if (heatedButton == null || superheatedButton == null) {
            return;
        }
        boolean heated = menu.getOutputMode()
                == ThermalBatteryBlockEntity.OutputMode.HEATED;
        heatedButton.active = !heated;
        superheatedButton.active = heated;
    }

    @Override
    protected void renderBg(
            @NotNull GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, BORDER);
        graphics.fill(x + 5, y + 5, x + imageWidth - 5, y + imageHeight - 5, INNER);

        float capacity = Math.max(1.0F, menu.getCapacity());
        float ratio = Math.min(1.0F, menu.getTotalHeat() / capacity);
        int barX = x + 18;
        int barY = y + 44;
        int barWidth = 174;
        graphics.fill(barX, barY, barX + barWidth, barY + 12, 0xFF111315);
        graphics.fill(
                barX + 1,
                barY + 1,
                barX + 1 + Math.round((barWidth - 2) * ratio),
                barY + 11,
                menu.getOutputMode()
                        == ThermalBatteryBlockEntity.OutputMode.SUPERHEATED
                        ? ORANGE : YELLOW
        );
    }

    @Override
    protected void renderLabels(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(font, title, 12, 11, TEXT, false);

        float capacity = Math.max(1.0F, menu.getCapacity());
        int percent = Math.round(100.0F * menu.getTotalHeat() / capacity);
        graphics.drawString(
                font,
                Component.translatable(
                        "gui.sulfuricresonance.thermal_battery.stored",
                        percent
                ),
                18,
                31,
                TEXT,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "gui.sulfuricresonance.thermal_battery.input",
                        menu.getInputTemperature()
                ),
                18,
                63,
                MUTED,
                false
        );

        graphics.drawString(
                font,
                Component.translatable(
                        "gui.sulfuricresonance.thermal_battery.runtime",
                        formatTicks(menu.getEstimatedRuntimeTicks())
                ),
                18,
                76,
                MUTED,
                false
        );

        ThermalBatteryBlock.IndicatorState indicator = menu.getIndicatorState();
        int color = switch (indicator) {
            case HEATED -> YELLOW;
            case SUPERHEATED -> ORANGE;
            case FAULT -> RED;
        };
        graphics.drawString(
                font,
                Component.translatable(
                        "gui.sulfuricresonance.thermal_battery.status."
                                + indicator.getSerializedName()
                ),
                18,
                90,
                color,
                false
        );
    }

    private static String formatTicks(int ticks) {
        int totalSeconds = Math.max(0, ticks / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public void render(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
