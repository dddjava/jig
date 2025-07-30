package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class JigConfig {

    String modelPattern = "";

    List<String> documentTypes = new ArrayList<>();
    List<String> documentTypesExclude = new ArrayList<>();

    String outputDirectory = "";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|type))\\.";

    JigDiagramFormat diagramFormat = JigDiagramFormat.SVG;
    String diagramTimeout = "10s";

    boolean diagramTransitiveReduction = true;

    List<JigDocument> documentTypes() {
        List<JigDocument> toExclude = documentTypesToExclude();
        return documentTypesToInclude().stream()
                .filter(each -> !toExclude.contains(each))
                .toList();
    }

    List<JigDocument> documentTypesToExclude() {
        if (documentTypesExclude.isEmpty()) return List.of();
        return documentTypesExclude.stream()
                .map(JigDocument::valueOf)
                .toList();
    }

    List<JigDocument> documentTypesToInclude() {
        if (documentTypes.isEmpty()) return JigDocument.canonical();
        return documentTypes.stream()
                .map(JigDocument::valueOf)
                .toList();
    }

    public JigProperties asProperties(Project project) {
        return new JigProperties(
                documentTypes(),
                modelPattern, resolveOutputDirectory(project),
                diagramFormat,
                diagramTransitiveReduction,
                parseDiagramTimeout()
        );
    }

    private Duration parseDiagramTimeout() {
        if (diagramTimeout.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(diagramTimeout.substring(0, diagramTimeout.length() - 2)));
        }
        if (diagramTimeout.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(diagramTimeout.substring(0, diagramTimeout.length() - 1)));
        }
        throw new IllegalArgumentException("diagramTimeout must be end with ms or s. " + diagramTimeout + " is invalid.");
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
        var buildDirectory = project.getLayout().getBuildDirectory();
        return buildDirectory.getAsFile().get().toPath().resolve("jig");
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

    public String getDiagramTimeout() {
        return diagramTimeout;
    }

    public void setDiagramTimeout(String diagramTimeout) {
        this.diagramTimeout = diagramTimeout;
    }

    public boolean isDiagramTransitiveReduction() {
        return diagramTransitiveReduction;
    }

    public void setDiagramTransitiveReduction(boolean diagramTransitiveReduction) {
        this.diagramTransitiveReduction = diagramTransitiveReduction;
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
                .add("diagramTimeout= '" + diagramTimeout + '\'')
                .add("diagramTransitiveReduction= '" + diagramTransitiveReduction + '\'')
                .add("outputOmitPrefix = '" + outputOmitPrefix + '\'')
                .toString();
    }
}
