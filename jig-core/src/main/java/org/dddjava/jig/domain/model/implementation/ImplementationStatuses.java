package org.dddjava.jig.domain.model.implementation;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ImplementationStatuses {

    List<ImplementationStatus> list;

    public ImplementationStatuses(List<ImplementationStatus> list) {
        this.list = list;
    }

    public boolean hasWarning() {
        return !list.isEmpty();
    }

    public boolean hasError() {
        return !listErrorOnly().isEmpty();
    }

    List<ImplementationStatus> listErrorOnly() {
        return list.stream()
                .filter(ImplementationStatus::isError)
                .collect(Collectors.toList());
    }

    public String errorLogText() {
        return listErrorOnly().stream()
                .map(status -> {
                    ResourceBundle resource = ResourceBundle.getBundle("jig-messages");
                    return resource.getString(status.messageKey);
                })
                .collect(Collectors.joining("\n- ", "- ", ""));
    }

    public String warningLogText() {
        return list.stream()
                .map(status -> {
                    ResourceBundle resource = ResourceBundle.getBundle("jig-messages");
                    return resource.getString(status.messageKey);
                })
                .collect(Collectors.joining("\n- ", "- ", ""));
    }
}
