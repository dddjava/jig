package org.dddjava.jig;

import java.util.List;

/**
 * JIGの出力結果
 */
public interface JigResult {
    List<HandleResult> listResult();

    JigSummary summary();

    java.nio.file.Path indexFilePath();

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
        public static JigSummary empty() {
            return new JigSummary(0, 0, 0, 0, 0);
        }
    }
}
