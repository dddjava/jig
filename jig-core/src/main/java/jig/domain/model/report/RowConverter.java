package jig.domain.model.report;

import java.util.function.Function;

public class RowConverter<T> {

    Enum<?> item;
    Function<T, String> rowConverter;

    public RowConverter(Enum<?> item, Function<T, String> rowConverter) {
        this.item = item;
        this.rowConverter = rowConverter;
    }

    String name() {
        return item.name();
    }

    public String convert(T row) {
        return rowConverter.apply(row);
    }
}
