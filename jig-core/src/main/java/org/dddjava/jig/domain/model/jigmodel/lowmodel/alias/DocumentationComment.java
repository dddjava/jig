package org.dddjava.jig.domain.model.jigmodel.lowmodel.alias;

import java.util.stream.Stream;

/**
 * 別名
 *
 * TODO rename。これ自体はコメントから取得した追加情報的なものとする。
 */
public class DocumentationComment {

    String value;

    private DocumentationComment(String value) {
        this.value = value;
    }

    public static DocumentationComment empty() {
        return new DocumentationComment("");
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean exists() {
        return value.length() > 0;
    }

    public static DocumentationComment fromText(String sourceText) {
        int end = Stream.of(sourceText.indexOf("\n"), sourceText.indexOf("。"), sourceText.length())
                .filter(length -> length >= 0)
                .min(Integer::compareTo).orElseThrow(IllegalStateException::new);
        return new DocumentationComment(sourceText.substring(0, end));
    }
}
