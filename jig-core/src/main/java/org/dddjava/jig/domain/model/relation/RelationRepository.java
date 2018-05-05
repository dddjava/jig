package org.dddjava.jig.domain.model.relation;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public interface RelationRepository {

    void registerMethod(MethodDeclaration methodDeclaration);

    void registerMethodUseMethods(MethodDeclaration methodDeclaration, MethodDeclarations methodDeclarations);

    void registerImplementation(MethodDeclaration methodDeclaration, MethodDeclaration methodDeclaration1);

    void registerField(FieldDeclaration fieldDeclaration);

    void registerConstants(FieldDeclaration fieldDeclaration);

    void registerMethodUseFields(MethodDeclaration methodDeclaration, FieldDeclarations fieldDeclarations);

    FieldDeclarations findUseFields(MethodDeclaration methodDeclaration);

    MethodDeclarations findConcrete(MethodDeclaration methodDeclaration);

    MethodDeclarations findUseMethod(MethodDeclaration methodDeclaration);

    FieldDeclarations findConstants(TypeIdentifier type);

    FieldDeclarations findFieldsOf(TypeIdentifier typeIdentifier);

    TypeIdentifiers findUserTypes(MethodDeclaration methodDeclaration);

    MethodDeclarations findUserMethods(MethodDeclaration equals);

    TypeIdentifiers findUserTypes(TypeIdentifier typeIdentifier);
}
