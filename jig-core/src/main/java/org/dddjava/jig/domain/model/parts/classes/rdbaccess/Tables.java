package org.dddjava.jig.domain.model.parts.classes.rdbaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * テーブル一覧
 */
public class Tables {
    private final List<Table> tables;

    public Tables(Table table) {
        this(Collections.singletonList(table));
    }

    private Tables(List<Table> tables) {
        this.tables = tables;
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
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
