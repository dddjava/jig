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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    private static final Logger LOGGER = Logger.getLogger(OnMemoryRelationRepository.class.getName());

    final EnumMap<RelationType, Set<Relation>> map;

    public OnMemoryRelationRepository() {
        map = new EnumMap<>(RelationType.class);
        for (RelationType relationType : RelationType.values()) {
            map.put(relationType, new HashSet<>());
        }
    }

    @Override
    public void register(Relation relation) {
        map.get(relation.relationType()).add(relation);
    }

    @Override
    public Relations all() {
        return new Relations(map.values().stream().flatMap(Set::stream).collect(toList()));
    }

    @Override
    public Relations methodsOf(Names names) {
        List<Relation> relations = stream(RelationType.METHOD)
                .filter(relation -> names.contains(relation.from()))
                .collect(toList());
        return new Relations(relations);
    }

    private Stream<Relation> stream(RelationType relationType) {
        return map.get(relationType).stream();
    }

    @Override
    public Relations findTo(Name toName, RelationType type) {
        List<Relation> relations = stream(type)
                .filter(relation -> toName.equals(relation.to()))
                .collect(toList());
        return new Relations(relations);
    }

    @Override
    public Optional<Relation> findToOne(Name toName, RelationType type) {
        return stream(type)
                .filter(relation -> toName.equals(relation.to()))
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
        List<Relation> relations = stream(type)
                .filter(relation -> relation.from().equals(name))
                .collect(toList());
        return new Relations(relations);
    }

    @Override
    public Optional<Relation> findOne(Name name, RelationType type) {
        return stream(type)
                .filter(relation -> relation.from().equals(name))
                // 複数あった時にどうする？
                .findFirst();
    }

}
