package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;

@Component
class CliConfig {
    private static final Logger logger = LoggerFactory.getLogger(CliConfig.class);

    @Value("${jig.document.types:}")
    String documentTypeText;
    @Value("${jig.pattern.domain:}")
    String modelPattern;
    @Value("${jig.output.directory}")
    String outputDirectory;
    @Value("${jig.output.diagram.format:svg}")
    JigDiagramFormat diagramFormat;
    @Value("${jig.output.diagram.timeout:10s}")
    Duration dotTimeout;
    @Value("${jig.output.diagram.transitiveReduction:true}")
    boolean diagramTransitiveReduction;

    @Value("${project.path}")
    String projectPath;
    @Value("${directory.classes:}")
    String directoryClasses;
    @Value("${directory.resources:}")
    String directoryResources;
    @Value("${directory.sources:}")
    String directorySources;

    @Value("${mode:default}")
    List<Mode> mode = List.of(Mode.DEFAULT);

    public String propertiesText() {
        return new StringJoiner("\n")
                .add("mode=" + mode)
                .add("jig.document.types=" + documentTypeText)
                .add("jig.pattern.domain=" + modelPattern)
                .add("jig.output.directory=" + outputDirectory)
                .add("jig.output.diagram.format=" + diagramFormat)
                .add("jig.output.diagram.timeout=" + dotTimeout.toString())
                .add("jig.output.diagram.transitiveReduction=" + diagramTransitiveReduction)
                .add("project.path=" + projectPath)
                .add("directory.classes=" + directoryClasses)
                .add("directory.resources=" + directoryResources)
                .add("directory.sources=" + directorySources)
                .toString();
    }

    List<JigDocument> jigDocuments() {
        return documentTypeText.isEmpty()
                ? JigDocument.canonical()
                : JigDocument.resolve(documentTypeText);
    }

    Configuration configuration() {
        // modeを適用
        if (mode.contains(Mode.LIGHT)) {
            documentTypeText = "PackageRelationDiagram";
            modelPattern = ".*";
        }

        return Configuration.from(
                new JigProperties(
                        jigDocuments(),
                        modelPattern, Paths.get(this.outputDirectory),
                        diagramFormat,
                        diagramTransitiveReduction,
                        dotTimeout
                ));
    }

    SourceBasePaths rawSourceLocations() {
        try {
            Path projectRoot = Paths.get(projectPath).toAbsolutePath().normalize();

            if (mode.contains(Mode.MAVEN)) {
                directoryClasses = "target/classes";
                directoryResources = "target/classes";
                directorySources = "src/main/java";
                logger.warn("--mode=maven が指定されています。このモードは2025.9.1以降に廃止予定です。" +
                        "2025.8.1にて自動検出が導入されました。プロジェクトディレクトリにpom.xmlがある場合はMavenデフォルト構造で検出します。");
            } else {
                if (directoryClasses.isEmpty() && directoryResources.isEmpty() && directorySources.isEmpty()) {
                    if (Files.exists(projectRoot.resolve("pom.xml"))) {
                        logger.info("pom.xml が検出されたため、Maven構成で読み取ります。");
                        directoryClasses = "target/classes";
                        directoryResources = "target/classes";
                        directorySources = "src/main/java";
                    }
                }
                // デフォルトの設定
                directoryClasses = getOrDefault(directoryClasses, "build/classes/java/main");
                directoryResources = getOrDefault(directoryResources, "build/resources/main");
                directorySources = getOrDefault(directorySources, "src/main/java");
            }

            DirectoryCollector sourcesCollector = new DirectoryCollector(directoryClasses, directoryResources, directorySources);
            Files.walkFileTree(projectRoot, sourcesCollector);

            return sourcesCollector.toSourcePaths();
        } catch (IOException e) {
            // TODO エラーメッセージ。たとえばルートパスの指定が変な時とかはここにくる。
            throw new UncheckedIOException(e);
        }
    }

    private String getOrDefault(String currentValue, String defaultValue) {
        if (currentValue.isEmpty()) {
            return defaultValue;
        }
        return currentValue;
    }
}
