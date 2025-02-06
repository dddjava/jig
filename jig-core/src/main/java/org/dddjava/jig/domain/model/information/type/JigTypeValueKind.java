package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

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
        if (fieldDeclarations.matches((TypeIdentifier.from(String.class)))) {
            return 文字列;
        }
        if (fieldDeclarations.matches(TypeIdentifier.from(BigDecimal.class))
                || fieldDeclarations.matches(TypeIdentifier.from(Long.class))
                || fieldDeclarations.matches(TypeIdentifier.from(Integer.class))
                || fieldDeclarations.matches(TypeIdentifier.from(long.class))
                || fieldDeclarations.matches(TypeIdentifier.from(int.class))) {
            return 数値;
        }
        if (fieldDeclarations.matches(TypeIdentifier.from(LocalDate.class))) {
            return 日付;
        }
        if (fieldDeclarations.matches(TypeIdentifier.from(LocalDate.class), TypeIdentifier.from(LocalDate.class))) {
            return 期間;
        }
        return 不明;
    }

    private static boolean isCollectionField(FieldDeclarations fieldDeclarations) {
        if (fieldDeclarations.matches(TypeIdentifier.from(List.class))) return true;
        return (fieldDeclarations.matches(TypeIdentifier.from(Set.class)));
    }
}
