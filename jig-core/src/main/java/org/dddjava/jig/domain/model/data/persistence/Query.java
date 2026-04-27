package org.dddjava.jig.domain.model.data.persistence;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * クエリ
 */
public record Query(String rawText, String normalizedQuery) {

    public static Optional<Query> from(@Nullable String text) {
        if (text == null) return Optional.empty();
        var normalizedQuery = normalizeSql(text);
        if (normalizedQuery.isEmpty()) return Optional.empty();
        return Optional.of(new Query(text, normalizedQuery));
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
