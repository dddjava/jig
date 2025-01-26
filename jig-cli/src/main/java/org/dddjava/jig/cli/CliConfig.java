package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;

@Component
class CliConfig {
    @Value("${jig.document.types:}")
    String documentTypeText;
    @Value("${jig.pattern.domain:}")
    String modelPattern;
    @Value("${jig.output.directory}")
    String outputDirectory;
    @Value("${jig.output.diagram.format:svg}")
    JigDiagramFormat diagramFormat;

    @Value("${project.path}")
    String projectPath;
    @Value("${directory.classes}")
    String directoryClasses;
    @Value("${directory.resources}")
    String directoryResources;
    @Value("${directory.sources}")
    String directorySources;

    @Value("${mode:default}")
    List<Mode> mode;

    public String propertiesText() {
        return new StringJoiner("\n")
                .add("mode=" + mode)
                .add("jig.document.types=" + documentTypeText)
                .add("jig.pattern.domain=" + modelPattern)
                .add("jig.output.directory=" + outputDirectory)
                .add("jig.output.diagram.format=" + diagramFormat)
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
        if (mode.contains(Mode.MAVEN)) {
            directoryClasses = "target/classes";
            directoryResources = "target/classes";
            directorySources = "src/main/java";
        }
        if (mode.contains(Mode.LIGHT)) {
            documentTypeText = "PackageRelationDiagram";
            modelPattern = ".*";
        }

        return new Configuration(
                new JigProperties(
                        jigDocuments(),
                        modelPattern, Paths.get(this.outputDirectory), diagramFormat
                ));
    }

    SourceBasePaths rawSourceLocations() {
        try {
            Path projectRoot = Paths.get(projectPath).toAbsolutePath().normalize();

            DirectoryCollector sourcesCollector = new DirectoryCollector(directoryClasses, directoryResources, directorySources);
            Files.walkFileTree(projectRoot, sourcesCollector);

            return sourcesCollector.toSourcePaths();
        } catch (IOException e) {
            // TODO エラーメッセージ。たとえばルートパスの指定が変な時とかはここにくる。
            throw new UncheckedIOException(e);
        }
    }
}
