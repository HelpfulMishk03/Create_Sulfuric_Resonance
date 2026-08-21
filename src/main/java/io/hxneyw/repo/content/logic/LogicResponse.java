package io.hxneyw.repo.content.logic;

import org.jetbrains.annotations.NotNull;

public enum LogicResponse {
    HOLD("hold"),
    PULSE_ON_ENTER("pulse_on_enter"),
    PULSE_ON_EXIT("pulse_on_exit"),
    PULSE_ON_CHANGE("pulse_on_change");

    private final String serializedId;

    LogicResponse(@NotNull String serializedId) {
        this.serializedId = serializedId;
    }

    public String serializedId() {
        return this.serializedId;
    }

    public static LogicResponse fromSerializedId(
            @NotNull String serializedId
    ) {
        for (LogicResponse response : values()) {
            if (response.serializedId.equals(serializedId)) {
                return response;
            }
        }

        return HOLD;
    }
}
