package org.dddjava.jig;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.sources.SourceBasePath;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Path;
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
        return new JigExecutor(configuration).execute(sourceBasePaths);
    }

    private List<HandleResult> execute(SourceBasePaths sourceBasePaths) {
        var registry = Metrics.globalRegistry;
        registry.add(new SimpleMeterRegistry());

        // Register memory usage gauges
        Gauge.builder("jig.memory.used", this, o -> getUsedMemory())
                .description("JVM memory used by JIG")
                .baseUnit("bytes")
                .register(registry);

        Gauge.builder("jig.memory.max", this, o -> getMaxMemory())
                .description("Maximum memory available to JVM")
                .baseUnit("bytes")
                .register(registry);

        Gauge.builder("jig.memory.total", this, o -> getTotalMemory())
                .description("Total memory allocated to JVM")
                .baseUnit("bytes")
                .register(registry);

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

            logger.debug("metrics: class files={}", Metrics.counter("jig.analysis.class.count").count());
            return results;
        } finally {
            long takenTime = sample.stop(Timer.builder("jig.execution.time")
                    .description("Total execution time for JIG")
                    .tag("phase", "total_execution")
                    .register(registry));
            logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        }
    }

    /**
     * 実装中
     */
    @Deprecated(since = "2025.1.1")
    public static JigExecutor standard(JigOptions jigOptions) {
        var targetRootPath = jigOptions.workingDirectory().toAbsolutePath();

        var binarySourcePaths = switch (jigOptions.resolveBuildTool()) {
            case MAVEN -> new SourceBasePath(List.of(
                    targetRootPath.resolve(Path.of("target", "classes"))));
            case GRADLE -> new SourceBasePath(List.of(
                    targetRootPath.resolve(Path.of("build", "classes", "java", "main")),
                    targetRootPath.resolve(Path.of("build", "resources", "main"))
            ));
        };
        SourceBasePath sourceBasePath = new SourceBasePath(List.of(
                targetRootPath.resolve(Path.of("src", "main", "java")),
                targetRootPath.resolve(Path.of("src", "main", "resources"))
        ));

        return new JigExecutor(
                new Configuration(new JigProperties(
                        JigDocument.canonical(),
                        jigOptions.domainPattern(),
                        jigOptions.outputDirectory()
                ))
        );
    }

    /**
     * 実装中
     */
    @Deprecated(since = "2025.1.1")
    public JigExecutor withSourcePaths(SourceBasePaths sourceBasePaths) {
        return new JigExecutor(configuration);
    }

    private long getUsedMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        return heapMemoryUsage.getUsed() + nonHeapMemoryUsage.getUsed();
    }

    private long getMaxMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        return heapMemoryUsage.getMax();
    }

    private long getTotalMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        return heapMemoryUsage.getCommitted() + nonHeapMemoryUsage.getCommitted();
    }
}
