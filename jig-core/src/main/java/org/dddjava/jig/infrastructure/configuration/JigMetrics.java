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
                    var globalRegistry = io.micrometer.core.instrument.Metrics.globalRegistry;
                    var metricsText = new StringBuilder();
                    globalRegistry.getRegistries().forEach(it -> {
                        if (it instanceof PrometheusMeterRegistry prometheusMeterRegistry) {
                            metricsText.append(prometheusMeterRegistry.scrape());
                        }
                    });
                    // このclose以降は記録されない
                    globalRegistry.close();

                    var text = metricsText.toString();

                    // jig-metrics.txt に書き出す
                    var txtPath = outputDirectory.resolve("jig-metrics.txt");
                    try {
                        Files.writeString(txtPath, text);
                    } catch (IOException e) {
                        logger.error("Failed to export metrics to file: {}", txtPath, e);
                    }

                    // data/metrics-data.js に書き出す（file:// でも loadScript で読める）
                    var jsPath = outputDirectory.resolve("data/metrics-data.js");
                    try {
                        var escaped = text
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\r\n", "\\n")
                                .replace("\n", "\\n")
                                .replace("\r", "\\n");
                        Files.writeString(jsPath, "globalThis.metricsData = \"" + escaped + "\";");
                    } catch (IOException e) {
                        logger.error("Failed to export metrics JS to file: {}", jsPath, e);
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
