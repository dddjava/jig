package org.dddjava.jig.domain.model.information.outbound.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringDataBaseMethodTest {

    @Test
    void enum値が正しく定義されている() {
        assertEquals("save", SpringDataBaseMethod.SAVE.methodName());
        assertEquals(PersistenceOperationType.INSERT, SpringDataBaseMethod.SAVE.persistenceOperationType());

        assertEquals("findById", SpringDataBaseMethod.FIND_BY_ID.methodName());
        assertEquals(PersistenceOperationType.SELECT, SpringDataBaseMethod.FIND_BY_ID.persistenceOperationType());

        assertEquals("delete", SpringDataBaseMethod.DELETE.methodName());
        assertEquals(PersistenceOperationType.DELETE, SpringDataBaseMethod.DELETE.persistenceOperationType());
    }

    @Test
    void streamで全メソッドが取得できる() {
        List<String> methodNames = SpringDataBaseMethod.stream()
                .map(SpringDataBaseMethod::methodName)
                .collect(Collectors.toList());

        List<String> expected = List.of(
                "save", "saveAll", "findById", "findAll", "findAllById",
                "existsById", "count", "deleteById", "delete", "deleteAllById", "deleteAll"
        );

        assertEquals(expected.size(), methodNames.size());
        assertTrue(methodNames.containsAll(expected));
        assertTrue(expected.containsAll(methodNames));
    }
}
