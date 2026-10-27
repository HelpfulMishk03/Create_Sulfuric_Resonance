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
@SuppressWarnings({"unused", "DataFlowIssue", "ConstantValue"})
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
        if (!(currentItem.stack.getItem() instanceof io.hxneyw.repo.content.items.BlockCaddyItem)) return;
        // Empty Caddies use Create's native belt processing path so attached
        // deployers can hold them and the Caddy deployer callback can load them.
        // The custom station processor below only applies to Caddies with cargo.
        if (io.hxneyw.repo.content.items.BlockCaddyItem.getCargoStack(currentItem.stack).isEmpty()) return;
        // Loaded Caddies bypass native processing because custom station
        // processing owns their cargo state and timing.
        cir.setReturnValue(false);
        if (belt.getLevel() == null) return;
        if (CaddyBeltProcessing.hasPendingCapture(currentItem.stack)) {
            CaddyBeltProcessing.capturePendingArrival(belt.getLevel(), belt,
                    (BeltInventory) (Object) this, currentItem);
            cir.setReturnValue(false);
            return;
        }
        if (currentItem.locked) {
            if (CaddyBeltProcessing.tickHeld(belt.getLevel(), belt,
                    (BeltInventory) (Object) this, currentItem)) {
                cir.setReturnValue(false);
            }
            return;
        }

        if (belt.getSpeed() == 0) return;
        // A queued carrier can be released at a station center. This branch is
        // already at the natural Create position, so it can be held directly.
        int currentSegment = (int) Math.floor(currentItem.beltPosition);
        if ((currentItem.beltPosition > .5f || beltMovementPositive)
                && Math.abs(currentItem.beltPosition - (currentSegment + .5f)) <= 1 / 128f
                && CaddyBeltProcessing.startAt(belt.getLevel(), belt, (BeltInventory) (Object) this,
                currentItem, currentSegment, beltMovementPositive)) {
            cir.setReturnValue(false);
            return;
        }
        if (noMovement) return;
        if (!beltMovementPositive && currentItem.beltPosition <= .5f) return;
        int firstUpcomingSegment = beltMovementPositive
                ? (int) Math.floor(currentItem.beltPosition + .5f)
                : (int) Math.ceil(currentItem.beltPosition - .5f) - 1;
        int step = beltMovementPositive ? 1 : -1;
        for (int segment = firstUpcomingSegment;
             beltMovementPositive ? segment + .5f <= nextOffset : segment + .5f >= nextOffset;
             segment += step) {
            if (CaddyBeltProcessing.deferArrival(belt.getLevel(), belt, currentItem,
                    segment, beltMovementPositive)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
