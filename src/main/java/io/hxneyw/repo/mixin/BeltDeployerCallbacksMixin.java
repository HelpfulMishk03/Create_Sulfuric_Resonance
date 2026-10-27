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
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
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
@SuppressWarnings({"unused", "DataFlowIssue"})
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
        ItemStack cargo = BlockCaddyItem.getCargoStack(transported.stack);
        if (!cargo.isEmpty()) {
            if (deployer.getSpeed() == 0
                    || !deployer.getBlockState().hasProperty(DirectionalKineticBlock.FACING)
                    || deployer.getBlockState().getValue(DirectionalKineticBlock.FACING) != Direction.DOWN
                    || !hasEnumValue(CSR_MODE, deployer, "USE")) {
                cir.setReturnValue(ProcessingResult.PASS);
            } else if (!hasEnumValue(CSR_STATE, deployer, "WAITING")) {
                cir.setReturnValue(ProcessingResult.HOLD);
            } else if (accessor.sulfuricresonance$isRedstoneLocked()
                    || deployer.getPlayer() == null
                    || deployer.getPlayer().getMainHandItem().isEmpty()
                    || deployer.getRecipe(cargo) == null) {
                cir.setReturnValue(ProcessingResult.PASS);
            } else {
                accessor.sulfuricresonance$start();
                cir.setReturnValue(ProcessingResult.HOLD);
            }
            return;
        }
        if (isNotEligibleForCaddyDeployerProcessing(transported.stack, deployer)
                || accessor.sulfuricresonance$isRedstoneLocked()) {
            // Create holds belt items with an empty Deployer hand. Caddies are
            // handled only by the loader below, so invalid/full Caddies must pass.
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }

        DeployerFakePlayer player = deployer.getPlayer();
        ItemStack held = player == null ? ItemStack.EMPTY : player.getMainHandItem();
        if (hasEnumValue(CSR_STATE, deployer, "WAITING")
                && held.getItem() instanceof BlockItem) {
            accessor.sulfuricresonance$start();
        }
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
        ItemStack cargo = BlockCaddyItem.getCargoStack(transported.stack);
        if (!cargo.isEmpty()) {
            processLoadedCaddy(transported, handler, deployer, accessor, cir);
            return;
        }
        if (isNotEligibleForCaddyDeployerProcessing(transported.stack, deployer)
                || accessor.sulfuricresonance$isRedstoneLocked()) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }

        DeployerFakePlayer player = deployer.getPlayer();
        if (player == null) {
            cir.setReturnValue(ProcessingResult.HOLD);
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem) || held.isEmpty()) {
            cir.setReturnValue(ProcessingResult.HOLD);
            return;
        }

        if (hasEnumValue(CSR_STATE, deployer, "WAITING")) {
            accessor.sulfuricresonance$start();
            cir.setReturnValue(ProcessingResult.HOLD);
            return;
        }
        if (!hasEnumValue(CSR_STATE, deployer, "RETRACTING")
                || accessor.sulfuricresonance$getTimer() != 1000) {
            cir.setReturnValue(ProcessingResult.HOLD);
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

    private static boolean isCaddyProcessor(DeployerBlockEntity deployer, DeployerBlockEntityAccessor accessor) {
        return deployer.getSpeed() != 0
                && deployer.getBlockState().hasProperty(DirectionalKineticBlock.FACING)
                && deployer.getBlockState().getValue(DirectionalKineticBlock.FACING) == Direction.DOWN
                && hasEnumValue(CSR_MODE, deployer, "USE")
                && hasEnumValue(CSR_STATE, deployer, "WAITING")
                && !accessor.sulfuricresonance$isRedstoneLocked();
    }

    private static void processLoadedCaddy(
            TransportedItemStack transported,
            TransportedItemStackHandlerBehaviour handler,
            DeployerBlockEntity deployer,
            DeployerBlockEntityAccessor accessor,
            CallbackInfoReturnable<ProcessingResult> cir
    ) {
        if (deployer.getSpeed() == 0
                || !deployer.getBlockState().hasProperty(DirectionalKineticBlock.FACING)
                || deployer.getBlockState().getValue(DirectionalKineticBlock.FACING) != Direction.DOWN
                || !hasEnumValue(CSR_MODE, deployer, "USE")
                || (hasEnumValue(CSR_STATE, deployer, "WAITING")
                && accessor.sulfuricresonance$isRedstoneLocked())) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }
        net.minecraft.world.level.Level level = deployer.getLevel();
        if (level == null) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }
        ItemStack cargo = BlockCaddyItem.getCargoStack(transported.stack);
        DeployerFakePlayer player = deployer.getPlayer();
        if (player == null) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }
        ItemStack held = player.getMainHandItem();
        RecipeHolder<? extends Recipe<?>> recipe = deployer.getRecipe(cargo);
        if (recipe == null || held.isEmpty()) {
            cir.setReturnValue(ProcessingResult.PASS);
            return;
        }
        if (hasEnumValue(CSR_STATE, deployer, "WAITING")) {
            accessor.sulfuricresonance$start();
            cir.setReturnValue(ProcessingResult.HOLD);
            return;
        }
        if (hasEnumValue(CSR_STATE, deployer, "RETRACTING") && accessor.sulfuricresonance$getTimer() == 1000) {
            var outputs = RecipeApplier.applyRecipeOn(level, cargo.copyWithCount(1), recipe.value(), true);
            if (outputs.size() != 1) {
                cir.setReturnValue(ProcessingResult.PASS);
                return;
            }
            ItemStack output = outputs.getFirst();
            if (output.getCount() != 1 || !(output.getItem() instanceof BlockItem)) {
                cir.setReturnValue(ProcessingResult.PASS);
                return;
            }
            ItemStack resultCaddy = transported.stack.copyWithCount(1);
            BlockCaddyItem.setCargoStack(resultCaddy, output);
            TransportedItemStack processed = transported.copy();
            processed.stack = resultCaddy;
            processed.locked = false;
            ItemStack remainder = transported.stack.copy();
            remainder.shrink(1);
            TransportedResult result;
            if (remainder.isEmpty()) {
                result = TransportedResult.convertTo(processed);
            } else {
                TransportedItemStack remainingCarrier = transported.copy();
                remainingCarrier.stack = remainder;
                remainingCarrier.locked = false;
                result = TransportedResult.convertToAndLeaveHeld(
                        java.util.List.of(processed), remainingCarrier);
            }
            transported.clearFanProcessingData();
            handler.handleProcessingOnItem(transported, result);

            boolean keepHeld = recipe.value() instanceof ItemApplicationRecipe application
                    && application.shouldKeepHeldItem();
            if (!keepHeld) {
                if (held.getMaxDamage() > 0) {
                    held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                } else {
                    ItemStack leftover = held.getCraftingRemainingItem();
                    held.shrink(1);
                    if (held.isEmpty()) player.setItemInHand(InteractionHand.MAIN_HAND, leftover);
                    else if (!player.getInventory().add(leftover)) player.drop(leftover, false);
                }
            }
            level.playSound(null, deployer.getBlockPos(), SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS, .25f, .75f);
            deployer.notifyUpdate();
        }
        cir.setReturnValue(ProcessingResult.HOLD);
    }
    private static boolean isNotEligibleForCaddyDeployerProcessing(
            ItemStack transported,
            DeployerBlockEntity deployer
    ) {
        return !hasEnumValue(CSR_MODE, deployer, "USE")
                || deployer.getSpeed() == 0
                || !deployer.getBlockState().hasProperty(DirectionalKineticBlock.FACING)
                || deployer.getBlockState().getValue(DirectionalKineticBlock.FACING) != Direction.DOWN
                || !(transported.getItem() instanceof BlockCaddyItem)
                || BlockCaddyItem.getCargoItem(transported) != null;
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
