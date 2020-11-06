package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.jigtype.TypeKind;

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

    public static BusinessRuleCategory choice(BusinessRuleFields businessRuleFields, TypeKind typeKind) {
        if (typeKind.isCategory()) {
            return 区分;
        }
        if (businessRuleFields.satisfyCollection()) {
            return コレクション;
        }

        for (ValueKind valueKind : ValueKind.values()) {
            if (businessRuleFields.satisfyValue(valueKind)) {
                return valueKind.businessRuleCategory;
            }
        }

        return 不明;
    }
}
