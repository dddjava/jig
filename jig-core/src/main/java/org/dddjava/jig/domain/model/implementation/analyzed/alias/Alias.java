package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import java.util.stream.Stream;

/**
 * 別名
 */
public class Alias {

    final String value;

    public Alias(String value) {
        this.value = value;
    }

    public String summarySentence() {
        int end = Stream.of(value.indexOf("\n"), value.indexOf("。"), value.length())
                .filter(length -> length >= 0)
                .min(Integer::compareTo).orElseThrow(IllegalStateException::new);
        return value.substring(0, end);
    }

    public String value() {
        return value;
    }

    public boolean exists() {
        return value.length() > 0;
    }
}
