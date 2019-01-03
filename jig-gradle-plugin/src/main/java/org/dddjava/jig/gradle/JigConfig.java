package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.implementation.analyzed.architecture.BusinessRuleCondition;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.presentation.view.JigDocument;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class JigConfig {

    String modelPattern = ".+\\.domain\\.model\\..+";

    List<String> documentTypes = new ArrayList<>();

    String outputDirectory = "";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    boolean enableDebugDocument = false;

    List<JigDocument> documentTypes() {
        if (documentTypes.isEmpty()) return JigDocument.canonical();
        return documentTypes.stream()
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
    }

    public JigProperties asProperties() {
        return new JigProperties(
                new BusinessRuleCondition(modelPattern),
                new OutputOmitPrefix(outputOmitPrefix),
                enableDebugDocument
        );
    }

    public String getModelPattern() {
        return modelPattern;
    }

    public void setModelPattern(String modelPattern) {
        this.modelPattern = modelPattern;
    }

    List<String> getDocumentTypes() {
        return documentTypes;
    }

    void setDocumentTypes(List<String> documentTypes) {
        this.documentTypes = documentTypes;
    }

    String getOutputDirectory() {
        return outputDirectory;
    }

    void setOutputDirectory(String outputDirectory) {
        if (!Paths.get(outputDirectory).isAbsolute()) {
            throw new IllegalArgumentException("outputDirectoryは絶対パスを指定してください");
        }
        this.outputDirectory = outputDirectory;
    }

    String getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    void setOutputOmitPrefix(String outputOmitPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;
    }

    public boolean isEnableDebugDocument() {
        return enableDebugDocument;
    }

    public void setEnableDebugDocument(boolean enableDebugDocument) {
        this.enableDebugDocument = enableDebugDocument;
    }

    public String propertiesText() {
        return new StringJoiner("\n\t", "jig {\n\t", "\n}")
                .add("modelPattern = '" + modelPattern + '\'')
                .add("documentTypes = '" + documentTypes + '\'')
                .add("outputDirectory = '" + outputDirectory + '\'')
                .add("outputOmitPrefix = '" + outputOmitPrefix + '\'')
                .add("enableDebugDocument = " + enableDebugDocument)
                .toString();
    }
}
