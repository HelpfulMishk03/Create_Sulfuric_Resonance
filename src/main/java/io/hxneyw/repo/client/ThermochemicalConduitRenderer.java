package io.hxneyw.repo.client;

import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import io.hxneyw.repo.content.blocks.thermochemicalconduit.ThermochemicalConduitBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ThermochemicalConduitRenderer
        extends ShaftRenderer<ThermochemicalConduitBlockEntity> {

    public ThermochemicalConduitRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(context);
    }
}