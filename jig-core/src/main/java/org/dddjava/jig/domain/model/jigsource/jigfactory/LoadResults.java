package org.dddjava.jig.domain.model.jigsource.jigfactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 解析結果一式
 */
public class LoadResults {

    List<LoadResult> list;

    public LoadResults(List<LoadResult> list) {
        this.list = list;
    }

    public boolean hasWarning() {
        return !list.isEmpty();
    }

    public boolean hasError() {
        return !listErrorOnly().isEmpty();
    }

    List<LoadResult> listErrorOnly() {
        return list.stream()
                .filter(LoadResult::isError)
                .collect(Collectors.toList());
    }

    public List<LoadResult> listWarning() {
        return list;
    }

    public List<LoadResult> listErrors() {
        return listErrorOnly();
    }
}
