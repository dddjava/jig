package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;

public interface RelationRepository {

    void registerMethod(MethodDeclaration methodDeclaration);

    void registerMethodUseMethods(MethodDeclaration methodDeclaration, MethodDeclarations methodDeclarations);

    void registerImplementation(MethodDeclaration methodDeclaration, MethodDeclaration methodDeclaration1);

    void registerField(FieldDeclaration fieldDeclaration);

    void registerConstants(FieldDeclaration fieldDeclaration);

    void registerMethodUseFields(MethodDeclaration methodDeclaration, FieldDeclarations fieldDeclarations);

    FieldDeclarations findUseFields(MethodDeclaration methodDeclaration);

    MethodDeclarations findConcrete(MethodDeclaration methodDeclaration);

    MethodDeclarations findUseMethods(MethodDeclaration methodDeclaration);

    FieldDeclarations findConstants(TypeIdentifier type);

    FieldDeclarations findFieldsOf(TypeIdentifier typeIdentifier);

    TypeIdentifiers findUserTypes(MethodDeclaration methodDeclaration);

    MethodDeclarations findUserMethods(MethodDeclaration equals);

    TypeIdentifiers findUserTypes(TypeIdentifier typeIdentifier);

    MethodRelations allMethodRelations();

    MethodUsingFields allMethodUsingFields();

    FieldDeclarations allFieldDeclarations();

    FieldDeclarations allStaticFieldDeclarations();
}
