package org.dddjava.jig.application.metrics;

import java.util.function.Supplier;

public class Metrics {

    private final String metricName;

    private Metrics(String metricName) {
        this.metricName = metricName;
    }

    public static Metrics of(String metricName) {
        return new Metrics(metricName);
    }

    public <T> T measure(String phase, Supplier<T> operation) {
        return io.micrometer.core.instrument.Metrics.globalRegistry.timer(metricName, "phase", phase).record(operation);
    }

    public void measureVoid(String phase, Runnable operation) {
        io.micrometer.core.instrument.Metrics.globalRegistry.timer(metricName, "phase", phase).record(operation);
    }
}
