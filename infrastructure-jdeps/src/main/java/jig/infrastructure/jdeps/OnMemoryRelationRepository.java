package jig.infrastructure.jdeps;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;

import java.util.ArrayList;
import java.util.List;

public class OnMemoryRelationRepository implements RelationRepository {

    List<Relation> list = new ArrayList<>();

    @Override
    public void persist(Relation relation) {
        list.add(relation);
    }

    @Override
    public Relations all() {
        return new Relations(list);
    }
}
