package io.hxneyw.repo.content.logic;

import org.jetbrains.annotations.NotNull;

public enum ThermalLogicLevel {
    UNHEATED("unheated", 0),
    HEATED("heated", 1),
    SUPERHEATED("superheated", 2),
    COMBUSTION("combustion", 3);

    private final String serializedId;
    private final int value;

    ThermalLogicLevel(
            @NotNull String serializedId,
            int value
    ) {
        this.serializedId = serializedId;
        this.value = value;
    }

    public String serializedId() {
        return this.serializedId;
    }

    public int value() {
        return this.value;
    }

    public static ThermalLogicLevel fromSerializedId(
            @NotNull String serializedId
    ) {
        for (ThermalLogicLevel level : values()) {
            if (level.serializedId.equals(serializedId)) {
                return level;
            }
        }

        return UNHEATED;
    }
}
