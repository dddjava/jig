package org.dddjava.jig.domain.model.data.rdbaccess;

import org.jspecify.annotations.Nullable;

/**
 * クエリ
 */
public class Query {

    @Nullable
    String text;

    public Query(@Nullable String text) {
        this.text = text;
    }

    public Tables extractTable(SqlType sqlType) {
        Table table = text == null ? sqlType.unexpectedTable() : sqlType.extractTable(text);
        return new Tables(table);
    }

    public static Query unsupported() {
        return new Query(null);
    }
}
