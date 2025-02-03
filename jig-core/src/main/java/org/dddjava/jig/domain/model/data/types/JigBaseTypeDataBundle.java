package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.Optional;

/**
 * @param superType      親クラスの完全修飾クラス名。classの場合は未指定でもObjectが入るが、interfaceなどではempty。
 * @param interfaceTypes 実装インタフェースの完全修飾クラス名
 */
public record JigBaseTypeDataBundle(
        Optional<JigBaseTypeData> superType,
        Collection<JigBaseTypeData> interfaceTypes) {
}
