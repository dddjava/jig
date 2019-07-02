package org.dddjava.jig.domain.model.interpret.analyzed;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 解析結果一式
 */
public class AnalyzeStatuses {

    List<AnalyzeStatus> list;

    public AnalyzeStatuses(List<AnalyzeStatus> list) {
        this.list = list;
    }

    public boolean hasWarning() {
        return !list.isEmpty();
    }

    public boolean hasError() {
        return !listErrorOnly().isEmpty();
    }

    List<AnalyzeStatus> listErrorOnly() {
        return list.stream()
                .filter(AnalyzeStatus::isError)
                .collect(Collectors.toList());
    }

    public List<AnalyzeStatus> listWarning() {
        return list;
    }

    public List<AnalyzeStatus> listErrors() {
        return listErrorOnly();
    }
}
