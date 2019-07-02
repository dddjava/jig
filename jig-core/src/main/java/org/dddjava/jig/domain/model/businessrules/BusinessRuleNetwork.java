package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelations;

import java.util.ArrayList;
import java.util.List;

/**
 * ビジネスルールの関連網
 */
public class BusinessRuleNetwork {

    BusinessRules businessRules;
    ClassRelations classRelations;

    public BusinessRuleNetwork(BusinessRules businessRules, ClassRelations classRelations) {
        this.businessRules = businessRules;
        this.classRelations = classRelations;
    }

    public List<BusinessRulePackage> groups() {
        BusinessRulePackages businessRulePackages = businessRules.businessRulePackages();
        return businessRulePackages.list;
    }

    public BusinessRuleRelations relations() {
        List<BusinessRuleRelation> list = new ArrayList<>();
        for (ClassRelation classRelation : classRelations.list()) {
            if (businessRules.contains(classRelation.from()) && businessRules.contains(classRelation.to())) {
                list.add(new BusinessRuleRelation(classRelation));
            }
        }
        return new BusinessRuleRelations(list);
    }
}
