package org.dddjava.jig.application.metrics;

import io.micrometer.core.instrument.Metrics;

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
        return Metrics.globalRegistry.timer(metricName, "phase", phase).record(operation);
    }

    public void measureVoid(String phase, Runnable operation) {
        Metrics.globalRegistry.timer(metricName, "phase", phase).record(operation);
    }
}
