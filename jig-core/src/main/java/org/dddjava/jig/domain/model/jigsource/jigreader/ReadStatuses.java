package org.dddjava.jig.domain.model.jigsource.jigreader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 読み取り結果一覧
 */
public class ReadStatuses {

    List<ReadStatus> list;

    public ReadStatuses(List<ReadStatus> list) {
        this.list = list;
    }

    public boolean hasWarning() {
        return !list.isEmpty();
    }

    public boolean hasError() {
        return !listErrorOnly().isEmpty();
    }

    List<ReadStatus> listErrorOnly() {
        return list.stream()
                .filter(ReadStatus::isError)
                .collect(Collectors.toList());
    }

    public List<ReadStatus> listWarning() {
        return list;
    }

    public List<ReadStatus> listErrors() {
        return listErrorOnly();
    }
}
