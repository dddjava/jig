package org.dddjava.jig.domain.model.information.types.relations;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 型依存関係一覧
 */
public class TypeRelationships {

    List<TypeRelationship> list;

    public TypeRelationships(List<TypeRelationship> list) {
        this.list = list;
    }

    public static TypeRelationships from(JigTypes jigTypes) {
        return new TypeRelationships(jigTypes.stream()
                .flatMap(jigType -> jigType.usingTypes().list().stream()
                        .flatMap(usingType -> TypeRelationship.from(jigType.id(), usingType).stream()))
                .toList());
    }

    private static final ConcurrentHashMap<JigTypes, TypeRelationships> cache = new ConcurrentHashMap<>();

    public static TypeRelationships internalRelation(JigTypes jigTypes) {
        synchronized (jigTypes) {
            return cache.computeIfAbsent(jigTypes, keyJigTypes -> {
                return keyJigTypes.stream()
                        .flatMap(jigType -> jigType.usingTypes().list().stream()
                                .filter(typeIdentifier -> keyJigTypes.contains(typeIdentifier))
                                .flatMap(typeIdentifier -> TypeRelationship.from(jigType.id(), typeIdentifier).stream()))
                        .collect(collectingAndThen(toList(), TypeRelationships::new));
            });
        }
    }

    public static TypeRelationships internalTypeRelationsFrom(JigTypes jigTypes, JigType targetJigType) {
        return internalRelation(jigTypes).filterFrom(targetJigType.id());
    }

    public static TypeRelationships internalTypeRelationsTo(JigTypes jigTypes, JigType targetJigType) {
        return internalRelation(jigTypes).filterTo(targetJigType.id());
    }

    public TypeIdentifiers collectTypeIdentifierWhichRelationTo(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(classRelation -> classRelation.toIs(typeIdentifier))
                .map(TypeRelationship::from)
                .collect(TypeIdentifiers.collector())
                .normalize();
    }

    public List<TypeRelationship> list() {
        return list;
    }

    public TypeRelationships filterRelationsTo(TypeIdentifiers toTypeIdentifiers) {
        List<TypeRelationship> collect = list.stream()
                .filter(classRelation -> toTypeIdentifiers.contains(classRelation.to()))
                .collect(Collectors.toList());
        return new TypeRelationships(collect);
    }

    public TypeRelationships distinct() {
        return new TypeRelationships(distinctList());
    }

    public List<TypeRelationship> distinctList() {
        List<TypeRelationship> results = new ArrayList<>();
        ADD:
        for (TypeRelationship typeRelationship : list) {
            for (TypeRelationship result : results) {
                if (typeRelationship.sameRelation(result)) {
                    continue ADD;
                }
            }
            results.add(typeRelationship);
        }
        return results;
    }

    public TypeIdentifiers allTypeIdentifiers() {
        return list.stream()
                .flatMap(classRelation -> Stream.of(classRelation.from(), classRelation.to()))
                .map(TypeIdentifier::normalize)
                .sorted()
                .distinct()
                .collect(TypeIdentifiers.collector());
    }

    public TypeRelationships relationsFromRootTo(TypeIdentifiers toTypeIdentifiers) {
        HashSet<TypeRelationship> set = new HashSet<>();

        int size = 0;
        while (true) {
            TypeRelationships temp = filterRelationsTo(toTypeIdentifiers);
            set.addAll(temp.list());

            if (size == set.size()) break;
            size = set.size();
            toTypeIdentifiers = temp.fromTypeIdentifiers();
        }
        return new TypeRelationships(new ArrayList<>(set));
    }

    public TypeRelationships filterFrom(TypeIdentifier typeIdentifier) {
        List<TypeRelationship> collect = list.stream()
                .filter(classRelation -> classRelation.from().equals(typeIdentifier))
                .collect(Collectors.toList());
        return new TypeRelationships(collect);
    }

    public TypeRelationships filterTo(TypeIdentifier typeIdentifier) {
        List<TypeRelationship> collect = list.stream()
                .filter(classRelation -> classRelation.to().equals(typeIdentifier))
                .collect(Collectors.toList());
        return new TypeRelationships(collect);
    }

    public TypeIdentifiers fromTypeIdentifiers() {
        return list.stream()
                .map(classRelation -> classRelation.from())
                .sorted()
                .distinct()
                .collect(TypeIdentifiers.collector());
    }

    public TypeIdentifiers toTypeIdentifiers() {
        return list.stream()
                .map(classRelation -> classRelation.to())
                .sorted()
                .distinct()
                .collect(TypeIdentifiers.collector());
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public String dotText() {
        return list.stream()
                .map(TypeRelationship::dotText)
                .collect(Collectors.joining("\n"));
    }
}
