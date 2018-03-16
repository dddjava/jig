package jig.infrastructure.mybatis;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlIdentifier;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlType;
import jig.infrastructure.OnMemorySqlRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverTest {

    SqlRepository repository = new OnMemorySqlRepository();
    MyBatisSqlResolver sut = new MyBatisSqlResolver(repository);

    @Test
    void SQLのテーブル名とCRUDを取得する() throws Exception {
        sut.resolve(
                Paths.get("../sut/build/classes/java/main").toUri().toURL(),
                Paths.get("../sut/build/resources/main").toUri().toURL());

        assertThat(repository.get(new SqlIdentifier("sut.infrastructure.datasource.FugaMapper.get")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("fuga", SqlType.SELECT);

        assertThat(repository.get(new SqlIdentifier("sut.infrastructure.datasource.AnnotationMapper.select")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.SELECT);
        assertThat(repository.get(new SqlIdentifier("sut.infrastructure.datasource.AnnotationMapper.insert")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.INSERT);
        assertThat(repository.get(new SqlIdentifier("sut.infrastructure.datasource.AnnotationMapper.update")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.UPDATE);
        assertThat(repository.get(new SqlIdentifier("sut.infrastructure.datasource.AnnotationMapper.delete")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.DELETE);
    }
}