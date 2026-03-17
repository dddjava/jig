package org.dddjava.jig.domain.model.information.outputs.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringDataUtilTest {

    @Test
    void testInferSqlType() {
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferSqlType("findSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferSqlType("readSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferSqlType("getSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferSqlType("querySomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferSqlType("countSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferSqlType("existsSomething").get());

        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferSqlType("saveSomething").get());
        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferSqlType("insertSomething").get());
        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferSqlType("createSomething").get());
        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferSqlType("addSomething").get());

        assertEquals(PersistenceOperationType.UPDATE, SpringDataUtil.inferSqlType("updateSomething").get());
        assertEquals(PersistenceOperationType.UPDATE, SpringDataUtil.inferSqlType("setSomething").get());

        assertEquals(PersistenceOperationType.DELETE, SpringDataUtil.inferSqlType("deleteSomething").get());
        assertEquals(PersistenceOperationType.DELETE, SpringDataUtil.inferSqlType("removeSomething").get());

        assertTrue(SpringDataUtil.inferSqlType("unknownMethod").isEmpty());
    }
}
