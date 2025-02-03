package org.dddjava.jig.domain.model.data.types;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * JSL`NormalClassDeclaration` の `ClassBody` 以外で得られる情報
 *
 * @param id             完全修飾クラス名
 * @param jigTypeKind
 * @param jigTypeAttributeData
 * @param baseTypeDataBundle 親クラス及び実装インタフェース
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html">jls/Chapter 8. Classes</a>
 */
public record JigTypeHeader(JigObjectId<JigTypeHeader> id,
                            JigTypeKind jigTypeKind,
                            JigTypeAttributeData jigTypeAttributeData,
                            JigBaseTypeDataBundle baseTypeDataBundle) {

    /**
     * FQNのみで生成する。主にテスト用。
     */
    public static JigTypeHeader simple(String fqn) {
        return new JigTypeHeader(new JigObjectId<>(fqn), JigTypeKind.CLASS, JigTypeAttributeData.simple(), JigBaseTypeDataBundle.simple());
    }

    public String simpleName() {
        return id.simpleValue();
    }

    public String fqn() {
        return id.value();
    }

    public String simpleNameWithGenerics() {
        return simpleName() + jigTypeAttributeData.typeParametersSimpleName();
    }

    public Optional<JigBaseTypeData> superType() {
        return baseTypeDataBundle.superType();
    }

    public List<JigBaseTypeData> interfaceTypeList() {
        return baseTypeDataBundle.interfaceTypes().stream()
                .sorted(Comparator.comparing(jigBaseTypeData -> jigBaseTypeData.id()))
                .toList();
    }
}
