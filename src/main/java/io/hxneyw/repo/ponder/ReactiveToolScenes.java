package io.hxneyw.repo.ponder;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.entities.CinderFlareEntity;
import io.hxneyw.repo.content.entities.ModEntities;
import io.hxneyw.repo.content.entities.PyroclastBombEntity;
import io.hxneyw.repo.content.entities.SulfuricAcidFlaskEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ReactiveToolScenes {

    private static final float BELT_SPEED = -24.0F;
    private static final float MACHINE_SPEED = -32.0F;

    private ReactiveToolScenes() {
    }

    public static void deployerAutomation(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "reactive_tools.deployer",
                "Automating Cinder Flare Ignition"
        );
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(1.15F);
        scene.setSceneOffsetY(-0.15F);

        BlockPos beltStart = util.grid().at(0, 1, 2);
        BlockPos beltCenter = util.grid().at(2, 1, 2);
        BlockPos beltEnd = util.grid().at(4, 1, 2);
        BlockPos deployerPos = util.grid().at(2, 3, 2);
        Selection beltSelection = util.select().fromTo(beltStart, beltEnd);
        Selection deployerSelection = util.select().position(deployerPos);
        Selection driveSelection = util.select().fromTo(2, 1, 3, 3, 3, 4);

        ItemStack unlitFlare = new ItemStack(Items.CINDER_FLARE.get());
        ItemStack litFlare = new ItemStack(Items.LIT_CINDER_FLARE.get());
        ItemStack flintAndSteel = new ItemStack(
                net.minecraft.world.item.Items.FLINT_AND_STEEL
        );

        scene.showBasePlate();
        scene.idle(8);
        scene.world().showSection(driveSelection, Direction.SOUTH);
        scene.world().setKineticSpeed(driveSelection, MACHINE_SPEED);
        scene.idle(10);
        scene.world().showSection(beltSelection, Direction.DOWN);
        scene.world().setKineticSpeed(beltSelection, BELT_SPEED);
        scene.idle(10);
        scene.world().showSection(deployerSelection, Direction.DOWN);
        scene.world().setKineticSpeed(deployerSelection, MACHINE_SPEED);
        scene.world().modifyBlockEntityNBT(
                deployerSelection,
                DeployerBlockEntity.class,
                nbt -> nbt.put(
                        "HeldItem",
                        flintAndSteel.saveOptional(
                                scene.world().getHolderLookupProvider()
                        )
                )
        );
        scene.idle(18);

        scene.overlay()
                .showControls(
                        util.vector().centerOf(deployerPos).add(0.0, 0.55, 0.0),
                        Pointing.DOWN,
                        28
                )
                .withItem(flintAndSteel);
        scene.idle(32);

        scene.overlay()
                .showText(85)
                .text(
                        "A Deployer holding Flint and Steel ignites an unlit Cinder Flare on a moving Belt"
                )
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(util.vector().topOf(beltCenter))
                .placeNearTarget();

        ElementLink<BeltItemElement> flare =
                scene.world().createItemOnBelt(
                        beltStart,
                        Direction.DOWN,
                        unlitFlare
                );
        scene.idle(34);
        scene.world().stallBeltItem(flare, true);
        scene.idle(8);
        scene.world().moveDeployer(deployerPos, 1.0F, 15);
        scene.idle(16);
        scene.world().changeBeltItemTo(flare, litFlare);
        scene.effects().indicateSuccess(beltCenter);
        scene.idle(8);
        scene.world().moveDeployer(deployerPos, -1.0F, 15);
        scene.idle(16);
        scene.idle(5);

        scene.overlay()
                .showText(70)
                .text(
                        "The Lit Cinder Flare continues down the Belt for collection"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().topOf(beltCenter))
                .placeNearTarget();
        scene.world().stallBeltItem(flare, false);
        scene.idle(75);
        scene.markAsFinished();
    }

    public static void dispenserLaunching(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(
                "reactive_tools.dispensers",
                "Launching Reactive Tools"
        );
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(1.05F);
        scene.setSceneOffsetY(-0.2F);

        BlockPos[] dispensers = {
                util.grid().at(1, 1, 4),
                util.grid().at(3, 1, 4),
                util.grid().at(5, 1, 4)
        };
        ItemStack[] tools = {
                new ItemStack(Items.LIT_CINDER_FLARE.get()),
                new ItemStack(Items.SULFURIC_ACID_FLASK.get()),
                new ItemStack(Items.PYROCLAST_BOMB.get())
        };

        scene.showBasePlate();
        scene.idle(8);
        scene.world().showSection(
                util.select().fromTo(1, 1, 0, 5, 1, 0),
                Direction.SOUTH
        );
        scene.idle(8);
        scene.world().showSection(
                util.select().fromTo(1, 1, 4, 5, 1, 5),
                Direction.DOWN
        );
        scene.idle(15);

        scene.overlay()
                .showText(70)
                .text(
                        "Dispensers launch Lit Cinder Flares, Sulfuric Acid Flasks, and Pyroclast Bombs"
                )
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(util.vector().centerOf(dispensers[1]))
                .placeNearTarget();
        scene.idle(75);

        for (int index = 0; index < dispensers.length; index++) {
            BlockPos dispenserPos = dispensers[index];
            Vec3 front = util.vector()
                    .blockSurface(dispenserPos, Direction.NORTH)
                    .add(0.0, 0.0, -0.12);

            scene.overlay()
                    .showControls(front, Pointing.DOWN, 18)
                    .withItem(tools[index]);
            scene.idle(10);
            scene.effects().indicateRedstone(dispenserPos);
            ElementLink<EntityElement> projectile = createProjectile(
                    scene,
                    index,
                    front,
                    util.vector().of(0.0, 0.04, -0.32)
            );
            scene.idle(10);
            scene.world().modifyEntity(projectile, Entity::discard);
            scene.idle(18);
        }

        scene.idle(25);
        scene.markAsFinished();
    }

    private static ElementLink<EntityElement> createProjectile(
            CreateSceneBuilder scene,
            int index,
            Vec3 position,
            Vec3 motion
    ) {
        return switch (index) {
            case 0 -> scene.world().createEntity(level -> {
                CinderFlareEntity entity = new CinderFlareEntity(
                        ModEntities.CINDER_FLARE.get(),
                        level
                );
                entity.setItem(new ItemStack(Items.LIT_CINDER_FLARE.get()));
                positionEntity(entity, position, motion);
                return entity;
            });
            case 1 -> scene.world().createEntity(level -> {
                SulfuricAcidFlaskEntity entity = new SulfuricAcidFlaskEntity(
                        ModEntities.SULFURIC_ACID_FLASK.get(),
                        level
                );
                entity.setItem(new ItemStack(Items.SULFURIC_ACID_FLASK.get()));
                positionEntity(entity, position, motion);
                return entity;
            });
            default -> scene.world().createEntity(level -> {
                PyroclastBombEntity entity = new PyroclastBombEntity(
                        ModEntities.PYROCLAST_BOMB.get(),
                        level
                );
                entity.setItem(new ItemStack(Items.PYROCLAST_BOMB.get()));
                positionEntity(entity, position, motion);
                return entity;
            });
        };
    }

    private static void positionEntity(
            Entity entity,
            Vec3 position,
            Vec3 motion
    ) {
        entity.setPos(position.x, position.y, position.z);
        entity.setDeltaMovement(motion);
    }
}
