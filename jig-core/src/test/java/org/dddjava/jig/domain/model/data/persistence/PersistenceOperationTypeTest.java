package org.dddjava.jig.domain.model.data.persistence;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceOperationTypeTest {

    @Test
    void UNKNOWNはクエリがあってもテーブルを解析せず解析失敗として扱う() {
        var query = Query.from("select * from crud_test").orElseThrow();
        var id = PersistenceAccessorOperationId.fromTypeIdAndName(
                org.dddjava.jig.domain.model.data.types.TypeId.valueOf("example.ExampleMapper"), "example");

        var targetOperationTypes = PersistenceOperationType.UNKNOWN.extractTable(Optional.of(query), id);

        var targets = targetOperationTypes.persistenceTargets();
        assertEquals(1, targets.size());
        var target = targets.iterator().next();
        assertEquals(PersistenceOperationType.unexpectedTable(), target.persistenceTarget());
        assertEquals(PersistenceOperationType.UNKNOWN, target.operationType());
    }
}
