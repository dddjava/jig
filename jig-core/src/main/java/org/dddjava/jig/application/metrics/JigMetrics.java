package org.dddjava.jig.application.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.function.Supplier;

public class JigMetrics {
    private static final Logger logger = LoggerFactory.getLogger(JigMetrics.class);

    private final String metricName;

    private JigMetrics(String metricName) {
        this.metricName = metricName;
    }

    public static JigMetrics of(String metricName) {
        return new JigMetrics(metricName);
    }

    public static Closeable init(Configuration configuration) {
        var registry = Metrics.globalRegistry;
        registry.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));

        new UptimeMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        var jvmGcMetrics = new JvmGcMetrics();
        jvmGcMetrics.bindTo(registry);

        return new Closeable(configuration, jvmGcMetrics);
    }

    static public class Closeable implements AutoCloseable {
        private final Configuration configuration;
        private final AutoCloseable[] closeables;

        public Closeable(Configuration configuration, AutoCloseable... closeables) {
            this.configuration = configuration;
            this.closeables = closeables;
        }

        @Override
        public void close() {
            for (var closeable : closeables) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    logger.warn("Failed to close {}", closeable, e);
                }
            }

            // メトリクスを出力
            JigDocumentGenerator jigDocumentGenerator = configuration.jigDocumentGenerator();
            jigDocumentGenerator.close(outputDirectory -> {
                String metricsFilePath = "jig-metrics.txt";
                var path = outputDirectory.resolve(metricsFilePath);
                try (var outputStream = Files.newOutputStream(path)) {
                    var globalRegistry = io.micrometer.core.instrument.Metrics.globalRegistry;
                    globalRegistry.getRegistries().forEach(it -> {
                        if (it instanceof PrometheusMeterRegistry prometheusMeterRegistry) {
                            try {
                                prometheusMeterRegistry.scrape(outputStream);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    });
                    // このclose以降は記録されない
                    globalRegistry.close();
                } catch (IOException | UncheckedIOException e) {
                    logger.error("Failed to export metrics to file: {}", path, e);
                }
            });
        }
    }

    public <T> T measure(String phase, Supplier<T> operation) {
        return io.micrometer.core.instrument.Metrics.globalRegistry.timer(metricName, "phase", phase).record(operation);
    }

    public void measureVoid(String phase, Runnable operation) {
        io.micrometer.core.instrument.Metrics.globalRegistry.timer(metricName, "phase", phase).record(operation);
    }
}
