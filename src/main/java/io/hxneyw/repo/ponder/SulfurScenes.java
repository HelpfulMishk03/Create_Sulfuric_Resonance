package io.hxneyw.repo.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.CreateSulfuricResonance;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class SulfurScenes {

    private static final ResourceLocation SULFUR_ID =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "sulfur"
            );

    private SulfurScenes() {
    }

    public static void compatibility(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "sulfur.compatibility",
                "Cross-Mod Sulfur Compatibility"
        );

        BlockPos sulfurPedestal = util.grid().at(1, 1, 2);
        BlockPos compatibilityPedestal = util.grid().at(3, 1, 2);

        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                util.select()
                        .position(sulfurPedestal)
                        .add(util.select().position(compatibilityPedestal)),
                Direction.DOWN
        );

        scene.idle(20);

        scene.world().createItemEntity(
                util.vector().centerOf(sulfurPedestal).add(0.0, 0.7, 0.0),
                Vec3.ZERO,
                new ItemStack(BuiltInRegistries.ITEM.get(SULFUR_ID))
        );

        scene.overlay()
                .showText(120)
                .text("Sulfuric Resonance accepts processed sulfur through common item tags")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().topOf(sulfurPedestal))
                .placeNearTarget();

        scene.idle(130);

        scene.world().createItemEntity(
                util.vector().centerOf(compatibilityPedestal).add(0.0, 0.7, 0.0),
                Vec3.ZERO,
                new ItemStack(BuiltInRegistries.ITEM.get(SULFUR_ID))
        );

        scene.overlay()
                .showText(130)
                .text("Compatible sulfur items use c:sulfur; compatible sulfur dusts use c:dusts/sulfur")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(compatibilityPedestal))
                .placeNearTarget();

        scene.idle(140);

        scene.overlay()
                .showText(125)
                .text("Properly tagged processed sulfur from other mods can replace Sulfuric Resonance Sulfur")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(compatibilityPedestal))
                .placeNearTarget();

        scene.idle(135);

        scene.overlay()
                .showText(120)
                .text("Raw ores, chunks, and storage blocks are not processed sulfur inputs")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(sulfurPedestal))
                .placeNearTarget();

        scene.idle(130);

        scene.overlay()
                .showText(130)
                .text("Materials using private or missing tags may require dedicated compatibility")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(compatibilityPedestal))
                .placeNearTarget();

        scene.idle(140);
        scene.markAsFinished();
    }
}