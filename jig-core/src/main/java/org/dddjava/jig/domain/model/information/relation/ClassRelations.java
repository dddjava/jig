package org.dddjava.jig.domain.model.information.relation;

import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.data.packages.PackageRelation;
import org.dddjava.jig.domain.model.data.packages.PackageRelations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * 型依存関係一覧
 */
public class ClassRelations {

    List<ClassRelation> list;

    public ClassRelations(List<ClassRelation> list) {
        this.list = list;
    }

    public static ClassRelations from(JigTypes jigTypes) {
        return new ClassRelations(jigTypes.stream()
                .flatMap(jigType -> jigType.usingTypes().list().stream()
                        .map(usingType -> new ClassRelation(jigType.typeIdentifier(), usingType)))
                .filter(classRelation -> !classRelation.selfRelation())
                .toList());
    }

    public static ClassRelations internalRelation(JigTypes jigTypes) {
        return jigTypes.stream()
                .flatMap(jigType -> jigType.usingTypes().list().stream()
                        .filter(typeIdentifier -> jigTypes.contains(typeIdentifier))
                        .map(typeIdentifier -> new ClassRelation(jigType.typeIdentifier(), typeIdentifier)))
                .filter(classRelation -> !classRelation.selfRelation())
                .collect(collectingAndThen(toList(), ClassRelations::new));
    }

    public static ClassRelations internalTypeRelationsFrom(JigTypes jigTypes, JigType targetJigType) {
        return internalRelation(jigTypes).filterFrom(targetJigType.typeIdentifier());
    }

    public static ClassRelations internalTypeRelationsTo(JigTypes jigTypes, JigType targetJigType) {
        return internalRelation(jigTypes).filterTo(targetJigType.identifier());
    }

    public PackageRelations toPackageRelations() {
        Map<PackageRelation, List<PackageRelation>> map = list().stream()
                .map(ClassRelation::toPackageRelation)
                .filter(PackageRelation::notSelfRelation)
                .collect(groupingBy(Function.identity()));
        return new PackageRelations(map);
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

    public ClassRelations distinct() {
        return new ClassRelations(distinctList());
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

        int size = 0;
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

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public String dotText() {
        return list.stream()
                .map(ClassRelation::dotText)
                .collect(Collectors.joining("\n"));
    }
}
