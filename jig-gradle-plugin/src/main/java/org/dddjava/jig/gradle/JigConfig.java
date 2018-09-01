package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.ModelPattern;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.infrastructure.configuration.RepositoryPattern;
import org.dddjava.jig.presentation.view.JigDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JigConfig {

    String modelPattern = ".+\\.domain\\.model\\..+";

    String repositoryPattern = ".+Repository";

    List<String> documentTypes = new ArrayList<>();

    String outputDirectory = "build/jig";

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
                new ModelPattern(modelPattern),
                new RepositoryPattern(repositoryPattern),
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

    public String getRepositoryPattern() {
        return repositoryPattern;
    }

    public void setRepositoryPattern(String repositoryPattern) {
        this.repositoryPattern = repositoryPattern;
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
