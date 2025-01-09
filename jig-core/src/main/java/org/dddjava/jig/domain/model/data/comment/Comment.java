package org.dddjava.jig.domain.model.data.comment;

import java.util.stream.Stream;

/**
 * ドキュメントコメント
 *
 * 通常はソースコードから読み取るJavadoc
 */
public class Comment {
    private static final Comment EMPTY = new Comment("");

    final String value;
    volatile String firstSentence = null;

    private Comment(String value) {
        this.value = value;
    }

    public static Comment empty() {
        return EMPTY;
    }

    public boolean exists() {
        return value.length() > 0;
    }

    public String summaryText() {
        if (firstSentence != null) return firstSentence;
        if (value.isEmpty()) {
            firstSentence = "";
            return firstSentence;
        }

        // 改行や句点の手前まで。
        firstSentence = Stream.of(this.value.indexOf("\n"), this.value.indexOf("。"))
                .filter(length -> length >= 0)
                .min(Integer::compareTo)
                .map(end -> this.value.substring(0, end))
                .orElse(this.value);
        return firstSentence; // 改行も句点も無い場合はそのまま返す
    }

    public static Comment fromCodeComment(String sourceText) {
        return new Comment(sourceText);
    }

    public String asText() {
        return summaryText();
    }

    public String asTextOrDefault(String defaultText) {
        if (exists()) {
            return summaryText();
        }
        return defaultText;
    }

    public String fullText() {
        return value;
    }

    public String bodyText() {
        // 改行や句点を除くために+1
        int beginIndex = summaryText().length() + 1;
        if (value.length() <= beginIndex) {
            return "";
        }

        return value.substring(beginIndex);
    }
}
