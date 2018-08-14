package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.presentation.view.report.JigDocument;

import java.util.ArrayList;
import java.util.List;

public class JigProperties {
    String modelPattern = ".+\\.domain\\.model\\..+";

    String repositoryPattern = ".+Repository";

    List<JigDocument> documentTypes = new ArrayList<>();

    String outputDirectory = "build/jig";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    int depth = -1;

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

    public List<JigDocument> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<JigDocument> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public void setOutputOmitPrefix(String outputOmitPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
