package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class HandleResult {

    JigDocument jigDocument;
    List<Path> outputFilePaths;
    @Nullable
    String failureMessage;

    private HandleResult(JigDocument jigDocument, List<Path> outputFilePaths, @Nullable String failureMessage) {
        this.jigDocument = jigDocument;
        this.outputFilePaths = outputFilePaths;
        this.failureMessage = failureMessage;
    }

    private HandleResult(JigDocument jigDocument, List<Path> outputFilePaths) {
        this(jigDocument, outputFilePaths, outputFilePaths.isEmpty() ? "skip" : null);
    }

    private HandleResult(JigDocument jigDocument, String failureMessage) {
        this(jigDocument, Collections.emptyList(), failureMessage);
    }

    public static HandleResult withException(JigDocument jigDocument, Exception e) {
        return new HandleResult(jigDocument, e.getMessage());
    }

    public static HandleResult withOutput(JigDocument jigDocument, List<Path> outputFilePaths) {
        return new HandleResult(jigDocument, outputFilePaths);
    }

    public boolean isOutputDiagram() {
        // TODO JigDocumentによって固定になっているが、実際の出力結果によって制御する
        return switch (jigDocument()) {
            case PackageRelationDiagram,
                 BusinessRuleRelationDiagram,
                 CategoryDiagram,
                 CategoryUsageDiagram,
                 ServiceMethodCallHierarchyDiagram -> true;
            case ApplicationList,
                 BusinessRuleList,
                 DomainSummary,
                 ApplicationSummary,
                 UsecaseSummary,
                 EntrypointSummary,
                 RepositorySummary,
                 EnumSummary,
                 TermList,
                 Insight,
                 Sequence,
                 Glossary,
                 PackageSummary -> false;
        };
    }

    boolean failure() {
        return !success()
                // 現状、skipかどうかはfailureMessageで見るしかない
                && !"skip".equals(failureMessage);
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
                .toList();
    }

    @Override
    public String toString() {
        if (success()) {
            return String.format("%s: %s", jigDocument(), outputFileNames());
        }
        return String.format("%s: %s", jigDocument(), failureMessage);
    }
}
