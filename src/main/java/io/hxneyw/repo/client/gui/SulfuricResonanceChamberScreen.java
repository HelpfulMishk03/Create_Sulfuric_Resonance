package io.hxneyw.repo.client.gui;

import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberBlockEntity;
import io.hxneyw.repo.content.blocks.sulfuricresonancechamber.SulfuricResonanceChamberMenu;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipe;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipeRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class SulfuricResonanceChamberScreen
        extends AbstractContainerScreen<SulfuricResonanceChamberMenu> {

    private static final int WIDTH = 304;
    private static final int HEIGHT = 244;
    private static final int AUTO_ROLL_TICKS = 150;

    private static final int BG = 0xFF191A1C;
    private static final int PANEL = 0xFF25272A;
    private static final int PANEL_INNER = 0xFF1E2023;
    private static final int BORDER = 0xFF5D6268;
    private static final int ACCENT = 0xFFD4AD3D;
    private static final int ACID = 0xFF8E62B7;
    private static final int TEXT = 0xFFE8E8E8;
    private static final int MUTED = 0xFFA8ACB2;
    private static final int GOOD = 0xFF7DCB77;
    private static final int WARN = 0xFFE2B95B;
    private static final int BAD = 0xFFE06B65;

    private static final int MACHINE_LEFT = 8;
    private static final int MACHINE_RIGHT = 150;
    private static final int RECIPE_LEFT = 154;
    private static final int RECIPE_RIGHT = 296;
    private static final int TOP_PANEL_TOP = 26;
    private static final int TOP_PANEL_BOTTOM = 142;
    private static final int INVENTORY_TOP = 148;
    private static final int INVENTORY_BOTTOM = 240;

    private static final int[] INPUT_X = {22, 46, 70};
    private static final int OUTPUT_X = 118;
    private static final int MACHINE_SLOT_Y = 50;

    private static final int PLAYER_X = 71;
    private static final int PLAYER_Y = 163;
    private static final int HOTBAR_Y = 221;

    private static final int RECIPE_CENTER = 225;
    private static final int RECIPE_INPUT_RIGHT_X = RECIPE_CENTER - 33;
    private static final int RECIPE_INPUT_SPACING = 18;
    private static final int RECIPE_OUTPUT_X = RECIPE_CENTER + 17;
    private static final int RECIPE_NAME_Y = 45;
    private static final int RECIPE_ITEM_Y = 58;

    private final List<RecipeHolder<SulfuricResonanceChamberRecipe>> recipes =
            new ArrayList<>();

    private int selectedRecipe;
    private int autoRollTimer;
    private Button previousButton;
    private Button nextButton;

    public SulfuricResonanceChamberScreen(
            SulfuricResonanceChamberMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        inventoryLabelX = PLAYER_X;
        inventoryLabelY = 152;
    }

    @Override
    protected void init() {
        super.init();
        refreshRecipes();

        previousButton = addRenderableWidget(
                Button.builder(
                                Component.literal("‹"),
                                button -> changeRecipe(-1)
                        )
                        .bounds(leftPos + RECIPE_LEFT + 8, topPos + 106, 16, 16)
                        .build()
        );

        nextButton = addRenderableWidget(
                Button.builder(
                                Component.literal("›"),
                                button -> changeRecipe(1)
                        )
                        .bounds(leftPos + RECIPE_RIGHT - 24, topPos + 106, 16, 16)
                        .build()
        );

        updateButtonState();
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (recipes.isEmpty()) {
            refreshRecipes();
            return;
        }

        int contextualRecipe = findContextualRecipe();
        if (contextualRecipe != -1) {
            selectedRecipe = contextualRecipe;
            autoRollTimer = 0;
            updateButtonState();
            return;
        }

        updateButtonState();
        if (++autoRollTimer >= AUTO_ROLL_TICKS) {
            autoRollTimer = 0;
            changeRecipe(1);
        }
    }

    private void refreshRecipes() {
        recipes.clear();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            updateButtonState();
            return;
        }

        recipes.addAll(
                level.getRecipeManager()
                        .getAllRecipesFor(
                                SulfuricResonanceChamberRecipeRegistry.TYPE.get()
                        )
        );

        selectedRecipe = Math.clamp(
                selectedRecipe,
                0,
                Math.max(0, recipes.size() - 1)
        );
        updateButtonState();
    }

    private void updateButtonState() {
        boolean multiple = recipes.size() > 1;
        boolean locked = findContextualRecipe() != -1;

        if (previousButton != null) {
            previousButton.active = multiple && !locked;
            previousButton.visible = !recipes.isEmpty();
        }
        if (nextButton != null) {
            nextButton.active = multiple && !locked;
            nextButton.visible = !recipes.isEmpty();
        }
    }

    private void changeRecipe(int direction) {
        if (recipes.isEmpty() || findContextualRecipe() != -1) {
            return;
        }

        selectedRecipe = Math.floorMod(
                selectedRecipe + direction,
                recipes.size()
        );
        autoRollTimer = 0;
    }

    private int findContextualRecipe() {
        ItemStack input1 = menu.getMachineStack(0);
        ItemStack input2 = menu.getMachineStack(1);
        ItemStack input3 = menu.getMachineStack(2);

        if (input1.isEmpty() && input2.isEmpty() && input3.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).value().matchesInputs(
                    input1,
                    input2,
                    input3
            )) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void render(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderRecipeTooltips(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
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

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, BG);
        drawBorder(graphics, x, y);

        graphics.fill(x + 5, y + 5, x + WIDTH - 5, y + 21, PANEL);
        graphics.fill(x + 5, y + 20, x + WIDTH - 5, y + 21, ACCENT);

        graphics.fill(
                x + MACHINE_LEFT,
                y + TOP_PANEL_TOP,
                x + MACHINE_RIGHT,
                y + TOP_PANEL_BOTTOM,
                PANEL
        );
        graphics.fill(
                x + MACHINE_LEFT + 2,
                y + TOP_PANEL_TOP + 2,
                x + MACHINE_RIGHT - 2,
                y + TOP_PANEL_BOTTOM - 2,
                PANEL_INNER
        );

        graphics.fill(
                x + RECIPE_LEFT,
                y + TOP_PANEL_TOP,
                x + RECIPE_RIGHT,
                y + TOP_PANEL_BOTTOM,
                PANEL
        );
        graphics.fill(
                x + RECIPE_LEFT + 2,
                y + TOP_PANEL_TOP + 2,
                x + RECIPE_RIGHT - 2,
                y + TOP_PANEL_BOTTOM - 2,
                PANEL_INNER
        );

        graphics.fill(
                x + 8,
                y + INVENTORY_TOP,
                x + WIDTH - 8,
                y + INVENTORY_BOTTOM,
                PANEL
        );
        graphics.fill(
                x + 10,
                y + INVENTORY_TOP + 2,
                x + WIDTH - 10,
                y + INVENTORY_BOTTOM - 2,
                PANEL_INNER
        );

        for (int input : INPUT_X) {
            drawSlotFrame(
                    graphics,
                    x + input - 1,
                    y + MACHINE_SLOT_Y - 1,
                    false
            );
        }
        drawSlotFrame(
                graphics,
                x + OUTPUT_X - 1,
                y + MACHINE_SLOT_Y - 1,
                true
        );

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotFrame(
                        graphics,
                        x + PLAYER_X - 1 + column * 18,
                        y + PLAYER_Y - 1 + row * 18,
                        false
                );
            }
        }

        for (int column = 0; column < 9; column++) {
            drawSlotFrame(
                    graphics,
                    x + PLAYER_X - 1 + column * 18,
                    y + HOTBAR_Y - 1,
                    false
            );
        }

        drawMachineMeters(graphics, x, y);
        drawRecipePanel(graphics, x, y);
    }

    @Override
    protected void renderLabels(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(font, title, 10, 9, TEXT, false);

        graphics.drawCenteredString(
                font,
                Component.translatable("gui.sulfuricresonance.chamber.inputs"),
                55,
                33,
                MUTED
        );

        graphics.drawCenteredString(
                font,
                Component.translatable("gui.sulfuricresonance.chamber.output"),
                OUTPUT_X + 8,
                33,
                MUTED
        );

        graphics.drawCenteredString(
                font,
                Component.translatable("gui.sulfuricresonance.chamber.recipes"),
                RECIPE_CENTER,
                33,
                MUTED
        );

        graphics.drawString(
                font,
                Component.translatable("container.inventory"),
                PLAYER_X,
                152,
                MUTED,
                false
        );
    }

    private void drawMachineMeters(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        SulfuricResonanceChamberBlockEntity.ChamberStatus status =
                menu.getStatus();
        int statusColor = statusColor(status);

        Component statusText =
                Component.translatable(status.translationKey());

        graphics.drawWordWrap(
                font,
                statusText,
                x + 14,
                y + 74,
                130,
                statusColor
        );

        if (menu.isProcessing() && menu.getProcessingTime() > 0) {
            float progress = Math.clamp(
                    menu.getProcessingTicks()
                            / (float) menu.getProcessingTime(),
                    0.0F,
                    1.0F
            );

            graphics.fill(
                    x + 14,
                    y + 91,
                    x + 144,
                    y + 94,
                    0xFF34373B
            );
            graphics.fill(
                    x + 14,
                    y + 91,
                    x + 14 + Math.round(130 * progress),
                    y + 94,
                    ACCENT
            );
        }

        int acidCapacity = Math.max(1, menu.getAcidCapacity());
        float acidFraction = Math.clamp(
                menu.getAcidAmount() / (float) acidCapacity,
                0.0F,
                1.0F
        );

        Component acidText = Component.translatable(
                "gui.sulfuricresonance.chamber.acid_compact",
                menu.getAcidAmount(),
                acidCapacity
        );
        graphics.drawString(font, acidText, x + 14, y + 99, MUTED, false);

        graphics.fill(
                x + 14,
                y + 110,
                x + 144,
                y + 117,
                0xFF34373B
        );
        graphics.fill(
                x + 15,
                y + 111,
                x + 15 + Math.round(128 * acidFraction),
                y + 116,
                ACID
        );

        Component heatLine = Component.translatable(
                "gui.sulfuricresonance.chamber.heat_readout",
                heatName(menu.getHeatRank()),
                menu.getTemperature()
        );
        graphics.drawString(
                font,
                heatLine,
                x + 14,
                y + 120,
                MUTED,
                false
        );

        Component rpmLine = Component.translatable(
                "gui.sulfuricresonance.chamber.rotation_readout",
                menu.getSpeed()
        );
        graphics.drawString(
                font,
                rpmLine,
                x + 14,
                y + 130,
                MUTED,
                false
        );
    }

    private void drawRecipePanel(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        if (recipes.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable(
                            "gui.sulfuricresonance.chamber.no_recipes"
                    ),
                    x + RECIPE_CENTER,
                    y + 69,
                    MUTED
            );
            return;
        }

        RecipeHolder<SulfuricResonanceChamberRecipe> holder =
                recipes.get(selectedRecipe);
        SulfuricResonanceChamberRecipe recipe = holder.value();

        String resultName = recipe.result().getHoverName().getString();
        if (font.width(resultName) > 132) {
            resultName =
                    font.plainSubstrByWidth(resultName, 124) + "…";
        }

        graphics.drawCenteredString(
                font,
                resultName,
                x + RECIPE_CENTER,
                y + RECIPE_NAME_Y,
                TEXT
        );

        int ingredientCount = 1
                + (recipe.catalyst().isPresent() ? 1 : 0)
                + (recipe.auxiliary().isPresent() ? 1 : 0);
        int firstIngredientX = recipeFirstIngredientX(ingredientCount);
        int ingredientIndex = 0;

        renderIngredient(
                graphics,
                recipe.substrate(),
                x + firstIngredientX,
                y + RECIPE_ITEM_Y
        );
        ingredientIndex++;

        if (recipe.catalyst().isPresent()) {
            renderIngredient(
                    graphics,
                    recipe.catalyst().get(),
                    x + firstIngredientX
                            + ingredientIndex * RECIPE_INPUT_SPACING,
                    y + RECIPE_ITEM_Y
            );
            ingredientIndex++;
        }

        if (recipe.auxiliary().isPresent()) {
            renderIngredient(
                    graphics,
                    recipe.auxiliary().get(),
                    x + firstIngredientX
                            + ingredientIndex * RECIPE_INPUT_SPACING,
                    y + RECIPE_ITEM_Y
            );
        }

        int arrowX = x + RECIPE_CENTER - (font.width("→") / 2);
        int arrowY = y + RECIPE_ITEM_Y + 4;

        graphics.drawString(
                font,
                "→",
                arrowX,
                arrowY,
                ACCENT,
                false
        );
        graphics.renderItem(
                recipe.result(),
                x + RECIPE_OUTPUT_X,
                y + RECIPE_ITEM_Y
        );
        graphics.renderItemDecorations(
                font,
                recipe.result(),
                x + RECIPE_OUTPUT_X,
                y + RECIPE_ITEM_Y
        );

        Component requirements = Component.translatable(
                "gui.sulfuricresonance.chamber.recipe_requirements",
                recipe.acidAmount(),
                heatRequirementName(recipe.minimumHeat()),
                recipe.minimumSpeed(),
                formatSeconds(recipe.processingTime())
        );

        graphics.drawWordWrap(
                font,
                requirements,
                x + RECIPE_LEFT + 8,
                y + 81,
                126,
                MUTED
        );

        Component count = Component.translatable(
                "gui.sulfuricresonance.chamber.recipe_count",
                selectedRecipe + 1,
                recipes.size()
        );
        graphics.drawCenteredString(
                font,
                count,
                x + RECIPE_CENTER,
                y + 111,
                MUTED
        );
    }

    private void renderIngredient(
            GuiGraphics graphics,
            Ingredient ingredient,
            int x,
            int y
    ) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) {
            return;
        }

        ItemStack stack =
                stacks[cyclingIngredientIndex(stacks.length)];
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
    }

    private void renderRecipeTooltips(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        if (recipes.isEmpty()) {
            return;
        }

        SulfuricResonanceChamberRecipe recipe =
                recipes.get(selectedRecipe).value();

        int x = leftPos;
        int y = topPos;

        ItemStack ingredient = hoveredIngredient(
                recipe,
                mouseX,
                mouseY,
                x,
                y
        );

        if (!ingredient.isEmpty()) {
            graphics.renderTooltip(font, ingredient, mouseX, mouseY);
            return;
        }

        if (inside(
                mouseX,
                mouseY,
                x + RECIPE_OUTPUT_X,
                y + RECIPE_ITEM_Y,
                16,
                16
        )) {
            graphics.renderTooltip(
                    font,
                    recipe.result(),
                    mouseX,
                    mouseY
            );
            return;
        }

        if (inside(mouseX, mouseY, x + 14, y + 110, 130, 7)) {
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            "gui.sulfuricresonance.chamber.acid_amount",
                            menu.getAcidAmount(),
                            menu.getAcidCapacity()
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private ItemStack hoveredIngredient(
            SulfuricResonanceChamberRecipe recipe,
            int mouseX,
            int mouseY,
            int x,
            int y
    ) {
        int ingredientCount = 1
                + (recipe.catalyst().isPresent() ? 1 : 0)
                + (recipe.auxiliary().isPresent() ? 1 : 0);
        int firstIngredientX = recipeFirstIngredientX(ingredientCount);
        int ingredientIndex = 0;

        if (inside(
                mouseX,
                mouseY,
                x + firstIngredientX,
                y + RECIPE_ITEM_Y,
                16,
                16
        )) {
            return displayStack(recipe.substrate());
        }
        ingredientIndex++;

        if (recipe.catalyst().isPresent()) {
            if (inside(
                    mouseX,
                    mouseY,
                    x + firstIngredientX
                            + ingredientIndex * RECIPE_INPUT_SPACING,
                    y + RECIPE_ITEM_Y,
                    16,
                    16
            )) {
                return displayStack(recipe.catalyst().get());
            }
            ingredientIndex++;
        }

        if (recipe.auxiliary().isPresent()
                && inside(
                        mouseX,
                        mouseY,
                        x + firstIngredientX
                                + ingredientIndex * RECIPE_INPUT_SPACING,
                        y + RECIPE_ITEM_Y,
                        16,
                        16
                )) {
            return displayStack(recipe.auxiliary().get());
        }

        return ItemStack.EMPTY;
    }

    private int recipeFirstIngredientX(int ingredientCount) {
        int count = Math.clamp(ingredientCount, 1, 3);
        return RECIPE_INPUT_RIGHT_X
                - (count - 1) * RECIPE_INPUT_SPACING;
    }

    private ItemStack displayStack(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) {
            return ItemStack.EMPTY;
        }

        return stacks[cyclingIngredientIndex(stacks.length)];
    }

    private int cyclingIngredientIndex(int stackCount) {
        if (stackCount <= 1) {
            return 0;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return 0;
        }

        return (int) ((level.getGameTime() / 20L) % stackCount);
    }

    private String heatName(int band) {
        String key = switch (band) {
            case 1 -> "gui.sulfuricresonance.chamber.heat.heated";
            case 2 -> "gui.sulfuricresonance.chamber.heat.superheated";
            case 3 -> "gui.sulfuricresonance.chamber.heat.combustion";
            default -> "gui.sulfuricresonance.chamber.heat.cold";
        };

        return Component.translatable(key).getString();
    }

    private String heatRequirementName(
            SulfuricResonanceChamberRecipe.HeatRequirement requirement
    ) {
        String key = switch (requirement) {
            case HEATED ->
                    "gui.sulfuricresonance.chamber.heat.heated";
            case SUPERHEATED ->
                    "gui.sulfuricresonance.chamber.heat.superheated";
            case COMBUSTION ->
                    "gui.sulfuricresonance.chamber.heat.combustion";
        };
        return Component.translatable(key).getString();
    }

    private String formatSeconds(int ticks) {
        float seconds = ticks / 20.0F;

        if (Math.abs(seconds - Math.round(seconds)) < 0.001F) {
            return Integer.toString(Math.round(seconds));
        }

        return String.format(java.util.Locale.ROOT, "%.1f", seconds);
    }

    private int statusColor(
            SulfuricResonanceChamberBlockEntity.ChamberStatus status
    ) {
        return switch (status) {
            case READY, PROCESSING -> GOOD;
            case IDLE -> MUTED;
            case MISSING_INGREDIENTS,
                 MISSING_ACID,
                 INSUFFICIENT_HEAT,
                 INSUFFICIENT_SPEED -> WARN;
            case OUTPUT_BLOCKED, NO_VALID_RECIPE -> BAD;
        };
    }

    private static void drawSlotFrame(
            GuiGraphics graphics,
            int x,
            int y,
            boolean output
    ) {
        int frame = output ? ACCENT : BORDER;
        graphics.fill(x, y, x + 18, y + 18, frame);
        graphics.fill(
                x + 1,
                y + 1,
                x + 17,
                y + 17,
                0xFF111214
        );
    }

    private static void drawBorder(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        graphics.fill(x, y, x + WIDTH, y + 1, BORDER);
        graphics.fill(
                x,
                y + HEIGHT - 1,
                x + WIDTH,
                y + HEIGHT,
                BORDER
        );
        graphics.fill(x, y, x + 1, y + HEIGHT, BORDER);
        graphics.fill(
                x + WIDTH - 1,
                y,
                x + WIDTH,
                y + HEIGHT,
                BORDER
        );
    }

    private static boolean inside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}
