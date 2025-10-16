package org.dddjava.jig;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class HandleResultImpl implements HandleResult {

    private final JigDocument jigDocument;
    private final List<Path> outputFilePaths;
    @Nullable
    private final String failureMessage;

    /**
     * 成功時のコンストラクタ
     */
    HandleResultImpl(JigDocument jigDocument, List<Path> outputFilePaths) {
        this.jigDocument = jigDocument;
        this.outputFilePaths = outputFilePaths;
        this.failureMessage = outputFilePaths.isEmpty() ? "skip" : null;
    }

    /**
     * 失敗時のコンストラクタ
     */
    HandleResultImpl(JigDocument jigDocument, Exception e) {
        this.jigDocument = jigDocument;
        this.outputFilePaths = Collections.emptyList();
        this.failureMessage = e.getMessage();
    }

    @Override
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

    @Override
    public boolean failure() {
        return !success()
                // 現状、skipかどうかはfailureMessageで見るしかない
                && !"skip".equals(failureMessage);
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
        return failureMessage == null;
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
        return String.format("%s: %s", jigDocument(), failureMessage);
    }
}
