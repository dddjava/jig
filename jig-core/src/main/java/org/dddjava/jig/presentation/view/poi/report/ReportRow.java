package org.dddjava.jig.presentation.view.poi.report;

import java.util.List;

public class ReportRow {

    List<String> list;

    ReportRow(List<String> list) {
        this.list = list;
    }

    public List<String> list() {
        return list;
    }
}
