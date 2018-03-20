package jig.domain.model.relation;

import jig.domain.model.thing.Identifier;
import jig.domain.model.thing.Identifiers;

import java.util.Optional;

public interface RelationRepository {

    void register(Relation relation);

    Relations all();

    Relation get(Identifier identifier, RelationType type);

    Relations find(Identifier identifier, RelationType type);

    Relations methodsOf(Identifiers identifiers);

    Relations findTo(Identifier toIdentifier, RelationType type);

    Optional<Relation> findToOne(Identifier to, RelationType type);
}
