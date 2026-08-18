package io.hxneyw.repo.content.process;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public enum ProcessState {
    IDLE("idle", "IDLE"),
    READY("ready", "READY"),
    PROCESSING("processing", "PROCESS"),
    BLOCKED("blocked", "BLOCKED");

    private final String serializedName;
    private final String rendererLabel;

    ProcessState(String serializedName, String rendererLabel) {
        this.serializedName = serializedName;
        this.rendererLabel = rendererLabel;
    }

    public @NotNull String serializedName() {
        return serializedName;
    }

    public @NotNull String rendererLabel() {
        return rendererLabel;
    }



    public static @NotNull ProcessState fromSerializedName(String name) {
        if (name == null || name.isBlank()) {
            return IDLE;
        }

        String normalized = name.toLowerCase(Locale.ROOT);
        for (ProcessState state : values()) {
            if (state.serializedName.equals(normalized)) {
                return state;
            }
        }
        return IDLE;
    }

    public static @NotNull ProcessState fromOrdinal(int ordinal) {
        ProcessState[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return IDLE;
        }
        return values[ordinal];
    }
}
