package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.presentation.view.JigDocument;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JigConfig {

    String modelPattern = ".+\\.domain\\.model\\..+";

    List<String> documentTypes = new ArrayList<>();

    String outputDirectory = "";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    int depth = -1;

    boolean enableDebugDocument = false;

    List<JigDocument> documentTypes() {
        if (documentTypes.isEmpty()) return Arrays.asList(JigDocument.values());
        return documentTypes.stream()
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
    }

    public JigProperties asProperties() {
        return new JigProperties(
                new BusinessRuleCondition(modelPattern),
                new OutputOmitPrefix(outputOmitPrefix),
                new PackageDepth(depth),
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

    int getDepth() {
        return depth;
    }

    void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isEnableDebugDocument() {
        return enableDebugDocument;
    }

    public void setEnableDebugDocument(boolean enableDebugDocument) {
        this.enableDebugDocument = enableDebugDocument;
    }
}
