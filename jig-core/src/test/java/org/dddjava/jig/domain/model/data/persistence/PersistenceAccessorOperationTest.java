package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceAccessorOperationTest {

    @Test
    void Queryがunsupportedでも解決済みテーブルがあればそれを返す() {
        PersistenceAccessorOperation statement = new PersistenceAccessorOperation(
                PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf("example.ExampleRepository"), "findById"),
                PersistenceOperationType.SELECT, new PersistenceOperations(PersistenceOperation.from(new PersistenceTarget("example_table"), PersistenceOperationType.SELECT)), Query.unsupported()
        );

        assertEquals("[example_table]", statement.operations().asText());
    }
}
