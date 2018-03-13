package jig.infrastructure.mybatis;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverTest {

    MyBatisSqlResolver sut = new MyBatisSqlResolver();

    @Test
    void SQLのテーブル名とCRUDを取得する() throws Exception {
        URL[] urls = {
                Paths.get("../sut/build/classes/java/main").toUri().toURL(),
                Paths.get("../sut/build/resources/main").toUri().toURL()};
        Sqls sqls = sut.collectSqls(urls);

        assertThat(sqls.get(new SqlIdentifier("sut.infrastructure.datasource.FugaMapper.get")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("fuga", SqlType.SELECT);

        assertThat(sqls.get(new SqlIdentifier("sut.infrastructure.datasource.PiyoMapper.register")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("piyo", SqlType.INSERT);
    }
}