package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.LinkPrefix;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.sources.file.text.CodeSourcePaths;
import org.dddjava.jig.domain.model.sources.jigreader.SourceCodeAliasReader;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
import org.dddjava.jig.infrastructure.kotlin.KotlinSdkAliasReader;
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
class KotlinCliConfig {
    @Value("${jig.document.types:}")
    String documentTypeText;
    @Value("${jig.pattern.domain:}")
    String modelPattern;
    @Value("${jig.output.directory}")
    String outputDirectory;
    @Value("${jig.output.diagram.format:svg}")
    JigDiagramFormat diagramFormat;
    @Value("${jig.omit.prefix}")
    String outputOmitPrefix;
    @Value("${jig.link.prefix:" + LinkPrefix.DISABLE + "}")
    String linkPrefix;

    @Value("${project.path}")
    String projectPath;
    @Value("${directory.classes}")
    String directoryClasses;
    @Value("${directory.resources}")
    String directoryResources;
    @Value("${directory.sources}")
    String directorySources;

    public String propertiesText() {
        return new StringJoiner("\n")
                .add("jig.document.types=" + documentTypeText)
                .add("jig.pattern.domain=" + modelPattern)
                .add("jig.output.directory=" + outputDirectory)
                .add("jig.output.diagram.format=" + diagramFormat)
                .add("jig.omit.prefix=" + outputOmitPrefix)
                .add("jig.link.prefix=" + linkPrefix)
                .add("project.path=" + projectPath)
                .add("directory.classes=" + directoryClasses)
                .add("directory.resources=" + directoryResources)
                .add("directory.sources=" + directorySources)
                .add("linkPrefix=" + linkPrefix)
                .toString();
    }

    List<JigDocument> jigDocuments() {
        return documentTypeText.isEmpty()
                ? JigDocument.canonical()
                : JigDocument.resolve(documentTypeText);
    }


    Path outputDirectory() {
        return Paths.get(this.outputDirectory);
    }

    Configuration configuration() {
        return new Configuration(
                new JigProperties(
                        jigDocuments(),
                        modelPattern, Paths.get(this.outputDirectory), diagramFormat, new OutputOmitPrefix(outputOmitPrefix),
                        new LinkPrefix(linkPrefix)
                ),
                new SourceCodeAliasReader(new JavaparserAliasReader(), new KotlinSdkAliasReader())
        );
    }

    SourcePaths rawSourceLocations() {
        try {
            Path projectRoot = Paths.get(projectPath);

            DirectoryCollector binaryCollector = new DirectoryCollector(path -> path.endsWith(directoryClasses) || path.endsWith(directoryResources));
            Files.walkFileTree(projectRoot, binaryCollector);
            List<Path> binarySourcePaths = binaryCollector.listPath();

            DirectoryCollector sourcesCollector = new DirectoryCollector(path -> path.endsWith(directorySources));
            Files.walkFileTree(projectRoot, sourcesCollector);
            List<Path> textSourcesPaths = sourcesCollector.listPath();

            return new SourcePaths(
                    new BinarySourcePaths(binarySourcePaths),
                    new CodeSourcePaths(textSourcesPaths));
        } catch (IOException e) {
            // TODO エラーメッセージ。たとえばルートパスの指定が変な時とかはここにくる。
            throw new UncheckedIOException(e);
        }
    }
}
