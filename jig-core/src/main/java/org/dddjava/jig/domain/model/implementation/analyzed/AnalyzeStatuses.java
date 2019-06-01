package org.dddjava.jig.domain.model.implementation.analyzed;

import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;

import java.util.List;
import java.util.ResourceBundle;
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

    public String errorLogText() {
        return listErrorOnly().stream()
                .map(status -> {
                    ResourceBundle resource = Utf8ResourceBundle.messageBundle();
                    return resource.getString(status.messageKey);
                })
                .collect(Collectors.joining("\n- ", "- ", ""));
    }

    public String warningLogText() {
        return list.stream()
                .map(status -> {
                    ResourceBundle resource = Utf8ResourceBundle.messageBundle();
                    return resource.getString(status.messageKey);
                })
                .collect(Collectors.joining("\n- ", "- ", ""));
    }
}
