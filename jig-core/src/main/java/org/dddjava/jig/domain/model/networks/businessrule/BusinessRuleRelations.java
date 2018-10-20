package org.dddjava.jig.domain.model.networks.businessrule;

import java.util.List;

public class BusinessRuleRelations {

    List<BusinessRuleRelation> list;

    public BusinessRuleRelations(List<BusinessRuleRelation> list) {
        this.list = list;
    }

    public List<BusinessRuleRelation> list() {
        return list;
    }
}
