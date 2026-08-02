package io.hxneyw.repo.content.registry;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;

public final class ModSpriteShifts {

    public static final CTSpriteShiftEntry ASHESIL =
            CTSpriteShifter.getCT(
                    AllCTTypes.OMNIDIRECTIONAL,
                    ResourceLocation.fromNamespaceAndPath(
                            "sulfuricresonance",
                            "block/ashesil"
                    ),
                    ResourceLocation.fromNamespaceAndPath(
                            "sulfuricresonance",
                            "block/ashesil_connected"
                    )
            );

    private ModSpriteShifts() {
    }
}