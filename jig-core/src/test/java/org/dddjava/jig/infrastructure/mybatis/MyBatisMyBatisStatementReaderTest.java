package org.dddjava.jig.infrastructure.mybatis;

import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatement;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.infrastructure.datasource.SampleMapper;
import testing.JigServiceTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JigServiceTest
class MyBatisMyBatisStatementReaderTest {

    @Test
    void bindを使ってても解析できる(JigRepository jigRepository) {
        MyBatisStatements myBatisStatements = jigRepository.jigDataProvider().fetchMybatisStatements();

        MyBatisStatement myBatisStatement = myBatisStatements.list().stream()
                .filter(statement -> statement.identifier().equals(new MyBatisStatementId(SampleMapper.class.getCanonicalName() + ".binding")))
                .findAny().orElseThrow();
        assertEquals("[fuga]", myBatisStatement.tables().asText());
    }

    @ParameterizedTest
    @MethodSource
    void 標準的なパターン(String methodName, String tableName, SqlType sqlType, JigRepository jigRepository) {
        MyBatisStatements myBatisStatements = jigRepository.jigDataProvider().fetchMybatisStatements();

        MyBatisStatement myBatisStatement = myBatisStatements.list().stream()
                .filter(current -> current.identifier().equals(new MyBatisStatementId("stub.infrastructure.datasource.CanonicalMapper." + methodName)))
                .findFirst()
                .orElseThrow(AssertionError::new);

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