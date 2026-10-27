package io.hxneyw.repo.client;

import io.hxneyw.repo.content.registry.AllModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import com.mojang.blaze3d.vertex.PoseStack;

/** Renders the Caddy shell and its NBT cargo in item slots and held-item views. */
public final class BlockCaddyItemRenderer extends BlockEntityWithoutLevelRenderer {

    @SuppressWarnings("unused")
    public BlockCaddyItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void renderByItem(@NotNull ItemStack stack,
                             @NotNull ItemDisplayContext displayContext,
                             @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource buffer,
                             int light,
                             int overlay) {
        BlockState shell = AllModBlocks.BLOCK_CADDY.get().defaultBlockState();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                shell, poseStack, buffer, light, overlay, ModelData.EMPTY, null);
        BlockCaddyBeltRenderer.renderCargoItem(
                stack, poseStack, buffer, light, overlay);
    }
}
