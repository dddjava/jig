package org.dddjava.jig.domain.model.implementation.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceTarget;
import org.dddjava.jig.domain.model.data.persistence.PersistenceTargets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceTargetsTest {

    @Test
    void test() {
        PersistenceTarget hoge = new PersistenceTarget("hoge");
        PersistenceTarget fuga1 = new PersistenceTarget("fuga");
        PersistenceTarget fuga2 = new PersistenceTarget("fuga");

        PersistenceTargets sut = new PersistenceTargets(hoge)
                .merge(new PersistenceTargets(fuga1))
                .merge(new PersistenceTargets(fuga2));

        assertEquals("[fuga, hoge]", sut.asText());
    }
}