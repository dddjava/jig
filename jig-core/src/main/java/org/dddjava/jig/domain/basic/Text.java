package org.dddjava.jig.domain.basic;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Text {

    public static Collector<CharSequence, ?, String> collectionCollector() {
        return Collectors.joining(", ", "[", "]");
    }
}
