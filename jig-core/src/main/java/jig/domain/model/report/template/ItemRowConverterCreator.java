package jig.domain.model.report.template;

public interface Funcable<T> {
    ItemRowConverter<T> func();
}