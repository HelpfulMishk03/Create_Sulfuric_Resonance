package io.hxneyw.repo.content.transport;

import net.minecraft.world.phys.Vec3;

/** Per-transport rendering metadata; deliberately kept off the carried ItemStack. */
public interface CaddyBeltTransition {
    Vec3 sulfuricresonance$getPreviousWorldPosition();

    long sulfuricresonance$getTransitionTick();

    void sulfuricresonance$setTransition(Vec3 previousWorldPosition, long gameTick);
}
