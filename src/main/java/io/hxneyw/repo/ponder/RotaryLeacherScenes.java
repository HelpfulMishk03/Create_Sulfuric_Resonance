package io.hxneyw.repo.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.blocks.rotaryleacher.RotaryLeacherBlock;
import io.hxneyw.repo.content.blocks.rotaryleacher.RotaryLeacherBlockEntity;
import io.hxneyw.repo.content.registry.AllModBlocks;
import io.hxneyw.repo.content.registry.AllModFluids;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class RotaryLeacherScenes {
    private static final float ARM_SPEED = 48.0F;
    private static final int ARM_TRAVEL_TICKS = 22;

    private RotaryLeacherScenes() {
    }

    public static void operation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("rotary_leacher.operation", "Purifying Ore in a Rotary Leacher");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.74F);
        scene.setSceneOffsetY(-0.45F);
        scene.rotateCameraY(90);

        BlockPos lowerPos = util.grid().at(2, 1, 2);
        BlockPos upperPos = lowerPos.above();
        BlockPos acidTankBasePos = util.grid().at(0, 1, 2);
        BlockPos acidTankPos = acidTankBasePos.above();
        BlockPos acidPumpPos = util.grid().at(1, 2, 2);
        BlockPos pumpDriveCogPos = util.grid().at(1, 2, 3);
        BlockPos leacherDriveCogPos = util.grid().at(2, 1, 3);
        BlockPos inputBufferPos = util.grid().at(1, 1, 0);
        BlockPos armDriveCogPos = util.grid().at(2, 1, 1);
        BlockPos inputArmPos = util.grid().at(1, 1, 1);
        BlockPos outputArmPos = util.grid().at(3, 1, 1);
        BlockPos outputBufferPos = util.grid().at(3, 1, 0);
        BlockPos byproductBufferPos = util.grid().at(4, 1, 1);

        BlockState lowerState = AllModBlocks.ROTARY_LEACHER.get()
                .defaultBlockState()
                .setValue(RotaryLeacherBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upperState = lowerState.setValue(
                RotaryLeacherBlock.HALF,
                DoubleBlockHalf.UPPER
        );

        Selection machine = util.select().fromTo(lowerPos, upperPos);
        Selection acidSupply = util.select().position(acidTankBasePos)
                .add(util.select().position(acidTankPos))
                .add(util.select().position(acidPumpPos));
        Selection pumpDrive = util.select().position(pumpDriveCogPos);
        Selection leacherDrive = util.select().position(leacherDriveCogPos);
        Selection itemAutomation = util.select().position(inputBufferPos)
                .add(util.select().position(armDriveCogPos))
                .add(util.select().position(inputArmPos))
                .add(util.select().position(outputArmPos))
                .add(util.select().position(outputBufferPos))
                .add(util.select().position(byproductBufferPos));
        Selection armDrives = util.select().position(armDriveCogPos);
        Selection arms = util.select().position(inputArmPos)
                .add(util.select().position(outputArmPos));

        scene.showBasePlate();
        applyDarkFloor(scene, util);
        scene.world().setBlock(lowerPos, lowerState, false);
        scene.world().setBlock(upperPos, upperState, false);
        scene.world().setBlock(acidTankBasePos, AllBlocks.CREATIVE_FLUID_TANK.getDefaultState(), false);
        scene.world().setBlock(acidTankPos, AllBlocks.CREATIVE_FLUID_TANK.getDefaultState(), false);
        scene.world().setBlock(
                acidPumpPos,
                AllBlocks.MECHANICAL_PUMP.getDefaultState().setValue(PumpBlock.FACING, Direction.EAST),
                false
        );
        scene.world().setBlock(
                pumpDriveCogPos,
                AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X),
                false
        );
        scene.world().setBlock(
                leacherDriveCogPos,
                AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y),
                false
        );
        scene.world().setBlock(inputBufferPos, AllBlocks.DEPOT.getDefaultState(), false);
        scene.world().setBlock(outputBufferPos, AllBlocks.DEPOT.getDefaultState(), false);
        scene.world().setBlock(byproductBufferPos, AllBlocks.DEPOT.getDefaultState(), false);
        scene.world().setBlock(
                armDriveCogPos,
                AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y),
                false
        );
        scene.world().setBlock(inputArmPos, AllBlocks.MECHANICAL_ARM.getDefaultState(), false);
        scene.world().setBlock(outputArmPos, AllBlocks.MECHANICAL_ARM.getDefaultState(), false);
        configureArm(scene, util, inputArmPos, interactionPoints(
                inputArmPos,
                inputBufferPos, "create:depot", "TAKE",
                lowerPos, "sulfuricresonance:rotary_leacher", "DEPOSIT"
        ));
        configureArm(scene, util, outputArmPos, interactionPoints(
                outputArmPos,
                lowerPos, outputBufferPos, byproductBufferPos
        ));

        scene.world().showSection(
                util.select().fromTo(util.grid().at(0, 0, 0), util.grid().at(4, 0, 4)),
                Direction.UP
        );
        scene.idle(10);
        scene.world().showSection(
                machine.add(acidSupply).add(pumpDrive).add(leacherDrive).add(itemAutomation),
                Direction.DOWN
        );
        scene.idle(20);
        for (BlockPos tankPos : new BlockPos[] {acidTankBasePos, acidTankPos}) {
            scene.world().modifyBlockEntity(
                    tankPos,
                    CreativeFluidTankBlockEntity.class,
                    tank -> ((CreativeSmartFluidTank) tank.getTankInventory()).setContainedFluid(
                            new FluidStack(AllModFluids.SULFURIC_ACID.get(), 1)
                    )
            );
        }
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Ore and acid mix inside this powered two-block chamber.")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(upperPos))
                .placeNearTarget();
        scene.idle(100);

        ItemStack rawIronBatch = new ItemStack(net.minecraft.world.item.Items.RAW_IRON, 4);
        scene.overlay().showText(80)
                .text("The intake arm takes four Raw Iron from a Depot and inserts the full batch.")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().centerOf(inputArmPos))
                .placeNearTarget();
        scene.idle(100);
        scene.world().createItemOnBeltLike(inputBufferPos, Direction.UP, rawIronBatch);
        scene.world().setKineticSpeed(machine, 128.0F);
        scene.world().setKineticSpeed(leacherDrive, 128.0F);
        scene.world().setKineticSpeed(armDrives.add(arms), ARM_SPEED);
        scene.world().instructArm(inputArmPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(ARM_TRAVEL_TICKS);
        scene.world().removeItemsFromBelt(inputBufferPos);
        scene.world().instructArm(inputArmPos, Phase.SEARCH_OUTPUTS, rawIronBatch, -1);
        scene.idle(16);
        scene.world().instructArm(inputArmPos, Phase.MOVE_TO_OUTPUT, rawIronBatch, 0);
        scene.idle(ARM_TRAVEL_TICKS);
        scene.world().modifyBlockEntity(
                lowerPos,
                RotaryLeacherBlockEntity.class,
                leacher -> leacher.getManualItemCapability().insertItem(
                        RotaryLeacherBlockEntity.INPUT,
                        rawIronBatch.copy(),
                        false
                )
        );
        scene.world().instructArm(inputArmPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(16);
        scene.world().setKineticSpeed(util.select().position(acidPumpPos), 32.0F);
        scene.world().setKineticSpeed(pumpDrive, 32.0F);
        scene.world().propagatePipeChange(acidPumpPos);
        scene.overlay().showControls(
                        util.vector().blockSurface(upperPos, Direction.WEST),
                        Pointing.DOWN,
                        60
                )
                .withItem(new ItemStack(Items.SULFURIC_ACID_BUCKET.get()));
        scene.idle(10);
        for (int fillStep = 0; fillStep < 7; fillStep++) {
            scene.world().modifyBlockEntity(
                    lowerPos,
                    RotaryLeacherBlockEntity.class,
                    leacher -> leacher.getInternalFluidCapability().fill(
                            new FluidStack(AllModFluids.SULFURIC_ACID.get(), 500),
                            IFluidHandler.FluidAction.EXECUTE
                    )
            );
            scene.idle(5);
        }
        scene.overlay().showText(80)
                .text("The creative tank fills the leacher with acid; each iron batch uses 750 mB.")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().blockSurface(upperPos, Direction.WEST))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("The internal cog drives the arm gear. Leaching needs 128 RPM and 220 ticks.")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(lowerPos))
                .placeNearTarget();
        scene.idle(100);
        scene.idle(50);
        scene.world().modifyBlockEntity(
                lowerPos,
                RotaryLeacherBlockEntity.class,
                RotaryLeacherBlockEntity::completePonderBatch
        );
        ItemStack purifiedIron = new ItemStack(io.hxneyw.repo.content.Items.PURIFIED_IRON_CHUNK.get(), 5);
        ItemStack tailings = new ItemStack(io.hxneyw.repo.content.Items.MINERAL_TAILINGS.get());
        scene.overlay().showText(80)
                .text("Four Raw Iron yield five Purified Iron Chunks and one Mineral Tailings item.")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(lowerPos))
                .placeNearTarget();
        scene.idle(100);

        scene.world().instructArm(outputArmPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(ARM_TRAVEL_TICKS);
        scene.world().modifyBlockEntity(
                lowerPos,
                RotaryLeacherBlockEntity.class,
                leacher -> leacher.getManualItemCapability().extractItem(
                        RotaryLeacherBlockEntity.OUTPUT,
                        purifiedIron.getCount(),
                        false
                )
        );
        scene.world().instructArm(outputArmPos, Phase.SEARCH_OUTPUTS, purifiedIron, -1);
        scene.idle(16);
        scene.world().instructArm(outputArmPos, Phase.MOVE_TO_OUTPUT, purifiedIron, 0);
        scene.idle(ARM_TRAVEL_TICKS);
        scene.world().createItemOnBeltLike(outputBufferPos, Direction.UP, purifiedIron);
        scene.world().instructArm(outputArmPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(16);

        scene.world().instructArm(outputArmPos, Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(ARM_TRAVEL_TICKS);
        scene.world().modifyBlockEntity(
                lowerPos,
                RotaryLeacherBlockEntity.class,
                leacher -> leacher.getManualItemCapability().extractItem(
                        RotaryLeacherBlockEntity.BYPRODUCT,
                        tailings.getCount(),
                        false
                )
        );
        scene.world().instructArm(outputArmPos, Phase.SEARCH_OUTPUTS, tailings, -1);
        scene.idle(16);
        scene.world().instructArm(outputArmPos, Phase.MOVE_TO_OUTPUT, tailings, 1);
        scene.idle(ARM_TRAVEL_TICKS);
        scene.world().createItemOnBeltLike(byproductBufferPos, Direction.UP, tailings);
        scene.world().instructArm(outputArmPos, Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.effects().indicateSuccess(lowerPos);
        scene.idle(16);

        scene.overlay().showText(80)
                .text("The output arm moves purified chunks and tailings to separate Depots.")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(outputArmPos))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("Processing pauses if acid, speed, a valid batch, or output space is missing.")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().centerOf(upperPos))
                .placeNearTarget();
        scene.idle(100);
        scene.markAsFinished();
    }

    private static void configureArm(
            CreateSceneBuilder scene,
            SceneBuildingUtil util,
            BlockPos armPos,
            ListTag interactionPoints
    ) {
        scene.world().modifyBlockEntityNBT(
                util.select().position(armPos),
                ArmBlockEntity.class,
                tag -> tag.put("InteractionPoints", interactionPoints)
        );
    }

    private static ListTag interactionPoints(
            BlockPos armPos,
            BlockPos takePos,
            String takeType,
            String takeMode,
            BlockPos depositPos,
            String depositType,
            String depositMode
    ) {
        ListTag points = new ListTag();
        points.add(armPoint(armPos, takePos, takeType, takeMode));
        points.add(armPoint(armPos, depositPos, depositType, depositMode));
        return points;
    }

    private static ListTag interactionPoints(
            BlockPos armPos,
            BlockPos takePos,
            BlockPos firstDepositPos,
            BlockPos secondDepositPos
    ) {
        ListTag points = interactionPoints(
                armPos,
                takePos,
                "sulfuricresonance:rotary_leacher",
                "TAKE",
                firstDepositPos,
                "create:depot",
                "DEPOSIT"
        );
        points.add(armPoint(armPos, secondDepositPos, "create:depot", "DEPOSIT"));
        return points;
    }

    private static CompoundTag armPoint(
            BlockPos armPos,
            BlockPos targetPos,
            String type,
            String mode
    ) {
        CompoundTag point = new CompoundTag();
        point.putString("Type", type);
        point.put("Pos", NbtUtils.writeBlockPos(targetPos.subtract(armPos)));
        point.putString("Mode", mode);
        return point;
    }

    private static void applyDarkFloor(
            CreateSceneBuilder scene,
            SceneBuildingUtil util
    ) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                scene.world().setBlock(
                        util.grid().at(x, 0, z),
                        ((x + z) & 1) == 0
                                ? Blocks.POLISHED_BLACKSTONE.defaultBlockState()
                                : Blocks.BLACKSTONE.defaultBlockState(),
                        false
                );
            }
        }
    }
}
