package io.hxneyw.repo.compat.emi;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.compat.emi.animations.EmiAnimatedCeramicCrucibleMixer;
import io.hxneyw.repo.compat.emi.animations.EmiAnimatedMoltenRotor;
import io.hxneyw.repo.content.Items;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public final class CombustionMixingEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 177;
    private static final int HEIGHT = 112;
    private final ResourceLocation id;
    private final BasinRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final EmiStack crucible;
    private final EmiStack mixer;
    private final EmiStack furnace;
    private final EmiStack soulFiredCake;
    private final EmiAnimatedCeramicCrucibleMixer mixerAnimation =
            new EmiAnimatedCeramicCrucibleMixer();
    private final EmiAnimatedMoltenRotor furnaceAnimation =
            new EmiAnimatedMoltenRotor();

    public CombustionMixingEmiRecipe(
            RecipeHolder<BasinRecipe> holder
    ) {
        this.id = holder.id();
        this.recipe = holder.value();
        this.inputs = createInputs(this.recipe);
        this.outputs = createOutputs(this.recipe);
        this.crucible =
                SulfuricResonanceEmiPlugin.CERAMIC_CRUCIBLE.copy();
        this.mixer =
                SulfuricResonanceEmiPlugin.MECHANICAL_MIXER.copy();
        this.furnace =
                SulfuricResonanceEmiPlugin.MOLTEN_ROTOR.copy();
        this.soulFiredCake = EmiStack.of(
                Items.SOUL_FIRED_BLAZE_CAKE.get()
        );
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SulfuricResonanceEmiPlugin.COMBUSTION_MIXING;
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
        List<EmiIngredient> catalysts = new ArrayList<>();
        catalysts.add(this.crucible);
        catalysts.add(this.mixer);

        if (!this.recipe.getRequiredHeat()
                .testBlazeBurner(HeatLevel.NONE)) {
            catalysts.add(this.furnace);
        }

        if (!this.recipe.getRequiredHeat()
                .testBlazeBurner(HeatLevel.KINDLED)) {
            catalysts.add(this.soulFiredCake);
        }

        return List.copyOf(catalysts);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.outputs;
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

        addInputWidgets(widgets);
        addOutputWidgets(widgets);
        addHeatWidgets(widgets);
    }

    private void addInputWidgets(WidgetHolder widgets) {
        List<Pair<Ingredient, MutableInt>> condensedIngredients =
                ItemHelper.condenseIngredients(
                        this.recipe.getIngredients()
                );

        int size = condensedIngredients.size()
                + this.recipe.getFluidIngredients().size();
        int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
        int index = 0;

        for (Pair<Ingredient, MutableInt> pair
                : condensedIngredients) {
            EmiIngredient ingredient = EmiIngredient.of(
                    pair.getFirst(),
                    pair.getSecond().getValue()
            );

            widgets.addSlot(
                    ingredient,
                    17 + xOffset + index % 3 * 19,
                    51 - index / 3 * 19
            );

            index++;
        }

        for (SizedFluidIngredient fluidIngredient
                : this.recipe.getFluidIngredients()) {
            EmiIngredient ingredient =
                    toEmiIngredient(fluidIngredient);

            int x = 17 + xOffset + index % 3 * 19;
            int y = 51 - index / 3 * 19;

            widgets.addTank(
                    ingredient,
                    x,
                    y,
                    18,
                    18,
                    Math.max(1000, fluidIngredient.amount())
            );

            index++;
        }
    }

    private void addOutputWidgets(WidgetHolder widgets) {
        int size = this.recipe.getRollableResults().size()
                + this.recipe.getFluidResults().size();
        int index = 0;

        for (ProcessingOutput result
                : this.recipe.getRollableResults()) {
            int x = outputX(size, index);
            int y = 51 - 19 * (index / 2);

            EmiStack stack = EmiStack.of(result.getStack())
                    .setChance(result.getChance());

            widgets.addSlot(stack, x, y)
                    .recipeContext(this);

            index++;
        }

        for (FluidStack fluidResult
                : this.recipe.getFluidResults()) {
            int x = outputX(size, index);
            int y = 51 - 19 * (index / 2);
            EmiStack stack = toEmiStack(fluidResult);

            widgets.addTank(
                    stack,
                    x,
                    y,
                    18,
                    18,
                    Math.max(1000, fluidResult.getAmount())
            );

            index++;
        }
    }

    private void addHeatWidgets(WidgetHolder widgets) {
        HeatCondition requiredHeat =
                this.recipe.getRequiredHeat();

        if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
            widgets.addSlot(this.furnace, 134, 81)
                    .catalyst(true);
        }

        if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
            widgets.addSlot(this.soulFiredCake, 153, 81)
                    .catalyst(true);
        }
    }

    private void drawScreen(GuiGraphics graphics) {
        HeatCondition requiredHeat = this.recipe.getRequiredHeat();
        boolean noHeat = requiredHeat == HeatCondition.NONE;
        int verticalRows = (
                1
                        + this.recipe.getFluidResults().size()
                        + this.recipe.getRollableResults().size()
        ) / 2;
        int centerX = WIDTH / 2 + 3;

        if (verticalRows <= 2) {
            AllGuiTextures.JEI_DOWN_ARROW.render(
                    graphics,
                    136,
                    -19 * (verticalRows - 1) + 32
            );
        }

        AllGuiTextures shadow = noHeat
                ? AllGuiTextures.JEI_SHADOW
                : AllGuiTextures.JEI_LIGHT;
        shadow.render(graphics, 81, 58 + (noHeat ? 10 : 30));

        if (!noHeat) {
            this.furnaceAnimation
                    .withHeat(requiredHeat.visualizeAsBlazeBurner())
                    .draw(graphics, centerX, 55);
        }

        this.mixerAnimation.draw(graphics, centerX, 34);

        AllGuiTextures heatBar = noHeat
                ? AllGuiTextures.JEI_NO_HEAT_BAR
                : AllGuiTextures.JEI_HEAT_BAR;
        heatBar.render(graphics, 4, 80);

        Component heatText;
        int heatColor;

        if (requiredHeat == HeatCondition.SUPERHEATED) {
            heatText = Component.translatable(
                    "create.recipe.heat_requirement.radiant"
            );
            heatColor = 11141375;
        } else {
            heatText = CreateLang.translateDirect(
                    requiredHeat.getTranslationKey()
            );
            heatColor = requiredHeat.getColor();
        }

        Font font = Minecraft.getInstance().font;
        graphics.drawString(
                font,
                heatText,
                9,
                86,
                heatColor,
                false
        );
    }

    private static List<EmiIngredient> createInputs(
            BasinRecipe recipe
    ) {
        List<EmiIngredient> inputs = new ArrayList<>();
        List<Pair<Ingredient, MutableInt>> condensedIngredients =
                ItemHelper.condenseIngredients(
                        recipe.getIngredients()
                );

        for (Pair<Ingredient, MutableInt> pair
                : condensedIngredients) {
            inputs.add(
                    EmiIngredient.of(
                            pair.getFirst(),
                            pair.getSecond().getValue()
                    )
            );
        }

        for (SizedFluidIngredient ingredient
                : recipe.getFluidIngredients()) {
            inputs.add(toEmiIngredient(ingredient));
        }

        return List.copyOf(inputs);
    }

    private static List<EmiStack> createOutputs(
            BasinRecipe recipe
    ) {
        List<EmiStack> outputs = new ArrayList<>();

        for (ProcessingOutput result
                : recipe.getRollableResults()) {
            outputs.add(
                    EmiStack.of(result.getStack())
                            .setChance(result.getChance())
            );
        }

        for (FluidStack result : recipe.getFluidResults()) {
            outputs.add(toEmiStack(result));
        }

        return List.copyOf(outputs);
    }

    private static EmiIngredient toEmiIngredient(
            SizedFluidIngredient ingredient
    ) {
        List<EmiStack> stacks = Arrays.stream(
                        ingredient.ingredient().getStacks()
                )
                .map(stack -> EmiStack.of(
                        stack.getFluid(),
                        stack.getComponentsPatch(),
                        ingredient.amount()
                ))
                .toList();

        return EmiIngredient.of(
                stacks,
                ingredient.amount()
        );
    }

    private static EmiStack toEmiStack(
            FluidStack stack
    ) {
        return EmiStack.of(
                stack.getFluid(),
                stack.getComponentsPatch(),
                stack.getAmount()
        );
    }

    private static int outputX(int size, int index) {
        return 142 - (
                size % 2 != 0 && index == size - 1
                        ? 0
                        : index % 2 == 0 ? 10 : -9
        );
    }
}
