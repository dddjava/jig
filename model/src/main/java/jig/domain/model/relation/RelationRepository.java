package jig.domain.model.relation;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

public interface RelationRepository {

    void register(Relation relation);

    Relations all();

    Relation get(Name name, RelationType type);

    Relations find(Name name, RelationType type);

    Relations methodsOf(Names names);
}
