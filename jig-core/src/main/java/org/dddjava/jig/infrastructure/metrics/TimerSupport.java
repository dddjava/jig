package org.dddjava.jig.infrastructure.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

import java.util.function.Supplier;

public class TimerSupport {

    private final String metricName;

    private TimerSupport(String metricName) {
        this.metricName = metricName;
    }

    public static TimerSupport of(String metricName) {
        return new TimerSupport(metricName);
    }

    public <T> T measure(String phase, Supplier<T> operation) {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            return operation.get();
        } finally {
            sample.stop(Timer.builder(metricName)
                    .tag("phase", phase)
                    .register(Metrics.globalRegistry));
        }
    }

    public void measureVoid(String phase, Runnable operation) {
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        try {
            operation.run();
        } finally {
            sample.stop(Timer.builder(metricName)
                    .tag("phase", phase)
                    .register(Metrics.globalRegistry));
        }
    }
}
