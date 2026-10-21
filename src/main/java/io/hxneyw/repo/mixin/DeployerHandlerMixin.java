package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerHandler;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeployerHandler.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DeployerHandlerMixin {

    @Inject(method = "activateInner", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sulfuricresonance$loadCaddyBeforeDeployerUseFiltering(
            DeployerFakePlayer player,
            Vec3 deployerPosition,
            BlockPos targetPos,
            Vec3 extensionVector,
            @Coerce Object mode,
            CallbackInfo ci
    ) {
        if (mode == null || !"USE".equals(mode.toString())) {
            return;
        }

        BlockState targetState = player.level().getBlockState(targetPos);
        if (!AllBlocks.BELT.has(targetState)) {
            return;
        }

        ItemStack blockStack = player.getMainHandItem();
        if (!(blockStack.getItem() instanceof BlockItem)) {
            return;
        }

        BeltBlockEntity segment = BeltHelper.getSegmentBE(
                player.level(),
                targetPos
        );
        if (segment == null) {
            return;
        }
        BeltBlockEntity controller = segment.getControllerBE();
        if (controller == null || controller.getInventory() == null) {
            return;
        }

        AtomicBoolean loaded = new AtomicBoolean(false);
        controller.getInventory().applyToEachWithin(
                segment.index + .5f,
                .55f,
                transported -> {
                    if (loaded.get()
                            || !(transported.stack.getItem() instanceof BlockCaddyItem)
                            || !BlockCaddyItem.tryLoadFromBlockItem(
                                    transported.stack,
                                    blockStack
                            )) {
                        return TransportedResult.doNothing();
                    }

                    blockStack.shrink(1);
                    loaded.set(true);
                    return TransportedResult.convertTo(transported.copy());
                }
        );

        if (!loaded.get()) {
            return;
        }
        controller.setChanged();
        controller.sendData();
        ci.cancel();
    }
}
