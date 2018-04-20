package jig.domain.model.relation;

import jig.domain.model.definition.field.FieldDefinition;
import jig.domain.model.definition.field.FieldDefinitions;
import jig.domain.model.definition.method.MethodDefinition;
import jig.domain.model.definition.method.MethodDefinitions;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

public interface RelationRepository {

    void registerMethod(MethodDefinition methodDefinition);

    void registerMethodParameter(MethodDefinition methodDefinition);

    void registerMethodReturnType(MethodDefinition methodDefinition, TypeIdentifier returnTypeIdentifier);

    void registerMethodUseMethod(MethodDefinition identifier, MethodDefinition methodName);

    void registerMethodUseType(MethodDefinition identifier, TypeIdentifier fieldTypeName);

    void registerImplementation(MethodDefinition methodDefinition, MethodDefinition methodDefinition1);

    void registerField(FieldDefinition fieldDefinition);

    void registerConstants(FieldDefinition fieldDefinition);

    void registerMethodUseField(MethodDefinition methodDefinition, FieldDefinition fieldDefinition);

    TypeIdentifier getReturnTypeOf(MethodDefinition methodDefinition);

    TypeIdentifiers findUseTypeOf(MethodDefinition methodDefinition);

    MethodDefinitions findConcrete(MethodDefinition methodDefinition);

    MethodDefinitions findUseMethod(MethodDefinition methodDefinition);

    MethodDefinitions methodsOf(TypeIdentifier typeIdentifier);

    TypeIdentifiers findFieldUsage(TypeIdentifier name);

    MethodDefinitions findMethodUsage(TypeIdentifier name);

    FieldDefinitions findConstants(TypeIdentifier type);

    FieldDefinitions findFieldsOf(TypeIdentifier typeIdentifier);

    void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers);

    TypeIdentifiers findDependency(TypeIdentifier identifier);
}
