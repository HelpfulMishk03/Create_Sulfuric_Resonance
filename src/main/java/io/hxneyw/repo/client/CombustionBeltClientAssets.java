package io.hxneyw.repo.client;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import io.hxneyw.repo.content.blocks.combustionbelt.CombustionBeltAccessor;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side scrolling textures for marked Combustion Belts.
 * <p>
 * Create supplies the installed belt geometry. Sulfuric Resonance supplies
 * the replacement belt textures.
 */
public final class CombustionBeltClientAssets {

    public static final SpriteShiftEntry BELT =
            SpriteShifter.get(
                    createId("block/belt"),
                    modId("block/combustion_belt_scroll")
            );

    public static final SpriteShiftEntry BELT_OFFSET =
            SpriteShifter.get(
                    createId("block/belt_offset"),
                    modId("block/combustion_belt_scroll")
            );

    public static final SpriteShiftEntry BELT_DIAGONAL =
            SpriteShifter.get(
                    createId("block/belt_diagonal"),
                    modId("block/combustion_belt_diagonal_scroll")
            );

    private CombustionBeltClientAssets() {
    }

    public static void init() {
    }

    public static boolean isCombustionBelt(
            BeltBlockEntity belt
    ) {
        return belt instanceof CombustionBeltAccessor accessor
                && accessor.sulfuricresonance$isCombustionBelt();
    }

    public static SpriteShiftEntry getSpriteShift(
            boolean diagonal,
            boolean bottom
    ) {
        if (diagonal) {
            return BELT_DIAGONAL;
        }

        return bottom ? BELT_OFFSET : BELT;
    }

    private static ResourceLocation createId(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                "create",
                path
        );
    }

    private static ResourceLocation modId(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                "sulfuricresonance",
                path
        );
    }
}