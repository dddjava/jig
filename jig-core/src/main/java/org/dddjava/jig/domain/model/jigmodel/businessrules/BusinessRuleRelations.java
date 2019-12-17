package org.dddjava.jig.domain.model.jigmodel.businessrules;

import java.util.List;

/**
 * ビジネスルールの関連一覧
 */
public class BusinessRuleRelations {

    List<BusinessRuleRelation> list;

    public BusinessRuleRelations(List<BusinessRuleRelation> list) {
        this.list = list;
    }

    public List<BusinessRuleRelation> list() {
        return list;
    }
}
