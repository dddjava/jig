package org.dddjava.jig.infrastructure.view.poi.report;

import java.util.Arrays;
import java.util.List;

public class ModelReports {

    List<ModelReportInterface> list;

    public ModelReports(ModelReportInterface... reporters) {
        this.list = Arrays.asList(reporters);
    }

    public List<ModelReportInterface> list() {
        return list;
    }

    public boolean empty() {
        return list.stream()
                .allMatch(modelReportInterface -> modelReportInterface.nothing());
    }
}
