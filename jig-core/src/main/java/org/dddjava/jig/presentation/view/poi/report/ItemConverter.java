package org.dddjava.jig.presentation.view.poi.report;

public interface ItemConverter {

    String name();

    String convert(Object item);
}