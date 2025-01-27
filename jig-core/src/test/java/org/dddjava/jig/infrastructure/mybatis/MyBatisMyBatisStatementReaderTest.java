package org.dddjava.jig.infrastructure.mybatis;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatement;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatementId;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.sources.mybatis.SqlSources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.infrastructure.datasource.CanonicalMapper;
import stub.infrastructure.datasource.SampleMapper;
import testing.TestSupport;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyBatisMyBatisStatementReaderTest {

    @Test
    void bindを使ってても解析できる() {
        MyBatisSqlReader sut = new MyBatisSqlReader();

        MyBatisStatements myBatisStatements = sut.readFrom(new SqlSources(
                TestSupport.getTestResourceRootURLs(),
                List.of(TestSupport.getClassSource(SampleMapper.class))));

        MyBatisStatement myBatisStatement = myBatisStatements.list().get(0);
        assertEquals("[fuga]", myBatisStatement.tables().asText());
    }

    @ParameterizedTest
    @MethodSource
    void 標準的なパターン(String methodName, String tableName, SqlType sqlType) {
        MyBatisSqlReader sut = new MyBatisSqlReader();

        MyBatisStatements myBatisStatements = sut.readFrom(new SqlSources(
                TestSupport.getTestResourceRootURLs(),
                Collections.singletonList(TestSupport.getClassSource(CanonicalMapper.class))));

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