package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.simibubi.create.AllBlocks;

public final class SulfuricChamberMechanicalCraftingEmiRecipe
        implements EmiRecipe {

    private static final List<EmiIngredient> INPUTS = List.of(
            casing(), temperedAshesil(), temperedAshesil(), temperedAshesil(), casing(),
            casing(), temperedAshesil(), temperedAshesil(), temperedAshesil(), casing(),
            shaft(), casing(), flameborneCore(), casing(), mechanicalPump(),
            casing(), acidCeramic(), acidCeramic(), acidCeramic(), casing(),
            acidCeramic(), acidCeramic(), acidCeramic(), acidCeramic(), acidCeramic()
    );

    private static final EmiStack OUTPUT = EmiStack.of(
            AllModBlocks.SULFURIC_RESONANCE_CHAMBER.get()
    );

    private static final EmiStack MECHANICAL_CRAFTER = EmiStack.of(
            AllBlocks.MECHANICAL_CRAFTER.get()
    );

    @Override
    public EmiRecipeCategory getCategory() {
        return SulfuricResonanceEmiPlugin.SULFURIC_CHAMBER_CRAFTING;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(
                "sulfuricresonance",
                "sulfuric_chamber_mechanical"
        );
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return INPUTS;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(MECHANICAL_CRAFTER);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(OUTPUT);
    }

    @Override
    public int getDisplayWidth() {
        return 164;
    }

    @Override
    public int getDisplayHeight() {
        return 98;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(),
                (graphics, mouseX, mouseY, delta) -> drawArrow(graphics));

        for (int index = 0; index < INPUTS.size(); index++) {
            widgets.addSlot(INPUTS.get(index), (index % 5) * 18,
                    (index / 5) * 18);
        }

        widgets.addSlot(OUTPUT, 136, 36).large(true).recipeContext(this);
    }

    private static void drawArrow(GuiGraphics graphics) {
        int color = 0xFF777777;
        graphics.fill(101, 47, 122, 49, color);
        graphics.fill(117, 42, 120, 54, color);
        graphics.fill(120, 44, 123, 52, color);
        graphics.fill(123, 46, 126, 50, color);
    }

    private static EmiIngredient casing() {
        return EmiStack.of(Items.THERMOCHEMICAL_CASING.get());
    }

    private static EmiIngredient temperedAshesil() {
        return EmiStack.of(AllModBlocks.TEMPERED_ASHESIL.get());
    }

    private static EmiIngredient shaft() {
        return EmiStack.of(AllModBlocks.THERMOCHEMICAL_SHAFT.get());
    }

    private static EmiIngredient flameborneCore() {
        return EmiStack.of(Items.FLAMEBORNE_CORE.get());
    }

    private static EmiIngredient mechanicalPump() {
        return EmiStack.of(AllBlocks.MECHANICAL_PUMP.get());
    }

    private static EmiIngredient acidCeramic() {
        return EmiStack.of(Items.ACID_RESISTANT_CERAMIC.get());
    }
}
