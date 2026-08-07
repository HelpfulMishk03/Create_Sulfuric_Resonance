package io.hxneyw.repo.content.blocks.moltenrotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public final class MoltenRotorFuelRenderer {
    private MoltenRotorFuelRenderer() {
    }

    public static void render(
            MoltenRotorBlockEntity furnace,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay,
            Direction facing
    ) {
        ItemStack fuelStack = furnace.getRenderedFuelStack();

        if (fuelStack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel fuelModel = minecraft
                .getItemRenderer()
                .getModel(fuelStack, furnace.getLevel(), null, 0);

        if (fuelModel == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        MoltenRotorBlockEntity.FuelType fuelType =
                furnace.getRenderedFuelType();

        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        MoltenRotorRenderer.rotateToFacing(ms, facing);

        if (furnace.getDisplayFuelTime() > 0) {
            renderHeatingKindling(
                    furnace,
                    ms,
                    buffer,
                    light,
                    overlay
            );
        }

        switch (fuelType) {
            case COAL, CHARCOAL, COKE, INFERNAL_COKE -> renderCoalPile(
                    furnace,
                    fuelStack,
                    fuelModel,
                    ms,
                    buffer,
                    light,
                    overlay
            );

            case LOG -> renderLogFuelScene(
                    furnace,
                    fuelStack,
                    fuelModel,
                    ms,
                    buffer,
                    light,
                    overlay
            );

            case BLAZE_CAKE, SOUL_FIRED_BLAZE_CAKE -> renderSpecialFuel(
                    fuelStack,
                    fuelModel,
                    ms,
                    buffer,
                    light,
                    overlay
            );

            case TNT -> renderRestingFuel(
                    fuelStack,
                    fuelModel,
                    ms,
                    buffer,
                    light,
                    overlay,
                    0.27F
            );

            case CARBON_DEPOSIT_BLOCK,
                 INFERNAL_CARBON_DEPOSIT_BLOCK -> renderRestingFuel(
                    fuelStack,
                    fuelModel,
                    ms,
                    buffer,
                    light,
                    overlay,
                    0.36F
            );

            default -> renderRestingFuel(
                    fuelStack,
                    fuelModel,
                    ms,
                    buffer,
                    light,
                    overlay,
                    0.28F
            );
        }

        ms.popPose();
    }

    private static void renderCoalPile(
            MoltenRotorBlockEntity furnace,
            ItemStack fuelStack,
            BakedModel fuelModel,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        int visibleCount = Math.min(
                furnace.getRenderedFuelUnitCount(),
                3
        );

        double[][] positions =
                getCoalPositions(visibleCount);

        for (double[] position : positions) {
            ms.pushPose();

            if (visibleCount == 1) {

                ms.translate(0.0, -0.165, 0.045);
                ms.mulPose(Axis.ZP.rotationDegrees(-8.0F));
                ms.scale(0.23F, 0.23F, 0.23F);
            } else {

                ms.translate(position[0], position[1], position[2]);
                ms.mulPose(
                        Axis.ZP.rotationDegrees((float) position[3])
                );
                ms.mulPose(
                        Axis.YP.rotationDegrees(
                                (float) (position[3] * 0.35)
                        )
                );
                ms.scale(0.20F, 0.20F, 0.20F);
            }

            Minecraft.getInstance()
                    .getItemRenderer()
                    .render(
                            fuelStack,
                            ItemDisplayContext.FIXED,
                            false,
                            ms,
                            buffer,
                            light,
                            overlay,
                            fuelModel
                    );

            ms.popPose();
        }
    }



    private static double[][] getCoalPositions(
            int visibleCount
    ) {
        return switch (visibleCount) {
            case 1 -> new double[][]{
                    {0.00, -0.215, 0.050, 0.0}
            };

            case 2 -> new double[][]{
                    {-0.10, -0.235, 0.020, -14.0},
                    {0.10, -0.235, 0.020, 14.0}
            };

            case 3 -> new double[][]{
                    {-0.11, -0.235, 0.000, -14.0},
                    {0.11, -0.235, 0.000, 14.0},
                    {0.00, -0.195, 0.110, 4.0}
            };

            default -> new double[][]{
                    {-0.10, -0.235, -0.010, -14.0},
                    {0.10, -0.235, -0.010, 14.0},
                    {-0.10, -0.205, 0.110, 8.0},
                    {0.10, -0.205, 0.110, -8.0}
            };
        };
    }

    private static double[][] getLogPositions(int visibleLogs) {
        return visibleLogs == 2
                ? new double[][]{
                {0.0, -0.205, -0.050}, // BACKLOG: Changed from -0.100 to -0.050 (moves it forward)
                {0.0, -0.205, 0.130}   // FRONT LOG: Changed from 0.180 to 0.130 (moves it backward)
        }
                : new double[][]{
                {0.0, -0.205, 0.040}   // Single center log
        };
    }

    private static void renderRestingFuel(
            ItemStack fuelStack,
            BakedModel fuelModel,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay,
            float scale
    ) {
        ms.pushPose();
        ms.translate(0.0, -0.22, 0.075);
        ms.mulPose(Axis.XP.rotationDegrees(90.0F));
        ms.scale(scale, scale, scale);

        Minecraft.getInstance()
                .getItemRenderer()
                .render(
                        fuelStack,
                        ItemDisplayContext.FIXED,
                        false,
                        ms,
                        buffer,
                        light,
                        overlay,
                        fuelModel
                );

        ms.popPose();
    }

    private static void renderHeatingKindling(
            MoltenRotorBlockEntity furnace,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        ItemStack stickStack =
                new ItemStack(net.minecraft.world.item.Items.STICK);

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel stickModel = minecraft
                .getItemRenderer()
                .getModel(
                        stickStack,
                        furnace.getLevel(),
                        null,
                        0
                );

        if (stickModel
                == minecraft.getModelManager().getMissingModel()) {
            return;
        }

        /*
         * Kindling always stays on the chamber floor for every fuel type.
         * Logs are raised independently above it in renderLogFuelScene().
         */
        double kindlingY = -0.285;
        double kindlingDepth = 0.060;
        float kindlingScale = 0.12F;

        float[] rotations = {
                -38.0F,
                38.0F,
                90.0F
        };

        for (float rotation : rotations) {
            renderKindlingPiece(
                    stickStack,
                    stickModel,
                    ms,
                    buffer,
                    light,
                    overlay,
                    rotation,
                    kindlingY,
                    kindlingDepth,
                    kindlingScale
            );
        }
    }

    private static void renderKindlingPiece(
            ItemStack stickStack,
            BakedModel stickModel,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay,
            float rotation,
            double y,
            double depth,
            float scale
    ) {
        ms.pushPose();

        ms.translate(0.0, y, depth);
        ms.mulPose(Axis.YP.rotationDegrees(rotation));
        ms.mulPose(Axis.XP.rotationDegrees(90.0F));
        ms.scale(scale, scale, scale);

        Minecraft.getInstance()
                .getItemRenderer()
                .render(
                        stickStack,
                        ItemDisplayContext.FIXED,
                        false,
                        ms,
                        buffer,
                        light,
                        overlay,
                        stickModel
                );

        ms.popPose();
    }

    private static void renderLogFuelScene(
            MoltenRotorBlockEntity furnace,
            ItemStack fuelStack,
            BakedModel fuelModel,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        int visibleLogs = Math.clamp(
                furnace.getRenderedFuelUnitCount(),
                1,
                2
        );

        double[][] logPositions = getLogPositions(visibleLogs);
        List<ItemStack> renderedStickStacks =
                furnace.getRenderedLogStickStacks();
        int stickIndex = 0;

        Minecraft minecraft = Minecraft.getInstance();

        for (double[] logPos : logPositions) {
            ms.pushPose();

            ms.translate(
                    logPos[0],
                    logPos[1],
                    logPos[2]
            );

            ms.scale(0.28F, 0.28F, 0.28F);

            minecraft.getItemRenderer()
                    .render(
                            fuelStack,
                            ItemDisplayContext.FIXED,
                            false,
                            ms,
                            buffer,
                            light,
                            overlay,
                            fuelModel
                    );

            ms.popPose();

            if (stickIndex >= renderedStickStacks.size()) {
                continue;
            }

            double stickZ = logPos[2] - 0.015;

            ItemStack leftStickStack = renderedStickStacks.get(stickIndex++);
            renderLogTopStick(
                    leftStickStack,
                    minecraft.getItemRenderer().getModel(
                            leftStickStack,
                            furnace.getLevel(),
                            null,
                            0
                    ),
                    ms,
                    buffer,
                    light,
                    overlay,
                    logPos[0] - 0.040,
                    stickZ,
                    34.0F
            );

            if (stickIndex < renderedStickStacks.size()) {
                ItemStack rightStickStack =
                        renderedStickStacks.get(stickIndex++);
                renderLogTopStick(
                        rightStickStack,
                        minecraft.getItemRenderer().getModel(
                                rightStickStack,
                                furnace.getLevel(),
                                null,
                                0
                        ),
                        ms,
                        buffer,
                        light,
                        overlay,
                        logPos[0] + 0.040,
                        stickZ,
                        -34.0F
                );
            }
        }
    }

    private static void renderLogTopStick(
            ItemStack stickStack,
            BakedModel stickModel,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay,
            double x,
            double z,
            float angle
    ) {
        if (stickStack.isEmpty()
                || stickModel == Minecraft.getInstance()
                .getModelManager()
                .getMissingModel()) {
            return;
        }

        ms.pushPose();
        ms.translate(x, -0.118, z);
        ms.mulPose(Axis.ZP.rotationDegrees(45.0F));
        ms.mulPose(Axis.ZP.rotationDegrees(angle));
        ms.scale(0.125F, 0.125F, 0.125F);

        Minecraft.getInstance()
                .getItemRenderer()
                .render(
                        stickStack,
                        ItemDisplayContext.FIXED,
                        false,
                        ms,
                        buffer,
                        light,
                        overlay,
                        stickModel
                );

        ms.popPose();
    }

    private static void renderSpecialFuel(
            ItemStack fuelStack,
            BakedModel fuelModel,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        ms.pushPose();


        ms.translate(0.0, -0.245, 0.078);
        ms.scale(0.48F, 0.48F, 0.48F);

        Minecraft.getInstance()
                .getItemRenderer()
                .render(
                        fuelStack,
                        ItemDisplayContext.GROUND,
                        false,
                        ms,
                        buffer,
                        light,
                        overlay,
                        fuelModel
                );

        ms.popPose();
    }

}