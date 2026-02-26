package org.dddjava.jig.domain.model.implementation.datasource;

import org.dddjava.jig.domain.model.data.persistence.Table;
import org.dddjava.jig.domain.model.data.persistence.Tables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TablesTest {

    @Test
    void test() {
        Table hoge = new Table("hoge");
        Table fuga1 = new Table("fuga");
        Table fuga2 = new Table("fuga");

        Tables sut = new Tables(hoge)
                .merge(new Tables(fuga1))
                .merge(new Tables(fuga2));

        assertEquals("[fuga, hoge]", sut.asText());
    }
}