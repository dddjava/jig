package org.dddjava.jig.presentation.view.poi.report;

import java.util.List;
import java.util.stream.Collectors;

public class Report {

    ItemConverter[] itemConverters;

    String title;
    List<?> rows;

    public Report(String title, List<?> rows, ItemConverter[] values) {
        this.title = title;
        this.itemConverters = values;
        this.rows = rows;
    }

    public String title() {
        return title;
    }

    public ReportRow headerRow() {
        return ReportRow.of(itemConverters, ItemConverter::name);
    }

    public List<ReportRow> rows() {
        return rows.stream()
                .map(row -> ReportRow.of(itemConverters, converter -> converter.convert(row)))
                .collect(Collectors.toList());
    }
}
