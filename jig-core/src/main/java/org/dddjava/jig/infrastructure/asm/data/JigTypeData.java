package org.dddjava.jig.infrastructure.asm.data;

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
public record JigTypeData(JigObjectId<JigTypeData> id,
                          JigTypeKind jigTypeKind,
                          JigTypeAttributeData jigTypeAttributeData,
                          JigBaseTypeDataBundle baseTypeDataBundle) {

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
