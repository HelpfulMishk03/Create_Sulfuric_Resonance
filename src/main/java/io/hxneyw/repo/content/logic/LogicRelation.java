package io.hxneyw.repo.content.logic;

import org.jetbrains.annotations.NotNull;

public enum LogicRelation {
    AT_OR_ABOVE("at_or_above"),
    BELOW("below"),
    BETWEEN("between"),
    MATCH("match");

    private final String serializedId;

    LogicRelation(@NotNull String serializedId) {
        this.serializedId = serializedId;
    }

    public String serializedId() {
        return this.serializedId;
    }

    public static LogicRelation fromSerializedId(
            @NotNull String serializedId
    ) {
        for (LogicRelation relation : values()) {
            if (relation.serializedId.equals(serializedId)) {
                return relation;
            }
        }

        return AT_OR_ABOVE;
    }
}
