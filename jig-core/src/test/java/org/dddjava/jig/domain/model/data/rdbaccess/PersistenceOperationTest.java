package org.dddjava.jig.domain.model.data.rdbaccess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceOperationTest {

    @Test
    void Queryがunsupportedでも解決済みテーブルがあればそれを返す() {
        PersistenceOperation statement = new PersistenceOperation(
                SqlStatementId.from("example.ExampleRepository.findById"),
                Query.unsupported(),
                SqlType.SELECT,
                new Tables(new Table("example_table")));

        assertEquals("[example_table]", statement.tables().asText());
    }
}
