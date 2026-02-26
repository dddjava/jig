package org.dddjava.jig.domain.model.data.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceOperationTest {

    @Test
    void Queryがunsupportedでも解決済みテーブルがあればそれを返す() {
        PersistenceOperation statement = new PersistenceOperation(
                PersistenceOperationId.from("example.ExampleRepository.findById"),
                Query.unsupported(),
                SqlType.SELECT,
                new PersistenceTargets(new PersistenceTarget("example_table")));

        assertEquals("[example_table]", statement.persistenceTargets().asText());
    }
}
