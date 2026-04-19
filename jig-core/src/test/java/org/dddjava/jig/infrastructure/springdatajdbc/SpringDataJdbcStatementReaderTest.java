package org.dddjava.jig.infrastructure.springdatajdbc;

import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.persistence.PersistenceTargetOperationTypes;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.infrastructure.datasource.springdata.*;
import testing.JigTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigTest
class SpringDataJdbcStatementReaderTest {

    @ParameterizedTest
    @MethodSource("repositoryMethodAndSqlType")
    void SpringDataJdbcのRepositoryメソッドをSQLとして取得できる(
            String methodName,
            PersistenceOperationType expectedPersistenceOperationType,
            JigRepository jigRepository
    ) {
        var statements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();
        var statement = persistenceAccessorOf(statements, getPersistenceAccessorId(methodName, SpringDataJdbcOrderAccessor.class));

        assertEquals(List.of("spring_data_jdbc_orders"), tableNames(statement.targetOperationTypes()));
        assertEquals(expectedPersistenceOperationType, statement.statementOperationType());
    }

    @Test
    void SpringDataJdbcのRepositoryメソッドをSQLとして取得できる_Tableのnameから(JigRepository jigRepository) {
        var statements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();
        var statement = persistenceAccessorOf(statements, getPersistenceAccessorId("findByHoge", SpringDataJdbcNameRepository.class));

        assertEquals(List.of("spring_data_table_name"), tableNames(statement.targetOperationTypes()));
        assertEquals(PersistenceOperationType.SELECT, statement.statementOperationType());
    }


    @ParameterizedTest
    @MethodSource("repositoryMethodAndSqlType")
    void DatasourceAnglesにSpringDataJdbcのCRUDが反映される(
            String methodName,
            PersistenceOperationType expectedPersistenceOperationType,
            JigService jigService,
            JigRepository jigRepository
    ) {
        var datasourceAngles = jigService.datasourceAngles(jigRepository).list().stream()
                .filter(angle -> angle.declaringType().fqn().equals(SpringDataJdbcOrderPort.class.getCanonicalName()))
                .toList();
        var angle = datasourceAngles.stream()
                .filter(found -> found.interfaceMethod().name().equals(methodName))
                .findFirst()
                .orElseThrow();

        assertEquals(5, datasourceAngles.size());
        var expectedTables = "[spring_data_jdbc_orders]";
        switch (expectedPersistenceOperationType) {
            case INSERT -> assertEquals(expectedTables, angle.insertTables());
            case SELECT -> assertEquals(expectedTables, angle.selectTables());
            case UPDATE -> assertEquals(expectedTables, angle.updateTables());
            case DELETE -> assertEquals(expectedTables, angle.deleteTables());
            default -> throw new IllegalStateException("未対応のSQL種別: " + expectedPersistenceOperationType);
        }
    }

    @ParameterizedTest
    @MethodSource("crudRepositoryMethodAndSqlType")
    void 型引数なしのSpringDataRepository継承があっても後続の継承からエンティティ型を解決できる(
            String methodName,
            PersistenceOperationType expectedPersistenceOperationType,
            JigRepository jigRepository
    ) {
        var statements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();
        var statement = persistenceAccessorOptionalOf(statements, getPersistenceAccessorId(methodName, SpringDataJdbcMixedOrderRepository.class));

        assertTrue(statement.isPresent());
        assertEquals(List.of("spring_data_jdbc_orders"), tableNames(statement.get().targetOperationTypes()));
        assertEquals(expectedPersistenceOperationType, statement.get().statementOperationType());
    }

    @ParameterizedTest
    @MethodSource("crudRepositoryMethodAndSqlType")
    void MappedCollectionを辿って複数テーブルを解決できる(
            String methodName,
            PersistenceOperationType expectedPersistenceOperationType,
            JigRepository jigRepository
    ) {
        var statements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();
        var statement = persistenceAccessorOptionalOf(statements, getPersistenceAccessorId(methodName, SpringDataJdbcOrderWithItemsRepository.class));

        assertTrue(statement.isPresent());
        assertEquals(List.of("spring_data_jdbc_order_items", "spring_data_jdbc_orders_with_items"), tableNames(statement.get().targetOperationTypes()));
        assertEquals(expectedPersistenceOperationType, statement.get().statementOperationType());
    }

    @Test
    void 解析対象外のカスタム基底RepositoryをSpringDataRepositoryと推測できる(JigRepository jigRepository) {
        var statements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();
        // MyCrudRepository（解析対象外）を経由した場合でも save が解決できること
        var statement = persistenceAccessorOptionalOf(statements,
                getPersistenceAccessorId("save", SpringDataJdbcCustomBaseRepository.class));

        assertTrue(statement.isPresent());
        assertEquals(List.of("spring_data_jdbc_orders"), tableNames(statement.get().targetOperationTypes()));
        assertEquals(PersistenceOperationType.INSERT, statement.get().statementOperationType());
    }

    static Stream<Arguments> repositoryMethodAndSqlType() {
        return Stream.of(
                Arguments.of("save", PersistenceOperationType.INSERT),
                Arguments.of("findById", PersistenceOperationType.SELECT),
                Arguments.of("deleteById", PersistenceOperationType.DELETE),
                Arguments.of("updateById", PersistenceOperationType.UPDATE),
                Arguments.of("updateByIdWithComment", PersistenceOperationType.UPDATE)
        );
    }

    static Stream<Arguments> crudRepositoryMethodAndSqlType() {
        return Stream.of(
                Arguments.of("save", PersistenceOperationType.INSERT),
                Arguments.of("findById", PersistenceOperationType.SELECT),
                Arguments.of("deleteById", PersistenceOperationType.DELETE)
        );
    }

    private static PersistenceAccessorOperation persistenceAccessorOf(PersistenceAccessorRepository repository,
                                                                      PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return persistenceAccessorOptionalOf(repository, persistenceAccessorOperationId).orElseThrow();
    }

    private static java.util.Optional<PersistenceAccessorOperation> persistenceAccessorOptionalOf(PersistenceAccessorRepository repository,
                                                                                                  PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return repository.findByTypeId(persistenceAccessorOperationId.typeId(), Set.of())
                .stream()
                .flatMap(ops -> ops.persistenceAccessorOperations().stream())
                .filter(operation -> operation.id().equals(persistenceAccessorOperationId))
                .findFirst();
    }

    private static List<String> tableNames(PersistenceTargetOperationTypes types) {
        return types.persistenceTargets().stream()
                .map(t -> t.persistenceTarget().name())
                .sorted()
                .toList();
    }

    private static PersistenceAccessorOperationId getPersistenceAccessorId(String methodName, Class<?> clazz) {
        var typeId = TypeId.valueOf(clazz.getCanonicalName());
        return PersistenceAccessorOperationId.fromTypeIdAndName(typeId, methodName);
    }
}
