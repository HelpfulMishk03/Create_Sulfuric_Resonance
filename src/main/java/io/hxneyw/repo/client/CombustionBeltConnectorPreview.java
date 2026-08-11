package io.hxneyw.repo.client;

import com.simibubi.create.AllDataComponents;
import io.hxneyw.repo.content.items.CombustionBeltConnectorItem;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class CombustionBeltConnectorPreview {

    private static final Vector3f VALID_COLOR =
            new Vector3f(0.85F, 0.45F, 0.12F);

    private static final Vector3f INVALID_COLOR =
            new Vector3f(0.9F, 0.2F, 0.15F);

    private CombustionBeltConnectorPreview() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tickPreview();
    }

    private static void tickPreview() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;

        if (player == null || level == null) {
            return;
        }

        if (minecraft.screen != null) {
            return;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (!heldItem.is(
                    Items.COMBUSTION_BELT_CONNECTOR.get()
            )) {
                continue;
            }

            if (!heldItem.has(
                    AllDataComponents.BELT_FIRST_SHAFT
            )) {
                continue;
            }

            BlockPos first =
                    heldItem.get(
                            AllDataComponents.BELT_FIRST_SHAFT
                    );

            if (first == null) {
                continue;
            }

            renderPreview(level, minecraft, first);
        }
    }

    private static void renderPreview(
            Level level,
            Minecraft minecraft,
            BlockPos first
    ) {
        RandomSource random = level.random;

        if (!level.isLoaded(first)) {
            return;
        }

        if (!level.getBlockState(first)
                .hasProperty(BlockStateProperties.AXIS)) {
            return;
        }

        Axis shaftAxis =
                level.getBlockState(first)
                        .getValue(BlockStateProperties.AXIS);

        HitResult hitResult = minecraft.hitResult;

        if (!(hitResult instanceof BlockHitResult blockHit)) {
            showSelectedShaftParticle(level, random, first);
            return;
        }

        BlockPos selected = blockHit.getBlockPos();

        if (!CombustionBeltConnectorItem.isSupportedShaftState(
                level.getBlockState(selected)
        )) {
            selected = selected.relative(
                    blockHit.getDirection()
            );
        }

        if (CombustionBeltConnectorItem.isNotWithinConfiguredLength(
                first,
                selected
        )) {
            return;
        }

        boolean canConnect =
                CombustionBeltConnectorItem.canConnect(
                        level,
                        first,
                        selected
                );

        Vec3 start = Vec3.atLowerCornerOf(first);
        Vec3 end = Vec3.atLowerCornerOf(selected);

        Vec3 actualDifference = end.subtract(start);

        /*
         * Remove movement along the shaft axis. Belts travel
         * perpendicular to the shafts.
         */
        end = end.subtract(
                shaftAxis.choose(
                        actualDifference.x,
                        0,
                        0
                ),
                shaftAxis.choose(
                        0,
                        actualDifference.y,
                        0
                ),
                shaftAxis.choose(
                        0,
                        0,
                        actualDifference.z
                )
        );

        Vec3 difference = end.subtract(start);

        double x = Math.abs(difference.x);
        double y = Math.abs(difference.y);
        double z = Math.abs(difference.z);

        float length =
                (float) Math.max(
                        x,
                        Math.max(y, z)
                );

        if (length <= 0) {
            showSelectedShaftParticle(level, random, first);
            return;
        }

        Vec3 step = difference.normalize();

        int equalAxes =
                (x == y ? 1 : 0)
                        + (y == z ? 1 : 0)
                        + (z == x ? 1 : 0);

        if (equalAxes == 0) {
            step = findClosestValidStep(
                    shaftAxis,
                    step
            );
        }

        if (shaftAxis == Axis.Y
                && step.x != 0
                && step.z != 0) {
            return;
        }

        step = new Vec3(
                Math.signum(step.x),
                Math.signum(step.y),
                Math.signum(step.z)
        );

        Vector3f color =
                canConnect
                        ? VALID_COLOR
                        : INVALID_COLOR;


        for (float distance = 0;
             distance < length;
             distance += 0.0625F) {

            Vec3 position =
                    start.add(step.scale(distance));

            if (random.nextInt(10) != 0) {
                continue;
            }

            level.addParticle(
                    new DustParticleOptions(color, 1.0F),
                    position.x + 0.5F,
                    position.y + 0.5F,
                    position.z + 0.5F,
                    0,
                    0,
                    0
            );
        }
    }

    private static Vec3 findClosestValidStep(
            Axis shaftAxis,
            Vec3 currentStep
    ) {
        List<Vec3> validDirections =
                new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {

                    if (shaftAxis.choose(x, y, z) != 0) {
                        continue;
                    }

                    if (shaftAxis == Axis.Y
                            && x != 0
                            && z != 0) {
                        continue;
                    }

                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    validDirections.add(
                            new Vec3(x, y, z)
                    );
                }
            }
        }

        Vec3 closestDirection = Vec3.ZERO;
        double closestDistance = Double.MAX_VALUE;

        for (Vec3 validDirection : validDirections) {
            double distance =
                    currentStep.distanceTo(validDirection);

            if (distance >= closestDistance) {
                continue;
            }

            closestDistance = distance;
            closestDirection = validDirection;
        }

        return closestDirection;
    }

    private static void showSelectedShaftParticle(
            Level level,
            RandomSource random,
            BlockPos first
    ) {
        if (random.nextInt(50) != 0) {
            return;
        }

        level.addParticle(
                new DustParticleOptions(
                        VALID_COLOR,
                        1.0F
                ),
                first.getX()
                        + 0.5F
                        + randomOffset(random),
                first.getY()
                        + 0.5F
                        + randomOffset(random),
                first.getZ()
                        + 0.5F
                        + randomOffset(random),
                0,
                0,
                0
        );
    }

    private static float randomOffset(
            RandomSource random
    ) {
        return (random.nextFloat() - 0.5F)
                * 2.0F
                * (float) 0.25;
    }
}
