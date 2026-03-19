package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.external.ExternalAccessorRepositories;
import org.dddjava.jig.domain.model.data.external.OtherExternalAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import stub.infrastructure.datasource.springdata.SpringDataJdbcCrudDelegatingOutputAdapter;
import stub.infrastructure.datasource.springdata.SpringDataJdbcNameOutputAdapter;
import stub.infrastructure.datasource.springdata.SpringDataJdbcNameRepository;
import stub.infrastructure.datasource.trace.TraceHelper;
import stub.infrastructure.datasource.trace.TraceMapper;
import stub.infrastructure.datasource.trace.TraceOutputAdapter;
import testing.JigTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigTest
class OutputAdapterExecutionTest {

    private static OutputAdapter findOutputAdapter(OutputAdapters outputAdapters, Class<?> type) {
        return outputAdapters.stream()
                .filter(oa -> oa.jigType().id().equals(TypeId.valueOf(type.getCanonicalName())))
                .findFirst()
                .orElseThrow();
    }

    private static OutputAdapterExecution findExecution(OutputAdapter outputAdapter, String methodName) {
        return outputAdapter.executions().stream()
                .filter(e -> e.jigMethod().name().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void 自身起点で辿れるJigTypes内メソッドと永続化操作を解決できる(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.jigTypes(jigRepository);
        var accessorRepositories = new ExternalAccessorRepositories(jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), OtherExternalAccessorRepository.empty());
        var outputAdapters = OutputAdapters.from(jigTypes, accessorRepositories);

        var traceOutputAdapter = findOutputAdapter(outputAdapters, TraceOutputAdapter.class);
        var execution = findExecution(traceOutputAdapter, "save");

        assertEquals("TraceOutputPort.save(String)",
                execution.implementOperations().iterator().next().jigMethodId().simpleText(),
                "実装しているPortOperationを保持できている");

        assertTrue(execution.tracingJigMethods().stream()
                .anyMatch(method -> method.declaringType().equals(TypeId.valueOf(TraceHelper.class.getCanonicalName()))
                        && method.name().equals("save")));

        var persistenceAccessorIdList = execution.persistenceAccessorOperations().stream()
                .map(persistenceAccessor -> persistenceAccessor.persistenceAccessorOperationId())
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
        var outputAdapters = OutputAdapters.from(jigTypes, accessorRepositories);

        var targetOutputAdapter = findOutputAdapter(outputAdapters, SpringDataJdbcNameOutputAdapter.class);
        var execution = findExecution(targetOutputAdapter, "save");

        PersistenceAccessorOperation persistenceAccessorOperation = execution.persistenceAccessorOperations().stream()
                .filter(found -> found.persistenceAccessorOperationId().equals(
                        PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(SpringDataJdbcNameRepository.class.getCanonicalName()), "save")))
                .findAny()
                .orElseThrow();

        assertEquals(PersistenceOperationType.INSERT, persistenceAccessorOperation.persistenceOperationType());
        assertEquals("[spring_data_table_name]", persistenceAccessorOperation.persistenceOperations().asText());
    }

    @Test
    void CrudRepository型経由の呼び出しでもSpringDataJdbcのPersistenceAccessorを解決できる(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.jigTypes(jigRepository);
        var accessorRepositories = new ExternalAccessorRepositories(jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), OtherExternalAccessorRepository.empty());
        var outputAdapters = OutputAdapters.from(jigTypes, accessorRepositories);

        var targetOutputAdapter = findOutputAdapter(outputAdapters, SpringDataJdbcCrudDelegatingOutputAdapter.class);
        var execution = findExecution(targetOutputAdapter, "save");

        PersistenceAccessorOperation persistenceAccessorOperation = execution.persistenceAccessorOperations().stream()
                .filter(found -> found.persistenceAccessorOperationId().equals(
                        PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(SpringDataJdbcNameRepository.class.getCanonicalName()), "save")))
                .findAny()
                .orElseThrow();

        assertEquals(PersistenceOperationType.INSERT, persistenceAccessorOperation.persistenceOperationType());
        assertEquals("[spring_data_table_name]", persistenceAccessorOperation.persistenceOperations().asText());
    }
}
