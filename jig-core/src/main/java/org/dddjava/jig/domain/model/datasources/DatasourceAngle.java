package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.datasource.SqlType;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.datasource.Tables;
import org.dddjava.jig.domain.model.implementation.relation.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;

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

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public TypeIdentifier returnType() {
        return methodDeclaration.returnType();
    }

    public Tables deleteTables() {
        return sqls.tables(SqlType.DELETE);
    }

    public Tables updateTables() {
        return sqls.tables(SqlType.UPDATE);
    }

    public Tables selectTables() {
        return sqls.tables(SqlType.SELECT);
    }

    public Tables insertTables() {
        return sqls.tables(SqlType.INSERT);
    }
}
