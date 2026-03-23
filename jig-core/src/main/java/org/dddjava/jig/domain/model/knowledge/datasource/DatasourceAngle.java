package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapterExecution;
import org.dddjava.jig.domain.model.information.outbound.OutboundPort;
import org.dddjava.jig.domain.model.information.outbound.OutboundPortOperation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    private final OutboundPortOperation portOperation;
    private final OutboundAdapterExecution adapterExecution;
    private final OutboundPort outboundPort;
    private final Map<PersistenceOperationType, List<String>> tablesMap;
    private final CallerMethods callerMethods;

    public DatasourceAngle(OutboundPortOperation portOperation, OutboundAdapterExecution adapterExecution, OutboundPort outboundPort, Map<PersistenceOperationType, List<String>> tablesMap, CallerMethods callerMethods) {
        this.portOperation = portOperation;
        this.adapterExecution = adapterExecution;
        this.outboundPort = outboundPort;
        this.tablesMap = tablesMap;
        this.callerMethods = callerMethods;
    }

    public TypeId declaringType() {
        return outboundPort.jigType().id();
    }

    public JigMethod interfaceMethod() {
        return portOperation.jigMethod();
    }

    public String simpleMethodSignatureText() {
        return interfaceMethod().simpleMethodSignatureText();
    }

    public JigTypeReference methodReturnType() {
        return interfaceMethod().returnType();
    }

    public Stream<JigTypeReference> methodParameterTypeStream() {
        return interfaceMethod().parameterTypeStream();
    }

    public int cyclomaticComplexity() {
        return concreteMethod().instructions().cyclomaticComplexity();
    }

    public String insertTables() {
        return joining(insertTableNames());
    }

    public List<String> insertTableNames() {
        return tableNames(PersistenceOperationType.INSERT);
    }

    public String selectTables() {
        return joining(selectTableNames());
    }

    public List<String> selectTableNames() {
        return tableNames(PersistenceOperationType.SELECT);
    }

    public String updateTables() {
        return joining(updateTableNames());
    }

    public List<String> updateTableNames() {
        return tableNames(PersistenceOperationType.UPDATE);
    }

    public String deleteTables() {
        return joining(deleteTableNames());
    }

    public List<String> deleteTableNames() {
        return tableNames(PersistenceOperationType.DELETE);
    }

    public JigMethod concreteMethod() {
        return adapterExecution.jigMethod();
    }

    public CallerMethods callerMethods() {
        return callerMethods;
    }

    public String packageText() {
        return declaringType().packageId().asText();
    }

    public String typeSimpleName() {
        return declaringType().asSimpleText();
    }

    public String typeLabel() {
        return outboundPort.jigType().label();
    }

    private List<String> tableNames(PersistenceOperationType persistenceOperationType) {
        return tablesMap.getOrDefault(persistenceOperationType, List.of());
    }

    private String joining(List<String> strings) {
        return strings.stream().collect(Collectors.joining(", ", "[", "]"));
    }
}
