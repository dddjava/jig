package org.dddjava.jig.infrastructure.asm.data;

import java.util.Collection;
import java.util.Optional;

/**
 * JSL`NormalClassDeclaration` の `ClassBody` 以外で得られる情報
 *
 * @param id             完全修飾クラス名
 * @param jigTypeKind
 * @param jigTypeAttributeData
 * @param superType      親クラスの完全修飾クラス名。classの場合は未指定でもObjectが入るが、interfaceなどではempty。
 * @param interfaceTypes 実装インタフェースの完全修飾クラス名
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html">jls/Chapter 8. Classes</a>
 */
public record JigTypeData(JigObjectId<JigTypeData> id,
                          JigTypeKind jigTypeKind,
                          JigTypeAttributeData jigTypeAttributeData,
                          Optional<JigBaseTypeData> superType,
                          Collection<JigBaseTypeData> interfaceTypes) {

    public String simpleName() {
        return id.simpleValue();
    }

    public String fqn() {
        return id.value();
    }

    public String simpleNameWithGenerics() {
        return simpleName() + jigTypeAttributeData.typeParametersSimpleName();
    }
}
