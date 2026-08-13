package io.hxneyw.repo.compat.jei;

import io.hxneyw.repo.compat.jei.animations.AnimatedSulfuricResonanceChamber;
import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipe;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModFluids;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class SulfuricResonanceChamberCategory
        implements IRecipeCategory<SulfuricResonanceChamberRecipe> {

    public static final RecipeType<SulfuricResonanceChamberRecipe> RECIPE_TYPE =
            RecipeType.create(
                    "sulfuricresonance",
                    "sulfuric_resonance_chamber",
                    SulfuricResonanceChamberRecipe.class
            );

    private static final int WIDTH = 204;
    private static final int HEIGHT = 144;
    private static final int ACID_CAPACITY = 1500;
    private static final int LABEL_COLOR = 0x666666;
    private static final int VALUE_COLOR = 0x303030;
    private static final int HEAT_COLOR = 0xE94B22;
    private static final int SPEED_COLOR = 0xC9A33E;
    private static final int ACID_COLOR = 0x8860B8;
    private static final int TIME_COLOR = 0x5579C6;

    private final Component title = Component.translatable(
            "recipe.sulfuricresonance.sulfuric_resonance_chamber"
    );
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final AnimatedSulfuricResonanceChamber chamber =
            new AnimatedSulfuricResonanceChamber();

    public SulfuricResonanceChamberCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(
                new ItemStack(AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get())
        );
        slot = guiHelper.getSlotDrawable();
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public @NotNull RecipeType<SulfuricResonanceChamberRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            SulfuricResonanceChamberRecipe recipe,
            @NotNull IFocusGroup focuses
    ) {
        addItemInput(
                builder,
                recipe.substrate().getItems(),
                12,
                38,
                "jei.sulfuricresonance.sulfuric_resonance_chamber.substrate"
        );

        recipe.catalyst().ifPresent(ingredient -> addItemInput(
                builder,
                ingredient.getItems(),
                34,
                38,
                "jei.sulfuricresonance.sulfuric_resonance_chamber.catalyst"
        ));

        recipe.auxiliary().ifPresent(ingredient -> addItemInput(
                builder,
                ingredient.getItems(),
                56,
                38,
                "jei.sulfuricresonance.sulfuric_resonance_chamber.auxiliary"
        ));

        builder.addSlot(RecipeIngredientRole.INPUT, 34, 64)
                .setBackground(slot, -1, -1)
                .addFluidStack(AllModFluids.SULFURIC_ACID.get(), recipe.acidAmount())
                .addRichTooltipCallback((slotView, tooltip) -> {
                    tooltip.add(Component.translatable(
                            "jei.sulfuricresonance.sulfuric_resonance_chamber.acid_label"
                    ));
                    tooltip.add(Component.translatable(
                            "jei.sulfuricresonance.sulfuric_resonance_chamber.acid_amount",
                            recipe.acidAmount(),
                            ACID_CAPACITY
                    ));
                });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 174, 50)
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.result().copy())
                .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(
                        Component.translatable(
                                "jei.sulfuricresonance.sulfuric_resonance_chamber.output"
                        )
                ));
    }

    private void addItemInput(
            IRecipeLayoutBuilder builder,
            ItemStack[] stacks,
            int x,
            int y,
            String tooltipKey
    ) {
        builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .setBackground(slot, -1, -1)
                .addItemStacks(List.of(stacks))
                .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(
                        Component.translatable(tooltipKey)
                ));
    }

    @Override
    public void draw(
            SulfuricResonanceChamberRecipe recipe,
            @NotNull IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        Font font = Minecraft.getInstance().font;

        graphics.fill(6, 24, 198, 88, 0x11000000);
        graphics.fill(6, 24, 198, 25, 0xFF8A6B2D);
        graphics.fill(6, 94, 198, 140, 0x11000000);
        graphics.fill(6, 94, 198, 95, 0xFF8A6B2D);
        graphics.fill(101, 98, 102, 138, 0x22000000);
        graphics.fill(10, 117, 194, 118, 0x18000000);

        drawCenteredClamped(
                graphics,
                font,
                recipe.result().getHoverName().getString(),
                WIDTH / 2,
                7,
                WIDTH - 20,
                VALUE_COLOR
        );

        arrow.draw(graphics, 76, 49);
        chamber.draw(graphics, 119, 57);
        arrow.draw(graphics, 143, 49);

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

    private static Component heatValue(SulfuricResonanceChamberRecipe recipe) {
        return switch (recipe.minimumHeat()) {
            case HEATED -> Component.translatable(
                    "jei.sulfuricresonance.sulfuric_resonance_chamber.heated_short"
            );
            case COMBUSTION -> Component.translatable(
                    "jei.sulfuricresonance.sulfuric_resonance_chamber.combustion"
            );
            default -> Component.translatable(
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
            String text,
            int centerX,
            int y,
            int maxWidth,
            int color
    ) {
        if (font.width(text) > maxWidth) {
            int dots = font.width("...");
            text = font.plainSubstrByWidth(text, Math.max(0, maxWidth - dots)) + "...";
        }
        graphics.drawString(
                font,
                text,
                centerX - font.width(text) / 2,
                y,
                color,
                false
        );
    }
}
