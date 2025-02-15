package org.dddjava.jig;

import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceBasePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        long startTime = System.currentTimeMillis();

        // configurationに従ってJigRepositoryの生成と初期化を行う。
        // 現状はローカルのJava/Classファイルを読む形なので固定実装だが雰囲気分けておく。
        // JARなどを読み取る場合やJavaファイルのみなどはSourceBasePathsの形も変わる想定。いつやるかは未定。
        // このフェーズで source -> data の変換を終え、以降は source は触らない。
        DefaultJigRepositoryFactory jigRepositoryFactory = DefaultJigRepositoryFactory.init(configuration);
        jigRepositoryFactory.readPathSource(sourceBasePaths);
        JigRepository jigRepository = jigRepositoryFactory.jigTypesRepository();

        // JigRepositoryを参照してJIGドキュメントを生成する
        JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();
        var results = jigDocumentGenerator.generateDocuments(jigRepository);

        jigDocumentGenerator.generateIndex(results);
        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        return results;
    }

    /**
     * 実装中
     */
    @Deprecated(since = "2025.1.1")
    public static JigExecutor standard(JigOptions jigOptions) {
        var targetRootPath = jigOptions.workingDirectory().toAbsolutePath();

        var binarySourcePaths = switch (jigOptions.resolveBuildTool()) {
            case MAVEN -> new ClassSourceBasePaths(List.of(
                    targetRootPath.resolve(Path.of("target", "classes"))));
            case GRADLE -> new ClassSourceBasePaths(List.of(
                    targetRootPath.resolve(Path.of("build", "classes", "java", "main")),
                    targetRootPath.resolve(Path.of("build", "resources", "main"))
            ));
        };
        JavaSourceBasePaths javaSourceBasePaths = new JavaSourceBasePaths(List.of(
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
}
