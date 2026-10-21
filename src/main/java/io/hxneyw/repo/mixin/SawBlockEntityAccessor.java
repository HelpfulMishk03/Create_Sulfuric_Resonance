package io.hxneyw.repo.mixin;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SawBlockEntity.class, remap = false)
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface SawBlockEntityAccessor {
    @Accessor("playEvent")
    void sulfuricresonance$setPlayEvent(ItemStack stack);
}
