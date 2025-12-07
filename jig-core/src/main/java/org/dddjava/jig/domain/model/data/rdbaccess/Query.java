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

    public Tables extractTable(SqlType sqlType) {
        Table table = UNSUPPORTED.equals(text) ? sqlType.unexpectedTable() : sqlType.extractTable(text);
        return new Tables(table);
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
}
