package io.hxneyw.repo.content.logic;

import org.jetbrains.annotations.NotNull;

public enum LogicSource {
    THERMAL("thermal"),
    REDSTONE("redstone"),
    PROCESS_STATE("process_state");

    private final String serializedId;

    LogicSource(@NotNull String serializedId) {
        this.serializedId = serializedId;
    }

    public String serializedId() {
        return this.serializedId;
    }

    public static LogicSource fromSerializedId(
            @NotNull String serializedId
    ) {
        for (LogicSource source : values()) {
            if (source.serializedId.equals(serializedId)) {
                return source;
            }
        }

        return THERMAL;
    }
}
