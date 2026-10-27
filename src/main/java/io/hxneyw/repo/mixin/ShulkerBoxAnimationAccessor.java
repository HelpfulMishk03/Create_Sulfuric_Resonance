package io.hxneyw.repo.mixin;

import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ShulkerBoxBlockEntity.class, remap = false)
public interface ShulkerBoxAnimationAccessor {
    @Accessor("progress")
    void sulfuricresonance$setProgress(float progress);

    @Accessor("progressOld")
    void sulfuricresonance$setProgressOld(float progress);

    @Accessor("animationStatus")
    void sulfuricresonance$setAnimationStatus(ShulkerBoxBlockEntity.AnimationStatus status);
}
