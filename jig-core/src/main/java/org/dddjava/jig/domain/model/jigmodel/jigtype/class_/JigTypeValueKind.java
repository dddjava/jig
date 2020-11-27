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
        if (fieldDeclarations.matches((new TypeIdentifier(String.class)))) {
            return 文字列;
        }
        if (fieldDeclarations.matches(new TypeIdentifier(BigDecimal.class))
                || fieldDeclarations.matches(new TypeIdentifier(Long.class))
                || fieldDeclarations.matches(new TypeIdentifier(Integer.class))
                || fieldDeclarations.matches(new TypeIdentifier(long.class))
                || fieldDeclarations.matches(new TypeIdentifier(int.class))) {
            return 数値;
        }
        if (fieldDeclarations.matches(new TypeIdentifier(LocalDate.class))) {
            return 日付;
        }
        if (fieldDeclarations.matches(new TypeIdentifier(LocalDate.class), new TypeIdentifier(LocalDate.class))) {
            return 期間;
        }
        return 不明;
    }

    private static boolean isCollectionField(FieldDeclarations fieldDeclarations) {
        return (fieldDeclarations.matches(new TypeIdentifier(List.class))
                || fieldDeclarations.matches(new TypeIdentifier(Set.class)));
    }
}
