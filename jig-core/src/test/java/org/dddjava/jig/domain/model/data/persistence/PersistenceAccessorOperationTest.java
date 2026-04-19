package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceAccessorOperationTest {

    @Test
    void Queryがunsupportedでも解決済みテーブルがあればそれを返す() {
        PersistenceAccessorOperation statement = new PersistenceAccessorOperation(
                PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf("example.ExampleRepository"), "findById"),
                PersistenceOperationType.SELECT, new PersistenceTargetOperationTypes(PersistenceTargetOperationType.from(new PersistenceTarget("example_table"), PersistenceOperationType.SELECT)), Query.unsupported()
        );

        var targets = statement.targetOperationTypes().persistenceTargets();
        assertEquals(1, targets.size());
        var target = targets.iterator().next();
        assertEquals("example_table", target.persistenceTarget().name());
        assertEquals(PersistenceOperationType.SELECT, target.operationType());
    }
}
