package io.hxneyw.repo.ponder;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlock;
import io.hxneyw.repo.content.blocks.livingemberlamp.LivingEmberLampBlockEntity;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.items.LivingEmberLampItem;
import io.hxneyw.repo.content.registry.AllModBlocks;
import java.util.UUID;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public final class LivingEmberLampScenes {

    private static final int LIGHT_STEP_TICKS = 2;

    private LivingEmberLampScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "living_ember_lamp.operation",
                "Linking and Reading the Living Ember Lamp"
        );

        BlockPos pedestalPos = util.grid().at(2, 1, 2);
        BlockPos lampPos = util.grid().at(2, 2, 2);
        BlockPos furnacePos = util.grid().at(2, 1, 4);

        ItemStack unlinkedLamp = new ItemStack(
                Items.LIVING_EMBER_LAMP_ITEM.get()
        );
        ItemStack linkedLamp = unlinkedLamp.copy();
        UUID ponderFurnaceIdentity = UUID.randomUUID();
        LivingEmberLampItem.setLink(
                linkedLamp,
                new LivingEmberLampItem.FurnaceLink(
                        furnacePos,
                        "minecraft:overworld",
                        ponderFurnaceIdentity
                )
        );

        ItemStack clearedLamp = linkedLamp.copy();
        LivingEmberLampItem.clearLink(clearedLamp);

        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(
                util.select().position(pedestalPos),
                Direction.DOWN
        );

        scene.world().setBlock(
                furnacePos,
                AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                        .defaultBlockState(),
                false
        );
        scene.world().showIndependentSection(
                util.select().position(furnacePos),
                Direction.DOWN
        );

        scene.idle(20);

        scene.overlay()
                .showText(120)
                .text(
                        "The Living Ember Lamp remotely displays the heat and fuel condition of one Molten Rotor Furnace"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        scene.idle(130);

        scene.overlay()
                .showControls(
                        util.vector().centerOf(furnacePos)
                                .add(0.0, 0.6, 0.0),
                        Pointing.DOWN,
                        70
                )
                .rightClick()
                .withItem(unlinkedLamp);

        scene.overlay()
                .showText(115)
                .text(
                        "Before placing it, right-click the furnace while holding the Lamp"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(furnacePos))
                .placeNearTarget();

        scene.idle(125);

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "living_ember_lamp_selected_furnace",
                util.select().position(furnacePos),
                125
        );

        scene.overlay()
                .showText(125)
                .text(
                        "A pulsing blue outline marks the selected furnace while the linked Lamp is held"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().topOf(furnacePos))
                .placeNearTarget();

        scene.overlay()
                .showControls(
                        util.vector().topOf(furnacePos)
                                .add(0.0, 0.4, 0.0),
                        Pointing.DOWN,
                        80
                )
                .withItem(linkedLamp);

        scene.idle(135);

        scene.overlay()
                .showControls(
                        util.vector().centerOf(lampPos),
                        Pointing.DOWN,
                        50
                )
                .rightClick()
                .withItem(linkedLamp);

        scene.world().showSection(
                util.select().position(lampPos),
                Direction.DOWN
        );

        scene.world().modifyBlockEntity(
                lampPos,
                LivingEmberLampBlockEntity.class,
                lamp -> lamp.setLinkedFurnace(
                        furnacePos,
                        "minecraft:overworld",
                        ponderFurnaceIdentity
                )
        );

        scene.idle(20);

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "living_ember_lamp_network_furnace",
                util.select().position(furnacePos),
                130
        );

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "living_ember_lamp_network_lamp",
                util.select().position(lampPos),
                130
        );

        scene.overlay()
                .showText(130)
                .text(
                        "After placement, holding a Lamp from the same network outlines both the furnace and every connected Lamp"
                )
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        scene.idle(140);

        setFurnaceVisualHeat(scene, furnacePos, HeatLevel.NONE);
        setLampLight(scene, lampPos, 0);

        scene.overlay()
                .showText(105)
                .text("A cold or inactive furnace leaves the Lamp dark at level 0")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        scene.idle(115);

        setFurnaceVisualHeat(scene, furnacePos, HeatLevel.SMOULDERING);
        animateLampLight(scene, lampPos, 0, 7);

        scene.overlay()
                .showText(115)
                .text("Fading or Smouldering heat raises it smoothly to level 7")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        scene.idle(125);

        setFurnaceVisualHeat(scene, furnacePos, HeatLevel.KINDLED);
        animateLampLight(scene, lampPos, 7, 11);

        scene.overlay()
                .showText(115)
                .text("Kindled or Seething heat raises it smoothly to level 11")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        scene.idle(125);

        setFurnaceVisualHeat(scene, furnacePos, HeatLevel.SEETHING);
        animateLampLight(scene, lampPos, 11, 13);

        scene.overlay()
                .showText(120)
                .text("At Radiant heat, the Lamp reaches its steady level 13 glow")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        scene.idle(130);

        scene.overlay()
                .showText(175)
                .text(
                        "With 200 fuel ticks or less remaining and no fuel queued, a hot Lamp warns you by pulsing noticeably between levels 9 and 15"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        animateLampLight(scene, lampPos, 13, 15);
        scene.idle(8);
        animateLampLight(scene, lampPos, 15, 9);
        scene.idle(8);
        animateLampLight(scene, lampPos, 9, 15);
        scene.idle(8);
        animateLampLight(scene, lampPos, 15, 9);
        scene.idle(8);
        animateLampLight(scene, lampPos, 9, 15);

        scene.idle(105);

        scene.world().setBlock(
                furnacePos,
                net.minecraft.world.level.block.Blocks.AIR
                        .defaultBlockState(),
                false
        );

        scene.overlay()
                .showText(170)
                .text(
                        "If the linked furnace is removed, unloaded, or in another dimension, the Lamp safely fades back to darkness"
                )
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        animateLampLight(scene, lampPos, 15, 0);
        scene.idle(145);

        scene.overlay()
                .showText(145)
                .text(
                        "Breaking and replacing the furnace does not restore the link, even at the same position"
                )
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(lampPos))
                .placeNearTarget();

        scene.idle(155);

        scene.overlay()
                .showControls(
                        util.vector().topOf(lampPos)
                                .add(0.0, 0.45, 0.0),
                        Pointing.DOWN,
                        80
                )
                .whileSneaking()
                .rightClick()
                .withItem(linkedLamp);

        scene.overlay()
                .showText(165)
                .text(
                        "Sneak-right-click air with a linked Lamp to clear the connection; the action bar confirms Removed connection to network"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(lampPos))
                .placeNearTarget();

        scene.idle(110);

        scene.overlay()
                .showControls(
                        util.vector().topOf(lampPos)
                                .add(0.0, 0.45, 0.0),
                        Pointing.DOWN,
                        55
                )
                .withItem(clearedLamp);

        scene.idle(75);
        scene.markAsFinished();
    }

    private static void setFurnaceVisualHeat(
            CreateSceneBuilder scene,
            BlockPos furnacePos,
            HeatLevel heatLevel
    ) {
        scene.world().modifyBlock(
                furnacePos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        heatLevel
                ),
                false
        );
    }

    private static void setLampLight(
            CreateSceneBuilder scene,
            BlockPos lampPos,
            int lightLevel
    ) {
        scene.world().modifyBlock(
                lampPos,
                state -> state.setValue(
                        LivingEmberLampBlock.LIGHT_LEVEL,
                        lightLevel
                ),
                false
        );
    }

    private static void animateLampLight(
            CreateSceneBuilder scene,
            BlockPos lampPos,
            int startingLight,
            int targetLight
    ) {
        int direction = Integer.signum(targetLight - startingLight);
        if (direction == 0) {
            return;
        }

        for (int light = startingLight;
             light != targetLight;
             light += direction) {
            int nextLight = light + direction;
            setLampLight(scene, lampPos, nextLight);
            scene.idle(LIGHT_STEP_TICKS);
        }
    }
}
