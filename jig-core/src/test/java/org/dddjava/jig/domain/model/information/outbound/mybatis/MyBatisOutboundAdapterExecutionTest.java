package org.dddjava.jig.domain.model.information.outbound.mybatis;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.outbound.ExternalAccessorRepositories;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapter;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapterExecution;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.mybatis.ut.TraceHelper;
import org.dddjava.jig.domain.model.information.outbound.mybatis.ut.TraceMapper;
import org.dddjava.jig.domain.model.information.outbound.mybatis.ut.TraceOutboundAdapter;
import org.dddjava.jig.domain.model.information.outbound.mybatis.ut.TraceOutboundPort;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;
import org.dddjava.jig.infrastructure.mybatis.MyBatisStatementsReader;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OutboundAdapter が Mapper を直接ではなく中間クラス経由で呼び出す場合でも、
 * その呼び出し連鎖を辿って永続化操作を解決できることの契約。
 */
class MyBatisOutboundAdapterExecutionTest {

    private static final String FIXTURE_PACKAGE = "org/dddjava/jig/domain/model/information/outbound/mybatis/ut";

    @Test
    void 中間クラスを経由した呼び出しでも永続化操作を解決できる() {
        var jigTypes = TestSupport.buildJigTypes(
                TraceMapper.class, TraceHelper.class, TraceOutboundPort.class, TraceOutboundAdapter.class);

        var headers = jigTypes.list().stream().map(t -> t.jigTypeHeader()).toList();
        var classPaths = TestSupport.sourceLocationsFor(FIXTURE_PACKAGE).classSourceBasePaths();
        var persistenceAccessorRepository = new MyBatisStatementsReader().readFrom(headers, classPaths).persistenceAccessorRepository();

        var accessorRepositories = new ExternalAccessorRepositories(persistenceAccessorRepository, OtherExternalAccessorRepository.empty());
        var outboundAdapters = OutboundAdapters.from(jigTypes, accessorRepositories);

        OutboundAdapter adapter = outboundAdapters.stream()
                .filter(oa -> oa.jigType().id().equals(TypeId.valueOf(TraceOutboundAdapter.class.getCanonicalName())))
                .findFirst()
                .orElseThrow();
        OutboundAdapterExecution execution = adapter.executions().stream()
                .filter(e -> e.jigMethod().name().equals("save"))
                .findFirst()
                .orElseThrow();

        assertEquals("TraceOutboundPort.save(String)",
                execution.implementOperations().iterator().next().jigMethodId().simpleText(),
                "実装しているPortOperationを保持できている");

        assertTrue(execution.tracingJigMethods().stream()
                .anyMatch(method -> method.declaringType().equals(TypeId.valueOf(TraceHelper.class.getCanonicalName()))
                        && method.name().equals("save")),
                "中間クラスTraceHelperのsaveを辿れている");

        var persistenceAccessorIdList = execution.persistenceAccessorOperations().stream()
                .map(op -> op.id())
                .toList();
        assertEquals(1, persistenceAccessorIdList.size());
        assertEquals(
                PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(TraceMapper.class.getCanonicalName()), "binding"),
                persistenceAccessorIdList.getFirst());
    }
}
