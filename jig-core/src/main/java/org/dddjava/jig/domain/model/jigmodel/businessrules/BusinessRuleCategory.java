package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigInstanceMember;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.util.List;
import java.util.Set;

/**
 * ビジネスルールの種類
 */
public enum BusinessRuleCategory {
    文字列,
    数値,
    日付,
    期間,
    区分,
    コレクション,
    不明;

    public static BusinessRuleCategory choice(JigType jigType) {
        TypeKind typeKind = jigType.typeKind();
        if (typeKind.isCategory()) {
            return 区分;
        }

        JigInstanceMember jigInstanceMember = jigType.instanceMember();
        if (isCollectionField(jigInstanceMember.fieldDeclarations())) {
            return コレクション;
        }

        for (ValueKind valueKind : ValueKind.values()) {
            if (valueKind.matches(jigInstanceMember.fieldDeclarations())) {
                return valueKind.businessRuleCategory;
            }
        }

        return 不明;
    }

    private static boolean isCollectionField(FieldDeclarations fieldDeclarations) {
        return (fieldDeclarations.matches(new TypeIdentifier(List.class))
                || fieldDeclarations.matches(new TypeIdentifier(Set.class)));
    }
}
