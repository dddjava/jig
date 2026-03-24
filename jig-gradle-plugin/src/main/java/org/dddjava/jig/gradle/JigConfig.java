package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JigConfig {

    String modelPattern = "";

    List<String> documentTypes = new ArrayList<>();
    List<String> documentTypesExclude = new ArrayList<>();

    String outputDirectory = "";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|type))\\.";

    JigDiagramFormat diagramFormat = JigDiagramFormat.SVG;
    String dotTimeout = "10s";

    boolean diagramTransitiveReduction = true;

    public List<String> getDocumentTypesExclude() {
        return documentTypesExclude;
    }

    public void setDocumentTypesExclude(List<String> documentTypesExclude) {
        this.documentTypesExclude = documentTypesExclude;
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

    public String getDotTimeout() {
        return dotTimeout;
    }

    public void setDotTimeout(String dotTimeout) {
        this.dotTimeout = dotTimeout;
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

}
