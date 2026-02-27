package org.dddjava.jig.domain.model.data.persistence;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * クエリ
 */
public record Query(String rawText, String normalizedQuery) {

    public static final String UNSUPPORTED = "<<unsupported>>";

    public static Query from(@Nullable String text) {
        if (text == null) return unsupported();
        var normalizedQuery = normalizeSql(text);
        if (normalizedQuery.isEmpty()) return unsupported();
        return new Query(text, normalizedQuery);
    }

    public static Query unsupported() {
        return new Query(UNSUPPORTED, UNSUPPORTED);
    }

    public static Optional<Query> fromSafety(String annotationQueryString) {
        // TODO: Queryのunsupportedをなくしたくて作成したメソッド
        Query query = from(annotationQueryString);
        if (query.supported()) return Optional.of(query);
        return Optional.empty();
    }

    @Override
    public String rawText() {
        if (UNSUPPORTED.equals(rawText)) {
            // 特殊値を返さないようにする
            // Queryのtextは外部から使用しないので例外でよい。これが発生したらバグ。
            throw new IllegalArgumentException("BUG!!");
        }
        return rawText;
    }

    public boolean supported() {
        return !UNSUPPORTED.equals(rawText);
    }

    @Override
    public String normalizedQuery() {
        if (UNSUPPORTED.equals(rawText)) {
            // 特殊値を返さないようにする
            // Queryのtextは外部から使用しないので例外でよい。これが発生したらバグ。
            throw new IllegalArgumentException("BUG!!");
        }
        return normalizedQuery;
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
