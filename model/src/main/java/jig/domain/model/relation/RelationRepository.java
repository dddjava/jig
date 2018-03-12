package jig.domain.model.relation;

import jig.domain.model.thing.Name;

public interface RelationRepository {

    void persist(Relation relation);

    Relations all();

    Relations findDependency(Name name);
}
