package io.hxneyw.repo.compat.emi.animations;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

abstract class EmiAnimatedKinetics {

    public abstract void draw(
            GuiGraphics graphics,
            int xOffset,
            int yOffset
    );

    private static final ILightingSettings DEFAULT_LIGHTING =
            CustomLightingSettings.builder()
                    .firstLightRotation(12.5F, -45.0F)
                    .secondLightRotation(-20.0F, -50.0F)
                    .build();

    protected static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4.0F) % 360.0F;
    }

    protected BlockState shaft(Axis axis) {
        return AllBlocks.SHAFT.getDefaultState()
                .setValue(BlockStateProperties.AXIS, axis);
    }

    protected PartialModel cogwheel() {
        return AllPartialModels.SHAFTLESS_COGWHEEL;
    }

    protected GuiGameElement.GuiRenderBuilder blockElement(
            BlockState state
    ) {
        return GuiGameElement.of(state).lighting(DEFAULT_LIGHTING);
    }

    protected GuiGameElement.GuiRenderBuilder blockElement(
            PartialModel partial
    ) {
        return GuiGameElement.of(partial).lighting(DEFAULT_LIGHTING);
    }
}
