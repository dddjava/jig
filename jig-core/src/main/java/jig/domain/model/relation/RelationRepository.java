package jig.domain.model.relation;

import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

public interface RelationRepository {

    void registerMethod(MethodDeclaration methodDeclaration);

    void registerMethodParameter(MethodDeclaration methodDeclaration);

    void registerMethodReturnType(MethodDeclaration methodDeclaration, TypeIdentifier returnTypeIdentifier);

    void registerMethodUseMethod(MethodDeclaration identifier, MethodDeclaration methodName);

    void registerMethodUseType(MethodDeclaration identifier, TypeIdentifier fieldTypeName);

    void registerImplementation(MethodDeclaration methodDeclaration, MethodDeclaration methodDeclaration1);

    void registerField(FieldDeclaration fieldDeclaration);

    void registerConstants(FieldDeclaration fieldDeclaration);

    void registerMethodUseField(MethodDeclaration methodDeclaration, FieldDeclaration fieldDeclaration);

    TypeIdentifier getReturnTypeOf(MethodDeclaration methodDeclaration);

    TypeIdentifiers findUseTypeOf(MethodDeclaration methodDeclaration);

    MethodDeclarations findConcrete(MethodDeclaration methodDeclaration);

    MethodDeclarations findUseMethod(MethodDeclaration methodDeclaration);

    MethodDeclarations methodsOf(TypeIdentifier typeIdentifier);

    TypeIdentifiers findFieldUsage(TypeIdentifier name);

    MethodDeclarations findMethodUsage(TypeIdentifier name);

    FieldDeclarations findConstants(TypeIdentifier type);

    FieldDeclarations findFieldsOf(TypeIdentifier typeIdentifier);

    void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers);

    TypeIdentifiers findDependency(TypeIdentifier identifier);
}
