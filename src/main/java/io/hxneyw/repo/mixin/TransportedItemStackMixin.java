package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import io.hxneyw.repo.content.transport.CaddyBeltTransition;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TransportedItemStack.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TransportedItemStackMixin implements CaddyBeltTransition {
    @Unique
    @Nullable
    private Vec3 sulfuricresonance$previousWorldPosition;

    @Unique
    private long sulfuricresonance$transitionTick = Long.MIN_VALUE;

    @Override
    @Nullable
    public Vec3 sulfuricresonance$getPreviousWorldPosition() {
        return sulfuricresonance$previousWorldPosition;
    }

    @Override
    public long sulfuricresonance$getTransitionTick() {
        return sulfuricresonance$transitionTick;
    }

    @Override
    public void sulfuricresonance$setTransition(Vec3 previousWorldPosition, long gameTick) {
        sulfuricresonance$previousWorldPosition = previousWorldPosition;
        sulfuricresonance$transitionTick = gameTick;
    }

    @Inject(method = "getSimilar", at = @At("TAIL"), remap = false)
    private void sulfuricresonance$copyTransitionData(
            CallbackInfoReturnable<TransportedItemStack> cir
    ) {
        if (cir.getReturnValue() instanceof CaddyBeltTransition copy
                && sulfuricresonance$previousWorldPosition != null) {
            copy.sulfuricresonance$setTransition(
                    sulfuricresonance$previousWorldPosition,
                    sulfuricresonance$transitionTick
            );
        }
    }

    @Inject(method = "serializeNBT", at = @At("TAIL"), remap = false)
    private void sulfuricresonance$writeTransitionData(
            HolderLookup.Provider registries,
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        if (sulfuricresonance$previousWorldPosition == null) {
            return;
        }
        CompoundTag tag = cir.getReturnValue();
        tag.putDouble("CSRTransitionX", sulfuricresonance$previousWorldPosition.x);
        tag.putDouble("CSRTransitionY", sulfuricresonance$previousWorldPosition.y);
        tag.putDouble("CSRTransitionZ", sulfuricresonance$previousWorldPosition.z);
        tag.putLong("CSRTransitionTick", sulfuricresonance$transitionTick);
    }

    @Inject(method = "read", at = @At("TAIL"), remap = false)
    private static void sulfuricresonance$readTransitionData(
            CompoundTag tag,
            HolderLookup.Provider registries,
            CallbackInfoReturnable<TransportedItemStack> cir
    ) {
        if (!tag.contains("CSRTransitionX")
                || !tag.contains("CSRTransitionY")
                || !tag.contains("CSRTransitionZ")
                || !tag.contains("CSRTransitionTick")) {
            return;
        }
        if (cir.getReturnValue() instanceof CaddyBeltTransition transition) {
            transition.sulfuricresonance$setTransition(
                    new Vec3(
                            tag.getDouble("CSRTransitionX"),
                            tag.getDouble("CSRTransitionY"),
                            tag.getDouble("CSRTransitionZ")
                    ),
                    tag.getLong("CSRTransitionTick")
            );
        }
    }
}
