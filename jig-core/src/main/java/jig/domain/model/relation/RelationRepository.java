package jig.domain.model.relation;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

import java.util.Optional;

public interface RelationRepository {

    void register(Relation relation);

    Relations all();

    Relation get(Name name, RelationType type);

    Relations find(Name name, RelationType type);

    Relations methodsOf(Names names);

    Relations findTo(Name toName, RelationType type);

    Optional<Relation> findOne(Name from, RelationType implementation);

    Optional<Relation> findToOne(Name to, RelationType type);
}
