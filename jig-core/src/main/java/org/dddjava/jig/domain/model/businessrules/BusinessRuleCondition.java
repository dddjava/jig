package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.type.Type;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * ビジネスルールの条件
 */
public class BusinessRuleCondition {

    private final String typeIdentifierPattern;

    public BusinessRuleCondition(String typeIdentifierPattern) {
        this.typeIdentifierPattern = typeIdentifierPattern;
    }

    BusinessRuleJudge judge(Type type) {
        TypeIdentifier identifier = type.identifier();
        return judge(identifier) ? BusinessRuleJudge.BUSINESS_RULE : BusinessRuleJudge.NOT_BUSINESS_RULE;
    }

    public boolean judge(TypeIdentifier identifier) {
        String fullQualifiedName = identifier.fullQualifiedName();
        if (fullQualifiedName.matches(typeIdentifierPattern)) {
            // コンパイラの生成するクラスを除外
            return !fullQualifiedName.matches(".+\\$\\d+");
        }
        return false;
    }
}
