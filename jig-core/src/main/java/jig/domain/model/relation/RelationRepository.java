package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;

import java.util.Optional;

public interface RelationRepository {

    void register(Relation relation);

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
}
