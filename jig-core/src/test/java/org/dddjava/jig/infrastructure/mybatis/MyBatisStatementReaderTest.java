package org.dddjava.jig.infrastructure.mybatis;

import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatement;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatements;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.infrastructure.datasource.ComplexMapper;
import stub.infrastructure.datasource.SampleMapper;
import testing.JigTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@JigTest
class MyBatisStatementReaderTest {

    @Test
    void bindを使ってても解析できる(JigRepository jigRepository) {
        SqlStatements myBatisStatements = jigRepository.jigDataProvider().fetchSqlStatements();

        SqlStatement myBatisStatement = myBatisStatements.findById(SqlStatementId.from(SampleMapper.class.getCanonicalName() + ".binding")).orElseThrow();
        assertEquals("[fuga]", myBatisStatement.tables().asText());
    }

    @Test
    void OGNLを使ったSELECTが解析できない(JigRepository jigRepository) {
        SqlStatements myBatisStatements = jigRepository.jigDataProvider().fetchSqlStatements();

        SqlStatement myBatisStatement = myBatisStatements.findById(SqlStatementId.from(ComplexMapper.class.getCanonicalName() + ".select_ognl")).orElseThrow();
        assertEquals("[（解析失敗）]", myBatisStatement.tables().asText());
        // OGNLを使ったSQLは現時点では空になりunsupportedになる
        assertFalse(myBatisStatement.query().supported());
    }

    @Test
    void OGNLを使ったSELECTが解析できない2(JigRepository jigRepository) {
        SqlStatements myBatisStatements = jigRepository.jigDataProvider().fetchSqlStatements();

        SqlStatement myBatisStatement = myBatisStatements.findById(SqlStatementId.from(ComplexMapper.class.getCanonicalName() + ".select_ognl_where")).orElseThrow();

        assertEquals("[（解析失敗）]", myBatisStatement.tables().asText());
        // OGNLを使ったSQLは現時点では空になる
        // ・・・のだが、 <where>タグなどで分割されているとOGNLを使用していない部分だけクエリが出てくる
        assertEquals("order by 1", myBatisStatement.query().rawText());
    }

    @ParameterizedTest
    @MethodSource
    void 標準的なパターン(String methodName, String tableName, SqlType sqlType, JigRepository jigRepository) {
        SqlStatements myBatisStatements = jigRepository.jigDataProvider().fetchSqlStatements();

        SqlStatement myBatisStatement = myBatisStatements.findById(SqlStatementId.from("stub.infrastructure.datasource.CanonicalMapper." + methodName)).orElseThrow();
        assertEquals("[" + tableName + "]", myBatisStatement.tables().asText());
        assertEquals(sqlType, myBatisStatement.sqlType());
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