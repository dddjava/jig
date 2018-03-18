package jig.infrastructure.onmemoryrepository;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    private static final Logger LOGGER = Logger.getLogger(OnMemoryRelationRepository.class.getName());

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
    public Relations findTo(Name toName, RelationType type) {
        List<Relation> relations = list.stream()
                .filter(relation -> relation.relationType() == type)
                .filter(relation -> toName.equals(relation.to()))
                .collect(toList());
        return new Relations(relations);
    }

    @Override
    public Optional<Relation> findToOne(Name toName, RelationType type) {
        return list.stream()
                .filter(relation -> relation.relationType() == type)
                .filter(relation -> toName.equals(relation.to()))
                .findFirst();
    }

    @Override
    public Optional<Relation> findOne(Name name, RelationType type) {
        return list.stream()
                .filter(relation -> relation.from().equals(name))
                .filter(relation -> relation.relationType() == type)
                // 複数あった時にどうする？
                .findFirst();
    }

    @Override
    public Relation get(Name name, RelationType type) {
        return findOne(name, type)
                .orElseThrow(() -> {
                    LOGGER.warning("関連が見当たらない。 " + "name = " + name.value() + ", type = " + type);
                    return new NoSuchElementException();
                });
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
