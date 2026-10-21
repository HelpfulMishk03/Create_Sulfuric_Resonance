package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import io.hxneyw.repo.content.transport.CaddyBeltProcessing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltInventory.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BeltInventoryMixin {
    @Shadow @Final BeltBlockEntity belt;
    @Shadow boolean beltMovementPositive;

    @Inject(method = "handleBeltProcessingAndCheckIfRemoved", at = @At("HEAD"), cancellable = true,
            remap = false)
    private void sulfuricresonance$processBlockCaddies(TransportedItemStack currentItem, float nextOffset,
                                                        boolean noMovement,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (belt.getLevel() == null) return;
        if (currentItem.locked) {
            if (CaddyBeltProcessing.tickHeld(belt.getLevel(), belt,
                    (BeltInventory) (Object) this, currentItem)) {
                cir.setReturnValue(false);
            }
            return;
        }

        if (belt.getSpeed() == 0) return;
        // Create's belt processors trigger when a carrier reaches a segment
        // center. Retry only an item already centered there (for example, one
        // released from a queue); checking the whole current segment every tick
        // made Caddies lock as soon as they entered it and snap under machines.
        int currentSegment = (int) Math.floor(currentItem.beltPosition);
        if (Math.abs(currentItem.beltPosition - (currentSegment + .5f)) <= 1 / 128f
                && CaddyBeltProcessing.startAt(belt.getLevel(), belt, (BeltInventory) (Object) this,
                currentItem, currentSegment, beltMovementPositive)) {
            cir.setReturnValue(false);
            return;
        }
        if (noMovement) return;
        int firstUpcomingSegment = (int) (currentItem.beltPosition + (beltMovementPositive ? .5f : -.5f));
        int step = beltMovementPositive ? 1 : -1;
        for (int segment = firstUpcomingSegment;
             beltMovementPositive ? segment + .5f <= nextOffset : segment + .5f >= nextOffset;
             segment += step) {
            if (CaddyBeltProcessing.startAt(belt.getLevel(), belt, (BeltInventory) (Object) this,
                    currentItem, segment,
                    beltMovementPositive)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
