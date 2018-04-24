package jig.domain.model.report.template;

import java.util.function.Function;

public class ItemRowConverter<T> {

    Enum<?> item;
    Function<T, String> rowConverter;

    public ItemRowConverter(Enum<?> item, Function<T, String> rowConverter) {
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
