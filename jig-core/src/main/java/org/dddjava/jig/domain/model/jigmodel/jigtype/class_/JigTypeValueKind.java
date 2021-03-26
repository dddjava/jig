package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 値の種類
 */
public enum JigTypeValueKind {
    文字列,
    数値,
    日付,
    期間,
    区分,
    コレクション,
    不明;

    public static JigTypeValueKind from(JigType jigType) {
        TypeKind typeKind = jigType.typeKind();
        if (typeKind.isCategory()) {
            return 区分;
        }

        JigInstanceMember jigInstanceMember = jigType.instanceMember();
        FieldDeclarations fieldDeclarations = jigInstanceMember.fieldDeclarations();
        if (isCollectionField(fieldDeclarations)) {
            return コレクション;
        }
        if (fieldDeclarations.matches((TypeIdentifier.of(String.class)))) {
            return 文字列;
        }
        if (fieldDeclarations.matches(TypeIdentifier.of(BigDecimal.class))
                || fieldDeclarations.matches(TypeIdentifier.of(Long.class))
                || fieldDeclarations.matches(TypeIdentifier.of(Integer.class))
                || fieldDeclarations.matches(TypeIdentifier.of(long.class))
                || fieldDeclarations.matches(TypeIdentifier.of(int.class))) {
            return 数値;
        }
        if (fieldDeclarations.matches(TypeIdentifier.of(LocalDate.class))) {
            return 日付;
        }
        if (fieldDeclarations.matches(TypeIdentifier.of(LocalDate.class), TypeIdentifier.of(LocalDate.class))) {
            return 期間;
        }
        return 不明;
    }

    private static boolean isCollectionField(FieldDeclarations fieldDeclarations) {
        return (fieldDeclarations.matches(TypeIdentifier.of(List.class))
                || fieldDeclarations.matches(TypeIdentifier.of(Set.class)));
    }
}
