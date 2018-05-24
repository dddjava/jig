package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.relation.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;

public class DatasourceAngleSource {
    private final MethodDeclarations repositoryMethods;
    private final MethodDeclarations mapperMethods;
    private final ImplementationMethods implementationMethods;
    private final MethodRelations methodRelations;
    private final Sqls allSqls;

    public DatasourceAngleSource(MethodDeclarations repositoryMethods, MethodDeclarations mapperMethods, ImplementationMethods implementationMethods, MethodRelations methodRelations, Sqls allSqls) {
        this.repositoryMethods = repositoryMethods;
        this.mapperMethods = mapperMethods;
        this.implementationMethods = implementationMethods;
        this.methodRelations = methodRelations;
        this.allSqls = allSqls;
    }

    public MethodDeclarations getRepositoryMethods() {
        return repositoryMethods;
    }

    public MethodDeclarations getMapperMethods() {
        return mapperMethods;
    }

    public ImplementationMethods getImplementationMethods() {
        return implementationMethods;
    }

    public MethodRelations getMethodRelations() {
        return methodRelations;
    }

    public Sqls getAllSqls() {
        return allSqls;
    }
}
