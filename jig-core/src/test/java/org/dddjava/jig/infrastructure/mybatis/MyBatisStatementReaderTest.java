package org.dddjava.jig.infrastructure.mybatis;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.infrastructure.datasource.CanonicalMapper;
import stub.infrastructure.datasource.ComplexMapper;
import stub.infrastructure.datasource.SampleMapper;
import stub.infrastructure.datasource.trace.TraceOutboundPort;
import testing.JigTest;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@JigTest
class MyBatisStatementReaderTest {

    @Test
    void bindを使ってても解析できる(JigRepository jigRepository) {
        PersistenceAccessorRepository myBatisStatements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();

        PersistenceAccessorOperation myBatisStatement = persistenceAccessorOf(myBatisStatements, persistenceAccessorIdOf(SampleMapper.class, "binding"));
        assertEquals("[fuga]", myBatisStatement.targetOperationTypes().asText());
    }

    @Test
    void DatasourceAnglesで他クラス経由のMyBatis呼び出しを解決できる(JigService jigService, JigRepository jigRepository) {
        var datasourceAngles = jigService.datasourceAngles(jigRepository).list().stream()
                .filter(angle -> angle.declaringType().fqn().equals(TraceOutboundPort.class.getCanonicalName()))
                .toList();
        var angle = datasourceAngles.stream()
                .filter(found -> found.interfaceMethod().name().equals("save"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, datasourceAngles.size());
        assertEquals("[trace_table]", angle.selectTables());
    }

    @Test
    void OGNLを使ったSELECTが解析できない(JigRepository jigRepository) {
        PersistenceAccessorRepository myBatisStatements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();

        PersistenceAccessorOperation myBatisStatement = persistenceAccessorOf(myBatisStatements, persistenceAccessorIdOf(ComplexMapper.class, "select_ognl"));
        assertEquals("[（解析失敗）]", myBatisStatement.targetOperationTypes().asText());
        // OGNLを使ったSQLは現時点では空になりunsupportedになる
        assertFalse(myBatisStatement.query().supported());
    }

    private static PersistenceAccessorOperationId persistenceAccessorIdOf(Class<?> clz, String name) {
        return PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(clz.getCanonicalName()), name);
    }

    @Test
    void OGNLを使ったSELECTが解析できない2(JigRepository jigRepository) {
        PersistenceAccessorRepository myBatisStatements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();

        PersistenceAccessorOperation myBatisStatement = persistenceAccessorOf(myBatisStatements, persistenceAccessorIdOf(ComplexMapper.class, "select_ognl_where"));

        assertEquals("[（解析失敗）]", myBatisStatement.targetOperationTypes().asText());
        // OGNLを使ったSQLは現時点では空になる
        // ・・・のだが、 <where>タグなどで分割されているとOGNLを使用していない部分だけクエリが出てくる
        assertEquals("order by 1", myBatisStatement.query().rawText());
    }

    @ParameterizedTest
    @MethodSource
    void 標準的なパターン(String methodName, String tableName, PersistenceOperationType persistenceOperationType, JigRepository jigRepository) {
        PersistenceAccessorRepository myBatisStatements = jigRepository.externalAccessorRepositories().persistenceAccessorRepository();

        PersistenceAccessorOperation persistenceAccessorOperation = persistenceAccessorOf(myBatisStatements, persistenceAccessorIdOf(CanonicalMapper.class, methodName));
        assertEquals("[" + tableName + "]", persistenceAccessorOperation.targetOperationTypes().asText());
        assertEquals(persistenceOperationType, persistenceAccessorOperation.statementOperationType());
    }

    private static PersistenceAccessorOperation persistenceAccessorOf(PersistenceAccessorRepository repository,
                                                                      PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return repository.findByTypeId(persistenceAccessorOperationId.typeId(), Set.of())
                .stream()
                .flatMap(ops -> ops.persistenceAccessorOperations().stream())
                .filter(operation -> operation.id().equals(persistenceAccessorOperationId))
                .findFirst()
                .orElseThrow();
    }

    static Stream<Arguments> 標準的なパターン() {
        return Stream.of(
                Arguments.of("insert", "crud_test", PersistenceOperationType.INSERT),
                Arguments.of("select", "crud_test", PersistenceOperationType.SELECT),
                Arguments.of("update", "crud_test", PersistenceOperationType.UPDATE),
                Arguments.of("delete", "crud_test", PersistenceOperationType.DELETE),
                Arguments.of("annotationInsert", "crud_test", PersistenceOperationType.INSERT),
                Arguments.of("annotationSelect", "crud_test", PersistenceOperationType.SELECT),
                Arguments.of("annotationUpdate", "crud_test", PersistenceOperationType.UPDATE),
                Arguments.of("annotationDelete", "crud_test", PersistenceOperationType.DELETE),
                Arguments.of("tabInsert", "tab_test", PersistenceOperationType.INSERT),
                Arguments.of("tabSelect", "tab_test", PersistenceOperationType.SELECT),
                Arguments.of("tabUpdate", "tab_test", PersistenceOperationType.UPDATE),
                Arguments.of("tabDelete", "tab_test", PersistenceOperationType.DELETE),
                Arguments.of("japanese", "あのスキーマ.このテーブル", PersistenceOperationType.SELECT),
                Arguments.of("illegal", "（解析失敗）", PersistenceOperationType.INSERT),
                Arguments.of("sequence_postgresql", "nextval('seq_test')", PersistenceOperationType.SELECT),
                Arguments.of("joinSelect", "table_a, table_b", PersistenceOperationType.SELECT),
                Arguments.of("leftJoinSelect", "table_a, table_b, table_c", PersistenceOperationType.SELECT)
        );
    }
}
