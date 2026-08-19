package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.sulfurburner.SulfurBurnerBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class SulfurScenes {

    private static final ResourceLocation SULFUR_ID =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "sulfur"
            );

    private SulfurScenes() {
    }

    public static void burner(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "sulfur_burner.operation",
                "Burning Sulfur for Direct Heat"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9F);
        scene.setSceneOffsetY(-0.5F);

        BlockPos burnerPos = util.grid().at(2, 1, 2);
        BlockPos heatedPos = burnerPos.above();
        Selection burnerSelection = util.select().position(burnerPos);
        Selection heatedSelection = util.select().position(heatedPos);
        Vec3 burnerCenter = util.vector().centerOf(burnerPos);
        Vec3 burnerTop = util.vector().topOf(burnerPos);
        Block ashCeramicCrucible = Items.ASH_CERAMIC_CRUCIBLE_ITEM.get().getBlock();

        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                burnerSelection,
                Direction.DOWN
        );
        scene.idle(20);

        scene.overlay()
                .showText(105)
                .text(
                        "Sulfur Burners provide compact, direct heat"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(burnerCenter)
                .placeNearTarget();
        scene.idle(115);

        scene.overlay()
                .showControls(
                        burnerTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(new ItemStack(Items.SULFUR.get()));
        scene.idle(55);

        scene.overlay()
                .showControls(
                        burnerTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(new ItemStack(Items.SULFUR_BLOCK.get()));
        scene.idle(55);

        scene.overlay()
                .showControls(
                        burnerTop.add(0.0, 0.35, 0.0),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(new ItemStack(Items.SULFUR_FUEL_BRIQUETTE.get()));
        scene.idle(55);

        scene.overlay()
                .showText(110)
                .text(
                        "Burn sulfur-based items: Sulfur, Sulfur Blocks, or Sulfur Fuel Briquettes"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(burnerCenter)
                .placeNearTarget();
        scene.idle(120);

        scene.overlay()
                .showText(95)
                .text(
                        "Extra valid fuel is queued automatically, up to fifteen items"
                )
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(burnerCenter)
                .placeNearTarget();
        scene.idle(105);

        scene.world().modifyBlock(
                burnerPos,
                state -> state.setValue(
                        SulfurBurnerBlock.HEAT_LEVEL,
                        com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED
                ),
                false
        );
        scene.overlay()
                .showText(95)
                .text("Starting valid fuel makes the Burner Heated immediately")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(burnerTop)
                .placeNearTarget();
        emitBurnerFlames(scene, util, burnerPos, 2, 4);
        scene.idle(105);

        scene.overlay()
                .showText(100)
                .text("It remains Heated during a 100-tick, five-second warmup")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(burnerTop)
                .placeNearTarget();
        emitBurnerFlames(scene, util, burnerPos, 2, 4);
        scene.idle(110);

        scene.world().modifyBlock(
                burnerPos,
                state -> state.setValue(
                        SulfurBurnerBlock.HEAT_LEVEL,
                        com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SEETHING
                ),
                false
        );
        scene.overlay()
                .showText(95)
                .text("After that warmup, the Burner reaches Superheated")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(burnerTop)
                .placeNearTarget();
        emitBurnerFlames(scene, util, burnerPos, 3, 5);
        scene.effects().indicateSuccess(burnerPos);
        scene.idle(105);

        scene.world().setBlocks(
                heatedSelection,
                AllBlocks.BASIN.getDefaultState(),
                false
        );
        scene.world().showSection(
                heatedSelection,
                Direction.DOWN
        );
        scene.idle(20);

        scene.overlay()
                .showText(120)
                .text(
                        "Heat is applied only to the block directly above the Burner"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(heatedPos))
                .placeNearTarget();
        scene.effects().indicateSuccess(heatedPos);
        scene.idle(130);

        scene.world().hideSection(
                heatedSelection,
                Direction.UP
        );
        scene.idle(10);

        scene.world().setBlocks(
                heatedSelection,
                ashCeramicCrucible.defaultBlockState(),
                false
        );
        scene.world().showIndependentSection(
                heatedSelection,
                Direction.DOWN
        );
        scene.idle(20);

        scene.overlay()
                .showText(120)
                .text(
                        "Ash Ceramic Crucible Basins can also receive the Burner's direct heat"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(heatedPos))
                .placeNearTarget();
        scene.effects().indicateSuccess(heatedPos);
        scene.idle(130);

        scene.overlay()
                .showText(135)
                .text(
                        "Sulfur Burners never produce rotation, transmit thermochemical heat, or reach Combustion"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(burnerCenter)
                .placeNearTarget();
        scene.idle(145);
        scene.markAsFinished();
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

    private static void emitBurnerFlames(
            CreateSceneBuilder scene,
            SceneBuildingUtil util,
            BlockPos burnerPos,
            int density,
            int pulses
    ) {
        Vec3 flamePos = util.vector().centerOf(burnerPos).add(0.0, -0.18, 0.0);

        for (int pulse = 0; pulse < pulses; pulse++) {
            scene.effects().emitParticles(
                    flamePos,
                    scene.effects().simpleParticleEmitter(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            Vec3.ZERO
                    ),
                    0.1F,
                    density
            );

            scene.idle(4);
        }
    }
}