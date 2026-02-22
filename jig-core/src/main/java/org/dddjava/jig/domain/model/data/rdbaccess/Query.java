package org.dddjava.jig.domain.model.data.rdbaccess;

import org.jspecify.annotations.Nullable;

/**
 * クエリ
 */
public record Query(String text) {

    public static final String UNSUPPORTED = "<<unsupported>>";

    public static Query from(@Nullable String text) {
        if (text == null) return unsupported();
        return new Query(text);
    }

    public static Query unsupported() {
        return new Query(UNSUPPORTED);
    }

    @Override
    public String text() {
        if (UNSUPPORTED.equals(text)) {
            // 特殊値を返さないようにする
            // Queryのtextは外部から使用しないので例外でよい。これが発生したらバグ。
            throw new IllegalArgumentException("BUG!!");
        }
        return text;
    }

    public boolean supported() {
        return !UNSUPPORTED.equals(text);
    }

    public String normalizedQuery() {
        return normalizeSql(text());
    }

    private static String normalizeSql(String query) {
        String remaining = query;
        while (true) {
            String trimmed = remaining.stripLeading();
            if (trimmed.startsWith("\uFEFF")) {
                remaining = trimmed.substring(1);
                continue;
            }
            if (trimmed.startsWith("--")) {
                int newlineIndex = trimmed.indexOf('\n');
                if (newlineIndex < 0) return "";
                remaining = trimmed.substring(newlineIndex + 1);
                continue;
            }
            if (trimmed.startsWith("/*")) {
                int commentEndIndex = trimmed.indexOf("*/");
                if (commentEndIndex < 0) return "";
                remaining = trimmed.substring(commentEndIndex + 2);
                continue;
            }
            if (trimmed.startsWith("(")) {
                remaining = trimmed.substring(1);
                continue;
            }
            return trimmed;
        }
    }
}
