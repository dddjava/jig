package org.dddjava.jig.domain.model.japanese;

import java.util.stream.Stream;

/**
 * 和名
 */
public class JapaneseName {

    final String value;

    public JapaneseName(String value) {
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
