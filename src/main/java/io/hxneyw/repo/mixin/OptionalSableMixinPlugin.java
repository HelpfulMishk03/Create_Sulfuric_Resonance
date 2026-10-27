package io.hxneyw.repo.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

@SuppressWarnings("unused")
public final class OptionalSableMixinPlugin implements IMixinConfigPlugin {
    private static final String SABLE_ACTOR =
            "dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor";
    private static final String SABLE_COLLISION =
            "dev.ryanhcode.sable.api.block.BlockSubLevelCollisionShape";

    @Override
    public void onLoad(String mixinPackage) {
        org.slf4j.LoggerFactory.getLogger("CSRDiagnostics").info(
                "CSR_DIAGNOSTICS revision=scene-light-4-full-nbt mixin package={}", mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1).startsWith("Sable")) {
            return true;
        }
        String api = mixinClassName.endsWith("SableCaddyCollisionMixin")
                ? SABLE_COLLISION
                : SABLE_ACTOR;
        boolean available = getClass().getClassLoader().getResource(api.replace('.', '/') + ".class") != null;
        org.slf4j.LoggerFactory.getLogger("CSRDiagnostics").info(
                "CSR_MIXIN {} enabled={}", mixinClassName, available);
        return available;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }
}
