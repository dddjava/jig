package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;

public interface RelationRepository {

    void registerMethodUseMethods(MethodDeclaration methodDeclaration, MethodDeclarations methodDeclarations);

    void registerImplementation(MethodDeclaration methodDeclaration, MethodDeclaration methodDeclaration1);

    void registerField(FieldDeclaration fieldDeclaration);

    void registerConstants(FieldDeclaration fieldDeclaration);

    void registerMethodUseFields(MethodDeclaration methodDeclaration, FieldDeclarations fieldDeclarations);

    MethodRelations allMethodRelations();

    MethodUsingFields allMethodUsingFields();

    FieldDeclarations allFieldDeclarations();

    FieldDeclarations allStaticFieldDeclarations();

    ImplementationMethods allImplementationMethods();
}
