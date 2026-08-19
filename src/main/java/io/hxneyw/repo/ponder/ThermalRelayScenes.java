package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlock;
import io.hxneyw.repo.content.blocks.thermalrelay.ThermalRelaySwitchBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class ThermalRelayScenes {

    private ThermalRelayScenes() {
    }

    @SuppressWarnings("DuplicatedCode")
    public static void thermalRelaySwitch(
            @NotNull SceneBuilder scene,
            @NotNull SceneBuildingUtil util
    ) {
        scene.title(
                "thermal_relay_switch",
                "Monitoring Heat with the Thermal Relay Switch"
        );

        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos primaryFurnacePos =
                util.grid().at(0, 1, 2);


        BlockPos relaySupportPos =
                util.grid().at(2, 1, 2);

        BlockPos relayPos =
                util.grid().at(2, 2, 2);

        BlockPos wireSupportPos =
                util.grid().at(3, 1, 2);

        BlockPos clutchSupportPos =
                util.grid().at(4, 1, 2);

        BlockPos wirePos =
                util.grid().at(3, 2, 2);

        BlockPos clutchPos =
                util.grid().at(4, 2, 2);

        Selection primaryFurnace =
                util.select().position(primaryFurnacePos);


        Selection relaySupport =
                util.select().position(relaySupportPos);

        Selection relay =
                util.select().position(relayPos);

        Selection outputSupports =
                util.select().fromTo(
                        wireSupportPos,
                        clutchSupportPos
                );

        Selection outputBlocks =
                util.select().fromTo(
                        wirePos,
                        clutchPos
                );

        Vec3 primaryFurnaceTop =
                util.vector().topOf(primaryFurnacePos);


        Vec3 relayCenter =
                util.vector().centerOf(relayPos);

        Vec3 relayTop =
                util.vector().topOf(relayPos);

        Vec3 wireTop =
                util.vector().topOf(wirePos);

        Vec3 clutchCenter =
                util.vector().centerOf(clutchPos);

        


        scene.world().setBlocks(
                primaryFurnace,
                furnaceState(),
                false
        );


        scene.world().setBlocks(
                outputSupports,
                Blocks.POLISHED_BLACKSTONE
                        .defaultBlockState(),
                false
        );

        scene.world().setBlocks(
                util.select().position(wirePos),
                Blocks.REDSTONE_WIRE
                        .defaultBlockState()
                        .setValue(
                                RedStoneWireBlock.POWER,
                                0
                        ),
                false
        );

        scene.world().setBlocks(
                util.select().position(clutchPos),
                AllBlocks.CLUTCH.getDefaultState(),
                false
        );

        


        scene.world().showSection(
                primaryFurnace,
                Direction.DOWN
        );

        scene.idle(15);

        scene.overlay().showText(200)
                .text(
                        "The Thermal Relay Switch turns "
                                + "Molten Rotor heat and fuel status "
                                + "into configurable redstone output"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(primaryFurnaceTop);
        scene.idle(240);

        


        scene.overlay().showControls(
                        primaryFurnaceTop,
                        Pointing.DOWN,
                        50
                )
                .rightClick()
                .withItem(
                        new ItemStack(
                                Items.THERMAL_RELAY_SWITCH_ITEM.get()
                        )
                );

        scene.idle(10);

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_relay_primary_furnace_link",
                primaryFurnace,
                225
        );

        scene.overlay().showText(185)
                .colored(PonderPalette.BLUE)
                .text(
                        "Right-click a Molten Rotor Furnace "
                                + "with the relay item to select "
                                + "that furnace"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(primaryFurnaceTop);
        scene.idle(225);

        scene.overlay().showText(130)
                .text(
                        "Each relay item stores one furnace; "
                                + "selecting another replaces its link"
                )
                .placeNearTarget()
                .pointAt(primaryFurnaceTop);
        scene.idle(170);


        scene.world().showSection(
                relaySupport,
                Direction.UP
        );

        scene.idle(8);

        scene.overlay().showControls(
                        util.vector().topOf(relaySupportPos),
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .withItem(
                        new ItemStack(
                                Items.THERMAL_RELAY_SWITCH_ITEM.get()
                        )
                );

        scene.idle(8);

        scene.world().showSection(
                relay,
                Direction.DOWN
        );

        scene.idle(15);

        scene.overlay().showText(170)
                .colored(PonderPalette.BLUE)
                .text(
                        "Placing the item completes the connection "
                                + "and stores its selected furnace"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(210);

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_relay_network_furnace",
                primaryFurnace,
                230
        );

        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "thermal_relay_network_switch",
                relay,
                230
        );

        scene.overlay().showText(190)
                .text(
                        "Holding another connected relay item "
                                + "reveals the linked furnace "
                                + "and matching placed relays"
                )
                .placeNearTarget()
                .pointAt(relayTop);
        scene.idle(230);


        scene.overlay().showControls(
                        relayTop,
                        Pointing.DOWN,
                        45
                )
                .rightClick();

        scene.idle(10);

        scene.overlay().showText(115)
                .text(
                        "Empty-hand right-click opens the control panel"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayTop);
        scene.idle(155);

        scene.overlay().showText(215)
                .text(
                        "Custom Heat Output assigns independent "
                                + "redstone and glow values to Heated, "
                                + "Superheated, and Combustion states"
                )
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(255);

        scene.world().modifyBlockEntityNBT(
                relay,
                ThermalRelaySwitchBlockEntity.class,
                nbt -> {

                    nbt.putInt(
                            "HeatedRedstone",
                            5
                    );

                    nbt.putInt(
                            "HeatedGlow",
                            1
                    );

                    nbt.putInt(
                            "SuperheatedRedstone",
                            10
                    );

                    nbt.putInt(
                            "SuperheatedGlow",
                            2
                    );

                    nbt.putInt(
                            "CombustionRedstone",
                            15
                    );

                    nbt.putInt(
                            "CombustionGlow",
                            3
                    );
                }
        );


        scene.world().showSection(
                outputSupports,
                Direction.UP
        );

        scene.idle(8);

        scene.world().showSection(
                outputBlocks,
                Direction.DOWN
        );

        scene.idle(15);

        scene.overlay().showText(140)
                .text(
                        "The relay supplies its configured analog "
                                + "signal on every side"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(wireTop);
        scene.idle(180);


        applyHeatDemonstration(
                scene,
                primaryFurnacePos,
                HeatLevel.KINDLED,
                relayPos,
                5,
                1,
                wirePos,
                relayPos
        );

        scene.overlay().showText(110)
                .colored(PonderPalette.GREEN)
                .text(
                        "Heated: default signal 5, glow 1"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(150);


        applyHeatDemonstration(
                scene,
                primaryFurnacePos,
                HeatLevel.SEETHING,
                relayPos,
                10,
                2,
                wirePos,
                wirePos
        );

        scene.overlay().showText(110)
                .colored(PonderPalette.GREEN)
                .text(
                        "Superheated: default signal 10, glow 2"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(150);


        setRelayOutput(scene, relayPos, 15, 3);
        setWirePower(scene, wirePos, 15);
        scene.effects().indicateRedstone(clutchPos);

        scene.overlay().showText(110)
                .colored(PonderPalette.RED)
                .text(
                        "Combustion: default signal 15, glow 3"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(150);

        scene.overlay().showText(215)
                .text(
                        "Players may choose any values within the limits: "
                                + "Heated 0–7, Superheated 0–12, "
                                + "Combustion 0–15, and glow 0–5"
                )
                .placeNearTarget()
                .pointAt(relayTop);
        scene.idle(255);

        scene.overlay().showText(175)
                .text(
                        "This allows exact automation, including outputs "
                                + "that activate at only one heat tier"
                )
                .placeNearTarget()
                .pointAt(clutchCenter);
        scene.idle(215);

        scene.overlay().showText(185)
                .text(
                        "The signal can control a Clutch, Redstone Link, "
                                + "warning system, or automated fuel supply"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(clutchCenter);
        scene.idle(225);


        scene.overlay().showControls(
                        relayTop,
                        Pointing.DOWN,
                        40
                )
                .rightClick();

        scene.idle(8);

        scene.world().modifyBlockEntityNBT(
                relay,
                ThermalRelaySwitchBlockEntity.class,
                nbt -> {

                    nbt.putString(
                            "RelayMode",
                            "low_fuel"
                    );

                    nbt.putString(
                            "LowFuelScope",
                            "both"
                    );

                    nbt.putInt(
                            "LowFuelRedstone",
                            15
                    );

                    nbt.putInt(
                            "LowFuelGlow",
                            2
                    );
                }
        );

        scene.overlay().showText(245)
                .colored(PonderPalette.RED)
                .text(
                        "Low Fuel mode pulses only when the selected heat scope "
                                + "has ten seconds of active fuel or less, or ten seconds "
                                + "of final heated cooldown left after fuel reaches zero"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(285);

        scene.overlay().showText(195)
                .text(
                        "The heat scope can target Heated only, "
                                + "Superheated and Combustion together, "
                                + "or both ranges on that furnace"
                )
                .placeNearTarget()
                .pointAt(relayTop);
        scene.idle(235);

        for (int pulse = 0; pulse < 3; pulse++) {
            setRelayOutput(scene, relayPos, 15, 2);
            setWirePower(scene, wirePos, 15);
            scene.effects().indicateRedstone(relayPos);
            scene.idle(10);

            setRelayOutput(scene, relayPos, 0, 0);
            setWirePower(scene, wirePos, 0);
            scene.idle(10);
        }

        scene.overlay().showText(175)
                .colored(PonderPalette.RED)
                .text(
                        "The configured redstone and glow warning "
                                + "pulse together: 10 ticks on, "
                                + "10 ticks off"
                )
                .placeNearTarget()
                .pointAt(wireTop);
        scene.idle(215);

        scene.overlay().showText(160)
                .colored(PonderPalette.BLUE)
                .text(
                        "In Custom Heat mode, only the linked furnace's "
                                + "current heat selects the configured output profile"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(primaryFurnaceTop);
        scene.idle(200);

        scene.overlay().showText(225)
                .text(
                        "The two modes are exclusive: Low Fuel mode stays at zero "
                                + "until its warning condition is true, while Custom Heat "
                                + "mode never substitutes the low-fuel pulse"
                )
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(265);

        scene.addKeyframe();
        scene.idle(10);

        scene.world().destroyBlock(primaryFurnacePos);
        setRelayOutput(scene, relayPos, 0, 0);
        setWirePower(scene, wirePos, 0);

        scene.idle(15);

        scene.overlay().showText(240)
                .colored(PonderPalette.RED)
                .text(
                        "Breaking a linked furnace invalidates its stored "
                                + "identity. A replacement at the same "
                                + "coordinates does not inherit the link"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(primaryFurnaceTop);
        scene.idle(280);

        scene.overlay().showText(215)
                .text(
                        "Unloaded furnace chunks are ignored without "
                                + "being force-loaded, while the saved "
                                + "connection remains available"
                )
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(255);

        scene.overlay().showControls(
                        relayTop,
                        Pointing.DOWN,
                        45
                )
                .rightClick()
                .whileSneaking();

        scene.idle(8);

        scene.overlay().showText(205)
                .text(
                        "Sneak-right-click the placed relay to clear "
                                + "its furnace link and immediately reset "
                                + "redstone and glow to zero"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayTop);
        scene.idle(245);

        scene.overlay().showText(180)
                .colored(PonderPalette.GREEN)
                .text(
                        "One Molten Rotor Furnace can support any number "
                                + "of relays, each with its own control profile"
                )
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(relayCenter);
        scene.idle(220);

        scene.markAsFinished();
    }

    private static void applyHeatDemonstration(
            @NotNull SceneBuilder scene,
            @NotNull BlockPos furnacePos,
            @NotNull HeatLevel heatLevel,
            @NotNull BlockPos relayPos,
            int redstone,
            int glow,
            @NotNull BlockPos wirePos,
            @NotNull BlockPos indicationPos
    ) {
        scene.world().modifyBlock(
                furnacePos,
                state -> state.setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        heatLevel
                ),
                true
        );

        setRelayOutput(scene, relayPos, redstone, glow);
        setWirePower(scene, wirePos, redstone);
        scene.effects().indicateRedstone(indicationPos);
    }

    private static void setRelayOutput(
            @NotNull SceneBuilder scene,
            @NotNull BlockPos relayPos,
            int redstone,
            int glow
    ) {
        scene.world().modifyBlock(
                relayPos,
                state -> state
                        .setValue(
                                ThermalRelaySwitchBlock.POWER,
                                redstone
                        )
                        .setValue(
                                ThermalRelaySwitchBlock.GLOW,
                                glow
                        ),
                false
        );
    }

    private static void setWirePower(
            @NotNull SceneBuilder scene,
            @NotNull BlockPos wirePos,
            int power
    ) {
        scene.world().modifyBlock(
                wirePos,
                state -> state.setValue(
                        RedStoneWireBlock.POWER,
                        power
                ),
                false
        );
    }

    private static net.minecraft.world.level.block.state.BlockState
    furnaceState(
            ) {
        return AllModBlocks.MOLTEN_ROTOR_FURNACE
                .get()
                .defaultBlockState()
                .setValue(
                        MoltenRotorBlock.HEAT_LEVEL,
                        HeatLevel.NONE
                );
    }
}
