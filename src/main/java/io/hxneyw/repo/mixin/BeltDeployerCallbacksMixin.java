package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import io.hxneyw.repo.content.items.BlockCaddyItem;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import java.lang.reflect.Field;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BeltDeployerCallbacks.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BeltDeployerCallbacksMixin {
    private static final Field CSR_STATE = findField("state");
    private static final Field CSR_MODE = findField("mode");

    @Inject(method = "onItemReceived", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sulfuricresonance$startForEmptyCaddy(
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            CallbackInfoReturnable<ProcessingResult> cir
    ) {
        if (!(transported.stack.getItem() instanceof BlockCaddyItem)) {
            return;
        }

        DeployerBlockEntityAccessor accessor = (DeployerBlockEntityAccessor) deployer;
        if (!isCaddyLoadTarget(transported.stack, deployer, accessor)
                || !hasEnumValue(CSR_STATE, deployer, "WAITING")
                || accessor.sulfuricresonance$isRedstoneLocked()) {
            // Create holds belt items with an empty Deployer hand. Caddies are
            // handled only by the loader below, so invalid/full Caddies must pass.
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }

        accessor.sulfuricresonance$start();
        cir.setReturnValue(ProcessingResult.HOLD);
    }

    @Inject(method = "whenItemHeld", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sulfuricresonance$putBlockIntoCaddy(
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            CallbackInfoReturnable<ProcessingResult> cir
    ) {
        if (!(transported.stack.getItem() instanceof BlockCaddyItem)) {
            return;
        }

        DeployerBlockEntityAccessor accessor = (DeployerBlockEntityAccessor) deployer;
        if (!hasEnumValue(CSR_MODE, deployer, "USE")
                || !hasEnumValue(CSR_STATE, deployer, "RETRACTING")
                || BlockCaddyItem.getCargoItem(transported.stack) != null) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }

        DeployerFakePlayer player = deployer.getPlayer();
        if (player == null) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem) || held.isEmpty()) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }

        int caddyCount = transported.stack.getCount();
        ItemStack filledCaddyStack = transported.stack.copyWithCount(1);
        if (!BlockCaddyItem.tryLoadFromBlockItem(filledCaddyStack, held.copyWithCount(1))) {
            return;
        }

        TransportedItemStack filledCaddy = transported.copy();
        filledCaddy.stack = filledCaddyStack;
        filledCaddy.locked = false;
        TransportedResult result;
        if (caddyCount == 1) {
            result = TransportedResult.convertTo(filledCaddy);
        } else {
            TransportedItemStack remainingCaddies = transported.copy();
            remainingCaddies.stack = transported.stack.copyWithCount(caddyCount - 1);
            remainingCaddies.locked = false;
            result = TransportedResult.convertToAndLeaveHeld(
                    List.of(filledCaddy),
                    remainingCaddies
            );
        }

        held.shrink(1);
        handler.handleProcessingOnItem(transported, result);
        deployer.notifyUpdate();
        BeltBlockEntity segment = BeltHelper.getSegmentBE(
                deployer.getLevel(),
                deployer.getBlockPos().below(2)
        );
        if (segment != null) {
            BeltBlockEntity controller = segment.getControllerBE();
            if (controller != null) {
                controller.setChanged();
                controller.sendData();
            }
        }
        cir.setReturnValue(ProcessingResult.HOLD);
    }

    private static boolean isCaddyLoadTarget(
            ItemStack transported,
            DeployerBlockEntity deployer,
            DeployerBlockEntityAccessor accessor
    ) {
        if (!hasEnumValue(CSR_MODE, deployer, "USE")
                || deployer.getSpeed() == 0
                || !deployer.getBlockState().hasProperty(DirectionalKineticBlock.FACING)
                || deployer.getBlockState().getValue(DirectionalKineticBlock.FACING) != Direction.DOWN
                || !(transported.getItem() instanceof BlockCaddyItem)
                || BlockCaddyItem.getCargoItem(transported) != null) {
            return false;
        }
        DeployerFakePlayer player = deployer.getPlayer();
        return player != null && player.getMainHandItem().getItem() instanceof BlockItem;
    }

    private static Field findField(String name) {
        try {
            Field field = DeployerBlockEntity.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static boolean hasEnumValue(Field field, DeployerBlockEntity deployer, String expected) {
        try {
            Object value = field.get(deployer);
            return value != null && expected.equals(value.toString());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to inspect Create deployer state", exception);
        }
    }
}
