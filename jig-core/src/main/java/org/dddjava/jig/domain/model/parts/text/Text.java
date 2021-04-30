package org.dddjava.jig.domain.model.parts.text;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Text {

    private static Collector<CharSequence, ?, String> collectionCollector() {
        return Collectors.joining(", ", "[", "]");
    }

    public static <T> String of(List<T> list, Function<T, String> textConverter) {
        return list.stream()
                .map(textConverter)
                .collect(collectionCollector());
    }

    public static <T> String sortedOf(List<T> list, Function<T, String> textConverter) {
        return list.stream()
                .map(textConverter)
                .sorted()
                .collect(collectionCollector());
    }
}
