package io.hxneyw.repo.compat.jei;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * JEI Category for Combustion Mixing
 * Extends BasinCategory EXACTLY like Create's MixingCategory does
 */
@ParametersAreNonnullByDefault
public class CombustionMixingCategory extends BasinCategory {

    public static final RecipeType<RecipeHolder<BasinRecipe>> RECIPE_TYPE =
            RecipeType.create("sulfuricresonance", "combustion_mixing",
                    (Class<RecipeHolder<BasinRecipe>>)(Class<?>)RecipeHolder.class);

    private final AnimatedMixer mixer = new AnimatedMixer();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    public CombustionMixingCategory(Info<BasinRecipe> info) {
        super(info, true); // true = needs heating
    }

    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        // First, draw everything from BasinCategory (basin, slots, arrows, etc.)
        super.draw(recipe, iRecipeSlotsView, graphics, mouseX, mouseY);

        HeatCondition requiredHeat = recipe.getRequiredHeat();

        // Draw heater with SEETHING heat level (same as MixingCategory does)
        if (requiredHeat != HeatCondition.NONE) {
            heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
                    .draw(graphics, getBackground().getWidth() / 2 + 3, 55);
        }

        // Draw mixer (same position as Create)
        mixer.draw(graphics, getBackground().getWidth() / 2 + 3, 34);

        // Now override the heat text to say "Combustion" instead of "Super-Heated"
        if (requiredHeat != HeatCondition.NONE) {
            Minecraft mc = Minecraft.getInstance();

            // Cover the original "Super-Heated" text with background color
            int textX = 9;
            int textY = 86;
            int coverWidth = mc.font.width("Super-Heated") + 4;

            // Draw rectangle to cover old text (JEI background gray)
            graphics.fill(textX - 1, textY - 1, textX + coverWidth, textY + mc.font.lineHeight, 0xFFC6C6C6);

            // Draw "Combustion" in purple
            graphics.drawString(mc.font, "Combustion", textX, textY, 0xAA00FF, false);
        }
    }
}