package org.dddjava.jig.domain.model.jigmodel.lowmodel.alias;

import java.util.stream.Stream;

/**
 * ドキュメントコメント
 *
 * 通常はソースコードから読み取るJavadoc
 */
public class DocumentationComment {

    String value;

    private DocumentationComment(String value) {
        this.value = value;
    }

    public static DocumentationComment empty() {
        return new DocumentationComment("");
    }

    public boolean exists() {
        return value.length() > 0;
    }

    public String summaryText() {
        if (value.isEmpty()) {
            return "";
        }

        return Stream.of(value.indexOf("\n"), value.indexOf("。"))
                .filter(length -> length >= 0)
                .min(Integer::compareTo)
                .map(end -> value.substring(0, end))
                .orElse(value); // 改行も句点も無い場合はそのまま返す
    }

    public static DocumentationComment fromText(String sourceText) {
        return new DocumentationComment(sourceText);
    }

    public boolean markedCore() {
        return value.startsWith("*");
    }
}
