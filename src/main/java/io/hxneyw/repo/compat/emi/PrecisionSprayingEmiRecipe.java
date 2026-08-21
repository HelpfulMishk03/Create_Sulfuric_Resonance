package io.hxneyw.repo.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.recipes.precisionspraying.PrecisionSprayingDisplay;
import io.hxneyw.repo.content.registry.AllModFluids;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class PrecisionSprayingEmiRecipe implements EmiRecipe {

    private static final int WIDTH = 188;
    private static final int HEIGHT = 96;
    private static final int TEXT = 0xFFFFFF;
    private static final int MUTED = 0xD0D0D0;
    private static final int ACID_TEXT = 0x686868;
    private final ResourceLocation id;
    private final PrecisionSprayingDisplay display;
    private final EmiStack input;
    private final EmiStack acid;
    private final EmiStack output;

    public PrecisionSprayingEmiRecipe(PrecisionSprayingDisplay display) {
        this.display = display;
        ResourceLocation inputId = BuiltInRegistries.ITEM.getKey(
                display.input().getItem()
        );
        this.id = ResourceLocation.fromNamespaceAndPath(
                CreateSulfuricResonance.MODID,
                "precision_spraying/"
                        + inputId.getNamespace()
                        + "/"
                        + inputId.getPath()
        );
        this.input = EmiStack.of(display.input().copy());
        this.acid = EmiStack.of(
                AllModFluids.SULFURIC_ACID.get(),
                display.fluidAmount()
        );
        this.output = EmiStack.of(display.output().copy());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SulfuricResonanceEmiPlugin.PRECISION_SPRAYING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(this.input, this.acid);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(
                SulfuricResonanceEmiPlugin.PRECISION_SPRITZER,
                this.input
        );
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
                (graphics, mouseX, mouseY, delta) -> this.draw(graphics)
        );
        widgets.addSlot(this.input, 12, 31)
                .appendTooltip(Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.target"
                ));
        widgets.addSlot(this.input, 44, 31)
                .catalyst(true)
                .appendTooltip(Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.filter_field"
                ));
        widgets.addTank(
                        this.acid,
                        76,
                        31,
                        18,
                        18,
                        this.display.fluidAmount()
                )
                .appendTooltip(Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.acid",
                        this.display.fluidAmount()
                ));
        widgets.addSlot(this.output, 158, 31)
                .recipeContext(this);
    }

    private void draw(GuiGraphics graphics) {
        Font font = Minecraft.getInstance().font;
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.fields"
                ),
                53,
                18,
                MUTED
        );
        graphics.drawString(
                font,
                Component.literal("→"),
                122,
                36,
                TEXT,
                false
        );
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.total_acid",
                        this.display.fluidAmount()
                ),
                84,
                55,
                ACID_TEXT
        );
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.stage"
                ),
                WIDTH / 2,
                68,
                TEXT
        );
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "jei.sulfuricresonance.precision_spraying.filter"
                ),
                WIDTH / 2,
                82,
                MUTED
        );
    }
}
