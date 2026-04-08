package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class HandleResultImpl implements HandleResult {

    private final JigDocument jigDocument;
    private final List<Path> outputFilePaths;

    /**
     * 成功時のコンストラクタ
     */
    HandleResultImpl(JigDocument jigDocument, List<Path> outputFilePaths) {
        this.jigDocument = jigDocument;
        this.outputFilePaths = outputFilePaths;
    }

    @Override
    public String outputFilePathsText() {
        return outputFilePaths.stream()
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .map(Path::toString)
                .collect(joining(", ", "[ ", " ]"));
    }

    @Override
    public boolean success() {
        // 何かしらのアウトプットがある
        return !outputFilePaths.isEmpty();
    }

    @Override
    public JigDocument jigDocument() {
        return jigDocument;
    }

    @Override
    public List<String> outputFileNames() {
        return outputFilePaths.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();
    }

    @Override
    public String toString() {
        if (success()) {
            return String.format("%s: %s", jigDocument(), outputFileNames());
        }

        return String.format("%s: skip", jigDocument());
    }
}
