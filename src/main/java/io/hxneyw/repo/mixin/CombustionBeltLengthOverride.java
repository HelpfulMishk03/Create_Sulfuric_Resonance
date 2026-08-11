package io.hxneyw.repo.mixin;

import io.hxneyw.repo.Config;

public final class CombustionBeltLengthOverride {

    private static final ThreadLocal<Integer> LENGTH_OVERRIDE = new ThreadLocal<>();

    private CombustionBeltLengthOverride() {
    }

    public static Scope push() {
        LENGTH_OVERRIDE.set(Config.combustionBeltTraversalLimit());
        return LENGTH_OVERRIDE::remove;
    }

    public static Integer get() {
        return LENGTH_OVERRIDE.get();
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
