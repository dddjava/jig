package org.dddjava.jig.domain.model.relation;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public interface RelationRepository {

    void registerMethod(MethodDeclaration methodDeclaration);

    void registerMethodParameter(MethodDeclaration methodDeclaration);

    void registerMethodReturnType(MethodDeclaration methodDeclaration, TypeIdentifier returnTypeIdentifier);

    void registerMethodUseMethods(MethodDeclaration methodDeclaration, MethodDeclarations methodDeclarations);

    void registerImplementation(MethodDeclaration methodDeclaration, MethodDeclaration methodDeclaration1);

    void registerField(FieldDeclaration fieldDeclaration);

    void registerConstants(FieldDeclaration fieldDeclaration);

    void registerMethodUseFields(MethodDeclaration methodDeclaration, FieldDeclarations fieldDeclarations);

    TypeIdentifier getReturnTypeOf(MethodDeclaration methodDeclaration);

    FieldDeclarations findUseFields(MethodDeclaration methodDeclaration);

    MethodDeclarations findConcrete(MethodDeclaration methodDeclaration);

    MethodDeclarations findUseMethod(MethodDeclaration methodDeclaration);

    MethodDeclarations methodsOf(TypeIdentifier typeIdentifier);

    FieldDeclarations findConstants(TypeIdentifier type);

    FieldDeclarations findFieldsOf(TypeIdentifier typeIdentifier);

    TypeIdentifiers findUserTypes(MethodDeclaration methodDeclaration);

    MethodDeclarations findUserMethods(MethodDeclaration equals);

    TypeIdentifiers findUserTypes(TypeIdentifier typeIdentifier);
}
