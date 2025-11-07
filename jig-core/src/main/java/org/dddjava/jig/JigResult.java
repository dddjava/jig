package org.dddjava.jig;

import java.util.List;

/**
 * JIGの出力結果
 */
public interface JigResult {
    List<HandleResult> listResult();

    JigSummary summary();

    /**
     * 集計
     */
    record JigSummary(
            int numberOfSourceFiles,
            int numberOfClassFiles,
            int numberOfPackages,
            int numberOfClasses,
            int numberOfMethods
    ) {
    }
}

