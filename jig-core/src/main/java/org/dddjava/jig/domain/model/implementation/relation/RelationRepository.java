package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

public interface RelationRepository {

    void registerImplementation(MethodDeclaration methodDeclaration, MethodDeclaration methodDeclaration1);

    ImplementationMethods allImplementationMethods();
}
