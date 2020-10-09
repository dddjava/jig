package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;
import org.dddjava.jig.domain.model.jigsource.file.SourcePaths;
import org.dddjava.jig.domain.model.jigsource.file.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.jigsource.file.text.CodeSourcePaths;
import org.dddjava.jig.domain.model.jigsource.jigloader.SourceCodeAliasReader;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
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
    @Value("${documentType:}")
    String documentTypeText;
    @Value("${outputDirectory}")
    String outputDirectory;

    @Value("${output.omit.prefix}")
    String outputOmitPrefix;

    @Value("${jig.model.pattern:}")
    String modelPattern;

    @Value("${jig.application.pattern:}")
    String applicationPattern;
    @Value("${jig.infrastructure.pattern:}")
    String infrastructurePattern;
    @Value("${jig.presentation.pattern:}")
    String presentationPattern;

    @Value("${project.path}")
    String projectPath;
    @Value("${directory.classes}")
    String directoryClasses;
    @Value("${directory.resources}")
    String directoryResources;
    @Value("${directory.sources}")
    String directorySources;

    @Value("${linkPrefix:" + LinkPrefix.DISABLE + "}")
    String linkPrefix;

    public String propertiesText() {
        return new StringJoiner("\n")
                .add("documentType=" + documentTypeText)
                .add("outputDirectory=" + outputDirectory)
                .add("output.omit.prefix=" + outputOmitPrefix)
                .add("jig.model.pattern=" + modelPattern)
                .add("jig.infrastructure.pattern=" + infrastructurePattern)
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

    Configuration configuration() {
        JigProperties properties = new JigProperties(
                new OutputOmitPrefix(outputOmitPrefix),
                modelPattern,
                applicationPattern,
                infrastructurePattern,
                presentationPattern,
                new LinkPrefix(linkPrefix),
                Paths.get(this.outputDirectory),
                JigDiagramFormat.SVG
        );
        return new Configuration(properties, new SourceCodeAliasReader(new JavaparserAliasReader()));
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
