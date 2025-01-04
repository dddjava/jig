package org.dddjava.jig;

import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.sources.file.text.CodeSourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class JigExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JigExecutor.class);

    private final Configuration configuration;
    private final SourcePaths sourcePaths;

    public JigExecutor(Configuration configuration, SourcePaths sourcePaths) {
        this.configuration = configuration;
        this.sourcePaths = sourcePaths;
    }

    /**
     * @deprecated 2025.3.1以降で削除予定
     */
    @Deprecated(since = "2025.1.1")
    public static List<HandleResult> execute(Configuration configuration, SourcePaths sourcePaths) {
        return new JigExecutor(configuration, sourcePaths).execute();
    }

    private List<HandleResult> execute() {
        long startTime = System.currentTimeMillis();

        JigSourceReader jigSourceReader = configuration.sourceReader();
        JigDocumentGenerator jigDocumentGenerator = configuration.documentGenerator();

        jigDocumentGenerator.prepareOutputDirectory();
        var results = jigSourceReader.readPathSource(sourcePaths)
                .map(jigDocumentGenerator::generateDocuments)
                .orElseGet(List::of);

        jigDocumentGenerator.generateIndex(results);
        long takenTime = System.currentTimeMillis() - startTime;
        logger.info("[JIG] all JIG documents completed: {} ms", takenTime);
        return results;
    }

    public static JigExecutor standard(JigOptions jigOptions) {
        var targetRootPath = jigOptions.workingDirectory().toAbsolutePath();

        var binarySourcePaths = switch (jigOptions.resolveBuildTool()) {
            case MAVEN -> new BinarySourcePaths(List.of(
                    targetRootPath.resolve(Path.of("target", "classes"))));
            case GRADLE -> new BinarySourcePaths(List.of(
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
                new SourcePaths(
                        binarySourcePaths,
                        new CodeSourcePaths(List.of(
                                targetRootPath.resolve(Path.of("src", "main", "java")),
                                targetRootPath.resolve(Path.of("src", "main", "resources"))
                        ))
                )
        );
    }

    public JigExecutor withSourcePaths(SourcePaths sourcePaths) {
        return new JigExecutor(configuration, sourcePaths);
    }
}
