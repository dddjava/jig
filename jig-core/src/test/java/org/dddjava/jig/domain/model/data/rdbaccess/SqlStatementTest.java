package org.dddjava.jig.domain.model.data.rdbaccess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlStatementTest {

    @Test
    void Queryがunsupportedでも解決済みテーブルがあればそれを返す() {
        SqlStatement statement = new SqlStatement(
                SqlStatementId.from("example.ExampleRepository.findById"),
                Query.unsupported(),
                SqlType.SELECT,
                new Table("example_table"));

        assertEquals("[example_table]", statement.tables().asText());
    }
}
