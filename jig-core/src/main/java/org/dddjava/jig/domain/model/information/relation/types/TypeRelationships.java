package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.members.instruction.DynamicMethodCall;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * 型関係一覧
 */
public record TypeRelationships(Collection<TypeRelationship> relationships) {

    public static TypeRelationships from(JigTypes jigTypes) {
        return jigTypes.orderedStream()
                .flatMap(jigType -> classifiedRelationStream(jigType))
                .collect(collectingAndThen(toList(), TypeRelationships::new));
    }

    public static TypeRelationships internalRelation(JigTypes jigTypes) {
        return jigTypes.orderedStream()
                .flatMap(jigType -> classifiedRelationStream(jigType)
                        .filter(rel -> jigTypes.contains(rel.to())))
                .collect(collectingAndThen(toList(), TypeRelationships::new));
    }

    /**
     * 解析対象の型から、解析対象**外**（外部ライブラリ・JDK 等）の型への参照のみを返す。
     */
    public static TypeRelationships externalRelation(JigTypes jigTypes) {
        return jigTypes.orderedStream()
                .flatMap(jigType -> classifiedRelationStream(jigType)
                        .filter(rel -> !jigTypes.contains(rel.to())))
                .collect(collectingAndThen(toList(), TypeRelationships::new));
    }

    public static TypeRelationships from(JigType jigType) {
        return new TypeRelationships(classifiedRelationStream(jigType).toList());
    }

    private static Stream<TypeRelationship> classifiedRelationStream(JigType jigType) {
        TypeId id = jigType.id();
        JigTypeMembers members = jigType.jigTypeMembers();

        Stream<TypeRelationship> headerStream = headerTypeRelationshipStream(jigType.jigTypeHeader(), id)
                .filter(rel -> !id.equals(rel.to()));

        Stream<TypeRelationship> fieldStream = members.allJigFieldStream()
                .flatMap(field -> typeReferenceRelationshipStream(id, field.jigTypeReference(), TypeRelationKind.フィールド型, TypeRelationKind.フィールド型引数))
                .filter(rel -> !id.equals(rel.to()));

        Stream<TypeRelationship> fieldAnnotationStream = members.allJigFieldStream()
                .flatMap(field -> field.jigFieldHeader().declarationAnnotationStream())
                .map(anno -> TypeRelationship.of(id, anno.id(), TypeRelationKind.使用アノテーション))
                .filter(rel -> !id.equals(rel.to()));

        var methods = members.allJigMethodStream().toList();

        Stream<TypeRelationship> returnTypeStream = methods.stream()
                .flatMap(method -> typeReferenceRelationshipStream(id, method.returnType(), TypeRelationKind.メソッド戻り値, TypeRelationKind.メソッド戻り値型引数))
                .filter(rel -> !id.equals(rel.to()));

        Stream<TypeRelationship> paramStream = methods.stream()
                .flatMap(JigMethod::parameterTypeStream)
                .flatMap(typeRef -> typeReferenceRelationshipStream(id, typeRef, TypeRelationKind.メソッド引数, TypeRelationKind.メソッド引数型引数))
                .filter(rel -> !id.equals(rel.to()));

        Stream<TypeRelationship> methodAnnotationStream = methods.stream()
                .flatMap(JigMethod::declarationAnnotationStream)
                .map(anno -> TypeRelationship.of(id, anno.id(), TypeRelationKind.使用アノテーション))
                .filter(rel -> !id.equals(rel.to()));

        Stream<TypeRelationship> throwsStream = methods.stream()
                .flatMap(JigMethod::throwTypeStream)
                .flatMap(typeRef -> typeReferenceRelationshipStream(id, typeRef, TypeRelationKind.throws宣言))
                .filter(rel -> !id.equals(rel.to()));

        var methodCalls = methods.stream()
                .flatMap(method -> method.instructions().methodCallStream())
                .toList();

        Stream<TypeRelationship> callOwnerStream = methodCalls.stream()
                .map(MethodCall::methodOwner)
                .filter(toId -> !id.equals(toId))
                .map(toId -> TypeRelationship.of(id, toId, TypeRelationKind.呼び出しメソッドのオーナー));

        Stream<TypeRelationship> callReturnStream = methodCalls.stream()
                .map(MethodCall::returnType)
                .filter(toId -> !toId.isVoid() && !id.equals(toId))
                .map(toId -> TypeRelationship.of(id, toId, TypeRelationKind.呼び出しメソッドの戻り値));

        Stream<TypeRelationship> otherInstructionStream = methods.stream()
                .flatMap(method -> method.instructions().instructions().stream()
                        .filter(instr -> !(instr instanceof MethodCall) && !(instr instanceof DynamicMethodCall))
                        .flatMap(instr -> instr.associatedTypeStream()))
                .filter(toId -> !id.equals(toId))
                .map(toId -> TypeRelationship.of(id, toId, TypeRelationKind.不明));

        return Stream.of(headerStream, fieldStream, fieldAnnotationStream,
                        returnTypeStream, paramStream, methodAnnotationStream, throwsStream,
                        callOwnerStream, callReturnStream, otherInstructionStream)
                .flatMap(Function.identity());
    }

    private static Stream<TypeRelationship> headerTypeRelationshipStream(JigTypeHeader jigTypeHeader, TypeId id) {
        // 自身の型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
        Stream<TypeRelationship> typeParameterStream = jigTypeHeader.jigTypeAttributes().typeParameters().stream()
                // 型パラメタ自体は型ではないが、型パラメタの境界は型引数なので取得する
                .flatMap(jigTypeParameter -> jigTypeParameter.bounds().stream())
                .map(typeArg -> TypeRelationship.of(id, typeArg.typeId(), TypeRelationKind.型引数));
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
        return typeReferenceRelationshipStream(id, jigTypeReference, typeRelationKind, TypeRelationKind.型引数);
    }

    private static Stream<TypeRelationship> typeReferenceRelationshipStream(TypeId id, JigTypeReference jigTypeReference, TypeRelationKind typeRelationKind, TypeRelationKind typeArgumentKind) {
        return Stream.of(
                        // 自身
                        Stream.of(TypeRelationship.of(id, jigTypeReference.id(), typeRelationKind)),
                        // 型パラメタ（型パラメタのアノテーション、型パラメタの型パラメタは未対応）
                        jigTypeReference.typeArgumentList().stream()
                                .map(typeArg -> TypeRelationship.of(id, typeArg.typeId(), typeArgumentKind)),
                        // 型アノテーション
                        annotationTypeRelationshipStream(jigTypeReference.typeAnnotations(), id))
                .flatMap(Function.identity());
    }

    private static Stream<TypeRelationship> annotationTypeRelationshipStream(Collection<JigAnnotationReference> jigTypeReference, TypeId id) {
        // アノテーション（アノテーション引数は未対応）
        return jigTypeReference.stream()
                .map(annoRef -> TypeRelationship.of(id, annoRef.id(), TypeRelationKind.使用アノテーション));
    }

    public TypeIds collectTypeIdWhichRelationTo(TypeId typeId) {
        return relationships.stream()
                .filter(classRelation -> classRelation.toIs(typeId))
                .map(TypeRelationship::from)
                .collect(TypeIds.collector());
    }

    public List<TypeRelationship> list() {
        return relationships.stream()
                .sorted(Comparator.comparing(TypeRelationship::from).thenComparing(TypeRelationship::to))
                .toList();
    }

    public TypeRelationships filterFrom(TypeId typeId) {
        return filterRelationships(classRelation -> classRelation.from().equals(typeId));
    }


    public TypeRelationships filterTo(TypeId typeId) {
        return filterRelationships(classRelation -> classRelation.to().equals(typeId));
    }

    private TypeRelationships filterRelationships(Predicate<TypeRelationship> typeRelationshipPredicate) {
        return new TypeRelationships(relationships.stream()
                .filter(typeRelationshipPredicate)
                .toList());
    }

    public int size() {
        return relationships.size();
    }

    public boolean isEmpty() {
        return relationships.isEmpty();
    }
}
