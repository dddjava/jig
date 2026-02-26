package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.CrudTables;
import org.dddjava.jig.domain.model.data.persistence.Tables;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementation;

import java.util.List;
import java.util.stream.Stream;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    private final OutputImplementation outputImplementation;
    private final CrudTables crudTables;
    private final CallerMethods callerMethods;

    public DatasourceAngle(OutputImplementation outputImplementation, CrudTables crudTables, CallerMethods callerMethods) {
        this.outputImplementation = outputImplementation;
        this.callerMethods = callerMethods;
        this.crudTables = crudTables;
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
        return crudTables.create().asText();
    }

    public List<String> insertTableNames() {
        return tableNames(crudTables.create());
    }

    public String selectTables() {
        return crudTables.read().asText();
    }

    public List<String> selectTableNames() {
        return tableNames(crudTables.read());
    }

    public String updateTables() {
        return crudTables.update().asText();
    }

    public List<String> updateTableNames() {
        return tableNames(crudTables.update());
    }

    public String deleteTables() {
        return crudTables.delete().asText();
    }

    public List<String> deleteTableNames() {
        return tableNames(crudTables.delete());
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

    private List<String> tableNames(Tables tables) {
        return tables.tables().stream()
                .map(org.dddjava.jig.domain.model.data.persistence.Table::name)
                .distinct()
                .sorted()
                .toList();
    }
}
