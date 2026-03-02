package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import stub.infrastructure.datasource.trace.TraceHelper;
import stub.infrastructure.datasource.trace.TraceMapper;
import stub.infrastructure.datasource.trace.TraceOutputAdapter;
import testing.JigTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JigTest
class OutputAdapterExecutionTest {

    @Test
    void 自身起点で辿れるJigTypes内メソッドと永続化操作を解決できる(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.jigTypes(jigRepository);
        var outputAdapters = OutputAdapters.from(jigTypes);
        var sqlStatements = jigRepository.jigDataProvider().fetchSqlStatements();

        var traceOutputAdapter = outputAdapters.stream()
                .filter(outputAdapter -> outputAdapter.jigType().id().equals(TypeId.valueOf(TraceOutputAdapter.class.getCanonicalName())))
                .findFirst()
                .orElseThrow();
        var execution = traceOutputAdapter.outputAdapterExecutions().stream()
                .filter(outputAdapterExecution -> outputAdapterExecution.jigMethod().name().equals("save"))
                .findFirst()
                .orElseThrow();

        assertTrue(execution.tracingJigMethods().stream()
                .anyMatch(method -> method.declaringType().equals(TypeId.valueOf(TraceHelper.class.getCanonicalName()))
                        && method.name().equals("save")));

        var resolvedPersistenceOperationIds = execution.resolvePersistenceOperations(sqlStatements).stream()
                .map(persistenceOperation -> persistenceOperation.persistenceOperationId())
                .toList();
        assertEquals(1, resolvedPersistenceOperationIds.size());
        assertEquals(
                PersistenceOperationId.fromTypeIdAndName(TypeId.valueOf(TraceMapper.class.getCanonicalName()), "binding"),
                resolvedPersistenceOperationIds.getFirst());
    }
}
