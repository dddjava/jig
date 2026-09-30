package org.dddjava.jig.infrastructure.configuration;

import io.micrometer.core.instrument.MeterRegistry;
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

/**
 * 実行の計測。
 *
 * レジストリは実行ごとに専有する。{@code Metrics.globalRegistry} を使うと
 * 並行実行中の他インスタンスの記録が混入するため、計測箇所へは {@link #registry()} を渡す。
 */
public class JigMetrics {
    private static final Logger logger = LoggerFactory.getLogger(JigMetrics.class);

    private final PrometheusMeterRegistry registry;

    private JigMetrics(PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    public static JigMetrics init() {
        return new JigMetrics(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));
    }

    /**
     * 解析中の計測の記録先。
     */
    public MeterRegistry registry() {
        return registry;
    }

    public JigResult record(JigDocumentGenerator jigDocumentGenerator, Supplier<JigResult> supplier) {
        // JVMの計測は実行中のみ必要なので、レジストリの生成時ではなくここで束ねる
        new UptimeMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        var jvmGcMetrics = new JvmGcMetrics();
        jvmGcMetrics.bindTo(registry);

        try {
            var result = registry.timer("jig.execution.time", "phase", "total_execution").record(supplier);
            return Objects.requireNonNull(result);
        } finally {
            try {
                jvmGcMetrics.close();
            } catch (Exception e) {
                logger.warn("Failed to close {}", jvmGcMetrics, e);
            }

            try {
                // メトリクスを出力
                jigDocumentGenerator.close(outputDirectory -> {
                    var text = registry.scrape();
                    registry.close();

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
}
