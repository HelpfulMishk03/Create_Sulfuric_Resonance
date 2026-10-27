package io.hxneyw.repo.content.blocks.livingemberlamp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.hxneyw.repo.content.Items;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class LivingEmberLampRenderer
        implements BlockEntityRenderer<LivingEmberLampBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final ItemStack embersol;

    public LivingEmberLampRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.itemRenderer = context.getItemRenderer();
        this.embersol = new ItemStack(Items.EMBERSOL.get());
    }

    @Override
    public void render(
            LivingEmberLampBlockEntity blockEntity,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Level level = blockEntity.getLevel();

        if (level == null) {
            return;
        }

        float time = level.getGameTime() + partialTick;
        float bob = Mth.sin(time * 0.1F) * 0.018F;
        float rotation = time * 2.0F;

        poseStack.pushPose();

        
        poseStack.translate(
                0.5D,
                0.60D + bob,
                0.5D
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(rotation)
        );

        poseStack.scale(
                0.45F,
                0.45F,
                0.45F
        );

        // Use the same heat-derived brightness in placed and assembled modes.
        // LIGHT_LEVEL already includes the furnace tier, fades, and warning pulses.
        // Retain ambient light so an unpowered core still responds to its surroundings.
        int emittedLight = blockEntity.getBlockState().getValue(LivingEmberLampBlock.LIGHT_LEVEL);
        // Sable's assembled renderer can pass a zero block-light component
        // for plot actors even while their LIGHT_LEVEL state is positive.
        // Render the ember emissively from that heat-derived state so the
        // lamp core stays visibly lit in both ordinary and assembled worlds.
        int coreLight = emittedLight > 0
                ? LightTexture.FULL_BRIGHT
                : packedLight;
        itemRenderer.renderStatic(
                embersol,
                ItemDisplayContext.FIXED,
                coreLight,
                packedOverlay,
                poseStack,
                buffer,
                level,
                0
        );

        poseStack.popPose();
    }
}
