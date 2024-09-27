package org.dddjava.jig.presentation.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

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

    boolean failure() {
        return !success()
                // 現状、skipかどうかはfailureMessageで見るしかない
                && !failureMessage.equals("skip");
    }

    public String outputFilePathsText() {
        return outputFilePaths.stream()
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .map(Path::toString)
                .collect(joining(", ", "[ ", " ]"));
    }

    public boolean success() {
        return failureMessage == null;
    }

    public JigDocument jigDocument() {
        return jigDocument;
    }

    public List<String> outputFileNames() {
        return outputFilePaths.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        if (success()) {
            return String.format("%s: %s", jigDocument(), outputFileNames());
        }
        return String.format("%s: %s", jigDocument(), failureMessage);
    }
}
