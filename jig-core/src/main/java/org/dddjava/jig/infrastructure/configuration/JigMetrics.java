package org.dddjava.jig.infrastructure.configuration;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.adapter.JigDocumentGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Supplier;

public class JigMetrics {
    private static final Logger logger = LoggerFactory.getLogger(JigMetrics.class);

    private final Configuration configuration;
    private final JvmGcMetrics jvmGcMetrics;

    public JigMetrics(Configuration configuration, JvmGcMetrics jvmGcMetrics) {
        this.configuration = configuration;
        this.jvmGcMetrics = jvmGcMetrics;
    }

    public JigResult record(Supplier<JigResult> supplier) {
        try {
            var result = Metrics.timer("jig.execution.time", "phase", "total_execution").record(supplier);
            return Objects.requireNonNull(result);
        } finally {
            try {
                jvmGcMetrics.close();
            } catch (Exception e) {
                logger.warn("Failed to close {}", jvmGcMetrics, e);
            }

            try {
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
            } catch (Exception e) {
                logger.warn("メトリクスの出力で予期しない例外が発生しました", e);
            }
        }
    }

    public static JigMetrics init(Configuration configuration) {
        var registry = Metrics.globalRegistry;
        registry.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));

        new UptimeMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        var jvmGcMetrics = new JvmGcMetrics();
        jvmGcMetrics.bindTo(registry);

        return new JigMetrics(configuration, jvmGcMetrics);
    }
}
