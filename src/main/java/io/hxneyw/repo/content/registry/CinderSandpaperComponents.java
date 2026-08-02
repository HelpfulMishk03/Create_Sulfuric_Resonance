package io.hxneyw.repo.content.registry;

import io.hxneyw.repo.content.Items;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

public final class CinderSandpaperComponents {

    private CinderSandpaperComponents() {
    }

    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(
                Items.CINDER_SANDPAPER.get(),
                builder -> {
                    builder.set(DataComponents.MAX_STACK_SIZE, 1);
                    builder.set(DataComponents.MAX_DAMAGE, 16);
                    builder.set(DataComponents.DAMAGE, 0);
                }
        );
    }
}