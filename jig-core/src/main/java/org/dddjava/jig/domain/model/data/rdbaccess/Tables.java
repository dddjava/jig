package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * テーブル一覧
 */
public record Tables(List<Table> tables) {

    public Tables(Table table) {
        this(Collections.singletonList(table));
    }

    public static Tables nothing() {
        return new Tables(Collections.emptyList());
    }

    public Tables merge(Tables other) {
        ArrayList<Table> list = new ArrayList<>(this.tables);
        list.addAll(other.tables);
        return new Tables(list);
    }

    public String asText() {
        // 文字列としてユニーク。ソートされてるのは自然なのでメソッド名に含めない。
        return tables.stream()
                .map(Table::name)
                .distinct()
                .sorted()
                .collect(joining(", ", "[", "]"));
    }
}
