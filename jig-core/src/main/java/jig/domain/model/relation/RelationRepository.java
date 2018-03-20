package jig.domain.model.relation;

import jig.domain.model.thing.Identifier;
import jig.domain.model.thing.Names;

import java.util.Optional;

public interface RelationRepository {

    void register(Relation relation);

    Relations all();

    Relation get(Identifier identifier, RelationType type);

    Relations find(Identifier identifier, RelationType type);

    Relations methodsOf(Names names);

    Relations findTo(Identifier toIdentifier, RelationType type);

    Optional<Relation> findToOne(Identifier to, RelationType type);
}
