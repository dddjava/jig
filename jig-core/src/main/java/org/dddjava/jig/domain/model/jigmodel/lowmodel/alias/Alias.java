package org.dddjava.jig.domain.model.jigmodel.lowmodel.alias;

import java.util.stream.Stream;

/**
 * 別名
 * <p>
 * TODO rename。これ自体はコメントから取得した追加情報的なものとする。
 */
public class Alias {

    String value;

    private Alias(String value) {
        this.value = value;
    }

    public static Alias empty() {
        return new Alias("");
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean exists() {
        return value.length() > 0;
    }

    public static Alias fromText(String sourceText) {
        int end = Stream.of(sourceText.indexOf("\n"), sourceText.indexOf("。"), sourceText.length())
                .filter(length -> length >= 0)
                .min(Integer::compareTo).orElseThrow(IllegalStateException::new);
        return new Alias(sourceText.substring(0, end));
    }
}
