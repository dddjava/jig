package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.relation.graph.Edges;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 型依存関係一覧
 */
public record TypeRelationships(Collection<TypeRelationship> typeRelationships) {

    public static TypeRelationships from(JigTypes jigTypes) {
        return new TypeRelationships(jigTypes.orderedStream()
                .flatMap(jigType -> jigType.usingTypes().list().stream()
                        .flatMap(usingType -> TypeRelationship.of不明(jigType.id(), usingType).stream()))
                .toList());
    }

    public static TypeRelationships internalRelation(JigTypes jigTypes) {
        return jigTypes.orderedStream()
                .flatMap(jigType -> jigType.usingTypes().list().stream()
                        .filter(jigTypes::contains)
                        .flatMap(typeId -> TypeRelationship.of不明(jigType.id(), typeId).stream()))
                .collect(collectingAndThen(toList(), TypeRelationships::new));
    }

    public static TypeRelationships from(JigType jigType) {
        JigTypeHeader jigTypeHeader = jigType.jigTypeHeader();
        TypeId id = jigTypeHeader.id();

        Stream<TypeRelationship> typeRelationshipStream = headerTypeRelationshipStream(jigTypeHeader, id);

        return new TypeRelationships(typeRelationshipStream.toList());
    }

    private static Stream<TypeRelationship> headerTypeRelationshipStream(JigTypeHeader jigTypeHeader, TypeId id) {
        // 自身の型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
        Stream<TypeRelationship> typeParameterStream = jigTypeHeader.jigTypeAttributes().typeParameters().stream()
                // 型パラメタ自体は型ではないが、型パラメタの境界は型引数なので取得する
                .flatMap(jigTypeParameter -> jigTypeParameter.bounds().stream())
                .map(typeArg -> TypeRelationship.of型引数(id, typeArg.typeId()));
        // 自身のアノテーション
        Stream<TypeRelationship> annotationStream = annotationTypeRelationshipStream(jigTypeHeader.jigTypeAttributes().declarationAnnotationInstances(), id);

        // superとinterface
        Stream<TypeRelationship> superStream = jigTypeHeader.superType().stream()
                .flatMap(jigTypeReference -> typeReferenceRelationshipStream(id, jigTypeReference, TypeRelationKind.継承クラス));
        Stream<TypeRelationship> interfaceStream = jigTypeHeader.interfaceTypeList().stream()
                .flatMap(jigTypeReference -> typeReferenceRelationshipStream(id, jigTypeReference, TypeRelationKind.実装インタフェース));

        return Stream.of(typeParameterStream, annotationStream, superStream, interfaceStream).flatMap(Function.identity());
    }

    private static Stream<TypeRelationship> typeReferenceRelationshipStream(TypeId id, JigTypeReference jigTypeReference, TypeRelationKind typeRelationKind) {
        return Stream.of(
                        // 自身
                        Stream.of(TypeRelationship.of(id, jigTypeReference.id(), typeRelationKind)),
                        // 型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
                        jigTypeReference.typeArgumentList().stream()
                                .map(typeArg -> TypeRelationship.of型引数(id, typeArg.typeId())),
                        // 型アノテーション
                        annotationTypeRelationshipStream(jigTypeReference.typeAnnotations(), id))
                .flatMap(Function.identity());
    }

    private static Stream<TypeRelationship> annotationTypeRelationshipStream(Collection<JigAnnotationReference> jigTypeReference, TypeId id) {
        // アノテーション（アノテーション引数は未対応）
        return jigTypeReference.stream()
                .map(annoRef -> TypeRelationship.of使用アノテーション(id, annoRef.id()));
    }

    public Edges<TypeId> toEdges() {
        return new Edges<>(typeRelationships.stream()
                .map(TypeRelationship::edge)
                .toList());
    }

    public TypeIds collectTypeIdWhichRelationTo(TypeId typeId) {
        return typeRelationships.stream()
                .filter(classRelation -> classRelation.toIs(typeId))
                .map(TypeRelationship::from)
                .collect(TypeIds.collector())
                .normalize();
    }

    public List<TypeRelationship> list() {
        return typeRelationships.stream()
                .sorted(Comparator.comparing(TypeRelationship::from).thenComparing(TypeRelationship::to))
                .toList();
    }

    public TypeIds toTypeIds() {
        return typeRelationships.stream()
                .flatMap(classRelation -> Stream.of(classRelation.from(), classRelation.to()))
                .map(TypeId::normalize) // ここでnormalizeいる？？
                .collect(TypeIds.collector());
    }

    public TypeRelationships relationsFromRootTo(TypeIds toTypeIds) {
        HashSet<TypeRelationship> set = new HashSet<>();

        int size = 0;
        while (true) {
            TypeRelationships temp = filterRelationsTo(toTypeIds);
            set.addAll(temp.typeRelationships());

            if (size == set.size()) break;
            size = set.size();
            toTypeIds = temp.fromTypeIds();
        }
        return new TypeRelationships(set);
    }

    private TypeRelationships filterRelationsTo(TypeIds toTypeIds) {
        return filterRelationships(classRelation -> toTypeIds.contains(classRelation.to()));
    }

    public TypeRelationships filterFrom(TypeId typeId) {
        return filterRelationships(classRelation -> classRelation.from().equals(typeId));
    }


    public TypeRelationships filterTo(TypeId typeId) {
        return filterRelationships(classRelation -> classRelation.to().equals(typeId));
    }

    private TypeRelationships filterRelationships(Predicate<TypeRelationship> typeRelationshipPredicate) {
        return new TypeRelationships(typeRelationships.stream()
                .filter(typeRelationshipPredicate)
                .toList());
    }

    public TypeIds fromTypeIds() {
        return typeRelationships.stream()
                .map(classRelation -> classRelation.from())
                .collect(TypeIds.collector());
    }

    public int size() {
        return typeRelationships.size();
    }

    public boolean isEmpty() {
        return typeRelationships.isEmpty();
    }
}
