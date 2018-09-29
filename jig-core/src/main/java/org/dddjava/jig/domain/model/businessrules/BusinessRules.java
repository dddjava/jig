package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.type.Type;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    List<BusinessRule> list;

    public BusinessRules(Types types, BusinessRuleCondition businessRuleCondition) {
        this.list = new ArrayList<>();
        for (Type type : types.list()) {
            if (businessRuleCondition.judge(type) == BusinessRuleJudge.BUSINESS_RULE) {
                list.add(new BusinessRule(type));
            }
        }
    }

    public List<BusinessRule> list() {
        return list;
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        for (BusinessRule businessRule : list) {
            if (businessRule.type().identifier().equals(typeIdentifier)) {
                return true;
            }
        }
        return false;
    }
}
