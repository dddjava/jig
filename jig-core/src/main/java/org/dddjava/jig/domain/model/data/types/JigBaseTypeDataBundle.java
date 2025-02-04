package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param superType      親クラスの完全修飾クラス名。classの場合は未指定でもObjectが入るが、interfaceなどではempty。
 * @param interfaceTypes 実装インタフェースの完全修飾クラス名
 */
public record JigBaseTypeDataBundle(
        Optional<JigBaseTypeData> superType,
        Collection<JigBaseTypeData> interfaceTypes) {
    public static JigBaseTypeDataBundle simple() {
        return new JigBaseTypeDataBundle(Optional.of(JigBaseTypeData.fromId(TypeIdentifier.from(Object.class))), List.of());
    }

    public Set<TypeIdentifier> typeIdSet() {
        return Stream.concat(superType.stream(), interfaceTypes.stream())
                .flatMap(jigBaseTypeData -> Stream.concat(
                        Stream.of(jigBaseTypeData.id()),
                        jigBaseTypeData.attributeData().typeArgumentList().stream()
                                .map(JigTypeArgument::value)
                                .map(value -> TypeIdentifier.valueOf(value))
                ))
                // "." の含まれていないものは型パラメタとして扱う。デフォルトパッケージのクラスも該当してしまうが、良しとする。
                .filter(jigTypeHeaderJigObjectId -> jigTypeHeaderJigObjectId.value().contains("."))
                .collect(Collectors.toSet());
    }
}
