package io.hxneyw.repo.mixin;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FactoryPanelBlock.class, remap = false)
public abstract class FactoryPanelBlockMixin {

    @Inject(method = "onDestroyedByPlayer", at = @At("HEAD"), cancellable = true)
    private void sulfuricresonance$removeThermalGaugeOnly(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            boolean willHarvest,
            FluidState fluid,
            CallbackInfoReturnable<Boolean> cir
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ThermalGaugeBlockEntity gauge)
                || gauge.activePanels() < 2) {
            return;
        }

        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1.0D;
        HitResult hitResult = player.pick(range, 1.0F, false);
        FactoryPanelBlock.PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                pos,
                state,
                hitResult.getLocation()
        );

        if (!gauge.hasGauge(slot)) {
            return;
        }

        if (level.isClientSide) {
            cir.setReturnValue(false);
            return;
        }

        ItemStack drop = gauge.createItemStack(slot);
        if (!gauge.removeGauge(slot)) {
            return;
        }

        if (!player.isCreative()) {
            Block.popResource(level, pos, drop);
        }

        cir.setReturnValue(false);
    }

}
