package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @param superType      親クラスの完全修飾クラス名。classの場合は未指定でもObjectが入るが、interfaceなどではempty。
 * @param interfaceTypes 実装インタフェースの完全修飾クラス名
 */
public record JigBaseTypeDataBundle(
        Optional<JigTypeReference> superType,
        Collection<JigTypeReference> interfaceTypes) {

    public Set<TypeId> typeIdSet() {
        return Stream.concat(superType.stream(), interfaceTypes.stream())
                .flatMap(jigBaseTypeData -> Stream.concat(
                        Stream.of(jigBaseTypeData.id()),
                        jigBaseTypeData.typeArgumentList().stream()
                                .map(JigTypeArgument::value)
                                .map(value -> TypeId.valueOf(value))
                ))
                // "." の含まれていないものは型パラメタとして扱う。デフォルトパッケージのクラスも該当してしまうが、良しとする。
                .filter(jigTypeHeaderJigObjectId -> jigTypeHeaderJigObjectId.value().contains("."))
                .collect(toSet());
    }

    public boolean superTypeIsEnum() {
        return superType
                .filter(type -> type.id().equals(TypeId.ENUM))
                .isPresent();
    }

    public boolean superTypeIsRecord() {
        return superType
                .filter(type -> type.id().equals(TypeId.RECORD))
                .isPresent();
    }
}
