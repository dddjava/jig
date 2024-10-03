package org.dddjava.jig.infrastructure.view.poi.report;

import java.util.Arrays;
import java.util.List;

public class ModelReports {

    List<ModelReport<?>> list;

    public ModelReports(ModelReport<?>... reporters) {
        this.list = Arrays.asList(reporters);
    }

    public List<ModelReport<?>> list() {
        return list;
    }

    public boolean empty() {
        return list.stream()
                .allMatch(modelReport -> modelReport.pivotModels.isEmpty());
    }
}
