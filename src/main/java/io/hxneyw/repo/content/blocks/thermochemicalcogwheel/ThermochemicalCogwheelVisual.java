package io.hxneyw.repo.content.blocks.thermochemicalcogwheel;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import io.hxneyw.repo.client.ClientModEvents;
import net.minecraft.core.Direction;

import java.util.function.Consumer;

public final class ThermochemicalCogwheelVisual {

    private ThermochemicalCogwheelVisual() {
    }

    public static BlockEntityVisual<ThermochemicalCogwheelBlockEntity> create(
            VisualizationContext context,
            ThermochemicalCogwheelBlockEntity blockEntity,
            float partialTick
    ) {
        if (ICogWheel.isLargeCog(blockEntity.getBlockState())) {
            return new LargeCogVisual(
                    context,
                    blockEntity,
                    partialTick
            );
        }

        return new SingleAxisRotatingVisual<>(
                context,
                blockEntity,
                partialTick,
                Models.partial(
                        ClientModEvents.THERMOCHEMICAL_COGWHEEL
                )
        );
    }

    private static class LargeCogVisual
            extends SingleAxisRotatingVisual<ThermochemicalCogwheelBlockEntity> {

        private final RotatingInstance additionalShaft;

        private LargeCogVisual(
                VisualizationContext context,
                ThermochemicalCogwheelBlockEntity blockEntity,
                float partialTick
        ) {
            super(
                    context,
                    blockEntity,
                    partialTick,
                    Models.partial(
                            ClientModEvents.LARGE_THERMOCHEMICAL_COGWHEEL_SHAFTLESS
                    )
            );

            Direction.Axis axis =
                    KineticBlockEntityRenderer.getRotationAxisOf(
                            blockEntity
                    );

            additionalShaft = instancerProvider()
                    .instancer(
                            AllInstanceTypes.ROTATING,
                            Models.partial(
                                    ClientModEvents.THERMOCHEMICAL_COGWHEEL_SHAFT
                            )
                    )
                    .createInstance();

            additionalShaft
                    .rotateToFace(axis)
                    .setup(blockEntity)
                    .setRotationOffset(
                            BracketedKineticBlockEntityRenderer
                                    .getShaftAngleOffset(
                                            axis,
                                            pos
                                    )
                    )
                    .setPosition(getVisualPosition())
                    .setChanged();
        }

        @Override
        public void update(float partialTick) {
            super.update(partialTick);

            additionalShaft
                    .setup(blockEntity)
                    .setRotationOffset(
                            BracketedKineticBlockEntityRenderer
                                    .getShaftAngleOffset(
                                            rotationAxis(),
                                            pos
                                    )
                    )
                    .setChanged();
        }

        @Override
        public void updateLight(float partialTick) {
            super.updateLight(partialTick);
            relight(additionalShaft);
        }

        @Override
        protected void _delete() {
            super._delete();
            additionalShaft.delete();
        }

        @Override
        public void collectCrumblingInstances(
                Consumer<Instance> consumer
        ) {
            super.collectCrumblingInstances(consumer);
            consumer.accept(additionalShaft);
        }
    }
}