package jig.domain.model.report.template;

import java.util.List;
import java.util.stream.Collectors;

public class ReportImpl<T> implements Report {

    String title;
    List<ItemRowConverter<T>> itemRowConverters;
    List<T> rows;

    public ReportImpl(String title, List<ItemRowConverter<T>> itemRowConverters, List<T> rows) {
        this.title = title;
        this.itemRowConverters = itemRowConverters;
        this.rows = rows;
    }

    @Override
    public Title title() {
        return new Title(title);
    }

    @Override
    public ReportRow headerRow() {
        return ReportRow.of(
                itemRowConverters.stream().map(ItemRowConverter::name).toArray(String[]::new));
    }

    @Override
    public List<ReportRow> rows() {
        return rows.stream()
                .map(row -> ReportRow.of(
                        itemRowConverters.stream().map(itemRowConverter -> itemRowConverter.convert(row)).toArray(String[]::new)))
                .collect(Collectors.toList());
    }
}
