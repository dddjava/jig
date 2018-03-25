package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.MethodIdentifiers;

public interface RelationRepository {

    void registerMethod(MethodIdentifier methodIdentifier);

    void registerMethodParameter(MethodIdentifier methodIdentifier);

    void registerMethodReturnType(MethodIdentifier methodIdentifier, Identifier returnTypeIdentifier);

    void registerMethodUseMethod(MethodIdentifier identifier, MethodIdentifier methodName);

    void registerMethodUseType(MethodIdentifier identifier, Identifier fieldTypeName);

    void registerImplementation(MethodIdentifier methodIdentifier, MethodIdentifier methodIdentifier1);

    void registerField(Identifier identifier, Identifier fieldClassIdentifier);

    Identifier getReturnTypeOf(MethodIdentifier methodIdentifier);

    Identifiers findUseTypeOf(MethodIdentifier methodIdentifier);

    MethodIdentifiers findConcrete(MethodIdentifier methodIdentifier);

    MethodIdentifiers findUseMethod(MethodIdentifier methodIdentifier);

    MethodIdentifiers methodsOf(Identifier identifier);

    Identifiers findFieldUsage(Identifier name);

    MethodIdentifiers findMethodUsage(Identifier name);
}
