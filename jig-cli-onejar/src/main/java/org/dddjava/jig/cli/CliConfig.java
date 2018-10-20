package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.DefaultLayout;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.presentation.view.JigDocument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static picocli.CommandLine.Option;

class CliConfig {
    @Option(names = {"--documentType"})
    String documentTypeText = "";

    @Option(names = {"--outputDirectory"})
    String outputDirectory = "./build/jig";

    @Option(names = {"--output.omit.prefix"})
    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    @Option(names = {"--jig.model.pattern"})
    String modelPattern = ".+\\.domain\\.model\\..+";

    @Option(names = {"--project.path"})
    String projectPath = "./";

    @Option(names = {"--directory.classes"})
    String directoryClasses = "build/classes/java/main";

    @Option(names = {"--directory.resources"})
    String directoryResources = "build/resources/main";

    @Option(names = {"--directory.sources"})
    String directorySources = "src/main/java";

    @Option(names = {"--depth"})
    int depth = -1;

    @Option(names = {"--jig.debug"})
    boolean jigDebugMode = false;

    @Option(names = {"--jig.cli.extra"})
    String jigCliExtra = "";


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
                        new BusinessRuleCondition(modelPattern),
                        new OutputOmitPrefix(outputOmitPrefix),
                        new PackageDepth(depth),
                        jigDebugMode
                ),
                new CliConfigurationContext(this)
        );
    }

}
