package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.class_.ClassRelation;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.class_.ClassRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<BusinessRuleGroup> groups() {
        Map<PackageIdentifier, List<BusinessRule>> collect = businessRules.list().stream()
                .collect(Collectors.groupingBy(
                        businessRule -> businessRule.type().identifier().packageIdentifier()
                ));
        return collect.entrySet().stream()
                .map(entity -> new BusinessRuleGroup(
                        entity.getKey(),
                        new BusinessRules(entity.getValue())
                )).collect(Collectors.toList());
    }

    public BusinessRuleRelations relations() {
        List<BusinessRuleRelation> list = new ArrayList<>();
        for (ClassRelation classRelation : classRelations.list()) {
            if (classRelation.notSelfDependency()
                    && businessRules.contains(classRelation.from())
                    && businessRules.contains(classRelation.to())) {
                list.add(new BusinessRuleRelation(classRelation));
            }
        }
        return new BusinessRuleRelations(list);
    }
}
