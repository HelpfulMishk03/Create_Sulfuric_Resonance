package io.hxneyw.repo.client.screen;

import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.menu.ThermalRelaySwitchMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ThermalRelaySwitchScreen
        extends AbstractContainerScreen<
        ThermalRelaySwitchMenu
        > {

    private static final int PANEL = 0xF016181D;
    private static final int PANEL_INNER = 0xF020242B;
    private static final int CARD = 0xF02A2F38;
    private static final int CARD_ALT = 0xF0262B33;
    private static final int STATUS = 0xF0111317;

    private static final int BORDER_DARK = 0xFF5D3424;
    private static final int BORDER = 0xFFB6663A;
    private static final int ACCENT = 0xFFE37C3F;
    private static final int ACCENT_SOFT = 0xFF75442D;

    private static final int TEXT = 0xFFF1F1F1;
    private static final int MUTED = 0xFFAAB0BA;
    private static final int DIM = 0xFF777E89;
    private static final int REDSTONE = 0xFFFF6258;
    private static final int GLOW = 0xFFD8A5FF;
    private static final int WARNING = 0xFFFFB24A;
    private static final int STABLE = 0xFF91C98E;

    private final List<StepperBinding> steppers =
            new ArrayList<>();

    private Button customHeatModeButton;
    private Button lowFuelModeButton;

    private Button lowHeatScopeButton;
    private Button highHeatScopeButton;
    private Button bothScopeButton;

    private boolean showLowFuelPage;

    public ThermalRelaySwitchScreen(
            @NotNull ThermalRelaySwitchMenu menu,
            @NotNull Inventory playerInventory,
            @NotNull Component title
    ) {
        super(menu, playerInventory, title);

        this.imageWidth = 344;
        this.imageHeight = 258;
        this.titleLabelX = 12;
        this.titleLabelY = 10;

        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        this.steppers.clear();

        int tabY = this.topPos + 28;

        this.customHeatModeButton = addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.sulfuricresonance."
                                                + "thermal_relay_switch."
                                                + "mode.custom_heat"
                                ),
                                button -> selectSettingsPage(false)
                        )
                        .bounds(
                                this.leftPos + 12,
                                tabY,
                                156,
                                22
                        )
                        .build()
        );

        this.lowFuelModeButton = addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.sulfuricresonance."
                                                + "thermal_relay_switch."
                                                + "mode.low_fuel"
                                ),
                                button -> selectSettingsPage(true)
                        )
                        .bounds(
                                this.leftPos + 176,
                                tabY,
                                156,
                                22
                        )
                        .build()
        );

        addCustomHeatControls();
        addLowFuelControls();
        refreshWidgetState();
    }

    private void addCustomHeatControls() {
        addStepper(
                135,
                85,
                ThermalRelaySwitchMenu
                        .BUTTON_HEATED_REDSTONE_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_HEATED_REDSTONE_UP,
                this.menu::getHeatedRedstone,
                ThermalRelaySwitchBlockEntity
                        .MAX_HEATED_REDSTONE,
                Section.CUSTOM_HEAT
        );

        addStepper(
                239,
                85,
                ThermalRelaySwitchMenu
                        .BUTTON_HEATED_GLOW_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_HEATED_GLOW_UP,
                this.menu::getHeatedGlow,
                ThermalRelaySwitchBlockEntity.MAX_GLOW,
                Section.CUSTOM_HEAT
        );

        addStepper(
                135,
                123,
                ThermalRelaySwitchMenu
                        .BUTTON_SUPERHEATED_REDSTONE_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_SUPERHEATED_REDSTONE_UP,
                this.menu::getSuperheatedRedstone,
                ThermalRelaySwitchBlockEntity
                        .MAX_SUPERHEATED_REDSTONE,
                Section.CUSTOM_HEAT
        );

        addStepper(
                239,
                123,
                ThermalRelaySwitchMenu
                        .BUTTON_SUPERHEATED_GLOW_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_SUPERHEATED_GLOW_UP,
                this.menu::getSuperheatedGlow,
                ThermalRelaySwitchBlockEntity.MAX_GLOW,
                Section.CUSTOM_HEAT
        );

        addStepper(
                135,
                161,
                ThermalRelaySwitchMenu
                        .BUTTON_COMBUSTION_REDSTONE_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_COMBUSTION_REDSTONE_UP,
                this.menu::getCombustionRedstone,
                ThermalRelaySwitchBlockEntity
                        .MAX_COMBUSTION_REDSTONE,
                Section.CUSTOM_HEAT
        );

        addStepper(
                239,
                161,
                ThermalRelaySwitchMenu
                        .BUTTON_COMBUSTION_GLOW_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_COMBUSTION_GLOW_UP,
                this.menu::getCombustionGlow,
                ThermalRelaySwitchBlockEntity.MAX_GLOW,
                Section.CUSTOM_HEAT
        );
    }

    private void addLowFuelControls() {
        int scopeY = this.topPos + 80;

        this.lowHeatScopeButton = addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.sulfuricresonance."
                                                + "thermal_relay_switch."
                                                + "scope.low_heat"
                                ),
                                button -> sendButton(
                                        ThermalRelaySwitchMenu
                                                .BUTTON_SCOPE_LOW_HEAT
                                )
                        )
                        .bounds(
                                this.leftPos + 18,
                                scopeY,
                                96,
                                22
                        )
                        .build()
        );

        this.highHeatScopeButton = addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.sulfuricresonance."
                                                + "thermal_relay_switch."
                                                + "scope.high_heat"
                                ),
                                button -> sendButton(
                                        ThermalRelaySwitchMenu
                                                .BUTTON_SCOPE_HIGH_HEAT
                                )
                        )
                        .bounds(
                                this.leftPos + 124,
                                scopeY,
                                96,
                                22
                        )
                        .build()
        );

        this.bothScopeButton = addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "gui.sulfuricresonance."
                                                + "thermal_relay_switch."
                                                + "scope.both"
                                ),
                                button -> sendButton(
                                        ThermalRelaySwitchMenu
                                                .BUTTON_SCOPE_BOTH
                                )
                        )
                        .bounds(
                                this.leftPos + 230,
                                scopeY,
                                96,
                                22
                        )
                        .build()
        );

        addStepper(
                181,
                121,
                ThermalRelaySwitchMenu
                        .BUTTON_LOW_FUEL_REDSTONE_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_LOW_FUEL_REDSTONE_UP,
                this.menu::getLowFuelRedstone,
                15,
                Section.LOW_FUEL
        );

        addStepper(
                181,
                160,
                ThermalRelaySwitchMenu
                        .BUTTON_LOW_FUEL_GLOW_DOWN,
                ThermalRelaySwitchMenu
                        .BUTTON_LOW_FUEL_GLOW_UP,
                this.menu::getLowFuelGlow,
                ThermalRelaySwitchBlockEntity.MAX_GLOW,
                Section.LOW_FUEL
        );
    }

    private void addStepper(
            int localX,
            int localY,
            int downId,
            int upId,
            @NotNull IntSupplier value,
            int maximum,
            @NotNull Section section
    ) {
        Button minus = addRenderableWidget(
                Button.builder(
                                Component.literal("−"),
                                button -> sendButton(downId)
                        )
                        .bounds(
                                this.leftPos + localX,
                                this.topPos + localY,
                                22,
                                22
                        )
                        .build()
        );

        Button plus = addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                button -> sendButton(upId)
                        )
                        .bounds(
                                this.leftPos + localX + 66,
                                this.topPos + localY,
                                22,
                                22
                        )
                        .build()
        );

        this.steppers.add(
                new StepperBinding(
                        minus,
                        plus,
                        value,
                        0,
                        maximum,
                        section
                )
        );
    }

    private void selectSettingsPage(
            boolean lowFuelPage
    ) {
        this.showLowFuelPage = lowFuelPage;
        refreshWidgetState();
    }

    private void sendButton(int buttonId) {
        if (this.minecraft == null
                || this.minecraft.gameMode == null) {
            return;
        }

        this.minecraft.gameMode
                .handleInventoryButtonClick(
                        this.menu.containerId,
                        buttonId
                );
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshWidgetState();
    }

    private void refreshWidgetState() {
        boolean customHeat =
                !this.showLowFuelPage;

        this.customHeatModeButton.active = !customHeat;
        this.lowFuelModeButton.active = customHeat;

        for (StepperBinding stepper : this.steppers) {
            boolean visible = stepper.section()
                    == (
                    customHeat
                            ? Section.CUSTOM_HEAT
                            : Section.LOW_FUEL
            );

            stepper.minus().visible = visible;
            stepper.plus().visible = visible;

            int value = stepper.value().getAsInt();

            stepper.minus().active =
                    visible && value > stepper.minimum();

            stepper.plus().active =
                    visible && value < stepper.maximum();
        }

        boolean lowFuel = !customHeat;

        this.lowHeatScopeButton.visible = lowFuel;
        this.highHeatScopeButton.visible = lowFuel;
        this.bothScopeButton.visible = lowFuel;

        ThermalRelaySwitchBlockEntity.LowFuelScope scope =
                this.menu.getLowFuelScope();

        this.lowHeatScopeButton.active =
                lowFuel
                        && scope
                        != ThermalRelaySwitchBlockEntity
                        .LowFuelScope.LOW_HEAT;

        this.highHeatScopeButton.active =
                lowFuel
                        && scope
                        != ThermalRelaySwitchBlockEntity
                        .LowFuelScope.HIGH_HEAT;

        this.bothScopeButton.active =
                lowFuel
                        && scope
                        != ThermalRelaySwitchBlockEntity
                        .LowFuelScope.BOTH;
    }

    @Override
    public void render(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(graphics, mouseX, mouseY);
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

        graphics.fill(
                left,
                top,
                right,
                bottom,
                BORDER_DARK
        );

        graphics.fill(
                left + 2,
                top + 2,
                right - 2,
                bottom - 2,
                BORDER
        );

        graphics.fill(
                left + 4,
                top + 4,
                right - 4,
                bottom - 4,
                PANEL
        );


        graphics.fill(
                left + 12,
                top + 54,
                right - 12,
                top + 56,
                ACCENT_SOFT
        );

        boolean customHeat =
                !this.showLowFuelPage;

        if (customHeat) {
            graphics.fill(
                    left + 12,
                    top + 53,
                    left + 168,
                    top + 56,
                    ACCENT
            );
        } else {
            graphics.fill(
                    left + 176,
                    top + 53,
                    left + 332,
                    top + 56,
                    ACCENT
            );
        }

        graphics.fill(
                left + 12,
                top + 61,
                right - 12,
                top + 196,
                PANEL_INNER
        );

        if (customHeat) {
            drawCustomHeatCards(graphics);
        } else {
            drawLowFuelCards(graphics);
        }

        graphics.fill(
                left + 12,
                top + 202,
                right - 12,
                top + 247,
                STATUS
        );

        graphics.fill(
                left + 12,
                top + 202,
                right - 12,
                top + 204,
                ACCENT_SOFT
        );
    }

    private void drawCustomHeatCards(
            @NotNull GuiGraphics graphics
    ) {
        drawHeatCard(
                graphics,
                78,
                112,
                ThermalRelaySwitchBlockEntity
                        .HeatBand.HEATED,
                false
        );

        drawHeatCard(
                graphics,
                116,
                150,
                ThermalRelaySwitchBlockEntity
                        .HeatBand.SUPERHEATED,
                true
        );

        drawHeatCard(
                graphics,
                154,
                188,
                ThermalRelaySwitchBlockEntity
                        .HeatBand.COMBUSTION,
                false
        );
    }

    private void drawHeatCard(
            @NotNull GuiGraphics graphics,
            int localTop,
            int localBottom,
            @NotNull ThermalRelaySwitchBlockEntity.HeatBand band,
            boolean alternate
    ) {
        int left = this.leftPos + 16;
        int right = this.leftPos + this.imageWidth - 16;
        int top = this.topPos + localTop;
        int bottom = this.topPos + localBottom;

        graphics.fill(
                left,
                top,
                right,
                bottom,
                alternate ? CARD_ALT : CARD
        );

        if (this.menu.getCurrentHeatBand() == band) {
            graphics.fill(
                    left,
                    top,
                    left + 4,
                    bottom,
                    ACCENT
            );
        }
    }

    private void drawLowFuelCards(
            @NotNull GuiGraphics graphics
    ) {
        graphics.fill(
                this.leftPos + 16,
                this.topPos + 72,
                this.leftPos + this.imageWidth - 16,
                this.topPos + 107,
                CARD
        );

        graphics.fill(
                this.leftPos + 16,
                this.topPos + 114,
                this.leftPos + this.imageWidth - 16,
                this.topPos + 150,
                CARD_ALT
        );

        graphics.fill(
                this.leftPos + 16,
                this.topPos + 153,
                this.leftPos + this.imageWidth - 16,
                this.topPos + 189,
                CARD
        );

        if (this.menu.isLowFuelWarningActive()) {
            graphics.fill(
                    this.leftPos + 16,
                    this.topPos + 114,
                    this.leftPos + 20,
                    this.topPos + 189,
                    WARNING
            );
        }
    }

    @Override
    protected void renderLabels(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawCenteredString(
                this.font,
                this.title,
                this.imageWidth / 2,
                10,
                TEXT
        );

        if (!this.showLowFuelPage) {
            renderCustomHeatLabels(graphics);
        } else {
            renderLowFuelLabels(graphics);
        }

        renderStatus(graphics);
    }

    private void renderCustomHeatLabels(
            @NotNull GuiGraphics graphics
    ) {
        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "heat_tier"
                ),
                22,
                65,
                MUTED
        );

        drawCentered(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "redstone"
                ),
                179,
                65,
                REDSTONE
        );

        drawCentered(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "glow"
                ),
                283,
                65,
                GLOW
        );

        renderHeatRow(
                graphics,
                86,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "heated"
                ),
                this.menu.getHeatedRedstone(),
                ThermalRelaySwitchBlockEntity
                        .MAX_HEATED_REDSTONE,
                this.menu.getHeatedGlow()
        );

        renderHeatRow(
                graphics,
                124,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "superheated"
                ),
                this.menu.getSuperheatedRedstone(),
                ThermalRelaySwitchBlockEntity
                        .MAX_SUPERHEATED_REDSTONE,
                this.menu.getSuperheatedGlow()
        );

        renderHeatRow(
                graphics,
                162,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "combustion"
                ),
                this.menu.getCombustionRedstone(),
                ThermalRelaySwitchBlockEntity
                        .MAX_COMBUSTION_REDSTONE,
                this.menu.getCombustionGlow()
        );
    }

    private void renderHeatRow(
            @NotNull GuiGraphics graphics,
            int y,
            @NotNull Component name,
            int redstone,
            int redstoneMaximum,
            int glow
    ) {
        draw(
                graphics,
                name,
                24,
                y,
                TEXT
        );

        draw(
                graphics,
                Component.literal(
                        "0–" + redstoneMaximum
                ),
                24,
                y + 13,
                DIM
        );

        drawCentered(
                graphics,
                Component.literal(
                        Integer.toString(redstone)
                ),
                179,
                y + 5,
                REDSTONE
        );

        drawCentered(
                graphics,
                Component.literal(
                        glow + "  →  " + glowLight(glow)
                ),
                283,
                y + 5,
                GLOW
        );
    }

    private void renderLowFuelLabels(
            @NotNull GuiGraphics graphics
    ) {
        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "heat_scope"
                ),
                22,
                64,
                MUTED
        );

        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "warning_redstone"
                ),
                25,
                124,
                TEXT
        );

        draw(
                graphics,
                Component.literal("0–15"),
                25,
                137,
                DIM
        );

        drawCentered(
                graphics,
                Component.literal(
                        Integer.toString(
                                this.menu.getLowFuelRedstone()
                        )
                ),
                225,
                128,
                REDSTONE
        );

        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "warning_glow"
                ),
                25,
                163,
                TEXT
        );

        draw(
                graphics,
                Component.literal("Glow 0–5  →  Light 0–10"),
                25,
                176,
                DIM
        );

        int glow = this.menu.getLowFuelGlow();

        drawCentered(
                graphics,
                Component.literal(
                        glow + "  →  " + glowLight(glow)
                ),
                225,
                167,
                GLOW
        );

        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "fuel_threshold",
                        10
                ),
                20,
                191,
                MUTED
        );

        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "pulse_rate"
                ),
                182,
                191,
                MUTED
        );
    }

    private void renderStatus(
            @NotNull GuiGraphics graphics
    ) {
        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "linked",
                        this.menu.getLinkedCount()
                ),
                20,
                211,
                MUTED
        );

        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "hottest",
                        heatBandName(
                                this.menu.getCurrentHeatBand()
                        )
                ),
                117,
                211,
                MUTED
        );

        draw(
                graphics,
                Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "output",
                        this.menu.getCurrentPower(),
                        this.menu.getCurrentGlow()
                ),
                20,
                230,
                TEXT
        );

        Component fuelState =
                this.menu.isLowFuelWarningActive()
                        ? Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "fuel.warning"
                )
                        : Component.translatable(
                        "gui.sulfuricresonance."
                                + "thermal_relay_switch."
                                + "fuel.stable"
                );

        draw(
                graphics,
                fuelState,
                246,
                230,
                this.menu.isLowFuelWarningActive()
                        ? WARNING
                        : STABLE
        );
    }

    private Component heatBandName(
            @NotNull ThermalRelaySwitchBlockEntity.HeatBand band
    ) {
        String suffix = switch (band) {
            case UNHEATED -> "unheated";
            case HEATED -> "heated";
            case SUPERHEATED -> "superheated";
            case COMBUSTION -> "combustion";
        };

        return Component.translatable(
                "gui.sulfuricresonance."
                        + "thermal_relay_switch."
                        + suffix
        );
    }

    private static int glowLight(int glow) {
        return switch (glow) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 6;
            case 4 -> 8;
            case 5 -> 10;
            default -> 0;
        };
    }

    private void draw(
            @NotNull GuiGraphics graphics,
            @NotNull Component component,
            int x,
            int y,
            int color
    ) {
        graphics.drawString(
                this.font,
                component,
                x,
                y,
                color,
                false
        );
    }

    private void drawCentered(
            @NotNull GuiGraphics graphics,
            @NotNull Component component,
            int x,
            int y,
            int color
    ) {
        graphics.drawCenteredString(
                this.font,
                component,
                x,
                y,
                color
        );
    }

    private enum Section {
        CUSTOM_HEAT,
        LOW_FUEL
    }

    private record StepperBinding(
            Button minus,
            Button plus,
            IntSupplier value,
            int minimum,
            int maximum,
            Section section
    ) {
    }
}
