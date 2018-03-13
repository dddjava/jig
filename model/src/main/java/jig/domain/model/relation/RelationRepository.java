package jig.domain.model.relation;

import jig.domain.model.thing.Name;

public interface RelationRepository {

    void regisger(Relation relation);

    Relations all();

    Relations findDependency(Name name);

    Relations allMethods();

    Relation get(Name name, RelationType type);

    Relations find(Name name, RelationType type);
}
