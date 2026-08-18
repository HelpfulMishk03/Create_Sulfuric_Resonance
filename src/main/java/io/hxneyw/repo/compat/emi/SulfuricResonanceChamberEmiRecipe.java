package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.compat.emi.animations.EmiAnimatedSulfuricResonanceChamber;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipe;
import io.hxneyw.repo.content.registry.AllModFluids;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public final class SulfuricResonanceChamberEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 204;
    private static final int HEIGHT = 144;
    private static final int ACID_CAPACITY = 1500;
    private static final int LABEL_COLOR = 0x666666;
    private static final int VALUE_COLOR = 0x303030;
    private static final int HEAT_COLOR = 0xE94B22;
    private static final int SPEED_COLOR = 0xC9A33E;
    private static final int ACID_COLOR = 0x8860B8;
    private static final int TIME_COLOR = 0x5579C6;
    private static final int PANEL_COLOR = 0x11000000;
    private static final int DIVIDER_COLOR = 0xFF8A6B2D;
    private static final int CHAMBER_SCENE_X = 125;
    private static final int CHAMBER_SCENE_Y = 39;

    private final ResourceLocation id;
    private final SulfuricResonanceChamberRecipe recipe;
    private final EmiIngredient substrate;
    private final EmiIngredient catalyst;
    private final EmiIngredient auxiliary;
    private final EmiStack acid;
    private final EmiStack output;
    private final EmiStack chamber;
    private final List<EmiIngredient> inputs;
    private final EmiAnimatedSulfuricResonanceChamber chamberAnimation =
            new EmiAnimatedSulfuricResonanceChamber();

    public SulfuricResonanceChamberEmiRecipe(
            RecipeHolder<SulfuricResonanceChamberRecipe> holder
    ) {
        this.id = holder.id();
        this.recipe = holder.value();
        this.substrate = EmiIngredient.of(this.recipe.substrate());
        this.catalyst = this.recipe.catalyst()
                .map(EmiIngredient::of)
                .orElse(EmiStack.EMPTY);
        this.auxiliary = this.recipe.auxiliary()
                .map(EmiIngredient::of)
                .orElse(EmiStack.EMPTY);
        this.acid = EmiStack.of(
                AllModFluids.SULFURIC_ACID.get(),
                this.recipe.acidAmount()
        );
        this.output = EmiStack.of(this.recipe.result().copy());
        this.chamber = SulfuricResonanceEmiPlugin
                .SULFURIC_RESONANCE_CHAMBER.copy();

        List<EmiIngredient> recipeInputs = new ArrayList<>();
        recipeInputs.add(this.substrate);
        if (!this.catalyst.isEmpty()) {
            recipeInputs.add(this.catalyst);
        }
        if (!this.auxiliary.isEmpty()) {
            recipeInputs.add(this.auxiliary);
        }
        recipeInputs.add(this.acid);
        this.inputs = List.copyOf(recipeInputs);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SulfuricResonanceEmiPlugin
                .SULFURIC_RESONANCE_CHAMBER_PROCESSING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(this.chamber);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(this.output);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(
                0,
                0,
                WIDTH,
                HEIGHT,
                (graphics, mouseX, mouseY, delta) ->
                        drawScreen(graphics)
        );

        widgets.addSlot(this.substrate, 12, 37)
                .appendTooltip(Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.substrate"
                ));

        addOptionalSlot(
                widgets,
                this.catalyst,
                37,
                "jei.sulfuricresonance.sulfuric_resonance_chamber.catalyst"
        );
        addOptionalSlot(
                widgets,
                this.auxiliary,
                59,
                "jei.sulfuricresonance.sulfuric_resonance_chamber.auxiliary"
        );

        widgets.addTank(
                        this.acid,
                        12,
                        59,
                        18,
                        18,
                        ACID_CAPACITY
                )
                .appendTooltip(Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.acid_amount",
                        this.recipe.acidAmount(),
                        ACID_CAPACITY
                ));

        widgets.addSlot(this.output, 174, 50)
                .recipeContext(this)
                .appendTooltip(Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.output"
                ));
    }

    private static void addOptionalSlot(
            WidgetHolder widgets,
            EmiIngredient ingredient,
            int y,
            String labelKey
    ) {
        if (ingredient.isEmpty()) {
            widgets.addSlot(34, y)
                    .appendTooltip(Component.translatable(labelKey))
                    .appendTooltip(Component.translatable(
                            "jei.sulfuricresonance.sulfuric_resonance_chamber.not_used"
                    ));
            return;
        }

        widgets.addSlot(ingredient, 34, y)
                .appendTooltip(Component.translatable(labelKey));
    }

    private void drawScreen(GuiGraphics graphics) {
        SulfuricResonanceChamberRecipe recipe = this.recipe;
        Font font = Minecraft.getInstance().font;

        graphics.fill(6, 24, 198, 88, PANEL_COLOR);
        graphics.fill(6, 24, 198, 25, DIVIDER_COLOR);
        graphics.fill(6, 94, 198, 140, PANEL_COLOR);
        graphics.fill(6, 94, 198, 95, DIVIDER_COLOR);
        graphics.fill(101, 98, 102, 138, 0x22000000);
        graphics.fill(10, 117, 194, 118, 0x18000000);

        drawCenteredClamped(
                graphics,
                font,
                recipe.result().getHoverName().getString()
        );

        graphics.drawString(
                font,
                Component.literal("→"),
                76,
                54,
                VALUE_COLOR,
                false
        );
        graphics.drawString(
                font,
                Component.literal("→"),
                143,
                54,
                VALUE_COLOR,
                false
        );

        this.chamberAnimation.draw(
                graphics,
                CHAMBER_SCENE_X,
                CHAMBER_SCENE_Y
        );

        drawConditionCell(
                graphics,
                font,
                12,
                99,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.heat"
                ),
                heatValue(recipe),
                HEAT_COLOR
        );
        drawConditionCell(
                graphics,
                font,
                106,
                99,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.speed"
                ),
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.rpm",
                        recipe.minimumSpeed()
                ),
                SPEED_COLOR
        );
        drawConditionCell(
                graphics,
                font,
                12,
                120,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.acid_label"
                ),
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.amount_mb",
                        recipe.acidAmount()
                ),
                ACID_COLOR
        );
        drawConditionCell(
                graphics,
                font,
                106,
                120,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.time"
                ),
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.seconds",
                        seconds(recipe.processingTime())
                ),
                TIME_COLOR
        );
    }

    private static Component heatValue(
            SulfuricResonanceChamberRecipe recipe
    ) {
        return switch (recipe.minimumHeat()) {
            case HEATED -> Component.translatable(
                    "jei.sulfuricresonance.sulfuric_resonance_chamber.heated_short"
            );
            case COMBUSTION -> Component.translatable(
                    "jei.sulfuricresonance.sulfuric_resonance_chamber.combustion"
            );
            case SUPERHEATED -> Component.translatable(
                    "jei.sulfuricresonance.sulfuric_resonance_chamber.superheated_short"
            );
        };
    }

    private static String seconds(int ticks) {
        return BigDecimal.valueOf(ticks)
                .divide(BigDecimal.valueOf(20), 1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static void drawConditionCell(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            Component label,
            Component value,
            int valueColor
    ) {
        graphics.drawString(font, label, x, y, LABEL_COLOR, false);
        graphics.drawString(font, value, x, y + 9, valueColor, false);
    }

    private static void drawCenteredClamped(
            GuiGraphics graphics,
            Font font,
            String text
    ) {
        if (font.width(text) > 184) {
            int dots = font.width("...");
            text = font.plainSubstrByWidth(
                    text,
                    Math.max(0, 184 - dots)
            ) + "...";
        }
        graphics.drawString(
                font,
                text,
                102 - font.width(text) / 2,
                7,
                VALUE_COLOR,
                false
        );
    }
}
