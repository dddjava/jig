package org.dddjava.jig.domain.basic.report;

import java.util.List;
import java.util.stream.Collectors;

public class Report<ROW> {

    ConvertibleItem<ROW>[] convertibleItems;

    String title;
    List<ROW> rows;

    public Report(String title, List<ROW> rows, ConvertibleItem<ROW>[] values) {
        this.title = title;
        this.convertibleItems = values;
        this.rows = rows;
    }

    public Title title() {
        return new Title(title);
    }

    public ReportRow headerRow() {
        return ReportRow.of(convertibleItems, ConvertibleItem::name);
    }

    public List<ReportRow> rows() {
        return rows.stream()
                .map(row -> ReportRow.of(convertibleItems, converter -> converter.convert(row)))
                .collect(Collectors.toList());
    }
}
