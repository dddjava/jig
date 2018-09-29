package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.type.Type;

/**
 * ビジネスルールの条件
 */
public class BusinessRuleCondition {

    private final String typeIdentifierPattern;

    public BusinessRuleCondition(String typeIdentifierPattern) {
        this.typeIdentifierPattern = typeIdentifierPattern;
    }

    BusinessRuleJudge judge(Type type) {
        String fullQualifiedName = type.identifier().fullQualifiedName();
        if (fullQualifiedName.matches(typeIdentifierPattern)) {
            // コンパイラの生成するクラスを除外
            if (!fullQualifiedName.matches(".+\\$\\d+")) {
                return BusinessRuleJudge.BUSINESS_RULE;
            }
        }
        return BusinessRuleJudge.NOT_BUSINESS_RULE;
    }
}
