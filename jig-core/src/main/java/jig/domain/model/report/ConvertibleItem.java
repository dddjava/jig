package jig.domain.model.report;

public interface ConvertibleItem<T> {
    RowConverter<T> converter();

    String name();

    default String convert(T row) {
        return converter().convert(row);
    }
}