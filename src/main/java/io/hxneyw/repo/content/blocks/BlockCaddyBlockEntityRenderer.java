package io.hxneyw.repo.content.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import io.hxneyw.repo.client.BlockCaddyBeltRenderer;
import io.hxneyw.repo.content.Items;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockCaddyBlockEntityRenderer implements BlockEntityRenderer<BlockCaddyBlockEntity> {
    @SuppressWarnings("unused")
    public BlockCaddyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen(BlockCaddyBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(
            BlockCaddyBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (blockEntity.getCargo().isEmpty()) {
            return;
        }
        ItemStack caddy = new ItemStack(Items.BLOCK_CADDY.get());
        BlockCaddyItem.setCargoStack(caddy, blockEntity.getCargo());
        BlockCaddyBeltRenderer.renderCargoOnBlock(
                caddy, poseStack, buffer, light, overlay, partialTick, blockEntity);
    }
}
