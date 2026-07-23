package org.dddjava.jig.infrastructure.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.infrastructure.mybatis.ut.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import testing.PersistenceTestSupport;
import testing.TestSupport;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MyBatis の Mapper を JIG が実行時クラスパス経由で読み取れることの契約。
 *
 * {@link MyBatisStatementsReader} は MyBatis 内部APIでXMLマッパーを読むため、実際にコンパイル・
 * リソース展開されたクラスパスを必要とする。fixture は jig-core 自身の test ソースセットに
 * 同居させ、jig-test-fixtures には出さない（他モジュールから参照する必要がなく、
 * 実行時クラスパスに別モジュールの成果物を混在させたくないため）。
 */
class MyBatisStatementsReaderTest {

    private static PersistenceAccessorRepository readFixture(Class<?>... mapperClasses) {
        var headers = TestSupport.buildJigTypes(mapperClasses).list().stream()
                .map(t -> t.jigTypeHeader())
                .toList();
        var classPaths = TestSupport.sourceLocationsFor("org/dddjava/jig/infrastructure/mybatis/ut").classSourceBasePaths();
        return new MyBatisStatementsReader().readFrom(headers, classPaths).persistenceAccessorRepository();
    }

    private static PersistenceAccessorOperationId idOf(Class<?> mapperClass, String methodName) {
        return PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(mapperClass.getCanonicalName()), methodName);
    }

    @ParameterizedTest
    @MethodSource
    void 標準的なCRUDパターン(String methodName, PersistenceOperationType expectedType) {
        var repository = readFixture(CrudMapper.class);
        var operation = PersistenceTestSupport.persistenceAccessorOf(repository, idOf(CrudMapper.class, methodName));

        assertEquals(List.of("crud_test"), PersistenceTestSupport.tableNames(operation.targetOperationTypes()));
        assertEquals(expectedType, operation.statementOperationType());
    }

    static Stream<Arguments> 標準的なCRUDパターン() {
        return Stream.of(
                Arguments.of("insert", PersistenceOperationType.INSERT),
                Arguments.of("select", PersistenceOperationType.SELECT),
                Arguments.of("update", PersistenceOperationType.UPDATE),
                Arguments.of("delete", PersistenceOperationType.DELETE),
                Arguments.of("annotationInsert", PersistenceOperationType.INSERT),
                Arguments.of("annotationSelect", PersistenceOperationType.SELECT),
                Arguments.of("annotationUpdate", PersistenceOperationType.UPDATE),
                Arguments.of("annotationDelete", PersistenceOperationType.DELETE)
        );
    }

    @ParameterizedTest
    @MethodSource
    void 書式がばらついたSQLからもテーブル名を抽出できる(String methodName, String tableName, PersistenceOperationType expectedType) {
        var repository = readFixture(TableNameMapper.class);
        var operation = PersistenceTestSupport.persistenceAccessorOf(repository, idOf(TableNameMapper.class, methodName));

        assertEquals(List.of(tableName.split(", ")), PersistenceTestSupport.tableNames(operation.targetOperationTypes()));
        assertEquals(expectedType, operation.statementOperationType());
    }

    static Stream<Arguments> 書式がばらついたSQLからもテーブル名を抽出できる() {
        return Stream.of(
                Arguments.of("tabInsert", "tab_test", PersistenceOperationType.INSERT),
                Arguments.of("tabSelect", "tab_test", PersistenceOperationType.SELECT),
                Arguments.of("tabUpdate", "tab_test", PersistenceOperationType.UPDATE),
                Arguments.of("tabDelete", "tab_test", PersistenceOperationType.DELETE),
                Arguments.of("japanese", "あのスキーマ.このテーブル", PersistenceOperationType.SELECT),
                Arguments.of("sequencePostgresql", "nextval('seq_test')", PersistenceOperationType.SELECT),
                Arguments.of("joinSelect", "table_a, table_b", PersistenceOperationType.SELECT),
                Arguments.of("leftJoinSelect", "table_a, table_b, table_c", PersistenceOperationType.SELECT)
        );
    }

    @Test
    void OGNLを使ったSELECTは解析できない() {
        var repository = readFixture(UnparseableMapper.class);
        var operation = PersistenceTestSupport.persistenceAccessorOf(repository, idOf(UnparseableMapper.class, "selectOgnl"));

        assertEquals(List.of("（解析失敗）"), PersistenceTestSupport.tableNames(operation.targetOperationTypes()));
        assertTrue(operation.query().isEmpty());
    }

    @Test
    void OGNLを使ったSELECTがwhereタグで分割されているとその部分だけクエリが残る() {
        var repository = readFixture(UnparseableMapper.class);
        var operation = PersistenceTestSupport.persistenceAccessorOf(repository, idOf(UnparseableMapper.class, "selectOgnlWhere"));

        assertEquals(List.of("（解析失敗）"), PersistenceTestSupport.tableNames(operation.targetOperationTypes()));
        assertEquals("order by 1", operation.query().map(q -> q.rawText()).orElse(""));
    }

    @Test
    void bindとincludeを使った動的SQLでも解析できる() {
        var repository = readFixture(DynamicSqlMapper.class);
        var operation = PersistenceTestSupport.persistenceAccessorOf(repository, idOf(DynamicSqlMapper.class, "binding"));

        assertEquals(List.of("fuga"), PersistenceTestSupport.tableNames(operation.targetOperationTypes()));
    }
}
