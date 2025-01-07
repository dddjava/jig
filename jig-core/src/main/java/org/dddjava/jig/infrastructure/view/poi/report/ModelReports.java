package org.dddjava.jig.infrastructure.view.poi.report;

import java.util.Arrays;
import java.util.List;

public class ModelReports {

    List<GenericModelReport<?>> list;

    public ModelReports(GenericModelReport<?>... reporters) {
        this.list = Arrays.asList(reporters);
    }

    public List<GenericModelReport<?>> list() {
        return list;
    }

    public boolean empty() {
        return list.stream()
                .allMatch(modelReportInterface -> modelReportInterface.nothing());
    }
}
