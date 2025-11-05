package org.dddjava.jig;

import org.dddjava.jig.adapter.JigDocumentGenerator;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigMetrics;
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
    @Deprecated(since = "2025.11.1", forRemoval = true)
    public static List<HandleResult> execute(Configuration configuration, SourceBasePaths sourceBasePaths) {
        return standard(configuration, sourceBasePaths).listResult();
    }

    /**
     * 標準のJigExecutorを使用するエントリポイント
     */
    public static JigResult standard(Configuration configuration, SourceBasePaths sourceBasePaths) {
        return JigMetrics.init(configuration)
                .record(() -> new JigExecutor(configuration).execute(sourceBasePaths));
    }

    private JigResult execute(SourceBasePaths sourceBasePaths) {
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
            return jigDocumentGenerator.generate(jigRepository);
        } finally {
            configuration.jigEventRepository().notifyWithLogger();
            logger.info("[JIG] all JIG documents completed: {} ms", System.currentTimeMillis() - startTime);
        }
    }
}
