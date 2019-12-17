package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.jigdocument.JigDocument;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HandleResult {

    JigDocument jigDocument;
    List<Path> outputFilePaths;
    String failureMessage;

    HandleResult(JigDocument jigDocument, List<Path> outputFilePaths, String failureMessage) {
        this.jigDocument = jigDocument;
        this.outputFilePaths = outputFilePaths;
        this.failureMessage = failureMessage;
    }

    public HandleResult(JigDocument jigDocument, List<Path> outputFilePaths) {
        this(jigDocument, outputFilePaths, outputFilePaths.isEmpty() ? "skip" : null);
    }

    public HandleResult(JigDocument jigDocument, String failureMessage) {
        this(jigDocument, Collections.emptyList(), failureMessage);
    }

    public List<Path> outputFilePaths() {
        return outputFilePaths.stream().map(Path::toAbsolutePath).collect(Collectors.toList());
    }

    public boolean success() {
        return failureMessage == null;
    }

    public JigDocument jigDocument() {
        return jigDocument;
    }
}
