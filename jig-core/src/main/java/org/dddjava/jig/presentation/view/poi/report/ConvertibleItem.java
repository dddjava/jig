package org.dddjava.jig.presentation.view.poi.report;

public interface ConvertibleItem<T> {

    String name();

    String convert(T row);
}