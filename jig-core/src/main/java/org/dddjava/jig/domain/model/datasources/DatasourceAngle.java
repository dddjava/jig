package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.datasource.SqlType;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    Sqls sqls;

    public DatasourceAngle(MethodDeclaration methodDeclaration, Sqls sqls) {
        this.methodDeclaration = methodDeclaration;
        this.sqls = sqls;
    }

    public static DatasourceAngle of(MethodDeclaration methodDeclaration, MethodDeclarations mapperMethods, ImplementationMethods implementationMethods, MethodRelations methodRelations, Sqls allSqls) {
        MethodDeclarations datasourceMethods = implementationMethods.stream()
                .filterInterfaceMethodIs(methodDeclaration)
                .concrete();

        MethodDeclarations usingMapperMethods = methodRelations.stream()
                .filterAnyFrom(datasourceMethods)
                .filterAnyTo(mapperMethods)
                .toMethods();

        Sqls sqls = allSqls.filterRelationOn(usingMapperMethods);
        return new DatasourceAngle(methodDeclaration, sqls);
    }

    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public String insertTables() {
        return sqls.tables(SqlType.INSERT).asText();
    }

    public String selectTables() {
        return sqls.tables(SqlType.SELECT).asText();
    }

    public String updateTables() {
        return sqls.tables(SqlType.UPDATE).asText();
    }

    public String deleteTables() {
        return sqls.tables(SqlType.DELETE).asText();
    }
}
