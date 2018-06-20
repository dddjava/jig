package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;

import java.util.ArrayList;
import java.util.List;

/**
 * データソースの切り口一覧
 */
public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(List<DatasourceAngle> list) {
        this.list = list;
    }

    public static DatasourceAngles of(MethodDeclarations repositoryMethods, MethodDeclarations mapperMethodDeclarations, ImplementationMethods implementationMethods, MethodRelations methodRelations, Sqls sqls) {
        List<DatasourceAngle> list = new ArrayList<>();
        for (MethodDeclaration methodDeclaration : repositoryMethods.list()) {
            list.add(DatasourceAngle.of(methodDeclaration, mapperMethodDeclarations, implementationMethods, methodRelations, sqls));
        }
        return new DatasourceAngles(list);
    }

    public List<DatasourceAngle> list() {
        return list;
    }
}
