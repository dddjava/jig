package org.dddjava.jig.domain.model.information.types.relations;

import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
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

    public static TypeRelationships internalRelation(JigTypes jigTypes) {
        return jigTypes.stream()
                .flatMap(jigType -> jigType.usingTypes().list().stream()
                        .filter(typeIdentifier -> jigTypes.contains(typeIdentifier))
                        .flatMap(typeIdentifier -> TypeRelationship.from(jigType.id(), typeIdentifier).stream()))
                .collect(collectingAndThen(toList(), TypeRelationships::new));
    }

    public static TypeRelationships internalTypeRelationsFrom(JigTypesWithRelationships jigTypes, JigType targetJigType) {
        return jigTypes.typeRelationships().filterFrom(targetJigType.id());
    }

    public static TypeRelationships internalTypeRelationsTo(JigTypesWithRelationships jigTypes, JigType targetJigType) {
        return jigTypes.typeRelationships().filterTo(targetJigType.id());
    }

    public static TypeRelationships from(JigTypeHeader jigTypeHeader, JigTypeMembers jigTypeMembers) {
        TypeIdentifier id = jigTypeHeader.id();

        Stream<TypeRelationship> typeRelationshipStream = headerTypeRelationshipStream(jigTypeHeader, id);

        return new TypeRelationships(typeRelationshipStream.toList());
    }

    private static Stream<TypeRelationship> headerTypeRelationshipStream(JigTypeHeader jigTypeHeader, TypeIdentifier id) {
        // 自身の型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
        Stream<TypeRelationship> typeParameterStream = jigTypeHeader.jigTypeAttributeData().typeParameters().stream()
                // 型パラメタ自体は型ではないが、型パラメタの境界は型引数なので取得する
                .flatMap(jigTypeParameter -> jigTypeParameter.bounds().stream())
                .map(typeArg -> new TypeRelationship(id, typeArg.typeIdentifier(), TypeRelationKind.型引数));
        // 自身のアノテーション
        Stream<TypeRelationship> annotationStream = annotationTypeRelationshipStream(jigTypeHeader.jigTypeAttributeData().declarationAnnotationInstances(), id);

        // superとinterface
        Stream<TypeRelationship> superStream = jigTypeHeader.superType().stream()
                .flatMap(jigTypeReference -> typeReferenceRelationshipStream(id, jigTypeReference, TypeRelationKind.継承クラス));
        Stream<TypeRelationship> interfaceStream = jigTypeHeader.interfaceTypeList().stream()
                .flatMap(jigTypeReference -> typeReferenceRelationshipStream(id, jigTypeReference, TypeRelationKind.実装インタフェース));

        return Stream.of(typeParameterStream, annotationStream, superStream, interfaceStream).flatMap(Function.identity());
    }

    private static Stream<TypeRelationship> typeReferenceRelationshipStream(TypeIdentifier id, JigTypeReference jigTypeReference, TypeRelationKind typeRelationKind) {
        return Stream.of(
                        // 自身
                        Stream.of(new TypeRelationship(id, jigTypeReference.id(), typeRelationKind)),
                        // 型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
                        jigTypeReference.typeArgumentList().stream()
                                .map(typeArg -> new TypeRelationship(id, typeArg.typeIdentifier(), TypeRelationKind.型引数)),
                        // 型アノテーション
                        annotationTypeRelationshipStream(jigTypeReference.typeAnnotations(), id))
                .flatMap(Function.identity());
    }

    private static Stream<TypeRelationship> annotationTypeRelationshipStream(Collection<JigAnnotationReference> jigTypeReference, TypeIdentifier id) {
        // アノテーション（アノテーション引数は未対応）
        return jigTypeReference.stream()
                .map(annoRef -> new TypeRelationship(id, annoRef.id(), TypeRelationKind.使用アノテーション));
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
