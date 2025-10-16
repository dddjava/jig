package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.nio.file.Path;
import java.util.List;

public interface HandleResult {
    static HandleResult withException(JigDocument jigDocument, Exception e) {
        return new HandleResultImpl(jigDocument, e);
    }

    static HandleResult withOutput(JigDocument jigDocument, List<Path> outputFilePaths) {
        return new HandleResultImpl(jigDocument, outputFilePaths);
    }

    boolean isOutputDiagram();

    @Deprecated(since = "2025.10.1", forRemoval = true)
    boolean failure();

    String outputFilePathsText();

    boolean success();

    JigDocument jigDocument();

    List<String> outputFileNames();
}
