package org.dddjava.jig.gradle;

import org.dddjava.jig.presentation.view.JigDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JigConfig {

    List<String> documentTypes = new ArrayList<>();

    String outputDirectory = "build/jig";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    int depth = -1;


    List<JigDocument> documentTypes() {
        if (documentTypes.isEmpty()) return Arrays.asList(JigDocument.values());
        return documentTypes.stream()
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
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
}
