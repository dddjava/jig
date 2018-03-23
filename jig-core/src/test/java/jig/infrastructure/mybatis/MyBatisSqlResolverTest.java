package jig.infrastructure.mybatis;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlIdentifier;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlType;
import jig.infrastructure.JigPaths;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import testing.TestSupport;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverTest {

    @Test
    void bindを使ってても解析できる() {
        SqlRepository repository = new OnMemorySqlRepository();
        MyBatisSqlResolver sut = new MyBatisSqlResolver(repository, new JigPaths());

        sut.resolve(TestSupport.getTestResourceRootURLs());

        Sql sql = repository.get(new SqlIdentifier("stub.infrastructure.datasource.SampleMapper.binding"));
        assertThat(sql.tableName()).isEqualTo("fuga");
    }

    @ParameterizedTest
    @MethodSource
    void 標準的なパターン(String methodName, String tableName, SqlType sqlType) {
        SqlRepository repository = new OnMemorySqlRepository();
        MyBatisSqlResolver sut = new MyBatisSqlResolver(repository, new JigPaths());

        sut.resolve(TestSupport.getTestResourceRootURLs());

        SqlIdentifier sqlIdentifier = new SqlIdentifier("stub.infrastructure.datasource.CanonicalMapper." + methodName);
        Sql sql = repository.get(sqlIdentifier);
        assertThat(sql.tableName()).isEqualTo(tableName);
        assertThat(sql.sqlType()).isEqualTo(sqlType);
    }

    static Stream<Arguments> 標準的なパターン() {
        return Stream.of(
                Arguments.of("insert", "crud_test", SqlType.INSERT),
                Arguments.of("select", "crud_test", SqlType.SELECT),
                Arguments.of("update", "crud_test", SqlType.UPDATE),
                Arguments.of("delete", "crud_test", SqlType.DELETE),
                Arguments.of("annotationInsert", "crud_test", SqlType.INSERT),
                Arguments.of("annotationSelect", "crud_test", SqlType.SELECT),
                Arguments.of("annotationUpdate", "crud_test", SqlType.UPDATE),
                Arguments.of("annotationDelete", "crud_test", SqlType.DELETE),
                Arguments.of("tabInsert", "tab_test", SqlType.INSERT),
                Arguments.of("tabSelect", "tab_test", SqlType.SELECT),
                Arguments.of("tabUpdate", "tab_test", SqlType.UPDATE),
                Arguments.of("tabDelete", "tab_test", SqlType.DELETE),
                Arguments.of("japanese", "あのスキーマ.このテーブル", SqlType.SELECT),
                Arguments.of("illegal", "（解析失敗）", SqlType.INSERT),
                Arguments.of("sequence_postgresql", "nextval('seq_test')", SqlType.SELECT)
        );
    }
}