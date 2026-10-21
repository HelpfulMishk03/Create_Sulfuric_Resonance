package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Loads exactly one transported Caddy per physical Deployer activation. */
@Mixin(value = DeployerBlockEntity.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DeployerBlockEntityMixin {
    private static final Field MODE = findModeField();

    @Inject(method = "activate", at = @At("HEAD"), cancellable = true, remap = false)
    private void sulfuricresonance$loadBlockIntoCaddy(CallbackInfo ci) {
        DeployerBlockEntity deployer = (DeployerBlockEntity) (Object) this;
        if (deployer.getLevel() == null || deployer.getLevel().isClientSide
                || deployer.getSpeed() == 0) {
            return;
        }

        if (!isUseMode(deployer)
                || !deployer.getBlockState().hasProperty(DirectionalKineticBlock.FACING)
                || deployer.getBlockState().getValue(DirectionalKineticBlock.FACING) != Direction.DOWN) {
            return;
        }

        DeployerFakePlayer player = deployer.getPlayer();
        if (player == null || !(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        BlockPos target = deployer.getBlockPos().below(2);
        AtomicBoolean loaded = new AtomicBoolean(false);

        java.util.function.Function<TransportedItemStack, TransportedResult> loadOne = transported -> {
            if (loaded.get() || !(transported.stack.getItem() instanceof BlockCaddyItem)
                    || BlockCaddyItem.getCargoItem(transported.stack) != null
                    || held.isEmpty()) {
                return null;
            }

            int caddyCount = transported.stack.getCount();
            ItemStack filledStack = transported.stack.copyWithCount(1);
            if (!BlockCaddyItem.tryLoadFromBlockItem(filledStack, held.copyWithCount(1))) {
                return null;
            }

            TransportedItemStack filled = transported.copy();
            filled.stack = filledStack;
            filled.locked = false;
            loaded.set(true);
            held.shrink(1);

            if (caddyCount == 1) {
                return TransportedResult.convertTo(filled);
            }

            TransportedItemStack remaining = transported.copy();
            remaining.stack = transported.stack.copyWithCount(caddyCount - 1);
            remaining.locked = false;
            return TransportedResult.convertToAndLeaveHeld(List.of(filled), remaining);
        };

        BeltBlockEntity segment = BeltHelper.getSegmentBE(deployer.getLevel(), target);
        if (segment != null) {
            BeltBlockEntity controller = segment.getControllerBE();
            if (controller != null && controller.getInventory() != null) {
                controller.getInventory().applyToEachWithin(segment.index + .5f, .55f, loadOne);
                if (loaded.get()) {
                    controller.setChanged();
                    controller.sendData();
                }
            }
        } else {
            TransportedItemStackHandlerBehaviour handler = BlockEntityBehaviour.get(
                    deployer.getLevel(), target, TransportedItemStackHandlerBehaviour.TYPE);
            if (handler != null) {
                handler.handleCenteredProcessingOnAllItems(.51f, loadOne);
            }
        }

        if (!loaded.get()) {
            return;
        }
        deployer.notifyUpdate();
        ci.cancel();
    }

    private static Field findModeField() {
        try {
            Field field = DeployerBlockEntity.class.getDeclaredField("mode");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static boolean isUseMode(DeployerBlockEntity deployer) {
        try {
            Object mode = MODE.get(deployer);
            return mode != null && "USE".equals(mode.toString());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read Create deployer mode", exception);
        }
    }
}
