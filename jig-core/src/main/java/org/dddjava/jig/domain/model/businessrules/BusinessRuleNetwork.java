package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.networks.type.TypeRelation;
import org.dddjava.jig.domain.model.networks.type.TypeRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ビジネスルールの関連網
 */
public class BusinessRuleNetwork {

    BusinessRules businessRules;
    TypeRelations typeRelations;

    public BusinessRuleNetwork(BusinessRules businessRules, TypeRelations typeRelations) {
        this.businessRules = businessRules;
        this.typeRelations = typeRelations;
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
        for (TypeRelation typeRelation : typeRelations.list()) {
            if (typeRelation.notSelfDependency()
                    && businessRules.contains(typeRelation.from())
                    && businessRules.contains(typeRelation.to())) {
                list.add(new BusinessRuleRelation(typeRelation));
            }
        }
        return new BusinessRuleRelations(list);
    }
}
