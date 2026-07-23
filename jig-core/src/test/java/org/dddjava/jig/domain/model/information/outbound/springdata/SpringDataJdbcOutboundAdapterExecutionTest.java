package org.dddjava.jig.domain.model.information.outbound.springdata;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.outbound.*;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;
import org.dddjava.jig.domain.model.information.outbound.springdata.ut.*;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OutboundAdapter からの呼び出しを辿って Spring Data JDBC の永続化操作を解決できることの契約。
 * 呼び出し先の静的型（フィールドの宣言型 / 一時変数への代入で変わる型）が実際の登録型と
 * 異なっていても解決できることを含める。
 */
class SpringDataJdbcOutboundAdapterExecutionTest {

    private static OutboundAdapters buildOutboundAdapters() {
        var jigTypes = TestSupport.buildJigTypes(
                Order.class, OrderRepository.class,
                OrderOutboundPort.class, OrderOutboundAdapter.class,
                OrderCrudDelegatingOutboundPort.class, OrderCrudDelegatingOutboundAdapter.class,
                // 複数のCrudRepository実装が共存していても、呼び出し元の使用型から一意に絞り込めることを確認するため加える
                NameRepository.class, MixedRepository.class
        );
        var persistenceAccessorRepository = PersistenceAccessorRepository.from(
                new SpringDataJdbcStatementsReader().readFrom(jigTypes));
        var accessorRepositories = new ExternalAccessorRepositories(
                persistenceAccessorRepository, OtherExternalAccessorRepository.empty());
        return OutboundAdapters.from(jigTypes, accessorRepositories);
    }

    private static OutboundAdapter findOutboundAdapter(OutboundAdapters outboundAdapters, Class<?> type) {
        return outboundAdapters.stream()
                .filter(oa -> oa.jigType().id().equals(TypeId.valueOf(type.getCanonicalName())))
                .findFirst()
                .orElseThrow();
    }

    private static OutboundAdapterExecution findExecution(OutboundAdapter outboundAdapter, String methodName) {
        return outboundAdapter.executions().stream()
                .filter(e -> e.jigMethod().name().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void フィールド経由の呼び出しでPersistenceAccessorを解決できる() {
        var outboundAdapters = buildOutboundAdapters();

        var targetOutboundAdapter = findOutboundAdapter(outboundAdapters, OrderOutboundAdapter.class);
        var execution = findExecution(targetOutboundAdapter, "save");

        PersistenceAccessorOperation operation = execution.persistenceAccessorOperations().stream()
                .filter(found -> found.id().equals(
                        PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(OrderRepository.class.getCanonicalName()), "save")))
                .findAny()
                .orElseThrow();

        assertEquals(PersistenceOperationType.INSERT, operation.statementOperationType());
    }

    @Test
    void CrudRepository型の変数へ代入してからの呼び出しでもPersistenceAccessorを解決できる() {
        var outboundAdapters = buildOutboundAdapters();

        var targetOutboundAdapter = findOutboundAdapter(outboundAdapters, OrderCrudDelegatingOutboundAdapter.class);
        var execution = findExecution(targetOutboundAdapter, "save");

        // 呼び出し先の静的型はCrudRepositoryだが、実際に登録されているのはOrderRepositoryとして解決される
        PersistenceAccessorOperation operation = execution.persistenceAccessorOperations().stream()
                .filter(found -> found.id().equals(
                        PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(OrderRepository.class.getCanonicalName()), "save")))
                .findAny()
                .orElseThrow();

        assertEquals(PersistenceOperationType.INSERT, operation.statementOperationType());
    }
}
