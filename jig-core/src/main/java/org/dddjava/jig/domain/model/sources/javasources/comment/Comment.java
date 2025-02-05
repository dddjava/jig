package org.dddjava.jig.domain.model.sources.javasources.comment;

import java.util.regex.Pattern;
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

    public static Comment from(String title, String description) {
        return new Comment(title + "\n" + description);
    }

    public static Comment empty() {
        return EMPTY;
    }

    public boolean exists() {
        return !value.isEmpty();
    }

    public String summaryText() {
        if (firstSentence != null) return firstSentence;
        if (value.isEmpty()) {
            firstSentence = "";
            return firstSentence;
        }

        // 改行や句点の手前まで。
        firstSentence = Stream.of(this.value.indexOf("\r\n"), this.value.indexOf("\n"), this.value.indexOf("。"))
                .filter(length -> length >= 0)
                .min(Integer::compareTo)
                .map(end -> this.value.substring(0, end))
                .orElse(this.value);
        return firstSentence; // 改行も句点も無い場合はそのまま返す
    }

    /**
     * インラインのlinkタグをテキストにするためのパターン
     */
    private static final Pattern INLINETAG_LINK_PATTERN = Pattern.compile("\\{@link\\s+(?:\\S+\\s+)?(\\S+)\\s*}");

    /**
     * 改行コードを統一するためのパターン。
     * Javaparserを使用する場合、ソースの改行コードに関わらずline.separatorに置き換えられる。JIGの出力は\nに寄せるので、ここで一律置き換える。
     */
    private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("\\R");

    public static Comment fromCodeComment(String sourceText) {
        return new Comment(
                INLINETAG_LINK_PATTERN.matcher(
                        LINE_SEPARATOR_PATTERN.matcher(sourceText).replaceAll("\n")
                ).replaceAll("$1"));
    }

}
