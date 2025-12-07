package org.dddjava.jig.domain.model.data.rdbaccess;

import org.jspecify.annotations.Nullable;

/**
 * クエリ
 */
public record Query(@Nullable String text) {

    public static Query from(String text) {
        return new Query(text);
    }

    public Tables extractTable(SqlType sqlType) {
        Table table = text == null ? sqlType.unexpectedTable() : sqlType.extractTable(text);
        return new Tables(table);
    }

    public static Query unsupported() {
        return new Query(null);
    }
}
