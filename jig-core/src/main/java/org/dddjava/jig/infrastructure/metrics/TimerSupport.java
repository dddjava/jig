package org.dddjava.jig.infrastructure.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.function.Supplier;

public class TimerSupport {
    private static final String METRIC_NAME = "jig.analysis.time";

    /**
     * Executes the given operation and measures its execution time.
     *
     * @param phase       The phase name to be used as a tag
     * @param description Description of the operation
     * @param operation   The operation to be executed and measured
     * @return The result of the operation
     */
    public static <T> T measure(String phase, String description, Supplier<T> operation) {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            return operation.get();
        } finally {
            sample.stop(Timer.builder(METRIC_NAME)
                    .description(description)
                    .tag("phase", phase)
                    .register(Metrics.globalRegistry));
        }
    }

    /**
     * Executes the given operation and measures its execution time.
     *
     * @param phase       The phase name to be used as a tag
     * @param description Description of the operation
     * @param operation   The operation to be executed and measured
     */
    public static void measureVoid(String phase, String description, Runnable operation) {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            operation.run();
        } finally {
            sample.stop(Timer.builder(METRIC_NAME)
                    .description(description)
                    .tag("phase", phase)
                    .register(Metrics.globalRegistry));
        }
    }
}
