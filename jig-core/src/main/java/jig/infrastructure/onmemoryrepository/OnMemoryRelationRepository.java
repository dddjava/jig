package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
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

    public void register(Relation relation) {
        map.get(relation.relationType()).add(relation);
    }

    @Override
    public Relations all() {
        return new Relations(map.values().stream().flatMap(Set::stream).collect(toList()));
    }

    @Override
    public Relations methodsOf(Identifiers identifiers) {
        List<Relation> relations = stream(RelationType.METHOD)
                .filter(relation -> identifiers.contains(relation.from()))
                .collect(toList());
        return new Relations(relations);
    }

    private Stream<Relation> stream(RelationType relationType) {
        return map.get(relationType).stream();
    }

    @Override
    public Relations findTo(Identifier toIdentifier, RelationType type) {
        List<Relation> relations = stream(type)
                .filter(relation -> toIdentifier.equals(relation.to()))
                .collect(toList());
        return new Relations(relations);
    }

    @Override
    public Optional<Relation> findToOne(Identifier toIdentifier, RelationType type) {
        return stream(type)
                .filter(relation -> toIdentifier.equals(relation.to()))
                .findFirst();
    }

    @Override
    public void registerMethod(Identifier classIdentifier, MethodIdentifier methodIdentifier) {
        register(RelationType.METHOD.of(classIdentifier, methodIdentifier.toIdentifier()));
    }

    @Override
    public void registerMethodParameter(MethodIdentifier methodIdentifier, Identifier argumentTypeIdentifier) {
        register(RelationType.METHOD_PARAMETER.of(methodIdentifier.toIdentifier(), argumentTypeIdentifier));
    }

    @Override
    public void registerMethodReturnType(MethodIdentifier methodIdentifier, Identifier returnTypeIdentifier) {
        register(RelationType.METHOD_RETURN_TYPE.of(methodIdentifier.toIdentifier(), returnTypeIdentifier));
    }

    @Override
    public void registerMethodUseMethod(MethodIdentifier from, MethodIdentifier to) {
        register(RelationType.METHOD_USE_METHOD.of(from.toIdentifier(), to.toIdentifier()));
    }

    @Override
    public void registerMethodUseType(MethodIdentifier methodIdentifier, Identifier identifier) {
        register(RelationType.METHOD_USE_TYPE.of(methodIdentifier.toIdentifier(), identifier));

    }

    @Override
    public void registerImplementation(MethodIdentifier from, MethodIdentifier to) {
        register(RelationType.IMPLEMENT.of(from.toIdentifier(), to.toIdentifier()));
    }

    @Override
    public void registerImplementation(Identifier identifier, Identifier interfaceIdentifier) {
        register(RelationType.IMPLEMENT.of(identifier, interfaceIdentifier));
    }

    @Override
    public void registerField(Identifier identifier, Identifier fieldClassIdentifier) {
        register(RelationType.FIELD.of(identifier, fieldClassIdentifier));
    }

    @Override
    public void registerDependency(Identifier from, Identifier to) {
        register(RelationType.DEPENDENCY.of(from, to));
    }

    @Override
    public Relation get(Identifier identifier, RelationType type) {
        return findOne(identifier, type)
                .orElseThrow(() -> {
                    LOGGER.warning("関連が見当たらない。 " + "identifier = " + identifier.value() + ", type = " + type);
                    return new NoSuchElementException();
                });
    }

    @Override
    public Relations find(Identifier identifier, RelationType type) {
        List<Relation> relations = stream(type)
                .filter(relation -> relation.from().equals(identifier))
                .collect(toList());
        return new Relations(relations);
    }

    Optional<Relation> findOne(Identifier identifier, RelationType type) {
        return stream(type)
                .filter(relation -> relation.from().equals(identifier))
                // 複数あった時にどうする？
                .findFirst();
    }

}
