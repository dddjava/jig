package org.dddjava.jig.domain.model.jigloaded.relation.class_;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 型依存関係一覧
 */
public class ClassRelations {

    List<ClassRelation> list;

    public ClassRelations(TypeByteCodes typeByteCodes) {
        this.list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifier form = typeByteCode.typeIdentifier();
            for (TypeIdentifier to : typeByteCode.useTypes().list()) {
                ClassRelation classRelation = new ClassRelation(form, to);
                if (classRelation.selfRelation()) continue;
                list.add(classRelation);
            }
        }
    }

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
                // TODO ここでnormalizeしたくない
                .map(classRelation -> new ClassRelation(classRelation.from().normalize(), classRelation.to()))
                .filter(classRelation -> !classRelation.selfRelation())
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

    public TypeIdentifiers collectTypeIdentifiers() {
        return list.stream()
                .flatMap(classRelation -> Stream.of(classRelation.from(), classRelation.to()))
                .map(TypeIdentifier::normalize)
                .sorted()
                .distinct()
                .collect(TypeIdentifiers.collector());
    }

    public boolean nothing() {
        return list.isEmpty();
    }
}
