package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class JigConfig {

    String modelPattern = "";

    List<String> documentTypes = new ArrayList<>();
    List<String> documentTypesExclude = new ArrayList<>();

    String outputDirectory = "";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|type))\\.";

    JigDiagramFormat diagramFormat = JigDiagramFormat.SVG;

    List<JigDocument> documentTypes() {
        List<JigDocument> toExclude = documentTypesToExclude();
        return documentTypesToInclude().stream()
                .filter(each -> !toExclude.contains(each))
                .collect(Collectors.toList());
    }

    List<JigDocument> documentTypesToExclude() {
        if (documentTypesExclude.isEmpty()) return List.of();
        return documentTypesExclude.stream()
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
    }

    List<JigDocument> documentTypesToInclude() {
        if (documentTypes.isEmpty()) return JigDocument.canonical();
        return documentTypes.stream()
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
    }

    public JigProperties asProperties(Project project) {
        return new JigProperties(
                documentTypes(),
                modelPattern, resolveOutputDirectory(project), diagramFormat
        );
    }

    private Path resolveOutputDirectory(Project project) {
        if (this.outputDirectory.isEmpty()) {
            return defaultOutputDirectory(project);
        }
        return Paths.get(this.outputDirectory);
    }

    private Path defaultOutputDirectory(Project project) {
        Path path = Paths.get(getOutputDirectory());
        if (path.isAbsolute()) return path;
        return project.getBuildDir().toPath().resolve("jig");
    }

    public String getModelPattern() {
        return modelPattern;
    }

    public void setModelPattern(String modelPattern) {
        this.modelPattern = modelPattern;
    }

    public List<String> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<String> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        if (!Paths.get(outputDirectory).isAbsolute()) {
            throw new IllegalArgumentException("outputDirectoryは絶対パスを指定してください");
        }
        this.outputDirectory = outputDirectory;
    }

    public JigDiagramFormat getDiagramFormat() {
        return diagramFormat;
    }

    public void setDiagramFormat(JigDiagramFormat diagramFormat) {
        this.diagramFormat = diagramFormat;
    }

    public String getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public void setOutputOmitPrefix(String outputOmitPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;
    }

    public String propertiesText() {
        return new StringJoiner("\n\t", "jig {\n\t", "\n}")
                .add("documentTypes = '" + documentTypes + '\'')
                .add("modelPattern = '" + modelPattern + '\'')
                .add("outputDirectory = '" + outputDirectory + '\'')
                .add("diagramFormat= '" + diagramFormat + '\'')
                .add("outputOmitPrefix = '" + outputOmitPrefix + '\'')
                .toString();
    }
}
