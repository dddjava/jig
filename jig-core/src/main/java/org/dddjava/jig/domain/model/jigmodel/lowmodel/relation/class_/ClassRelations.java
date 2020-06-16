package org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 型依存関係一覧
 */
public class ClassRelations {

    List<ClassRelation> list;

    public ClassRelations(List<ClassRelation> list) {
        this.list = list;
    }

    public TypeIdentifiers collectTypeIdentifierWhichRelationTo(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(classRelation -> classRelation.toIs(typeIdentifier))
                .map(ClassRelation::from)
                .collect(TypeIdentifiers.collector())
                .normalize();
    }

    public List<ClassRelation> list() {
        return list;
    }

    public ClassRelations filterRelationsTo(TypeIdentifiers toTypeIdentifiers) {
        List<ClassRelation> collect = list.stream()
                .filter(classRelation -> toTypeIdentifiers.contains(classRelation.to()))
                .filter(classRelation -> !classRelation.selfRelation())
                .collect(Collectors.toList());
        return new ClassRelations(collect);
    }

    public List<ClassRelation> distinctList() {
        List<ClassRelation> results = new ArrayList<>();
        ADD:
        for (ClassRelation classRelation : list) {
            for (ClassRelation result : results) {
                if (classRelation.sameRelation(result)) {
                    continue ADD;
                }
            }
            results.add(classRelation);
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

    public ClassRelations relationsFromRootTo(TypeIdentifiers toTypeIdentifiers) {
        HashSet<ClassRelation> set = new HashSet<>();

        int size = set.size();
        while (true) {
            ClassRelations temp = filterRelationsTo(toTypeIdentifiers);
            set.addAll(temp.list());

            if (size == set.size()) break;
            size = set.size();
            toTypeIdentifiers = temp.fromTypeIdentifiers();
        }
        return new ClassRelations(new ArrayList<>(set));
    }

    public ClassRelations filterFrom(TypeIdentifier typeIdentifier) {
        List<ClassRelation> collect = list.stream()
                .filter(classRelation -> classRelation.from().equals(typeIdentifier))
                .collect(Collectors.toList());
        return new ClassRelations(collect);
    }

    public ClassRelations filterTo(TypeIdentifier typeIdentifier) {
        List<ClassRelation> collect = list.stream()
                .filter(classRelation -> classRelation.to().equals(typeIdentifier))
                .collect(Collectors.toList());
        return new ClassRelations(collect);
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
}
