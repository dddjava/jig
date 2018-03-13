package jig.infrastructure.mybatis;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverTest {

    MyBatisSqlResolver sut = new MyBatisSqlResolver();

    @Test
    void test() throws Exception {
        // Mapper.classとMapper.xmlのあるディレクトリ
        URL[] urls = {
                Paths.get("../sut/build/classes/java/main").toUri().toURL(),
                Paths.get("../sut/build/resources/main").toUri().toURL(),
        };
        Sqls actual = sut.collectSqls(urls);

        assertThat(actual).isNotNull();
    }
}