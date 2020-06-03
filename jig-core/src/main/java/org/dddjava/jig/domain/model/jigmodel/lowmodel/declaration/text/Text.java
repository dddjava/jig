package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.text;

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

    public static <T> String uniqueOf(List<T> list, Function<T, String> textConverter) {
        // 文字列としてユニーク。ソートされてるのは自然なのでメソッド名に含めない。
        return list.stream()
                .map(textConverter)
                .distinct()
                .sorted()
                .collect(collectionCollector());
    }
}
