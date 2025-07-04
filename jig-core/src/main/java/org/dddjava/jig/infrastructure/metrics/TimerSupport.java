package org.dddjava.jig.infrastructure.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.function.Supplier;

public class TimerSupport {
    private static final String METRIC_NAME = "jig.analysis.time";

    public static <T> T measure(String phase, Supplier<T> operation) {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            return operation.get();
        } finally {
            sample.stop(Timer.builder(METRIC_NAME)
                    .tag("phase", phase)
                    .register(Metrics.globalRegistry));
        }
    }

    public static void measureVoid(String phase, Runnable operation) {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            operation.run();
        } finally {
            sample.stop(Timer.builder(METRIC_NAME)
                    .tag("phase", phase)
                    .register(Metrics.globalRegistry));
        }
    }
}
