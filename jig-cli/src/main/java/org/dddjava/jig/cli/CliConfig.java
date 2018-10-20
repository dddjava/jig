package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.DefaultLayout;
import org.dddjava.jig.infrastructure.configuration.*;
import org.dddjava.jig.presentation.view.JigDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
class CliConfig {
    @Value("${documentType:}")
    String documentTypeText;
    @Value("${outputDirectory}")
    String outputDirectory;

    @Value("${output.omit.prefix}")
    String outputOmitPrefix;
    @Value("${jig.model.pattern}")
    String modelPattern;
    @Value("${jig.repository.pattern}")
    String repositoryPattern;

    @Value("${project.path}")
    String projectPath;
    @Value("${directory.classes}")
    String directoryClasses;
    @Value("${directory.resources}")
    String directoryResources;
    @Value("${directory.sources}")
    String directorySources;

    @Value("${depth}")
    int depth;

    @Value("${jig.debug}")
    boolean jigDebugMode;

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
                        new RepositoryPattern(repositoryPattern),
                        new OutputOmitPrefix(outputOmitPrefix),
                        new PackageDepth(depth),
                        jigDebugMode
                ),
                new CliConfigurationContext(this)
        );
    }

}
