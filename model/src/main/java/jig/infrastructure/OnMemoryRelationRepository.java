package jig.infrastructure;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.Name;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class OnMemoryRelationRepository implements RelationRepository {

    List<Relation> list = new ArrayList<>();

    @Override
    public void regisger(Relation relation) {
        list.add(relation);
    }

    @Override
    public Relations all() {
        return new Relations(list);
    }

    @Override
    public Relations findDependency(Name name) {
        List<Relation> relations = list.stream()
                .filter(relation -> relation.from().equals(name))
                .collect(toList());
        return new Relations(relations);
    }
}
