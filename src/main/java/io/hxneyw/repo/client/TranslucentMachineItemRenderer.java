package io.hxneyw.repo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TranslucentMachineItemRenderer
        extends BlockEntityWithoutLevelRenderer {

    public TranslucentMachineItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            @NotNull ItemStack stack,
            @NotNull ItemDisplayContext displayContext,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (stack.getItem() == io.hxneyw.repo.content.Items.THERMAL_GAUGE_ITEM.get()) {
            renderThermalGauge(stack, poseStack, buffer, light, overlay);
            return;
        }

        if (stack.getItem() == io.hxneyw.repo.content.Items.THERMAL_RELAY_SWITCH_ITEM.get()) {
            renderThermalRelaySwitch(stack, poseStack, buffer, light, overlay);
            return;
        }

        if (stack.getItem() == io.hxneyw.repo.content.Items.SULFURIC_RESONANCE_CHAMBER_ITEM.get()) {
            renderSulfuricResonanceChamber(stack, poseStack, buffer, light, overlay);
            return;
        }

        if (stack.getItem() == io.hxneyw.repo.content.Items.THERMAL_WARNING_ALARM_ITEM.get()) {
            renderThermalWarningAlarm(stack, poseStack, buffer, light, overlay);
        }
    }

    private static void renderThermalGauge(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state =
                io.hxneyw.repo.content.registry.AllModBlocks.THERMAL_GAUGE
                        .get()
                        .defaultBlockState();

        renderPart(
                ClientModEvents.THERMAL_GAUGE_BASE,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.THERMAL_GAUGE_NEEDLE,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.THERMAL_GAUGE_COVER,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.translucent()
        );
    }

    private static void renderThermalRelaySwitch(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state =
                io.hxneyw.repo.content.registry.AllModBlocks.THERMAL_RELAY_SWITCH
                        .get()
                        .defaultBlockState();

        renderPart(
                ClientModEvents.THERMAL_RELAY_SWITCH_SOLID,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.THERMAL_RELAY_SWITCH_GLASS,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.translucent()
        );
    }

    private static void renderSulfuricResonanceChamber(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state =
                io.hxneyw.repo.content.registry.AllModBlocks.SULFURIC_RESONANCE_CHAMBER
                        .get()
                        .defaultBlockState();

        renderPart(
                ClientModEvents.RESONANCE_CHAMBER_BODY,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.RESONANCE_CHAMBER_RING_BOTTOM,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.RESONANCE_CHAMBER_RING_TOP,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.RESONANCE_CHAMBER_SHAFT,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.RESONANCE_CHAMBER_WINDOW,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.translucent()
        );
    }

    private static void renderThermalWarningAlarm(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state =
                io.hxneyw.repo.content.registry.AllModBlocks.THERMAL_WARNING_ALARM
                        .get()
                        .defaultBlockState();

        renderPart(
                ClientModEvents.THERMAL_WARNING_ALARM_BODY,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.THERMAL_WARNING_ALARM_BELL,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.THERMAL_WARNING_ALARM_STRIKER,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.THERMAL_WARNING_ALARM_MAIN_FILAMENT,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.cutout()
        );
        renderPart(
                ClientModEvents.THERMAL_WARNING_ALARM_MAIN_BULB,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.translucent()
        );
        renderPart(
                ClientModEvents.THERMAL_WARNING_ALARM_STATUS_RED,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.translucent()
        );
        renderPart(
                ClientModEvents.THERMAL_WARNING_ALARM_STATUS_GREEN,
                state,
                stack,
                poseStack,
                buffer,
                light,
                overlay,
                RenderType.translucent()
        );
    }

    private static void renderPart(
            PartialModel model,
            BlockState state,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            RenderType renderType
    ) {
        CachedBuffers.partial(model, state)
                .light(light)
                .overlay(overlay)
                .renderInto(
                        poseStack,
                        ItemRenderer.getFoilBufferDirect(
                                buffer,
                                renderType,
                                true,
                                stack.hasFoil()
                        )
                );
    }
}
