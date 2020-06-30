package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;

/**
 * ビジネスルールの傾向
 *
 * https://image.slidesharecdn.com/domain-object-200625050143/95/-6-638.jpg?cb=1593136427
 */
public enum BusinessRuleTendency {
    ドメイン特化型(TypeIs.ビジネスルールのみ, TypeIs.ビジネスルールのみ, TypeIs.ビジネスルールのみ),
    混合型A(TypeIs.ビジネスルールのみ, TypeIs.ビジネスルールのみ, TypeIs.基本型のみ),
    混合型B(TypeIs.ビジネスルールのみ, TypeIs.基本型のみ, TypeIs.ビジネスルールのみ),
    汎用部品(TypeIs.基本型のみ, TypeIs.基本型のみ, TypeIs.基本型のみ),
    混合型C(TypeIs.基本型のみ, TypeIs.ビジネスルールのみ, TypeIs.基本型のみ),
    混合型D(TypeIs.基本型のみ, TypeIs.基本型のみ, TypeIs.ビジネスルールのみ),
    自立型(TypeIs.基本型のみ, TypeIs.自分の型または基本型, TypeIs.自分の型または基本型),
    該当なし(TypeIs.不明, TypeIs.不明, TypeIs.不明);

    TypeIs field;
    TypeIs methodReturn;
    TypeIs methodArguments;

    BusinessRuleTendency(TypeIs field, TypeIs methodReturn, TypeIs methodArguments) {
        this.field = field;
        this.methodReturn = methodReturn;
        this.methodArguments = methodArguments;
    }

    public static BusinessRuleTendency from(BusinessRule businessRule, BusinessRules businessRules) {
        BusinessRuleFields fields = businessRule.fields();
        TypeIs フィールドの型 = TypeIs.from(businessRule, fields.typeIdentifiers(), businessRules);

        MethodDeclarations methodDeclarations = businessRule.instanceMethodDeclarations();
        TypeIs メソッドの返す型 = TypeIs.from(businessRule, methodDeclarations.returnTypeIdentifiers(), businessRules);
        TypeIs メソッドの引数の型 = TypeIs.from(businessRule, methodDeclarations.argumentsTypeIdentifiers(), businessRules);

        for (BusinessRuleTendency value : values()) {
            if (value.field == フィールドの型
                    && value.methodReturn.matches(メソッドの返す型)
                    && value.methodReturn.matches(メソッドの引数の型))
                return value;
        }
        return 該当なし;
    }

    enum TypeIs {
        ビジネスルールのみ {
            @Override
            boolean matches(TypeIs actual) {
                return super.matches(actual) || actual == 自分の型のみ;
            }
        },
        自分の型のみ,
        基本型のみ,
        自分の型または基本型 {
            @Override
            boolean matches(TypeIs actual) {
                return super.matches(actual) || actual == 自分の型のみ || actual == 基本型のみ;
            }
        },
        混在 {
            @Override
            boolean matches(TypeIs actual) {
                return super.matches(actual) || actual == 自分の型または基本型;
            }
        },
        無し,
        不明;

        public static TypeIs from(BusinessRule self, TypeIdentifiers typeIdentifiers, BusinessRules businessRules) {
            boolean containsBusinessRule = false;
            boolean containsPrimitive = false;
            boolean containsSelf = false;
            for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
                if (self.typeIdentifier().equals(typeIdentifier)) {
                    containsSelf = true;
                } else if (businessRules.contains(typeIdentifier)) {
                    containsBusinessRule = true;
                } else {
                    containsPrimitive = true;
                }
            }

            if (containsBusinessRule) {
                if (containsPrimitive) {
                    return 混在;
                }
                // selfの有無は関係なくビジネスルールのみとしてよい
                return ビジネスルールのみ;
            } else if (containsSelf) {
                if (containsPrimitive) {
                    return 自分の型または基本型;
                }
                return 自分の型のみ;
            } else if (containsPrimitive) {
                // TODO 非ビジネスルール＝基本型でない場合……
                return 基本型のみ;
            }

            // すべてfalse = すべてvoidやすべて引数なし
            return 無し;
        }

        boolean matches(TypeIs actual) {
            return this == actual || 無し == actual;
        }
    }
}
