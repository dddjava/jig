package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.SqlType;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.pair.OutputImplementation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    private final OutputImplementation outputImplementation;
    private final Map<SqlType, List<String>> tablesMap;
    private final CallerMethods callerMethods;

    public DatasourceAngle(OutputImplementation outputImplementation, Map<SqlType, List<String>> tablesMap, CallerMethods callerMethods) {
        this.outputImplementation = outputImplementation;
        this.tablesMap = tablesMap;
        this.callerMethods = callerMethods;
    }

    public TypeId declaringType() {
        return outputImplementation.interfaceJigType().id();
    }

    public JigMethod interfaceMethod() {
        return outputImplementation.outputPortOperaionAsJigMethod();
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
        return tableNames(SqlType.INSERT);
    }

    public String selectTables() {
        return joining(selectTableNames());
    }

    public List<String> selectTableNames() {
        return tableNames(SqlType.SELECT);
    }

    public String updateTables() {
        return joining(updateTableNames());
    }

    public List<String> updateTableNames() {
        return tableNames(SqlType.UPDATE);
    }

    public String deleteTables() {
        return joining(deleteTableNames());
    }

    public List<String> deleteTableNames() {
        return tableNames(SqlType.DELETE);
    }

    public JigMethod concreteMethod() {
        return outputImplementation.concreteMethod();
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
        return outputImplementation.interfaceJigType().label();
    }

    private List<String> tableNames(SqlType sqlType) {
        return tablesMap.getOrDefault(sqlType, List.of());
    }

    private String joining(List<String> strings) {
        return strings.stream().collect(Collectors.joining(", ", "[", "]"));
    }
}
