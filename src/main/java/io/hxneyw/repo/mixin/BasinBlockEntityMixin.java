package io.hxneyw.repo.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to BasinBlockEntity - kept for potential future debugging
 * Currently unused but confirms mixin system is working
 */
@Mixin(value = BasinBlockEntity.class, remap = false)
public class BasinBlockEntityMixin {
    // Mixin loaded successfully - no active hooks needed
}