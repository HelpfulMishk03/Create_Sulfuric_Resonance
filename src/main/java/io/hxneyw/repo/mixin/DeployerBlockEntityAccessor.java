package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = DeployerBlockEntity.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface DeployerBlockEntityAccessor {
    @Accessor("timer")
    int sulfuricresonance$getTimer();

    @Accessor("redstoneLocked")
    boolean sulfuricresonance$isRedstoneLocked();

    @Invoker("start")
    void sulfuricresonance$start();
}
