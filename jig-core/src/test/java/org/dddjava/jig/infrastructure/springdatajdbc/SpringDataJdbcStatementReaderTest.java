package org.dddjava.jig.infrastructure.springdatajdbc;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.infrastructure.datasource.springdata.SpringDataJdbcMixedOrderRepository;
import stub.infrastructure.datasource.springdata.SpringDataJdbcNameRepository;
import stub.infrastructure.datasource.springdata.SpringDataJdbcOrderRepository;
import stub.infrastructure.datasource.springdata.SpringDataJdbcOrderWithItemsRepository;
import testing.JigTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigTest
class SpringDataJdbcStatementReaderTest {

    @ParameterizedTest
    @MethodSource("repositoryMethodAndSqlType")
    void SpringDataJdbcのRepositoryメソッドをSQLとして取得できる(
            String methodName,
            SqlType expectedSqlType,
            JigRepository jigRepository
    ) {
        var statements = jigRepository.jigDataProvider().fetchSqlStatements();
        var namespace = SpringDataJdbcOrderRepository.class.getCanonicalName();
        var statement = statements.findById(SqlStatementId.from(namespace + "." + methodName)).orElseThrow();

        assertEquals("[spring_data_jdbc_orders]", statement.tables().asText());
        assertEquals(expectedSqlType, statement.sqlType());
    }

    @Test
    void SpringDataJdbcのRepositoryメソッドをSQLとして取得できる_Tableのnameから(JigRepository jigRepository) {
        var statements = jigRepository.jigDataProvider().fetchSqlStatements();
        var namespace = SpringDataJdbcNameRepository.class.getCanonicalName();
        var statement = statements.findById(SqlStatementId.from(namespace + "." + "findByHoge")).orElseThrow();

        assertEquals("[spring_data_table_name]", statement.tables().asText());
        assertEquals(SqlType.SELECT, statement.sqlType());
    }


    @ParameterizedTest
    @MethodSource("repositoryMethodAndSqlType")
    void DatasourceAnglesにSpringDataJdbcのCRUDが反映される(
            String methodName,
            SqlType expectedSqlType,
            JigService jigService,
            JigRepository jigRepository
    ) {
        var datasourceAngles = jigService.datasourceAngles(jigRepository).list().stream()
                .filter(angle -> angle.declaringType().fqn().equals(SpringDataJdbcOrderRepository.class.getCanonicalName()))
                .toList();
        var angle = datasourceAngles.stream()
                .filter(found -> found.interfaceMethod().name().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertEquals(5, datasourceAngles.size());
        var expectedTables = "[spring_data_jdbc_orders]";
        switch (expectedSqlType) {
            case INSERT -> assertEquals(expectedTables, angle.insertTables());
            case SELECT -> assertEquals(expectedTables, angle.selectTables());
            case UPDATE -> assertEquals(expectedTables, angle.updateTables());
            case DELETE -> assertEquals(expectedTables, angle.deleteTables());
            default -> throw new IllegalStateException("未対応のSQL種別: " + expectedSqlType);
        }
    }

    @ParameterizedTest
    @MethodSource("crudRepositoryMethodAndSqlType")
    void 型引数なしのSpringDataRepository継承があっても後続の継承からエンティティ型を解決できる(
            String methodName,
            SqlType expectedSqlType,
            JigRepository jigRepository
    ) {
        var statements = jigRepository.jigDataProvider().fetchSqlStatements();
        var namespace = SpringDataJdbcMixedOrderRepository.class.getCanonicalName();
        var statement = statements.findById(SqlStatementId.from(namespace + "." + methodName));

        assertTrue(statement.isPresent());
        assertEquals("[spring_data_jdbc_orders]", statement.get().tables().asText());
        assertEquals(expectedSqlType, statement.get().sqlType());
    }

    @ParameterizedTest
    @MethodSource("crudRepositoryMethodAndSqlType")
    void MappedCollectionを辿って複数テーブルを解決できる(
            String methodName,
            SqlType expectedSqlType,
            JigRepository jigRepository
    ) {
        var statements = jigRepository.jigDataProvider().fetchSqlStatements();
        var namespace = SpringDataJdbcOrderWithItemsRepository.class.getCanonicalName();
        var statement = statements.findById(SqlStatementId.from(namespace + "." + methodName));

        assertTrue(statement.isPresent());
        assertEquals("[spring_data_jdbc_order_items, spring_data_jdbc_orders_with_items]", statement.get().tables().asText());
        assertEquals(expectedSqlType, statement.get().sqlType());
    }

    static Stream<Arguments> repositoryMethodAndSqlType() {
        return Stream.of(
                Arguments.of("save", SqlType.INSERT),
                Arguments.of("findById", SqlType.SELECT),
                Arguments.of("deleteById", SqlType.DELETE),
                Arguments.of("updateById", SqlType.UPDATE),
                Arguments.of("updateByIdWithComment", SqlType.UPDATE)
        );
    }

    static Stream<Arguments> crudRepositoryMethodAndSqlType() {
        return Stream.of(
                Arguments.of("save", SqlType.INSERT),
                Arguments.of("findById", SqlType.SELECT),
                Arguments.of("deleteById", SqlType.DELETE)
        );
    }
}
