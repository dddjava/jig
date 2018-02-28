package jig.infrastructure.jdeps;

import jig.model.relation.Relation;
import jig.model.relation.RelationRepository;
import jig.model.relation.Relations;

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
