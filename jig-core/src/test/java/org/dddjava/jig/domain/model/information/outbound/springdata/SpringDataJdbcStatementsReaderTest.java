package org.dddjava.jig.domain.model.information.outbound.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessor;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.outbound.springdata.ut.*;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Data JDBC の Repository を Java 本体機構（production の {@code DefaultJigRepositoryFactory} と同じ
 * {@link SpringDataJdbcStatementsReader#readFrom(org.dddjava.jig.domain.model.information.types.JigTypes)}）
 * で読み取れることの契約。JigRepository/JigService は経由せず、読み取り機を直接検証する。
 */
class SpringDataJdbcStatementsReaderTest {

    private static Collection<PersistenceAccessor> readFixture() {
        var jigTypes = TestSupport.buildJigTypes(
                Order.class, OrderItem.class, OrderWithItems.class,
                OrderRepository.class, NameRepository.class, MixedRepository.class,
                OrderWithItemsRepository.class, CustomBaseRepository.class
                // common.MyCrudRepository は意図的に含めない（CustomBaseRepositoryが未解析の基底型に依存するケース）
        );
        return new SpringDataJdbcStatementsReader().readFrom(jigTypes);
    }

    private static PersistenceAccessor accessorOf(Collection<PersistenceAccessor> accessors, Class<?> repositoryClass) {
        var typeId = TypeId.valueOf(repositoryClass.getCanonicalName());
        return accessors.stream()
                .filter(a -> a.typeId().equals(typeId))
                .findFirst()
                .orElseThrow(() -> new AssertionError(repositoryClass + " のPersistenceAccessorが見つかりません: " + accessors));
    }

    private static List<String> tableNamesOf(PersistenceAccessor accessor) {
        return accessor.defaultPersistenceTargets().stream()
                .map(t -> t.name())
                .sorted()
                .toList();
    }

    @Test
    void オーバーライドしたCRUDメソッドを解決できる() {
        var accessor = accessorOf(readFixture(), OrderRepository.class);
        assertEquals(List.of("orders"), tableNamesOf(accessor));

        assertEquals(PersistenceOperationType.INSERT, operationTypeOf(accessor, "save"));
        assertEquals(PersistenceOperationType.SELECT, operationTypeOf(accessor, "findById"));
        assertEquals(PersistenceOperationType.DELETE, operationTypeOf(accessor, "deleteById"));
    }

    @Test
    void Queryアノテーションで種別を判定できる() {
        var accessor = accessorOf(readFixture(), OrderRepository.class);

        assertEquals(PersistenceOperationType.UPDATE, operationTypeOf(accessor, "updateById"));
    }

    @Test
    void Queryアノテーションの前後の空白やコメントが混じっていても種別を判定できる() {
        var accessor = accessorOf(readFixture(), OrderRepository.class);

        assertEquals(PersistenceOperationType.UPDATE, operationTypeOf(accessor, "updateByIdWithComment"));
    }

    @Test
    void CRUDメソッドをオーバーライドしなくても継承元から解決できる() {
        var accessor = accessorOf(readFixture(), NameRepository.class);
        assertEquals(List.of("orders"), tableNamesOf(accessor));

        // 継承のみ・オーバーライドなしのメソッドはSpringDataBaseMethod経由で解決される
        assertEquals(PersistenceOperationType.INSERT, operationTypeOf(accessor, "save"));
        assertEquals(PersistenceOperationType.SELECT, operationTypeOf(accessor, "findById"));
        assertEquals(PersistenceOperationType.DELETE, operationTypeOf(accessor, "deleteById"));
    }

    @Test
    void Queryアノテーションが無いカスタムメソッドはメソッド名から種別を推測する() {
        var accessor = accessorOf(readFixture(), NameRepository.class);

        assertEquals(PersistenceOperationType.SELECT, operationTypeOf(accessor, "findByName"));
    }

    @Test
    void SpringData以外のインタフェースを混ぜて継承していても認識できる() {
        var accessor = accessorOf(readFixture(), MixedRepository.class);

        assertEquals(List.of("orders"), tableNamesOf(accessor));
        assertEquals(PersistenceOperationType.INSERT, operationTypeOf(accessor, "save"));
    }

    @Test
    void MappedCollectionを辿って複数テーブルを解決できる() {
        var accessor = accessorOf(readFixture(), OrderWithItemsRepository.class);

        assertEquals(List.of("order_items", "orders_with_items"), tableNamesOf(accessor));
    }

    @Test
    void 解析対象外の共通基底Repositoryを経由してもSpringDataRepositoryと推測できる() {
        var accessor = accessorOf(readFixture(), CustomBaseRepository.class);

        assertEquals(List.of("orders"), tableNamesOf(accessor));
        assertEquals(PersistenceOperationType.INSERT, operationTypeOf(accessor, "save"));
    }

    private static PersistenceOperationType operationTypeOf(PersistenceAccessor accessor, String methodName) {
        TypeId typeId = accessor.typeId();
        Optional<PersistenceOperationType> found = accessor
                .findPersistenceAccessorById(PersistenceAccessorOperationId.fromTypeIdAndName(typeId, methodName))
                .map(op -> op.statementOperationType());
        return found.orElseThrow(() -> new AssertionError(methodName + " のPersistenceAccessorOperationが見つかりません: " + accessor));
    }
}
