package io.hxneyw.repo.compat.jade;

import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
@SuppressWarnings("unused")
@WailaPlugin
public class SulfuricResonanceJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            if (!(accessor instanceof BlockAccessor blockAccessor))
                return accessor;

            if (!(blockAccessor.getBlockEntity() instanceof CombustionBeltAccessor belt))
                return accessor;

            if (!belt.sulfuricresonance$isCombustionBelt())
                return accessor;

            ItemStack displayStack =
                    new ItemStack(io.hxneyw.repo.content.Items.COMBUSTION_BELT_CONNECTOR.get());

            displayStack.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("jade.sulfuricresonance.combustion_belt")
            );

            return registration.blockAccessor()
                    .from(blockAccessor)
                    .fakeBlock(displayStack)
                    .build();
        });
    }
}