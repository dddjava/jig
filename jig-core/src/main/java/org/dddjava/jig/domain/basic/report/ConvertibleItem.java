package org.dddjava.jig.domain.basic.report;

public interface ConvertibleItem<T> {

    String name();

    String convert(T row);
}