package jig.domain.model.report;

public interface ConvertibleItem<T> {

    String name();

    String convert(T row);
}