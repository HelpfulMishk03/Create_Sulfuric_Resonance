package io.hxneyw.repo.content.logic;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class LogicEvaluator {

    private LogicEvaluator() {
    }

    public static boolean matches(
            int measuredValue,
            @NotNull LogicCondition condition
    ) {
        Objects.requireNonNull(condition);

        return switch (condition.relation()) {
            case AT_OR_ABOVE ->
                    measuredValue >= condition.lowerThreshold();
            case BELOW ->
                    measuredValue < condition.lowerThreshold();
            case BETWEEN ->
                    measuredValue >= condition.lowerThreshold()
                            && measuredValue <= condition.upperThreshold();
            case MATCH ->
                    measuredValue == condition.lowerThreshold();
        };
    }

    public static LogicEvaluation evaluate(
            int previousValue,
            int currentValue,
            @NotNull LogicCondition condition,
            @NotNull LogicResponse response
    ) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(response);

        boolean previousMatch = matches(
                previousValue,
                condition
        );

        boolean currentMatch = matches(
                currentValue,
                condition
        );

        boolean pulse = switch (response) {
            case HOLD -> false;
            case PULSE_ON_ENTER ->
                    !previousMatch && currentMatch;
            case PULSE_ON_EXIT ->
                    previousMatch && !currentMatch;
            case PULSE_ON_CHANGE ->
                    previousMatch != currentMatch;
        };

        boolean outputActive = response == LogicResponse.HOLD
                ? currentMatch
                : pulse;

        return new LogicEvaluation(
                currentMatch,
                outputActive,
                pulse
        );
    }
}
