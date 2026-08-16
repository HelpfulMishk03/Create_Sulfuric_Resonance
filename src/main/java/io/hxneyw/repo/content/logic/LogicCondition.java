package io.hxneyw.repo.content.logic;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record LogicCondition(
        @NotNull LogicSource source,
        @NotNull LogicRelation relation,
        int lowerThreshold,
        int upperThreshold
) {
    public LogicCondition {
        Objects.requireNonNull(source);
        Objects.requireNonNull(relation);

        if (relation == LogicRelation.BETWEEN
                && upperThreshold < lowerThreshold) {
            throw new IllegalArgumentException(
                    "upperThreshold must be greater than or equal to lowerThreshold"
            );
        }
    }

    public static LogicCondition highTrip(
            @NotNull LogicSource source,
            int threshold
    ) {
        return new LogicCondition(
                source,
                LogicRelation.AT_OR_ABOVE,
                threshold,
                threshold
        );
    }

    public static LogicCondition lowTrip(
            @NotNull LogicSource source,
            int threshold
    ) {
        return new LogicCondition(
                source,
                LogicRelation.BELOW,
                threshold,
                threshold
        );
    }

    public static LogicCondition operatingBand(
            @NotNull LogicSource source,
            int low,
            int high
    ) {
        return new LogicCondition(
                source,
                LogicRelation.BETWEEN,
                low,
                high
        );
    }

    public static LogicCondition stateMatch(
            @NotNull LogicSource source,
            int state
    ) {
        return new LogicCondition(
                source,
                LogicRelation.MATCH,
                state,
                state
        );
    }
}
