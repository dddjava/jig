package org.dddjava.jig;

import io.micrometer.core.instrument.Metrics;
import org.dddjava.jig.adapter.JigDocumentGenerator;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigMetrics;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

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
        try (var ignore = JigMetrics.init(configuration)) {
            return Objects.requireNonNull(Metrics.timer("jig.execution.time", "phase", "total_execution").record(() ->
                    new JigExecutor(configuration).execute(sourceBasePaths)));
        }
    }

    private List<HandleResult> execute(SourceBasePaths sourceBasePaths) {
        var startTime = System.currentTimeMillis();
        try {
            // configurationに従ってJigRepositoryの生成と初期化を行う。
            // 現状はローカルのJava/Classファイルを読む形なので固定実装だが雰囲気分けておく。
            // JARなどを読み取る場合やJavaファイルのみなどはSourceBasePathsの形も変わる想定。いつやるかは未定。
            // このフェーズで source -> data の変換を終え、以降は source は触らない。
            DefaultJigRepositoryFactory jigRepositoryFactory = DefaultJigRepositoryFactory.init(configuration);
            JigRepository jigRepository = jigRepositoryFactory.createJigRepository(sourceBasePaths);

            // JigRepositoryを参照してJIGドキュメントを生成する
            JigDocumentGenerator jigDocumentGenerator = configuration.jigDocumentGenerator();
            var results = jigDocumentGenerator.generateDocuments(jigRepository);

            jigDocumentGenerator.generateIndex(results);
            jigDocumentGenerator.generateAssets();

            return results;
        } finally {
            configuration.jigEventRepository().notifyWithLogger();
            logger.info("[JIG] all JIG documents completed: {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
