package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.type.Type;

/**
 * ビジネスルールの条件
 *
 * @see org.dddjava.jig.infrastructure.configuration.ModelPattern
 */
public class BusinessRuleCondition {

    private final String typeIdentifierPattern;

    public BusinessRuleCondition(String typeIdentifierPattern) {
        this.typeIdentifierPattern = typeIdentifierPattern;
    }

    BusinessRuleJudge judge(Type type) {
        if (type.identifier().fullQualifiedName().matches(typeIdentifierPattern)) {
            return BusinessRuleJudge.BUSINESS_RULE;
        }
        return BusinessRuleJudge.NOT_BUSINESS_RULE;
    }
}
