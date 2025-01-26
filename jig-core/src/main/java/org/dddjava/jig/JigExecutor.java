package org.dddjava.jig;

import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceBasePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class JigExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JigExecutor.class);

    private final Configuration configuration;
    private final SourceBasePaths sourceBasePaths;

    public JigExecutor(Configuration configuration, SourceBasePaths sourceBasePaths) {
        this.configuration = configuration;
        this.sourceBasePaths = sourceBasePaths;
    }

    public static List<HandleResult> execute(Configuration configuration, SourceBasePaths sourceBasePaths) {
        return new JigExecutor(configuration, sourceBasePaths).execute();
    }

    private List<HandleResult> execute() {
        long startTime = System.currentTimeMillis();

        JigSourceReader jigSourceReader = configuration.sourceReader();
        JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();

        jigDocumentGenerator.prepareOutputDirectory();
        var results = jigSourceReader.readPathSource(sourceBasePaths)
                .map(jigDocumentGenerator::generateDocuments)
                .orElseGet(List::of);

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

        return new JigExecutor(
                new Configuration(new JigProperties(
                        JigDocument.canonical(),
                        jigOptions.domainPattern(),
                        jigOptions.outputDirectory()
                )),
                new SourceBasePaths(
                        binarySourcePaths,
                        new JavaSourceBasePaths(List.of(
                                targetRootPath.resolve(Path.of("src", "main", "java")),
                                targetRootPath.resolve(Path.of("src", "main", "resources"))
                        ))
                )
        );
    }

    /**
     * 実装中
     */
    @Deprecated(since = "2025.1.1")
    public JigExecutor withSourcePaths(SourceBasePaths sourceBasePaths) {
        return new JigExecutor(configuration, sourceBasePaths);
    }
}
