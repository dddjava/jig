package jig.domain.model.relation;

import jig.domain.model.thing.Name;

public interface RelationRepository {

    void register(Relation relation);

    Relations all();

    Relations allMethods();

    Relation get(Name name, RelationType type);

    Relations find(Name name, RelationType type);
}
