package jig.domain.model.datasource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TablesTest {

    @Test
    void test() {
        Table hoge = new Table("hoge");
        Table fuga1 = new Table("fuga");
        Table fuga2 = new Table("fuga");

        Tables sut = new Tables(hoge)
                .merge(new Tables(fuga1))
                .merge(new Tables(fuga2));

        assertThat(sut.asText()).isEqualTo("hoge,fuga");
    }
}