package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public record JigTypeHeaderBuilder(TypeId id,
                                   JavaTypeDeclarationKind javaTypeDeclarationKind,
                                   JigBaseTypeDataBundle baseTypeDataBundle,
                                   // JigTypeAttributesからアノテーションを除いたもの
                                   JigTypeVisibility jigTypeVisibility,
                                   Collection<JigTypeModifier> jigTypeModifiers,
                                   List<JigTypeParameter> typeParameters
) {

    /**
     * クラスの読み取りで集めた情報を合わせて構築する。
     *
     * @param declarationAnnotations 宣言に付与されたアノテーション
     * @param staticNestedClass      staticなネストクラスであるか
     */
    JigTypeHeader build(Collection<JigAnnotationReference> declarationAnnotations, boolean staticNestedClass) {
        EnumSet<JigTypeModifier> modifiers = EnumSet.noneOf(JigTypeModifier.class);
        modifiers.addAll(jigTypeModifiers);
        // staticなネストクラスの場合の修飾子を追加。JVMSではフラグはないが、JLSでは修飾子を記述するので、ここで追加する。
        if (staticNestedClass) {
            modifiers.add(JigTypeModifier.STATIC);
        }

        return new JigTypeHeader(id, javaTypeDeclarationKind,
                new JigTypeAttributes(jigTypeVisibility, modifiers, List.copyOf(declarationAnnotations), typeParameters),
                baseTypeDataBundle);
    }
}
