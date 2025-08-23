package org.dddjava.jig.adapter.poi;

import java.util.function.Function;

public record ReportItem<T>(String label, Function<T, ?> valueResolver) {

    public static <T> ReportItem<T> ofString(String label, Function<T, String> valueResolver) {
        return new ReportItem<>(label, valueResolver);
    }

    public static <T> ReportItem<T> ofNumber(String label, Function<T, Number> valueResolver) {
        return new ReportItem<>(label, valueResolver);
    }
}
