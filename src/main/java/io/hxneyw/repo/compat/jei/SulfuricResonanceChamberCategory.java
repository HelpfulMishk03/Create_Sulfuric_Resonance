package io.hxneyw.repo.compat.jei;

import io.hxneyw.repo.content.recipes.sulfuricresonancechamber.SulfuricResonanceChamberRecipe;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModFluids;
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

    public static final RecipeType<SulfuricResonanceChamberRecipe>
            RECIPE_TYPE = RecipeType.create(
                    "sulfuricresonance",
                    "sulfuric_resonance_chamber",
                    SulfuricResonanceChamberRecipe.class
            );

    private static final int WIDTH = 190;
    private static final int HEIGHT = 136;
    private static final int ACID_CAPACITY = 1500;
    private static final int LABEL_COLOR = 0x555555;
    private static final int VALUE_COLOR = 0x303030;
    private static final int HEAT_COLOR = 0xE94B22;
    private static final int SPEED_COLOR = 0xC9A33E;
    private static final int ACID_COLOR = 0x8860B8;

    private final Component title = Component.translatable(
            "recipe.sulfuricresonance.sulfuric_resonance_chamber"
    );
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public SulfuricResonanceChamberCategory(
            IGuiHelper guiHelper
    ) {
        icon = guiHelper.createDrawableItemStack(
                new ItemStack(
                        AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
                )
        );
        slot = guiHelper.getSlotDrawable();
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public @NotNull RecipeType<SulfuricResonanceChamberRecipe>
    getRecipeType() {
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
        builder.addSlot(
                RecipeIngredientRole.INPUT,
                        8,
                        38
                )
                .setBackground(slot, -1, -1)
                .addItemStacks(List.of(recipe.substrate().getItems()));

        builder.addSlot(
                RecipeIngredientRole.INPUT,
                        8,
                        78
                )
                .setBackground(slot, -1, -1)
                .addFluidStack(
                        AllModFluids.SULFURIC_ACID.get(),
                        recipe.acidAmount()
                )
                .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(
                        Component.translatable(
                                "jei.sulfuricresonance.sulfuric_resonance_chamber.acid_amount",
                                recipe.acidAmount(),
                                ACID_CAPACITY
                        )
                ));

        builder.addSlot(
                RecipeIngredientRole.OUTPUT,
                        166,
                        48
                )
                .setBackground(slot, -1, -1)
                .addItemStack(recipe.result().copy());
    }

    @Override
    public void draw(
            SulfuricResonanceChamberRecipe recipe,
            @NotNull IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        graphics.fill(40, 17, 151, 82, 0x16000000);
        graphics.fill(40, 17, 151, 18, 0xFF8A6B2D);
        arrow.draw(graphics, 58, 42);
        arrow.draw(graphics, 132, 42);
        graphics.renderItem(
                new ItemStack(
                        AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
                ),
                102,
                31
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.reaction"
                ),
                58,
                64,
                VALUE_COLOR,
                false
        );

        drawRequirement(
                graphics,
                font,
                93,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.heat"
                ),
                Component.translatable(
                        recipe.minimumHeat()
                                == SulfuricResonanceChamberRecipe
                                .HeatRequirement.COMBUSTION
                                ? "jei.sulfuricresonance.sulfuric_resonance_chamber.combustion"
                                : "jei.sulfuricresonance.sulfuric_resonance_chamber.superheated"
                ),
                HEAT_COLOR
        );
        drawRequirement(
                graphics,
                font,
                108,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.speed"
                ),
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.rpm",
                        recipe.minimumSpeed()
                ),
                SPEED_COLOR
        );
        drawRequirement(
                graphics,
                font,
                123,
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.acid"
                ),
                Component.translatable(
                        "jei.sulfuricresonance.sulfuric_resonance_chamber.acid_amount",
                        recipe.acidAmount(),
                        ACID_CAPACITY
                ),
                ACID_COLOR
        );
    }

    private static void drawRequirement(
            GuiGraphics graphics,
            Font font,
            int y,
            Component label,
            Component value,
            int valueColor
    ) {
        graphics.drawString(font, label, 12, y, LABEL_COLOR, false);
        graphics.drawString(
                font,
                value,
                WIDTH - 12 - font.width(value),
                y,
                valueColor,
                false
        );
    }
}
