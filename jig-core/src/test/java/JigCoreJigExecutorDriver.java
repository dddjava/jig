import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * 簡易出力ドライバ
 */
public class JigCoreJigExecutorDriver {
    private static final Logger logger = LoggerFactory.getLogger(JigCoreJigExecutorDriver.class);

    public static void main(String[] args) {
        JigProperties jigProperties = new JigProperties(
                JigDocument.canonical(),
                Optional.empty(),
                Path.of("build/jig")
        );

        var binaryPath = getPath("jig-core/build/classes/java/main");
        var sourcePath = getPath("jig-core/src/main/java");

        JigResult jigResult = JigExecutor.standard(
                Configuration.from(jigProperties),
                new SourceBasePaths(
                        new SourceBasePath(List.of(binaryPath)),
                        new SourceBasePath(List.of(sourcePath))
                )
        );

        logger.info(jigResult.listResult().toString());
    }

    private static Path getPath(String pathText) {
        var workingDir = Path.of("").toAbsolutePath();
        var currentPath = workingDir;
        var binarySubPath = Path.of(pathText);
        var binaryPath = currentPath.resolve(binarySubPath);
        while (!Files.exists(binaryPath)) {
            currentPath = currentPath.getParent();
            if (currentPath == null || !Files.isReadable(currentPath)) {
                throw new RuntimeException("アクセスできません " + workingDir);
            }
            binaryPath = currentPath.resolve(binarySubPath);
        }
        return binaryPath;
    }
}
