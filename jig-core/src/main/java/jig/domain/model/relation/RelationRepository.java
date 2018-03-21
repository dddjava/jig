package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.MethodIdentifiers;

import java.util.Optional;

public interface RelationRepository {

    Relations all();

    Relation get(Identifier identifier, RelationType type);

    Relations find(Identifier identifier, RelationType type);

    Relations methodsOf(Identifiers identifiers);

    Relations findTo(Identifier toIdentifier, RelationType type);

    Optional<Relation> findToOne(Identifier to, RelationType type);

    void registerMethod(Identifier classIdentifier, MethodIdentifier methodIdentifier);

    void registerMethodParameter(MethodIdentifier methodIdentifier, Identifier argumentTypeIdentifier);

    void registerMethodReturnType(MethodIdentifier methodIdentifier, Identifier returnTypeIdentifier);

    void registerMethodUseMethod(MethodIdentifier identifier, MethodIdentifier methodName);

    void registerMethodUseType(MethodIdentifier identifier, Identifier fieldTypeName);

    void registerImplementation(MethodIdentifier methodIdentifier, MethodIdentifier methodIdentifier1);

    void registerImplementation(Identifier identifier, Identifier interfaceIdentifier);

    void registerField(Identifier identifier, Identifier fieldClassIdentifier);

    void registerDependency(Identifier from, Identifier to);

    Identifier getReturnTypeOf(MethodIdentifier methodIdentifier);

    Identifiers findUseTypeOf(MethodIdentifier methodIdentifier);

    MethodIdentifiers findConcrete(MethodIdentifier methodIdentifier);

    MethodIdentifiers findUseMethod(MethodIdentifier methodIdentifier);
}
