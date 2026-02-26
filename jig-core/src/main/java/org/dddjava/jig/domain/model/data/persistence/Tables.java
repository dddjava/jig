package org.dddjava.jig.domain.model.data.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.joining;

/**
 * テーブル一覧
 */
public record Tables(Collection<Table> tables) {

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
