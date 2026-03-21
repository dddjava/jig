package org.dddjava.jig.domain.model.information.outbound.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringDataUtilTest {

    @ParameterizedTest
    @CsvSource({
            "findSomething, SELECT",
            "readSomething, SELECT",
            "getSomething, SELECT",
            "querySomething, SELECT",
            "countSomething, SELECT",
            "existsSomething, SELECT",
            "saveSomething, INSERT",
            "insertSomething, INSERT",
            "createSomething, INSERT",
            "addSomething, INSERT",
            "updateSomething, UPDATE",
            "setSomething, UPDATE",
            "deleteSomething, DELETE",
            "removeSomething, DELETE",
    })
    void testInferOperationType(String methodName, PersistenceOperationType expectedType) {
        Optional<PersistenceOperationType> actual = SpringDataUtil.inferOperationType(methodName);
        assertEquals(expectedType, actual.orElseThrow());
    }

    @Test
    void testUnknownMethod() {
        assertTrue(SpringDataUtil.inferOperationType("unknownMethod").isEmpty());
    }
}
