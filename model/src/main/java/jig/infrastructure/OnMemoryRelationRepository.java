package jig.infrastructure;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;

public class OnMemoryRelationRepository implements RelationRepository {

    List<Relation> list = new ArrayList<>();

    @Override
    public void register(Relation relation) {
        if (list.contains(relation)) return;
        list.add(relation);
    }

    @Override
    public Relations all() {
        return new Relations(list);
    }

    @Override
    public Relations methodsOf(Names names) {
        List<Relation> relations = list.stream()
                .filter(relation -> relation.relationType() == RelationType.METHOD)
                .filter(relation -> names.contains(relation.from()))
                // クラス名昇順、メソッド名昇順
                .sorted(Comparator.<Relation, String>comparing(relation -> relation.from().value())
                        .thenComparing(relation -> relation.to().value()))
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
