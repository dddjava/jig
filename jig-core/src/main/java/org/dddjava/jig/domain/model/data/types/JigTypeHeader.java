package org.dddjava.jig.domain.model.data.types;

import java.util.*;

/**
 * JSL`NormalClassDeclaration` の `ClassBody` 以外で得られる情報
 *
 * @param id                   完全修飾クラス名
 * @param javaTypeDeclarationKind
 * @param jigTypeAttributes
 * @param baseTypeDataBundle   親クラス及び実装インタフェース
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html">jls/Chapter 8. Classes</a>
 */
public record JigTypeHeader(TypeIdentifier id,
                            JavaTypeDeclarationKind javaTypeDeclarationKind,
                            JigTypeAttributes jigTypeAttributes,
                            JigBaseTypeDataBundle baseTypeDataBundle) {

    public Set<TypeIdentifier> containedIds() {
        Set<TypeIdentifier> ids = new HashSet<>();
        ids.add(id);
        ids.addAll(jigTypeAttributes.typeIdSet());
        ids.addAll(baseTypeDataBundle.typeIdSet());
        return ids;
    }

    /**
     * FQNのみで生成する。主にテスト用。
     */
    public static JigTypeHeader simple(String fqn) {
        return new JigTypeHeader(TypeIdentifier.valueOf(fqn), JavaTypeDeclarationKind.CLASS, JigTypeAttributes.simple(), JigBaseTypeDataBundle.simple());
    }

    public String simpleName() {
        return id.asSimpleText();
    }

    public String fqn() {
        return id.value();
    }

    public String simpleNameWithGenerics() {
        return simpleName() + jigTypeAttributes.typeParametersSimpleName();
    }

    public Optional<JigTypeReference> superType() {
        return baseTypeDataBundle.superType();
    }

    public List<JigTypeReference> interfaceTypeList() {
        return baseTypeDataBundle.interfaceTypes().stream()
                .sorted(Comparator.comparing(jigBaseTypeData -> jigBaseTypeData.id()))
                .toList();
    }
}
