package jig.domain.model.relation;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.field.FieldIdentifiers;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

public interface RelationRepository {

    void registerMethod(MethodIdentifier methodIdentifier);

    void registerMethodParameter(MethodIdentifier methodIdentifier);

    void registerMethodReturnType(MethodIdentifier methodIdentifier, TypeIdentifier returnTypeIdentifier);

    void registerMethodUseMethod(MethodIdentifier identifier, MethodIdentifier methodName);

    void registerMethodUseType(MethodIdentifier identifier, TypeIdentifier fieldTypeName);

    void registerImplementation(MethodIdentifier methodIdentifier, MethodIdentifier methodIdentifier1);

    void registerField(FieldIdentifier fieldIdentifier);

    void registerConstants(FieldIdentifier fieldIdentifier);

    void registerMethodUseField(MethodIdentifier methodIdentifier, FieldIdentifier fieldIdentifier);

    TypeIdentifier getReturnTypeOf(MethodIdentifier methodIdentifier);

    TypeIdentifiers findUseTypeOf(MethodIdentifier methodIdentifier);

    MethodIdentifiers findConcrete(MethodIdentifier methodIdentifier);

    MethodIdentifiers findUseMethod(MethodIdentifier methodIdentifier);

    MethodIdentifiers methodsOf(TypeIdentifier typeIdentifier);

    TypeIdentifiers findFieldUsage(TypeIdentifier name);

    MethodIdentifiers findMethodUsage(TypeIdentifier name);

    FieldIdentifiers findConstants(TypeIdentifier type);

    FieldIdentifiers findFieldsOf(TypeIdentifier typeIdentifier);

    void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers);

    TypeIdentifiers findDependency(TypeIdentifier identifier);
}
