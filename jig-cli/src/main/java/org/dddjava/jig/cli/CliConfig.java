package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.DefaultLayout;
import org.dddjava.jig.infrastructure.configuration.*;
import org.dddjava.jig.presentation.view.JigDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
class CliConfig {
    @Value("${documentType:}")
    @Option(names = {"--documentType"})
    String documentTypeText = "";

    @Value("${outputDirectory}")
    @Option(names = {"--outputDirectory"})
    String outputDirectory = "./build/jig";

    @Value("${output.omit.prefix}")
    @Option(names = {"--output.omit.prefix"})
    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    @Value("${jig.model.pattern}")
    @Option(names = {"--jig.model.pattern"})
    String modelPattern = ".+\\.domain\\.model\\..+";

    @Value("${jig.repository.pattern}")
    @Option(names = {"--jig.repository.pattern"})
    String repositoryPattern = ".+Repository";

    @Value("${project.path}")
    @Option(names = {"--project.path"})
    String projectPath = "./";

    @Value("${directory.classes}")
    @Option(names = {"--directory.classes"})
    String directoryClasses = "build/classes/java/main";

    @Value("${directory.resources}")
    @Option(names = {"--directory.resources"})
    String directoryResources = "build/resources/main";

    @Value("${directory.sources}")
    @Option(names = {"--directory.sources"})
    String directorySources = "src/main/java";

    @Value("${depth}")
    @Option(names = {"--depth"})
    int depth = -1;

    @Value("${jig.debug}")
    @Option(names = {"--jig.debug"})
    boolean jigDebugMode = false;

    @Option(names = {"--jig.cli.extra"})
    String jigCliExtra="";


    List<JigDocument> jigDocuments() {
        return documentTypeText.isEmpty()
                ? Arrays.asList(JigDocument.values())
                : JigDocument.resolve(documentTypeText);
    }


    Path outputDirectory() {
        return Paths.get(this.outputDirectory);
    }

    Configuration configuration() {
        return new Configuration(
                new DefaultLayout(projectPath, directoryClasses, directoryResources, directorySources),
                new JigProperties(
                        new ModelPattern(modelPattern),
                        new RepositoryPattern(repositoryPattern),
                        new OutputOmitPrefix(outputOmitPrefix),
                        new PackageDepth(depth),
                        jigDebugMode
                ),
                new CliConfigurationContext(this)
        );
    }

}
