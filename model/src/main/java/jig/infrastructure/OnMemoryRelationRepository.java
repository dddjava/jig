package jig.infrastructure;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.Name;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

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

    @Override
    public Relations allMethods() {
        List<Relation> relations = list.stream()
                .filter(relation -> relation.relationType() == RelationType.METHOD)
                .sorted(Comparator.comparing(relation -> relation.from().value()))
                .collect(toList());
        return new Relations(relations);
    }

    @Override
    public Relation get(Name name, RelationType type) {
        return list.stream()
                .filter(relation -> relation.from().equals(name))
                .filter(relation -> relation.relationType() == type)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Relations find(Name name, RelationType type) {
        List<Relation> relations = list.stream()
                .filter(relation -> relation.from().equals(name))
                .filter(relation -> relation.relationType() == type)
                .collect(toList());
        return new Relations(relations);
    }
}
