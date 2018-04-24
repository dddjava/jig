package jig.domain.model.report;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ConvertibleItemReport<ROW> implements Report {

    ConvertibleItem<ROW>[] convertibleItems;

    String title;
    List<ROW> rows;

    public ConvertibleItemReport(String title, List<ROW> rows, ConvertibleItem<ROW>[] values) {
        this.title = title;
        this.convertibleItems = values;
        this.rows = rows;
    }

    @Override
    public Title title() {
        return new Title(title);
    }

    @Override
    public ReportRow headerRow() {
        return ReportRow.of(
                Arrays.stream(convertibleItems).map(ConvertibleItem::name).toArray(String[]::new));
    }

    @Override
    public List<ReportRow> rows() {
        return rows.stream()
                .map(row -> ReportRow.of(
                        Arrays.stream(convertibleItems).map(converter -> converter.convert(row)).toArray(String[]::new)))
                .collect(Collectors.toList());
    }
}
