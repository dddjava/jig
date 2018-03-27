package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.relation.DependencyRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Repository
public class OnMemoryDependencyRepository implements DependencyRepository {

    final EnumMap<RelationType, Set<Relation>> map;

    public OnMemoryDependencyRepository() {
        map = new EnumMap<>(RelationType.class);
        map.put(RelationType.DEPENDENCY, new HashSet<>());
    }

    @Override
    public void registerDependency(TypeIdentifier from, TypeIdentifier to) {
        Relation relation = RelationType.DEPENDENCY.of(from, to);
        map.get(relation.relationType()).add(relation);
    }

    @Override
    public Relations all() {
        return new Relations(map.values().stream().flatMap(Set::stream).collect(toList()));
    }
}
