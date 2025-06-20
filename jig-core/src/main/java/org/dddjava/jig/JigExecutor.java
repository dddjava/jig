package org.dddjava.jig;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JigExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JigExecutor.class);

    private final Configuration configuration;

    public JigExecutor(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 標準のJigExecutorを使用するエントリポイント
     */
    public static List<HandleResult> execute(Configuration configuration, SourceBasePaths sourceBasePaths) {
        var registry = Metrics.globalRegistry;
        registry.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));

        new UptimeMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        try (var jvmGcMetrics = new JvmGcMetrics()) {
            jvmGcMetrics.bindTo(registry);
            return new JigExecutor(configuration).execute(sourceBasePaths);
        } finally {
            registry.close();
        }
    }

    private List<HandleResult> execute(SourceBasePaths sourceBasePaths) {
        var registry = Metrics.globalRegistry;
        Timer.Sample sample = Timer.start(registry);

        try {
            // configurationに従ってJigRepositoryの生成と初期化を行う。
            // 現状はローカルのJava/Classファイルを読む形なので固定実装だが雰囲気分けておく。
            // JARなどを読み取る場合やJavaファイルのみなどはSourceBasePathsの形も変わる想定。いつやるかは未定。
            // このフェーズで source -> data の変換を終え、以降は source は触らない。
            DefaultJigRepositoryFactory jigRepositoryFactory = DefaultJigRepositoryFactory.init(configuration);
            JigRepository jigRepository = jigRepositoryFactory.createJigRepository(sourceBasePaths);

            // JigRepositoryを参照してJIGドキュメントを生成する
            JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();
            var results = jigDocumentGenerator.generateDocuments(jigRepository);

            jigDocumentGenerator.generateIndex(results);
            jigDocumentGenerator.generateAssets();

            return results;
        } finally {
            long takenTime = sample.stop(Timer.builder("jig.execution.time")
                    .description("Total execution time for JIG")
                    .tag("phase", "total_execution")
                    .register(registry));
            JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();
            jigDocumentGenerator.exportMetricsToFile();

            configuration.jigEventRepository().notifyWithLogger();
            logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        }
    }
}
