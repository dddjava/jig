package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public interface HandleResult {
    static HandleResult withException(JigDocument jigDocument, Exception e) {
        return new HandleResultImpl(jigDocument, Collections.emptyList(), e.getMessage());
    }

    static HandleResult withOutput(JigDocument jigDocument, List<Path> outputFilePaths) {
        return new HandleResultImpl(jigDocument, outputFilePaths, outputFilePaths.isEmpty() ? "skip" : null);
    }

    boolean isOutputDiagram();

    boolean failure();

    String outputFilePathsText();

    boolean success();

    JigDocument jigDocument();

    List<String> outputFileNames();
}
