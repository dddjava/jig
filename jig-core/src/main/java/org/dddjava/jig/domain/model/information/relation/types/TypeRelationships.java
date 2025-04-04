package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.relation.graph.Edges;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
                        .filter(typeIdentifier -> jigTypes.contains(typeIdentifier))
                        .flatMap(typeIdentifier -> TypeRelationship.of不明(jigType.id(), typeIdentifier).stream()))
                .collect(collectingAndThen(toList(), TypeRelationships::new));
    }

    public static TypeRelationships from(JigType jigType) {
        JigTypeHeader jigTypeHeader = jigType.jigTypeHeader();
        TypeIdentifier id = jigTypeHeader.id();

        Stream<TypeRelationship> typeRelationshipStream = headerTypeRelationshipStream(jigTypeHeader, id);

        return new TypeRelationships(typeRelationshipStream.toList());
    }

    private static Stream<TypeRelationship> headerTypeRelationshipStream(JigTypeHeader jigTypeHeader, TypeIdentifier id) {
        // 自身の型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
        Stream<TypeRelationship> typeParameterStream = jigTypeHeader.jigTypeAttributeData().typeParameters().stream()
                // 型パラメタ自体は型ではないが、型パラメタの境界は型引数なので取得する
                .flatMap(jigTypeParameter -> jigTypeParameter.bounds().stream())
                .map(typeArg -> TypeRelationship.of型引数(id, typeArg.typeIdentifier()));
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
                        Stream.of(TypeRelationship.of(id, jigTypeReference.id(), typeRelationKind)),
                        // 型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
                        jigTypeReference.typeArgumentList().stream()
                                .map(typeArg -> TypeRelationship.of型引数(id, typeArg.typeIdentifier())),
                        // 型アノテーション
                        annotationTypeRelationshipStream(jigTypeReference.typeAnnotations(), id))
                .flatMap(Function.identity());
    }

    private static Stream<TypeRelationship> annotationTypeRelationshipStream(Collection<JigAnnotationReference> jigTypeReference, TypeIdentifier id) {
        // アノテーション（アノテーション引数は未対応）
        return jigTypeReference.stream()
                .map(annoRef -> TypeRelationship.of使用アノテーション(id, annoRef.id()));
    }

    public Edges<TypeIdentifier> toEdges() {
        return new Edges<>(typeRelationships.stream()
                .map(TypeRelationship::edge)
                .toList());
    }

    public TypeIdentifiers collectTypeIdentifierWhichRelationTo(TypeIdentifier typeIdentifier) {
        return typeRelationships.stream()
                .filter(classRelation -> classRelation.toIs(typeIdentifier))
                .map(TypeRelationship::from)
                .collect(TypeIdentifiers.collector())
                .normalize();
    }

    public List<TypeRelationship> list() {
        return typeRelationships.stream()
                .sorted(Comparator.comparing(TypeRelationship::from).thenComparing(TypeRelationship::to))
                .toList();
    }

    public TypeRelationships filterRelationsTo(TypeIdentifiers toTypeIdentifiers) {
        List<TypeRelationship> collect = typeRelationships.stream()
                .filter(classRelation -> toTypeIdentifiers.contains(classRelation.to()))
                .collect(Collectors.toList());
        return new TypeRelationships(collect);
    }

    public TypeIdentifiers allTypeIdentifiers() {
        return typeRelationships.stream()
                .flatMap(classRelation -> Stream.of(classRelation.from(), classRelation.to()))
                .map(TypeIdentifier::normalize)
                .collect(TypeIdentifiers.collector());
    }

    public TypeRelationships relationsFromRootTo(TypeIdentifiers toTypeIdentifiers) {
        HashSet<TypeRelationship> set = new HashSet<>();

        int size = 0;
        while (true) {
            TypeRelationships temp = filterRelationsTo(toTypeIdentifiers);
            set.addAll(temp.typeRelationships());

            if (size == set.size()) break;
            size = set.size();
            toTypeIdentifiers = temp.fromTypeIdentifiers();
        }
        return new TypeRelationships(new ArrayList<>(set));
    }

    public TypeRelationships filterFrom(TypeIdentifier typeIdentifier) {
        List<TypeRelationship> collect = typeRelationships.stream()
                .filter(classRelation -> classRelation.from().equals(typeIdentifier))
                .collect(Collectors.toList());
        return new TypeRelationships(collect);
    }

    public TypeRelationships filterTo(TypeIdentifier typeIdentifier) {
        List<TypeRelationship> collect = typeRelationships.stream()
                .filter(classRelation -> classRelation.to().equals(typeIdentifier))
                .collect(Collectors.toList());
        return new TypeRelationships(collect);
    }

    public TypeIdentifiers fromTypeIdentifiers() {
        return typeRelationships.stream()
                .map(classRelation -> classRelation.from())
                .collect(TypeIdentifiers.collector());
    }

    public TypeIdentifiers toTypeIdentifiers() {
        return typeRelationships.stream()
                .map(classRelation -> classRelation.to())
                .collect(TypeIdentifiers.collector());
    }

    public int size() {
        return typeRelationships.size();
    }

    public boolean isEmpty() {
        return typeRelationships.isEmpty();
    }
}
