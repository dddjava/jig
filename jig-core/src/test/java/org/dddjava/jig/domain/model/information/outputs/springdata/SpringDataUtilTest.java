package org.dddjava.jig.domain.model.information.outputs.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringDataUtilTest {

    @Test
    void testInferOperationType() {
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferOperationType("findSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferOperationType("readSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferOperationType("getSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferOperationType("querySomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferOperationType("countSomething").get());
        assertEquals(PersistenceOperationType.SELECT, SpringDataUtil.inferOperationType("existsSomething").get());

        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferOperationType("saveSomething").get());
        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferOperationType("insertSomething").get());
        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferOperationType("createSomething").get());
        assertEquals(PersistenceOperationType.INSERT, SpringDataUtil.inferOperationType("addSomething").get());

        assertEquals(PersistenceOperationType.UPDATE, SpringDataUtil.inferOperationType("updateSomething").get());
        assertEquals(PersistenceOperationType.UPDATE, SpringDataUtil.inferOperationType("setSomething").get());

        assertEquals(PersistenceOperationType.DELETE, SpringDataUtil.inferOperationType("deleteSomething").get());
        assertEquals(PersistenceOperationType.DELETE, SpringDataUtil.inferOperationType("removeSomething").get());

        assertTrue(SpringDataUtil.inferOperationType("unknownMethod").isEmpty());
    }
}
