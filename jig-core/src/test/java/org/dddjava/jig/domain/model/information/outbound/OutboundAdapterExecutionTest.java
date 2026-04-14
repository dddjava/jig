package org.dddjava.jig.domain.model.information.outbound;

import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;
import org.junit.jupiter.api.Test;
import stub.infrastructure.datasource.springdata.SpringDataJdbcCrudDelegatingOutbohndAdapter;
import stub.infrastructure.datasource.springdata.SpringDataJdbcNameOutboundAdapter;
import stub.infrastructure.datasource.springdata.SpringDataJdbcNameRepository;
import stub.infrastructure.datasource.trace.TraceHelper;
import stub.infrastructure.datasource.trace.TraceMapper;
import stub.infrastructure.datasource.trace.TraceOutboundAdapter;
import testing.JigTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigTest
class OutboundAdapterExecutionTest {

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
    void 自身起点で辿れるJigTypes内メソッドと永続化操作を解決できる(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.jigTypes(jigRepository);
        var accessorRepositories = new ExternalAccessorRepositories(jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), OtherExternalAccessorRepository.empty());
        var outboundAdapters = OutboundAdapters.from(jigTypes, accessorRepositories);

        var traceOutboundAdapter = findOutboundAdapter(outboundAdapters, TraceOutboundAdapter.class);
        var execution = findExecution(traceOutboundAdapter, "save");

        assertEquals("TraceOutboundPort.save(String)",
                execution.implementOperations().iterator().next().jigMethodId().simpleText(),
                "実装しているPortOperationを保持できている");

        assertTrue(execution.tracingJigMethods().stream()
                .anyMatch(method -> method.declaringType().equals(TypeId.valueOf(TraceHelper.class.getCanonicalName()))
                        && method.name().equals("save")));

        var persistenceAccessorIdList = execution.persistenceAccessorOperations().stream()
                .map(persistenceAccessor -> persistenceAccessor.id())
                .toList();
        assertEquals(1, persistenceAccessorIdList.size());
        assertEquals(
                PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(TraceMapper.class.getCanonicalName()), "binding"),
                persistenceAccessorIdList.getFirst());
    }

    @Test
    void SpringDataJdbcの継承メソッド呼び出しでPersistenceAccessorを解決できる(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.jigTypes(jigRepository);
        var accessorRepositories = new ExternalAccessorRepositories(jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), OtherExternalAccessorRepository.empty());
        var outboundAdapters = OutboundAdapters.from(jigTypes, accessorRepositories);

        var targetOutboundAdapter = findOutboundAdapter(outboundAdapters, SpringDataJdbcNameOutboundAdapter.class);
        var execution = findExecution(targetOutboundAdapter, "save");

        PersistenceAccessorOperation persistenceAccessorOperation = execution.persistenceAccessorOperations().stream()
                .filter(found -> found.id().equals(
                        PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(SpringDataJdbcNameRepository.class.getCanonicalName()), "save")))
                .findAny()
                .orElseThrow();

        assertEquals(PersistenceOperationType.INSERT, persistenceAccessorOperation.statementOperationType());
        assertEquals("[spring_data_table_name]", persistenceAccessorOperation.targetOperationTypes().asText());
    }

    @Test
    void CrudRepository型経由の呼び出しでもSpringDataJdbcのPersistenceAccessorを解決できる(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.jigTypes(jigRepository);
        var accessorRepositories = new ExternalAccessorRepositories(jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), OtherExternalAccessorRepository.empty());
        var outboundAdapters = OutboundAdapters.from(jigTypes, accessorRepositories);

        var targetOutboundAdapter = findOutboundAdapter(outboundAdapters, SpringDataJdbcCrudDelegatingOutbohndAdapter.class);
        var execution = findExecution(targetOutboundAdapter, "save");

        PersistenceAccessorOperation persistenceAccessorOperation = execution.persistenceAccessorOperations().stream()
                .filter(found -> found.id().equals(
                        PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(SpringDataJdbcNameRepository.class.getCanonicalName()), "save")))
                .findAny()
                .orElseThrow();

        assertEquals(PersistenceOperationType.INSERT, persistenceAccessorOperation.statementOperationType());
        assertEquals("[spring_data_table_name]", persistenceAccessorOperation.targetOperationTypes().asText());
    }
}
