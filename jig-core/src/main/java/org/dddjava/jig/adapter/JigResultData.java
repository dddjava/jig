package org.dddjava.jig.adapter;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

record JigResultData(Collection<HandleResult> handleResults, Path indexFilePath) implements JigResult {

    @Override
    public List<HandleResult> listResult() {
        return handleResults.stream()
                .sorted(Comparator.comparing(HandleResult::jigDocument))
                .toList();
    }

    @Override
    public JigSummary summary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path indexFilePath() {
        return indexFilePath;
    }
}
