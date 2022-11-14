package org.dddjava.jig.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.dddjava.jig.domain.model.sources.file.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.sources.file.text.CodeSourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.presentation.controller.JigExecutor;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "jig")
public class JigMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
    private File targetClassesDirectory;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private File targetDirectory;

    @Parameter(defaultValue = "${project.build.sourceDirectory}", readonly = true, required = true)
    private File sourceDirectory;

    @Parameter(property = "jig.document.types")
    private String[] documentTypes;

    @Parameter(property = "jig.pattern.domain")
    private String domainPattern;

    public void execute() {
        JigExecutor.execute(configuration(), sourcePaths());
    }

    private Configuration configuration() {
        JigProperties properties = new JigProperties(
                documentTypes(),
                domainPattern,
                targetDirectory.toPath().resolve("jig")
        );
        return new Configuration(properties);
    }

    private List<JigDocument> documentTypes() {
        if (documentTypes == null || documentTypes.length == 0) {
            return JigDocument.canonical();
        }
        return Arrays.stream(documentTypes)
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
    }

    private SourcePaths sourcePaths() {
        return new SourcePaths(
                new BinarySourcePaths(Collections.singleton(targetClassesDirectory.toPath())),
                new CodeSourcePaths(Collections.singleton(sourceDirectory.toPath()))
        );
    }
}